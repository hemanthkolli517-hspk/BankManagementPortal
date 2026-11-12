package in.bankportal.filter;

import in.bankportal.util.DB;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebFilter("/*")
public class AuditFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);
        Object idObj = session == null ? null : session.getAttribute("userId");
        String username = session == null ? null : (String) session.getAttribute("username");
        String role = session == null ? null : (String) session.getAttribute("role");
        Long userId = idObj instanceof Long ? (Long) idObj : null;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        boolean record = userId != null && username != null &&
                ("EMPLOYEE".equals(role) || "ADMIN".equals(role)) &&
                !path.startsWith("/assets/") &&
                !path.endsWith(".css") && !path.endsWith(".js") &&
                !path.endsWith(".png") && !path.endsWith(".jpg") &&
                !path.endsWith(".ico");

        try {
            chain.doFilter(request, response);
        } finally {
            if (record) {
                StringBuilder details = new StringBuilder();
                appendParameter(details, req, "action");
                appendParameter(details, req, "customerId");
                appendParameter(details, req, "accountId");
                appendParameter(details, req, "employeeId");
                appendParameter(details, req, "transactionType");
                String detail = details.length() == 0 ? null : details.toString();
                try (Connection c = DB.getConnection();
                     PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO audit_log(user_id,actor_username,actor_role,http_method,request_path,action_detail,ip_address) VALUES(?,?,?,?,?,?,?)")) {
                    ps.setLong(1, userId);
                    ps.setString(2, username);
                    ps.setString(3, role);
                    ps.setString(4, req.getMethod());
                    ps.setString(5, path.length() > 255 ? path.substring(0, 255) : path);
                    ps.setString(6, detail);
                    ps.setString(7, req.getRemoteAddr());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    req.getServletContext().log("Could not write audit log", e);
                }
            }
        }
    }

    private void appendParameter(StringBuilder details, HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value != null && !value.isBlank()) {
            if (details.length() > 0) details.append("; ");
            details.append(name).append("=").append(value.trim());
        }
    }
}
