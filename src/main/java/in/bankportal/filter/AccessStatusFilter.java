package in.bankportal.filter;

import in.bankportal.util.DB;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebFilter("/*")
public class AccessStatusFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        Object idObj = session == null ? null : session.getAttribute("userId");
        String role = session == null ? null : (String) session.getAttribute("role");

        if (idObj instanceof Long && role != null && !req.getRequestURI().endsWith("/logout")) {
            long userId = (Long) idObj;
            try (Connection c = DB.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT u.status,r.role_name,e.status AS employee_status,e.application_status " +
                     "FROM users u JOIN roles r ON r.role_id=u.role_id " +
                     "LEFT JOIN employees e ON e.user_id=u.user_id WHERE u.user_id=?")) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean allowed = rs.next() && "ACTIVE".equals(rs.getString("status"));
                    if (allowed && "EMPLOYEE".equals(role)) {
                        allowed = "ACTIVE".equals(rs.getString("employee_status")) &&
                                  "APPROVED".equals(rs.getString("application_status"));
                    }
                    if (!allowed) {
                        session.invalidate();
                        resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Your+account+is+inactive+or+not+approved");
                        return;
                    }
                }
            } catch (SQLException e) {
                req.getServletContext().log("Could not verify account access status", e);
                resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Unable to verify account access. Please try again.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
