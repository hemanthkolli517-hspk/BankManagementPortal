package in.bankportal.servlet;

import in.bankportal.util.DB;
import in.bankportal.util.Mailer;
import in.bankportal.util.OtpStore;
import in.bankportal.util.Web;
import jakarta.mail.MessagingException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/employee/send-otp")
public class EmployeeOtpServlet extends HttpServlet {
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        String email = req.getParameter("email") == null ? "" : req.getParameter("email").trim().toLowerCase();
        if (!Web.validCsrf(req)) { resp.setStatus(400); resp.getWriter().write("Form expired. Refresh the page."); return; }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) { resp.setStatus(400); resp.getWriter().write("Enter a valid email address first."); return; }
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT user_id FROM users WHERE email=?")) {
            ps.setString(1,email);
            try (ResultSet rs=ps.executeQuery()) {
                if (rs.next()) { resp.setStatus(409); resp.getWriter().write("This email is already registered."); return; }
            }
        } catch (SQLException e) { getServletContext().log("Email lookup failed",e); resp.setStatus(500); resp.getWriter().write("Database connection failed."); return; }
        String otp = OtpStore.issue(email);
        try {
            Mailer.sendOtp(email, otp);
            resp.getWriter().write("Verification code sent. It expires in 10 minutes.");
        } catch (MessagingException | RuntimeException e) {
            getServletContext().log("SMTP send failed",e);
            resp.setStatus(500); resp.getWriter().write("Email could not be sent. Configure BANK_SMTP_APP_PASSWORD and try again.");
        }
    }
}
