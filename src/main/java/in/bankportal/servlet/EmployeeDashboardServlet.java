package in.bankportal.servlet;

import in.bankportal.util.DB;
import in.bankportal.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/employee/dashboard")
public class EmployeeDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!Web.requireRole(req, resp, "EMPLOYEE")) {
            return;
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        String[][] queries = {
            {"pending", "SELECT COUNT(*) FROM customers WHERE account_status='PENDING'"},
            {"approved", "SELECT COUNT(*) FROM customers WHERE account_status='APPROVED'"},
            {"customers", "SELECT COUNT(*) FROM customers"},
            {"customersWithoutAccount", "SELECT COUNT(*) FROM customers c WHERE c.account_status='APPROVED' AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.customer_id=c.customer_id)"},
            {"accounts", "SELECT COUNT(*) FROM accounts"},
            {"activeAccounts", "SELECT COUNT(*) FROM accounts WHERE status='ACTIVE'"},
            {"transactions", "SELECT COUNT(*) FROM transactions"},
            {"rejected", "SELECT COUNT(*) FROM customers WHERE account_status='REJECTED'"}
        };

        try (Connection c = DB.getConnection(); Statement s = c.createStatement()) {
            for (String[] query : queries) {
                try (ResultSet rs = s.executeQuery(query[1])) {
                    rs.next();
                    stats.put(query[0], rs.getLong(1));
                }
            }
            req.setAttribute("stats", stats);

            String latestSql = "SELECT c.customer_id,c.full_name,c.account_status,u.username,u.email,u.mobile,c.created_at " +
                    "FROM customers c JOIN users u ON u.user_id=c.user_id " +
                    "ORDER BY CASE c.account_status WHEN 'PENDING' THEN 0 WHEN 'APPROVED' THEN 1 ELSE 2 END, c.created_at DESC LIMIT 8";
            try (PreparedStatement ps = c.prepareStatement(latestSql); ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> latest = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    for (String key : new String[]{"customer_id", "full_name", "account_status", "username", "email", "mobile", "created_at"}) {
                        m.put(key, rs.getObject(key));
                    }
                    latest.add(m);
                }
                req.setAttribute("latest", latest);
            }
            req.getRequestDispatcher("/employee-dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Employee dashboard failed", e);
            throw new ServletException("Unable to load employee dashboard.", e);
        }
    }
}
