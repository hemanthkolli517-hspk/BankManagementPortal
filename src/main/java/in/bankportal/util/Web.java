package in.bankportal.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public final class Web {
    private Web() {}
    public static Long userId(HttpServletRequest req) {
        Object value = req.getSession(false) == null ? null : req.getSession(false).getAttribute("userId");
        return value instanceof Long ? (Long)value : null;
    }
    public static String role(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (String)s.getAttribute("role");
    }
    public static boolean requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (userId(req) == null) { resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Please+sign+in+first"); return false; }
        return true;
    }
    public static boolean requireRole(HttpServletRequest req, HttpServletResponse resp, String... roles) throws IOException {
        if (!requireLogin(req, resp)) return false;
        String current = role(req);
        for (String role : roles) if (role.equals(current)) return true;
        resp.sendError(403, "You are not authorized to access this page.");
        return false;
    }
    public static String csrf(HttpSession session) {
        Object token = session.getAttribute("csrfToken");
        if (token == null) {
            byte[] bytes = new byte[24]; new SecureRandom().nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute("csrfToken", token);
        }
        return token.toString();
    }
    public static boolean validCsrf(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        String expected = (String)s.getAttribute("csrfToken");
        String actual = req.getParameter("csrfToken");
        return expected != null && actual != null && expected.equals(actual);
    }
    public static void flash(HttpServletRequest req, String kind, String message) {
        req.getSession().setAttribute("flashKind", kind);
        req.getSession().setAttribute("flashMessage", message);
    }
    public static void redirectMessage(HttpServletRequest req, HttpServletResponse resp, String path, String kind, String message) throws IOException {
        flash(req, kind, message);
        resp.sendRedirect(req.getContextPath() + path);
    }
    public static String safeFileName(String name) {
        if (name == null) return "";
        name = name.replace("\\", "/");
        name = name.substring(name.lastIndexOf('/') + 1);
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
