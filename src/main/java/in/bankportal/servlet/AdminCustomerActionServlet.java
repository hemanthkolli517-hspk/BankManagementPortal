package in.bankportal.servlet;

import in.bankportal.util.DB;
import in.bankportal.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

@WebServlet("/admin/customer-action")
public class AdminCustomerActionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!Web.requireRole(req, resp, "ADMIN")) return;
        if (!Web.validCsrf(req)) {
            Web.redirectMessage(req, resp, "/admin/dashboard", "error", "Form expired. Please try again.");
            return;
        }

        String action = value(req, "action");
        long customerId;
        try {
            customerId = Long.parseLong(value(req, "customerId"));
        } catch (NumberFormatException ex) {
            Web.redirectMessage(req, resp, "/admin/dashboard", "error", "Invalid customer record.");
            return;
        }

        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                long userId;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT user_id FROM customers WHERE customer_id=? FOR UPDATE")) {
                    ps.setLong(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Customer not found.");
                        userId = rs.getLong("user_id");
                    }
                }

                switch (action) {
                    case "APPROVE":
                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE customers SET account_status='APPROVED' WHERE customer_id=? AND account_status='PENDING'")) {
                            ps.setLong(1, customerId);
                            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("This customer request is not pending.");
                        }
                        break;
                    case "REJECT":
                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE customers SET account_status='REJECTED' WHERE customer_id=? AND account_status='PENDING'")) {
                            ps.setLong(1, customerId);
                            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("This customer request is not pending.");
                        }
                        break;
                    case "UPDATE": {
                        String fullName = value(req, "fullName");
                        String email = value(req, "email");
                        String mobile = value(req, "mobile");
                        String address = value(req, "address");
                        String gender = value(req, "gender");
                        String dob = value(req, "dob");
                        String identityType = value(req, "identityType");
                        String accountNumber = value(req, "accountNumber");
                        if (fullName.isBlank() || address.isBlank()
                                || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                                || !mobile.matches("^[0-9+ -]{8,15}$")
                                || !java.util.Set.of("Male", "Female", "Other").contains(gender)
                                || !java.util.Set.of("Aadhaar", "Passport", "Driving Licence", "Voter ID", "Other").contains(identityType)
                                || (!accountNumber.isBlank() && !accountNumber.matches("^[A-Za-z0-9-]{1,20}$"))) {
                            throw new IllegalArgumentException("Enter valid customer details. Check gender, identity type and account number.");
                        }
                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE customers SET full_name=?,address=?,gender=?,dob=?,identity_type=?,account_number=? WHERE customer_id=?")) {
                            ps.setString(1, fullName);
                            ps.setString(2, address);
                            ps.setString(3, gender);
                            if (dob.isBlank()) ps.setNull(4, Types.DATE);
                            else ps.setDate(4, Date.valueOf(LocalDate.parse(dob)));
                            ps.setString(5, identityType);
                            if (accountNumber.isBlank()) ps.setNull(6, Types.VARCHAR);
                            else ps.setString(6, accountNumber);
                            ps.setLong(7, customerId);
                            ps.executeUpdate();
                        }
                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE users SET email=?,mobile=? WHERE user_id=?")) {
                            ps.setString(1, email);
                            ps.setString(2, mobile);
                            ps.setLong(3, userId);
                            ps.executeUpdate();
                        }
                        break;
                    }
                    case "ACTIVATE":
                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE users SET status='ACTIVE' WHERE user_id=?")) {
                            ps.setLong(1, userId);
                            ps.executeUpdate();
                        }
                        break;
                    case "DEACTIVATE":
                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE users SET status='INACTIVE' WHERE user_id=?")) {
                            ps.setLong(1, userId);
                            ps.executeUpdate();
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown customer action.");
                }

                c.commit();
                Web.redirectMessage(req, resp, "/admin/dashboard", "success", "Customer record updated successfully.");
            } catch (Exception ex) {
                c.rollback();
                Web.redirectMessage(req, resp, "/admin/dashboard", "error",
                    ex.getMessage() == null ? "Customer operation failed." : ex.getMessage());
            }
        } catch (SQLException ex) {
            getServletContext().log("Admin customer action failed", ex);
            Web.redirectMessage(req, resp, "/admin/dashboard", "error",
                "Could not update customer. Check whether the email is already registered.");
        }
    }

    private static String value(HttpServletRequest req, String key) {
        String v = req.getParameter(key);
        return v == null ? "" : v.trim();
    }
}
