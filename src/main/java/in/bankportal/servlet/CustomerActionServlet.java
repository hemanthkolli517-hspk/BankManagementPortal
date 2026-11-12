package in.bankportal.servlet;
import in.bankportal.util.DB;import in.bankportal.util.Web;
import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.math.BigDecimal;import java.sql.*;
@WebServlet("/customer/transaction")
public class CustomerActionServlet extends HttpServlet {
 @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  if(!Web.requireRole(req,resp,"CUSTOMER"))return;
  if(!Web.validCsrf(req)){Web.redirectMessage(req,resp,"/customer/dashboard","error","Form expired. Refresh and try again.");return;}
  String action=v(req,"action"), description=v(req,"description"); long accountId;
  try{accountId=Long.parseLong(v(req,"accountId"));}catch(Exception e){Web.redirectMessage(req,resp,"/customer/dashboard","error","Select a valid account.");return;}
  BigDecimal amount;try{amount=new BigDecimal(v(req,"amount"));if(amount.scale()>2 || amount.compareTo(BigDecimal.ZERO)<=0 || amount.compareTo(new BigDecimal("100000000"))>0)throw new Exception();}catch(Exception e){Web.redirectMessage(req,resp,"/customer/dashboard","error","Enter a valid amount greater than zero (maximum 100,000,000).");return;}
  if(!action.equals("DEPOSIT")&&!action.equals("WITHDRAWAL")){Web.redirectMessage(req,resp,"/customer/dashboard","error","Invalid transaction type.");return;}
  try(Connection c=DB.getConnection()){
   c.setAutoCommit(false);
   try{
    BigDecimal balance; long customerId; String status;
    try(PreparedStatement ps=c.prepareStatement("SELECT a.balance,a.customer_id,a.status FROM accounts a JOIN customers cu ON cu.customer_id=a.customer_id WHERE a.account_id=? AND cu.user_id=? FOR UPDATE")){
     ps.setLong(1,accountId);ps.setLong(2,Web.userId(req));try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Account not found.");balance=rs.getBigDecimal(1);customerId=rs.getLong(2);status=rs.getString(3);}
    }
    if(!"ACTIVE".equals(status))throw new IllegalArgumentException("This account is inactive.");
    BigDecimal newBalance=action.equals("DEPOSIT")?balance.add(amount):balance.subtract(amount);
    if(newBalance.compareTo(BigDecimal.ZERO)<0)throw new IllegalArgumentException("Insufficient balance.");
    try(PreparedStatement ps=c.prepareStatement("UPDATE accounts SET balance=? WHERE account_id=?")){ps.setBigDecimal(1,newBalance);ps.setLong(2,accountId);ps.executeUpdate();}
    try(PreparedStatement ps=c.prepareStatement("INSERT INTO transactions(account_id,transaction_type,amount,balance_after,description,performed_by) VALUES(?,?,?,?,?,?)")){
     ps.setLong(1,accountId);ps.setString(2,action);ps.setBigDecimal(3,amount);ps.setBigDecimal(4,newBalance);ps.setString(5,description.isBlank()?(action.equals("DEPOSIT")?"Customer deposit":"Customer withdrawal"):description);ps.setLong(6,Web.userId(req));ps.executeUpdate();
    }
    c.commit();Web.redirectMessage(req,resp,"/customer/dashboard","success",action+" completed successfully.");
   }catch(Exception e){c.rollback();Web.redirectMessage(req,resp,"/customer/dashboard","error",e.getMessage()==null?"Transaction failed.":e.getMessage());}
  }catch(SQLException e){getServletContext().log("Transaction failed",e);Web.redirectMessage(req,resp,"/customer/dashboard","error","Transaction failed due to a database error.");}
 }
 private static String v(HttpServletRequest r,String k){String s=r.getParameter(k);return s==null?"":s.trim();}
}
