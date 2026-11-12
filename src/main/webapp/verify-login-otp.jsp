<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="in.bankportal.util.Web" %>
<%
if (session.getAttribute("pendingLoginUserId") == null) {
    response.sendRedirect(request.getContextPath() + "/login.jsp?msg=Please+sign+in+first");
    return;
}
String token = Web.csrf(session);
Object flash = session.getAttribute("flashMessage");
Object kind = session.getAttribute("flashKind");
session.removeAttribute("flashMessage");
session.removeAttribute("flashKind");
String email = String.valueOf(session.getAttribute("pendingLoginEmail"));
%>
<!doctype html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Verify login · Bankwise</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/style.css">
</head>
<body class="auth-page">
<header class="topbar">
  <a class="brand" href="<%=request.getContextPath()%>/"><span class="brand-mark">B</span> Bank<span>wise</span></a>
  <a href="<%=request.getContextPath()%>/login.jsp">Cancel</a>
</header>
<main class="auth-wrap">
  <section class="auth-intro">
    <span class="eyebrow">SECURE SIGN IN</span>
    <h1>One more step to your banking workspace.</h1>
    <p>Enter the verification code sent to your registered email address.</p>
    <ul class="check-list">
      <li>OTP verification is required for every login</li>
      <li>The code expires in 10 minutes</li>
      <li>Never share your verification code</li>
    </ul>
  </section>
  <section class="form-card">
    <div class="form-heading">
      <div class="brand-mark">B</div>
      <h2>Verify your email</h2>
      <p>Code sent to <strong><%=email.replace("<","&lt;").replace(">","&gt;")%></strong></p>
    </div>
    <% if (flash != null) { %><div class="alert alert-<%=kind%>"><%=flash%></div><% } %>
    <form method="post" action="<%=request.getContextPath()%>/verify-login-otp" class="stack-form" data-validate>
      <input type="hidden" name="csrfToken" value="<%=token%>">
      <label>6-digit verification code
        <input name="otp" required inputmode="numeric" pattern="[0-9]{6}" minlength="6" maxlength="6" autocomplete="one-time-code" placeholder="Enter OTP">
      </label>
      <button class="btn btn-primary btn-full" type="submit">Verify and sign in</button>
    </form>
    <form method="post" action="<%=request.getContextPath()%>/resend-login-otp" class="stack-form" style="margin-top:12px">
      <input type="hidden" name="csrfToken" value="<%=token%>">
      <button class="btn btn-light btn-full" type="submit">Resend OTP</button>
    </form>
    <p class="form-footnote">If you did not request this code, sign out and secure your account.</p>
  </section>
</main>
<script src="<%=request.getContextPath()%>/assets/js/app.js"></script>
</body>
</html>
