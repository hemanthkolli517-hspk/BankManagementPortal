package in.bankportal.servlet;
import in.bankportal.util.DB;import in.bankportal.util.Web;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import jakarta.servlet.*;import java.io.IOException;import java.sql.*;import java.util.*;
@WebServlet("/transactions")
public class TransactionsServlet extends HttpServlet{
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
  if(!Web.requireLogin(req,resp))return;
  boolean staff=!"CUSTOMER".equals(Web.role(req));
  String sql="SELECT t.transaction_id,t.transaction_type,t.amount,t.balance_after,t.transaction_date,t.description,a.account_number,c.full_name FROM transactions t JOIN accounts a ON a.account_id=t.account_id JOIN customers c ON c.customer_id=a.customer_id "+(staff?"":"JOIN customers mine ON mine.customer_id=a.customer_id WHERE mine.user_id=? ")+"ORDER BY t.transaction_date DESC LIMIT 500";
  try(Connection c=DB.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
   if(!staff)ps.setLong(1,Web.userId(req));
   try(ResultSet rs=ps.executeQuery()){List<Map<String,Object>> rows=new ArrayList<>();while(rs.next()){Map<String,Object> m=new LinkedHashMap<>();for(String k:new String[]{"transaction_id","transaction_type","amount","balance_after","transaction_date","description","account_number","full_name"})m.put(k,rs.getObject(k));rows.add(m);}req.setAttribute("transactions",rows);}
   req.getRequestDispatcher("/transactions.jsp").forward(req,resp);
  }catch(SQLException e){getServletContext().log("Transaction list failed",e);throw new ServletException(e);}
 }
}
