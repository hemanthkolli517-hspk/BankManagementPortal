package in.bankportal.servlet;

import in.bankportal.util.DB;
import in.bankportal.util.OtpStore;
import in.bankportal.util.Mailer;
import in.bankportal.util.Passwords;
import in.bankportal.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/employee/register")
public class EmployeeRegisterServlet extends HttpServlet {
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        if (!Web.validCsrf(req)) { Web.redirectMessage(req,resp,"/employee-register.jsp","error","Form expired. Please try again."); return; }
        String username=v(req,"username"), password=v(req,"password"), email=v(req,"email").toLowerCase(), mobile=v(req,"mobile"),
            otp=v(req,"otp"), code=v(req,"employeeCode"), designation=v(req,"designation"), department=v(req,"department"), address=v(req,"address");
        if (username.length()<4 || password.length()<8 || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$") ||
            !mobile.matches("^[0-9+ -]{8,15}$") || code.isBlank() || designation.isBlank() || department.isBlank() || address.isBlank()) {
            Web.redirectMessage(req,resp,"/employee-register.jsp","error","Complete all required fields and use a password of at least 8 characters."); return;
        }
        if (!OtpStore.verify(email,otp)) { Web.redirectMessage(req,resp,"/employee-register.jsp","error","OTP is invalid, expired or has reached the attempt limit. Request a new code."); return; }
        try (Connection c=DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                long uid;
                try (PreparedStatement ps=c.prepareStatement("INSERT INTO users(username,password_hash,email,mobile,role_id,status) VALUES(?,?,?,?,2,'ACTIVE')",Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1,username); ps.setString(2,Passwords.hash(password)); ps.setString(3,email); ps.setString(4,mobile); ps.executeUpdate();
                    try(ResultSet rs=ps.getGeneratedKeys()){rs.next();uid=rs.getLong(1);}
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO employees(user_id,employee_code,designation,department,address,status,application_status) VALUES(?,?,?,?,?,'ACTIVE','PENDING')")) {
                    ps.setLong(1,uid); ps.setString(2,code); ps.setString(3,designation); ps.setString(4,department); ps.setString(5,address); ps.executeUpdate();
                }
                c.commit();
            } catch(Exception e) { c.rollback(); throw e; }
            try {
                Mailer.sendText("ganeshbajaru41@gmail.com", "New employee registration request",
                    "A new employee registration is awaiting your approval.\nUsername: " + username +
                    "\nEmployee code: " + code + "\nEmail: " + email);
            } catch (Exception mailError) {
                getServletContext().log("Admin notification email could not be sent", mailError);
            }
            Web.redirectMessage(req,resp,"/login.jsp","success","Employee registration submitted. Admin approval is required before login.");
        } catch(SQLIntegrityConstraintViolationException e) {
            Web.redirectMessage(req,resp,"/employee-register.jsp","error","Username, email or employee code already exists.");
        } catch(Exception e) {
            getServletContext().log("Employee registration failed",e);
            Web.redirectMessage(req,resp,"/employee-register.jsp","error","Could not complete registration. Check database settings and unique fields.");
        }
    }
    private static String v(HttpServletRequest r,String k){String s=r.getParameter(k);return s==null?"":s.trim();}
}
