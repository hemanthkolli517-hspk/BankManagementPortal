package in.bankportal.servlet;
import in.bankportal.util.DB;import in.bankportal.util.Web;
import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import jakarta.servlet.*;import java.io.IOException;import java.sql.*;
@WebServlet("/customer/dashboard")
public class CustomerDashboardServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
  if(!Web.requireRole(req,resp,"CUSTOMER"))return;
  String sql="SELECT c.customer_id,c.full_name,c.account_status,a.account_id,a.account_number,a.account_type,a.balance,a.status FROM customers c LEFT JOIN accounts a ON a.customer_id=c.customer_id WHERE c.user_id=? ORDER BY a.account_id";
  try(Connection con=DB.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
   ps.setLong(1,Web.userId(req));try(ResultSet rs=ps.executeQuery()){java.util.List<java.util.Map<String,Object>> rows=new java.util.ArrayList<>();while(rs.next()){java.util.Map<String,Object> m=new java.util.HashMap<>();m.put("customerId",rs.getLong("customer_id"));m.put("name",rs.getString("full_name"));m.put("applicationStatus",rs.getString("account_status"));m.put("accountId",rs.getObject("account_id"));m.put("accountNumber",rs.getString("account_number"));m.put("accountType",rs.getString("account_type"));m.put("balance",rs.getBigDecimal("balance"));m.put("accountStatus",rs.getString("status"));rows.add(m);}req.setAttribute("accounts",rows);}
   req.getRequestDispatcher("/customer-dashboard.jsp").forward(req,resp);
  }catch(SQLException e){getServletContext().log("Customer dashboard failed",e);throw new ServletException("Unable to load customer dashboard. Check database configuration.",e);}
 }
}
