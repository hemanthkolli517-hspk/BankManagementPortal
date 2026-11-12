package in.bankportal.servlet;

import in.bankportal.util.Mailer;
import in.bankportal.util.OtpStore;
import in.bankportal.util.Web;
import jakarta.mail.MessagingException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/resend-login-otp")
public class ResendLoginOtpServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("pendingLoginEmail") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Please+sign+in+again");
            return;
        }
        if (!Web.validCsrf(req)) {
            Web.redirectMessage(req, resp, "/verify-login-otp.jsp", "error", "Form expired. Refresh the page.");
            return;
        }
        String email = String.valueOf(session.getAttribute("pendingLoginEmail"));
        String otp = OtpStore.issue(email);
        try {
            Mailer.sendOtp(email, otp);
            Web.redirectMessage(req, resp, "/verify-login-otp.jsp", "success", "A new OTP has been sent to your email.");
        } catch (MessagingException | RuntimeException e) {
            getServletContext().log("Resending login OTP failed", e);
            Web.redirectMessage(req, resp, "/verify-login-otp.jsp", "error", "Could not send OTP. Check SMTP configuration.");
        }
    }
}
