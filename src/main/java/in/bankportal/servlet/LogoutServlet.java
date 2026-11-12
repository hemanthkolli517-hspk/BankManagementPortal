package in.bankportal.servlet;
import in.bankportal.util.DB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException {
  HttpSession s=req.getSession(false);
  if(s!=null){
   Object id=s.getAttribute("dbSessionId");
   if(id instanceof Long) try(Connection c=DB.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE user_sessions SET logout_time=CURRENT_TIMESTAMP,is_active=FALSE WHERE session_id=?")){ps.setLong(1,(Long)id);ps.executeUpdate();}catch(SQLException e){getServletContext().log("Logout session update failed",e);}
   s.invalidate();
  }
  resp.sendRedirect(req.getContextPath()+"/login.jsp?msg=You+have+signed+out");
 }
}
