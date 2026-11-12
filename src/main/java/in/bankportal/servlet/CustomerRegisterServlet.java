package in.bankportal.servlet;

import in.bankportal.util.DB;
import in.bankportal.util.Passwords;
import in.bankportal.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;

@WebServlet("/register")
@MultipartConfig(maxFileSize = 2 * 1024 * 1024, maxRequestSize = 3 * 1024 * 1024)
public class CustomerRegisterServlet extends HttpServlet {
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!Web.validCsrf(req)) { Web.redirectMessage(req, resp, "/register.jsp", "error", "Form expired. Please try again."); return; }
        String username = val(req,"username"), password = val(req,"password"), email = val(req,"email"), mobile = val(req,"mobile");
        String fullName = val(req,"fullName"), gender = val(req,"gender"), dob = val(req,"dob"), address = val(req,"address");
        String identityType = val(req,"identityType");
        String accountNumber = val(req,"accountNumber");
        if (!accountNumber.isBlank() && !accountNumber.matches("^[A-Za-z0-9-]{1,20}$")) {
            Web.redirectMessage(req, resp, "/register.jsp", "error", "Account number must contain only letters, numbers or hyphens and be at most 20 characters."); return;
        }
        if (username.length() < 4 || password.length() < 8 || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$") ||
            !mobile.matches("^[0-9+ -]{8,15}$") || fullName.isBlank() || address.isBlank() ||
            !java.util.Set.of("Male","Female","Other").contains(gender) ||
            !java.util.Set.of("Aadhaar","Passport","Driving Licence","Voter ID","Other").contains(identityType)) {
            Web.redirectMessage(req, resp, "/register.jsp", "error", "Please check all required fields. Username must be 4+ characters and password 8+ characters."); return;
        }
        String storedName = null;
        try {
            Part file = req.getPart("identityFile");
            String original = Web.safeFileName(file.getSubmittedFileName());
            if (original != null && !original.isBlank() && file.getSize() > 0) {
                String lower = original.toLowerCase();
                if (!(lower.endsWith(".pdf") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) || file.getSize() > 2 * 1024 * 1024) {
                    Web.redirectMessage(req, resp, "/register.jsp", "error", "Identity file must be PDF, JPG or PNG and at most 2 MB."); return;
                }
                String directory = System.getenv("BANK_UPLOAD_DIR");
                Path base = directory == null || directory.isBlank()
                        ? Paths.get(System.getProperty("user.home"), "bank-management-private-uploads")
                        : Paths.get(directory);
                Files.createDirectories(base);
                storedName = java.util.UUID.randomUUID() + "_" + original;
                file.write(base.resolve(storedName).toString());
            }
            try (Connection c = DB.getConnection()) {
                c.setAutoCommit(false);
                try {
                    long userId;
                    try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO users(username,password_hash,email,mobile,role_id,status) VALUES(?,?,?,?,3,'ACTIVE')", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, username); ps.setString(2, Passwords.hash(password)); ps.setString(3,email); ps.setString(4,mobile); ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); userId = rs.getLong(1); }
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO customers(user_id,full_name,gender,dob,address,identity_type,identity_file_name,account_number,account_status) VALUES(?,?,?,?,?,?,?,?, 'PENDING')")) {
                        ps.setLong(1,userId); ps.setString(2,fullName); ps.setString(3,gender);
                        if (dob.isBlank()) ps.setNull(4,Types.DATE); else ps.setDate(4,Date.valueOf(LocalDate.parse(dob)));
                        ps.setString(5,address); ps.setString(6,identityType); ps.setString(7,storedName);
                        if (accountNumber.isBlank()) ps.setNull(8, Types.VARCHAR); else ps.setString(8, accountNumber);
                        ps.executeUpdate();
                    }
                    c.commit();
                } catch (Exception e) { c.rollback(); throw e; }
            }
            Web.redirectMessage(req, resp, "/login.jsp", "success", "Registration submitted. Sign in to view your application status after employee review.");
        } catch (SQLIntegrityConstraintViolationException e) {
            Web.redirectMessage(req, resp, "/register.jsp", "error", "Username or email already exists. Please use another one.");
        } catch (Exception e) {
            getServletContext().log("Customer registration failed", e);
            Web.redirectMessage(req, resp, "/register.jsp", "error", "Registration could not be completed. Check database configuration and required fields.");
        }
    }
    private static String val(HttpServletRequest r, String k) { String s=r.getParameter(k); return s==null?"":s.trim(); }
}
