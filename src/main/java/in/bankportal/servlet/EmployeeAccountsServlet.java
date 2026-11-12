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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/employee/accounts")
public class EmployeeAccountsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!Web.requireRole(req, resp, "EMPLOYEE", "ADMIN")) {
            return;
        }

        String accountStatus = req.getParameter("status");
        if (accountStatus == null || !java.util.Set.of("ALL", "ACTIVE", "INACTIVE").contains(accountStatus)) {
            accountStatus = "ALL";
        }
        String accountSql = "SELECT a.account_id,a.customer_id,a.account_number,a.account_type,a.balance,a.status,a.created_at,c.full_name,c.account_status " +
                "FROM accounts a JOIN customers c ON c.customer_id=a.customer_id " +
                ("ALL".equals(accountStatus) ? "" : "WHERE a.status=? ") +
                "ORDER BY a.created_at DESC";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(accountSql)) {
            if (!"ALL".equals(accountStatus)) {
                ps.setString(1, accountStatus);
            }
            try (ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                for (String key : new String[]{"account_id", "customer_id", "account_number", "account_type", "balance", "status", "created_at", "full_name", "account_status"}) {
                    m.put(key, rs.getObject(key));
                }
                rows.add(m);
            }
            req.setAttribute("accounts", rows);
            }
            req.setAttribute("accountStatusFilter", accountStatus);
        } catch (SQLException e) {
            getServletContext().log("Loading accounts failed", e);
            throw new ServletException("Unable to load accounts.", e);
        }

        // Show approved customers who do not yet have an account, so account creation
        // is a clear next step after an application is approved.
        String eligibleSql = "SELECT c.customer_id,c.full_name FROM customers c " +
                "WHERE c.account_status='APPROVED' AND NOT EXISTS " +
                "(SELECT 1 FROM accounts a WHERE a.customer_id=c.customer_id) ORDER BY c.full_name";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(eligibleSql);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> customers = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getLong("customer_id"));
                m.put("name", rs.getString("full_name"));
                customers.add(m);
            }
            req.setAttribute("approvedCustomers", customers);
            req.setAttribute("eligibleCustomerCount", customers.size());
        } catch (SQLException e) {
            getServletContext().log("Loading eligible customers failed", e);
            throw new ServletException("Unable to load approved customers.", e);
        }

        req.getRequestDispatcher("/employee-accounts.jsp").forward(req, resp);
    }
}
