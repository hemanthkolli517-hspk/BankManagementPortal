package in.bankportal.servlet;

import in.bankportal.util.DB;
import in.bankportal.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/admin/employee-action")
public class AdminEmployeeActionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!Web.requireRole(req, resp, "ADMIN")) return;
        if (!Web.validCsrf(req)) {
            Web.redirectMessage(req, resp, "/admin/dashboard", "error", "Form expired. Please try again.");
            return;
        }
        String action = val(req, "action");
        long employeeId;
        try { employeeId = Long.parseLong(val(req, "employeeId")); }
        catch (Exception e) {
            Web.redirectMessage(req, resp, "/admin/dashboard", "error", "Invalid employee.");
            return;
        }

        String sql;
        switch (action) {
            case "APPROVE":
                sql = "UPDATE employees e JOIN users u ON u.user_id=e.user_id SET e.application_status='APPROVED',e.status='ACTIVE',u.status='ACTIVE' WHERE e.employee_id=? AND e.application_status='PENDING'";
                break;
            case "REJECT":
                sql = "UPDATE employees e JOIN users u ON u.user_id=e.user_id SET e.application_status='REJECTED',e.status='INACTIVE',u.status='INACTIVE' WHERE e.employee_id=? AND e.application_status='PENDING'";
                break;
            case "ACTIVATE":
                sql = "UPDATE employees e JOIN users u ON u.user_id=e.user_id SET e.status='ACTIVE',u.status='ACTIVE' WHERE e.employee_id=? AND e.application_status='APPROVED'";
                break;
            case "DEACTIVATE":
                sql = "UPDATE employees e JOIN users u ON u.user_id=e.user_id SET e.status='INACTIVE',u.status='INACTIVE' WHERE e.employee_id=? AND e.application_status='APPROVED'";
                break;
            default:
                Web.redirectMessage(req, resp, "/admin/dashboard", "error", "Unknown employee action.");
                return;
        }

        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, employeeId);
            int changed = ps.executeUpdate();
            Web.redirectMessage(req, resp, "/admin/dashboard",
                changed > 0 ? "success" : "error",
                changed > 0 ? "Employee status updated successfully." : "No employee was updated. Check the current application status.");
        } catch (SQLException e) {
            getServletContext().log("Admin employee action failed", e);
            Web.redirectMessage(req, resp, "/admin/dashboard", "error", "Could not update employee. Check the database migration.");
        }
    }
    private static String val(HttpServletRequest req, String key) {
        String value = req.getParameter(key);
        return value == null ? "" : value.trim();
    }
}
