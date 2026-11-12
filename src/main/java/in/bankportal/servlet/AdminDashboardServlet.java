package in.bankportal.servlet;

import in.bankportal.util.DB;
import in.bankportal.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!Web.requireRole(req, resp, "ADMIN")) return;

        try (Connection c = DB.getConnection()) {
            req.setAttribute("pendingEmployees", query(c,
                "SELECT e.employee_id,e.employee_code,e.designation,e.department,e.application_status,e.status, " +
                "u.user_id,u.username,u.email,u.mobile,e.created_at FROM employees e " +
                "JOIN users u ON u.user_id=e.user_id WHERE e.application_status='PENDING' ORDER BY e.created_at"));

            req.setAttribute("employees", query(c,
                "SELECT e.employee_id,e.employee_code,e.designation,e.department,e.application_status,e.status, " +
                "u.user_id,u.username,u.email,u.mobile,e.created_at FROM employees e " +
                "JOIN users u ON u.user_id=e.user_id ORDER BY e.created_at DESC"));

            String customerSelect = "SELECT c.customer_id,c.full_name,c.gender,c.dob,c.address,c.identity_type, " +
                "c.identity_file_name,c.account_number,c.account_status,c.created_at,u.user_id,u.username, " +
                "u.email,u.mobile,u.status AS login_status FROM customers c JOIN users u ON u.user_id=c.user_id ";
            req.setAttribute("pendingCustomers", query(c, customerSelect +
                "WHERE c.account_status='PENDING' ORDER BY c.created_at DESC"));
            req.setAttribute("customers", query(c, customerSelect + "ORDER BY c.created_at DESC"));

            String statsSql = "SELECT " +
                "(SELECT COUNT(*) FROM employees) employeeCount," +
                "(SELECT COUNT(*) FROM customers) customerCount," +
                "(SELECT COUNT(*) FROM transactions) transactionCount," +
                "(SELECT COALESCE(SUM(amount),0) FROM transactions WHERE transaction_type='DEPOSIT') totalDeposits," +
                "(SELECT COALESCE(SUM(amount),0) FROM transactions WHERE transaction_type='WITHDRAWAL') totalWithdrawals," +
                "(SELECT COUNT(*) FROM employees WHERE application_status='PENDING') pendingEmployees," +
                "(SELECT COUNT(*) FROM customers WHERE account_status='PENDING') pendingCustomers";
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(statsSql)) {
                rs.next();
                Map<String,Object> stats = new LinkedHashMap<>();
                stats.put("employeeCount", rs.getLong("employeeCount"));
                stats.put("customerCount", rs.getLong("customerCount"));
                stats.put("transactionCount", rs.getLong("transactionCount"));
                stats.put("totalDeposits", rs.getBigDecimal("totalDeposits"));
                stats.put("totalWithdrawals", rs.getBigDecimal("totalWithdrawals"));
                stats.put("pendingEmployees", rs.getLong("pendingEmployees"));
                stats.put("pendingCustomers", rs.getLong("pendingCustomers"));
                req.setAttribute("stats", stats);
            }
            req.setAttribute("csrfToken", Web.csrf(req.getSession()));
            req.getRequestDispatcher("/admin-dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Admin dashboard failed", e);
            throw new ServletException("Unable to load Admin dashboard. Check database configuration and schema.", e);
        }
    }

    private List<Map<String,Object>> query(Connection c, String sql) throws SQLException {
        List<Map<String,Object>> rows = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    row.put(md.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
        }
        return rows;
    }
}
