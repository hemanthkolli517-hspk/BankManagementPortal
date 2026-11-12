package in.bankportal.servlet;
import in.bankportal.util.DB;import in.bankportal.util.Web;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.*;
@WebServlet("/employee/customer-action")
public class EmployeeCustomerActionServlet extends HttpServlet{
 @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  if(!Web.requireRole(req,resp,"EMPLOYEE"))return;
  if(!Web.validCsrf(req)){Web.redirectMessage(req,resp,"/employee/customers","error","Form expired.");return;}
  String action=v(req,"action");long customerId;try{customerId=Long.parseLong(v(req,"customerId"));}catch(Exception e){Web.redirectMessage(req,resp,"/employee/customers","error","Invalid customer.");return;}
  try(Connection c=DB.getConnection()){
   c.setAutoCommit(false);
   try{
    long userId;
    try(PreparedStatement ps=c.prepareStatement("SELECT user_id FROM customers WHERE customer_id=? FOR UPDATE")){ps.setLong(1,customerId);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Customer not found.");userId=rs.getLong(1);}}
    switch(action){
     case "APPROVE" -> {try(PreparedStatement ps=c.prepareStatement("UPDATE customers SET account_status='APPROVED' WHERE customer_id=?")){ps.setLong(1,customerId);ps.executeUpdate();}}
     case "REJECT" -> {try(PreparedStatement ps=c.prepareStatement("UPDATE customers SET account_status='REJECTED' WHERE customer_id=?")){ps.setLong(1,customerId);ps.executeUpdate();}}
     case "UPDATE" -> {String name=v(req,"fullName"),mobile=v(req,"mobile"),email=v(req,"email"),address=v(req,"address");
      if(name.isBlank()||!mobile.matches("^[0-9+ -]{8,15}$")||!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")||address.isBlank())throw new IllegalArgumentException("Enter valid name, email, mobile and address.");
      try(PreparedStatement ps=c.prepareStatement("UPDATE customers SET full_name=?,address=? WHERE customer_id=?")){ps.setString(1,name);ps.setString(2,address);ps.setLong(3,customerId);ps.executeUpdate();}
      try(PreparedStatement ps=c.prepareStatement("UPDATE users SET email=?,mobile=? WHERE user_id=?")){ps.setString(1,email);ps.setString(2,mobile);ps.setLong(3,userId);ps.executeUpdate();}
     }
     case "DEACTIVATE" -> {try(PreparedStatement ps=c.prepareStatement("UPDATE users SET status='INACTIVE' WHERE user_id=?")){ps.setLong(1,userId);ps.executeUpdate();}try(PreparedStatement ps=c.prepareStatement("UPDATE accounts SET status='INACTIVE' WHERE customer_id=?")){ps.setLong(1,customerId);ps.executeUpdate();}}
     case "DEACTIVATE_LOGIN" -> {try(PreparedStatement ps=c.prepareStatement("UPDATE users SET status='INACTIVE' WHERE user_id=?")){ps.setLong(1,userId);ps.executeUpdate();}}
     case "ACTIVATE" -> {try(PreparedStatement ps=c.prepareStatement("UPDATE users SET status='ACTIVE' WHERE user_id=?")){ps.setLong(1,userId);ps.executeUpdate();}}
     default -> throw new IllegalArgumentException("Unknown action.");
    }
    c.commit();Web.redirectMessage(req,resp,"/employee/customers","success","Customer record updated.");
   }catch(Exception e){c.rollback();Web.redirectMessage(req,resp,"/employee/customers","error",e.getMessage()==null?"Operation failed.":e.getMessage());}
  }catch(SQLException e){getServletContext().log("Customer action failed",e);Web.redirectMessage(req,resp,"/employee/customers","error","Could not update customer. Check for duplicate email values.");}
 }
 private static String v(HttpServletRequest r,String k){String s=r.getParameter(k);return s==null?"":s.trim();}
}
