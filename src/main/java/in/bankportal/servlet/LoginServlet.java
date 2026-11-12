package in.bankportal.servlet;

import in.bankportal.util.DB;
import in.bankportal.util.Mailer;
import in.bankportal.util.OtpStore;
import in.bankportal.util.Passwords;
import in.bankportal.util.Web;
import jakarta.mail.MessagingException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final String ADMIN_USERNAME = "Admin";
    private static final String ADMIN_PASSWORD = "Admin@123";
    private static final String ADMIN_EMAIL = "ganeshbajaru41@gmail.com";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String username = value(req, "username");
        String password = value(req, "password");

        if (ADMIN_USERNAME.equals(username) && !ADMIN_PASSWORD.equals(password)) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Invalid+credentials");
            return;
        }

        HttpSession current = req.getSession(false);
        if (current != null && current.getAttribute("userId") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        try (Connection c = DB.getConnection()) {
            long userId;
            String role;
            String email;
            String dbStatus = "ACTIVE";
            String employeeApplicationStatus = "APPROVED";

            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                userId = ensureAdminUser(c);
                role = "ADMIN";
                email = ADMIN_EMAIL;
            } else {
                String sql = "SELECT u.user_id,u.username,u.password_hash,u.email,u.status,r.role_name," +
                    "e.application_status FROM users u JOIN roles r ON r.role_id=u.role_id " +
                    "LEFT JOIN employees e ON e.user_id=u.user_id WHERE u.username=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !Passwords.matches(password, rs.getString("password_hash"))) {
                            resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Invalid+credentials");
                            return;
                        }
                        userId = rs.getLong("user_id");
                        role = rs.getString("role_name");
                        email = rs.getString("email");
                        dbStatus = rs.getString("status");
                        employeeApplicationStatus = rs.getString("application_status");
                    }
                }
                if (!"ACTIVE".equals(dbStatus)) {
                    resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Your+account+is+inactive");
                    return;
                }
                if ("EMPLOYEE".equals(role) && !"APPROVED".equals(employeeApplicationStatus)) {
                    String message = "REJECTED".equals(employeeApplicationStatus)
                        ? "Employee+registration+was+rejected" : "Employee+registration+is+awaiting+Admin+approval";
                    resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=" + message);
                    return;
                }
            }

            if (email == null || email.isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=No+email+address+is+configured+for+this+account");
                return;
            }

            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();
            HttpSession session = req.getSession(true);
            session.setMaxInactiveInterval(15 * 60);
            session.setAttribute("pendingLoginUserId", userId);
            session.setAttribute("pendingLoginUsername", username);
            session.setAttribute("pendingLoginRole", role);
            session.setAttribute("pendingLoginEmail", email);
            session.setAttribute("csrfToken", java.util.UUID.randomUUID().toString());

            String otp = OtpStore.issue(email);
            try {
                Mailer.sendOtp(email, otp);
                resp.sendRedirect(req.getContextPath() + "/verify-login-otp.jsp");
            } catch (MessagingException | RuntimeException e) {
                OtpStore.verify(email, "INVALID");
                session.removeAttribute("pendingLoginUserId");
                session.removeAttribute("pendingLoginUsername");
                session.removeAttribute("pendingLoginRole");
                session.removeAttribute("pendingLoginEmail");
                getServletContext().log("Login OTP email could not be sent", e);
                resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Could+not+send+login+OTP.+Check+SMTP+configuration");
            }
        } catch (SQLException e) {
            getServletContext().log("Login failed", e);
            resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Database+connection+error");
        }
    }

    private long ensureAdminUser(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT user_id FROM users WHERE username=?")) {
            ps.setString(1, ADMIN_USERNAME);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    try (PreparedStatement up = c.prepareStatement(
                            "UPDATE users SET email=?,role_id=(SELECT role_id FROM roles WHERE role_name='ADMIN'),status='ACTIVE' WHERE user_id=?")) {
                        up.setString(1, ADMIN_EMAIL);
                        up.setLong(2, id);
                        up.executeUpdate();
                    }
                    return id;
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO users(username,password_hash,email,mobile,role_id,status) " +
                "VALUES(?,?,?,'0000000000',(SELECT role_id FROM roles WHERE role_name='ADMIN'),'ACTIVE')",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ADMIN_USERNAME);
            ps.setString(2, Passwords.hash(ADMIN_PASSWORD));
            ps.setString(3, ADMIN_EMAIL);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Unable to create the Admin account.");
    }

    private static String value(HttpServletRequest req, String key) {
        String value = req.getParameter(key);
        return value == null ? "" : value.trim();
    }
}
