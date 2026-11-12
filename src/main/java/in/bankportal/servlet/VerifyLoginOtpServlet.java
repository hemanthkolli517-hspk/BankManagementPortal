package in.bankportal.servlet;

import in.bankportal.util.DB;
import in.bankportal.util.OtpStore;
import in.bankportal.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/verify-login-otp")
public class VerifyLoginOtpServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("pendingLoginUserId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Please+sign+in+again");
            return;
        }
        if (!Web.validCsrf(req)) {
            Web.redirectMessage(req, resp, "/verify-login-otp.jsp", "error", "Form expired. Please try again.");
            return;
        }
        String email = String.valueOf(session.getAttribute("pendingLoginEmail"));
        String otp = req.getParameter("otp");
        if (!OtpStore.verify(email, otp)) {
            Web.redirectMessage(req, resp, "/verify-login-otp.jsp", "error", "Invalid or expired OTP. Please try again or resend the code.");
            return;
        }

        long userId = (Long) session.getAttribute("pendingLoginUserId");
        String username = String.valueOf(session.getAttribute("pendingLoginUsername"));
        String role = String.valueOf(session.getAttribute("pendingLoginRole"));

        try (Connection c = DB.getConnection()) {
            if (!"ADMIN".equals(role)) {
                String sql = "SELECT u.status,r.role_name,e.application_status FROM users u " +
                    "JOIN roles r ON r.role_id=u.role_id LEFT JOIN employees e ON e.user_id=u.user_id WHERE u.user_id=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setLong(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !"ACTIVE".equals(rs.getString("status")) ||
                            ("EMPLOYEE".equals(rs.getString("role_name")) &&
                             !"APPROVED".equals(rs.getString("application_status")))) {
                            session.invalidate();
                            resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Account+is+not+active+or+approved");
                            return;
                        }
                    }
                }
            }

            session.removeAttribute("pendingLoginUserId");
            session.removeAttribute("pendingLoginUsername");
            session.removeAttribute("pendingLoginRole");
            session.removeAttribute("pendingLoginEmail");
            session.setAttribute("userId", userId);
            session.setAttribute("username", username);
            session.setAttribute("role", role);
            session.setAttribute("csrfToken", java.util.UUID.randomUUID().toString());

            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO user_sessions(user_id,ip_address,is_active) VALUES(?,?,TRUE)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ins.setLong(1, userId);
                ins.setString(2, req.getRemoteAddr());
                ins.executeUpdate();
                try (ResultSet key = ins.getGeneratedKeys()) {
                    if (key.next()) session.setAttribute("dbSessionId", key.getLong(1));
                }
            }
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (SQLException e) {
            getServletContext().log("OTP login verification failed", e);
            Web.redirectMessage(req, resp, "/verify-login-otp.jsp", "error", "Could not complete login. Please try again.");
        }
    }
}
