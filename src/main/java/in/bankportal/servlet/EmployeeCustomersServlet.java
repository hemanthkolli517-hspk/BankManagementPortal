package in.bankportal.servlet;
import in.bankportal.util.DB;import in.bankportal.util.Web;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import jakarta.servlet.*;import java.io.IOException;import java.sql.*;import java.util.*;
@WebServlet("/employee/customers")
public class EmployeeCustomersServlet extends HttpServlet{
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
  if(!Web.requireRole(req,resp,"EMPLOYEE"))return;
  String filter=req.getParameter("status");if(filter==null||!Set.of("ALL","PENDING","APPROVED","REJECTED").contains(filter))filter="ALL";
  String sql="SELECT c.customer_id,c.full_name,c.gender,c.dob,c.address,c.identity_type,c.identity_file_name,c.account_number,c.account_status,u.user_id,u.username,u.email,u.mobile,u.status AS login_status FROM customers c JOIN users u ON u.user_id=c.user_id "+("ALL".equals(filter)?"":"WHERE c.account_status=? ")+"ORDER BY c.created_at DESC";
  try(Connection c=DB.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
   if(!"ALL".equals(filter))ps.setString(1,filter);
   try(ResultSet rs=ps.executeQuery()){List<Map<String,Object>> rows=new ArrayList<>();while(rs.next()){Map<String,Object> m=new LinkedHashMap<>();for(String k:new String[]{"customer_id","full_name","gender","dob","address","identity_type","identity_file_name","account_number","account_status","user_id","username","email","mobile","login_status"})m.put(k,rs.getObject(k));rows.add(m);}req.setAttribute("customers",rows);}
   req.setAttribute("statusFilter",filter);req.getRequestDispatcher("/employee-customers.jsp").forward(req,resp);
  }catch(SQLException e){getServletContext().log("Customer list failed",e);throw new ServletException(e);}
 }
}
