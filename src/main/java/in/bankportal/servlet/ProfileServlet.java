package in.bankportal.servlet;
import in.bankportal.util.DB;import in.bankportal.util.Web;import in.bankportal.util.Passwords;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import jakarta.servlet.*;import java.io.IOException;import java.sql.*;import java.util.*;
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet{
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
  if(!Web.requireLogin(req,resp))return;
  try(Connection c=DB.getConnection();PreparedStatement ps=c.prepareStatement("SELECT u.username,u.email,u.mobile,c.full_name,c.gender,c.dob,c.address,c.identity_type,c.account_number,e.employee_code,e.designation,e.department,e.address AS employee_address FROM users u LEFT JOIN customers c ON c.user_id=u.user_id LEFT JOIN employees e ON e.user_id=u.user_id WHERE u.user_id=?")){
   ps.setLong(1,Web.userId(req));try(ResultSet rs=ps.executeQuery()){if(rs.next()){Map<String,Object> p=new HashMap<>();ResultSetMetaData md=rs.getMetaData();for(int i=1;i<=md.getColumnCount();i++)p.put(md.getColumnLabel(i),rs.getObject(i));req.setAttribute("profile",p);}}
   req.getRequestDispatcher("/profile.jsp").forward(req,resp);
  }catch(SQLException e){throw new ServletException(e);}
 }
 @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  if(!Web.requireLogin(req,resp))return;
  if("CUSTOMER".equals(Web.role(req))){Web.redirectMessage(req,resp,"/profile","error","Registered customer details can only be edited by an administrator.");return;}
  if(!Web.validCsrf(req)){Web.redirectMessage(req,resp,"/profile","error","Form expired.");return;}
  String email=v(req,"email"),mobile=v(req,"mobile"),address=v(req,"address"),fullName=v(req,"fullName"),password=v(req,"newPassword");
  if(!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")||!mobile.matches("^[0-9+ -]{8,15}$")){Web.redirectMessage(req,resp,"/profile","error","Enter a valid email and mobile number.");return;}
  try(Connection c=DB.getConnection()){
   c.setAutoCommit(false);
   try{
    try(PreparedStatement ps=c.prepareStatement("UPDATE users SET email=?,mobile=? WHERE user_id=?")){ps.setString(1,email);ps.setString(2,mobile);ps.setLong(3,Web.userId(req));ps.executeUpdate();}
    if(!password.isBlank()){if(password.length()<8)throw new IllegalArgumentException("New password must be at least 8 characters.");try(PreparedStatement ps=c.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")){ps.setString(1,Passwords.hash(password));ps.setLong(2,Web.userId(req));ps.executeUpdate();}}
    c.commit();Web.redirectMessage(req,resp,"/profile","success","Profile updated successfully.");
   }catch(Exception e){c.rollback();Web.redirectMessage(req,resp,"/profile","error",e.getMessage()==null?"Profile update failed.":e.getMessage());}
  }catch(SQLException e){getServletContext().log("Profile update failed",e);Web.redirectMessage(req,resp,"/profile","error","Update failed. Email may already be registered.");}
 }
 private static String v(HttpServletRequest r,String k){String s=r.getParameter(k);return s==null?"":s.trim();}
}
