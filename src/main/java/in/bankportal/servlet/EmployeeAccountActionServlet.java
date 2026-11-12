package in.bankportal.servlet;
import in.bankportal.util.DB;import in.bankportal.util.Web;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.math.BigDecimal;import java.sql.*;
@WebServlet("/employee/account-action")
public class EmployeeAccountActionServlet extends HttpServlet{
 @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  if(!Web.requireRole(req,resp,"EMPLOYEE","ADMIN"))return;
  if(!Web.validCsrf(req)){Web.redirectMessage(req,resp,"/employee/accounts","error","Form expired.");return;}
  String action=v(req,"action");
  try(Connection c=DB.getConnection()){
   switch(action){
    case "CREATE" -> {
     long customerId=Long.parseLong(v(req,"customerId"));String type=v(req,"accountType");BigDecimal opening=new BigDecimal(v(req,"openingBalance"));
     if(!java.util.Set.of("SAVINGS","CURRENT").contains(type)||opening.compareTo(BigDecimal.ZERO)<0||opening.scale()>2||opening.compareTo(new BigDecimal("100000000"))>0)throw new IllegalArgumentException("Check account type and opening balance.");
     String accountNo=null;
     c.setAutoCommit(false);
     try{
      try(PreparedStatement chk=c.prepareStatement("SELECT account_status,account_number FROM customers WHERE customer_id=? FOR UPDATE")){chk.setLong(1,customerId);try(ResultSet rs=chk.executeQuery()){if(!rs.next()||!"APPROVED".equals(rs.getString("account_status")))throw new IllegalArgumentException("Only approved customers can receive an account.");accountNo=rs.getString("account_number");}}
      if(accountNo==null||accountNo.isBlank())accountNo="10"+java.util.UUID.randomUUID().toString().replace("-","").substring(0,14).toUpperCase();
      if(!accountNo.matches("^[A-Za-z0-9-]{1,20}$"))throw new IllegalArgumentException("Invalid customer account number.");
      try(PreparedStatement existing=c.prepareStatement("SELECT COUNT(*) FROM accounts WHERE customer_id=?")){existing.setLong(1,customerId);try(ResultSet rs=existing.executeQuery()){rs.next();if(rs.getLong(1)>0)throw new IllegalArgumentException("This customer already has an account. Select a customer without an account.");}}
      long accountId;
      try(PreparedStatement ps=c.prepareStatement("INSERT INTO accounts(customer_id,account_number,account_type,balance,status) VALUES(?,?,?,?,'ACTIVE')",Statement.RETURN_GENERATED_KEYS)){ps.setLong(1,customerId);ps.setString(2,accountNo);ps.setString(3,type);ps.setBigDecimal(4,opening);ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){rs.next();accountId=rs.getLong(1);}}
      try(PreparedStatement ps=c.prepareStatement("UPDATE customers SET account_number=? WHERE customer_id=?")){ps.setString(1,accountNo);ps.setLong(2,customerId);ps.executeUpdate();}
      if(opening.compareTo(BigDecimal.ZERO)>0)try(PreparedStatement ps=c.prepareStatement("INSERT INTO transactions(account_id,transaction_type,amount,balance_after,description,performed_by) VALUES(?,'DEPOSIT',?,?,?,?)")){ps.setLong(1,accountId);ps.setBigDecimal(2,opening);ps.setBigDecimal(3,opening);ps.setString(4,"Opening balance");ps.setLong(5,Web.userId(req));ps.executeUpdate();}
      c.commit();Web.redirectMessage(req,resp,"/employee/accounts","success","Account created successfully.");
     }catch(Exception e){c.rollback();throw e;}
    }
    case "UPDATE" -> {long id=Long.parseLong(v(req,"accountId"));String type=v(req,"accountType"),status=v(req,"status");if(!java.util.Set.of("SAVINGS","CURRENT").contains(type)||!java.util.Set.of("ACTIVE","INACTIVE").contains(status))throw new IllegalArgumentException("Invalid account type or status.");try(PreparedStatement ps=c.prepareStatement("UPDATE accounts SET account_type=?,status=? WHERE account_id=?")){ps.setString(1,type);ps.setString(2,status);ps.setLong(3,id);if(ps.executeUpdate()==0)throw new IllegalArgumentException("Account not found.");}Web.redirectMessage(req,resp,"/employee/accounts","success","Account updated.");}
    case "DELETE" -> {long id=Long.parseLong(v(req,"accountId"));try(PreparedStatement ps=c.prepareStatement("UPDATE accounts SET status='INACTIVE' WHERE account_id=?")){ps.setLong(1,id);if(ps.executeUpdate()==0)throw new IllegalArgumentException("Account not found.");}Web.redirectMessage(req,resp,"/employee/accounts","success","Account deactivated. Transaction history was retained.");}
    default -> throw new IllegalArgumentException("Unknown account action.");
   }
  }catch(Exception e){getServletContext().log("Account action failed",e);Web.redirectMessage(req,resp,"/employee/accounts","error",e.getMessage()==null?"Account operation failed.":e.getMessage());}
 }
 private static String v(HttpServletRequest r,String k){String s=r.getParameter(k);return s==null?"":s.trim();}
}
