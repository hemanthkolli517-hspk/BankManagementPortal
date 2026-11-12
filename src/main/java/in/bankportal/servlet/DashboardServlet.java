package in.bankportal.servlet;

import in.bankportal.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!Web.requireLogin(req, resp)) return;
        String role = Web.role(req);
        if ("CUSTOMER".equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/customer/dashboard");
        } else if ("EMPLOYEE".equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/employee/dashboard");
        } else if ("ADMIN".equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else {
            resp.sendError(403);
        }
    }
}
