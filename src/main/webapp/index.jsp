<%@ page contentType="text/html;charset=UTF-8" %>
<%
String role=(String)session.getAttribute("role");
if(role!=null){response.sendRedirect(request.getContextPath()+"/dashboard");return;}
%>
<!doctype html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Bankwise | Bank Management Portal</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/style.css">
</head>
<body class="landing">
<header class="topbar">
  <a class="brand" href="<%=request.getContextPath()%>/"><span class="brand-mark">B</span> Bank<span>wise</span></a>
  <nav><a href="<%=request.getContextPath()%>/login.jsp">Sign in</a><a class="btn btn-primary btn-small" href="<%=request.getContextPath()%>/register.jsp">Open an account</a></nav>
</header>
<main class="hero">
  <div class="hero-copy">
    <span class="eyebrow">SIMPLE · SECURE · CLEAR</span>
    <h1>Your banking,<br><em>made simpler.</em></h1>
    <p>Access your account, review transactions and manage banking services in one convenient place.</p>
    <div class="hero-actions">
      <a class="btn btn-primary" href="<%=request.getContextPath()%>/login.jsp">Sign in to your account</a>
      <a class="btn btn-light" href="<%=request.getContextPath()%>/register.jsp">Customer registration</a>
    </div>
    <div class="trust-row"><span>✓ Secure sign-in</span><span>✓ Email OTP verification</span><span>✓ Easy account access</span></div>
  </div>
  <div class="hero-card">
    <div class="hero-card-top"><span class="small-label">BANKWISE SERVICES</span><span class="status-dot">Available</span></div>
    <div class="mock-balance"><span>Your banking workspace</span><strong>All in one place.</strong><p>Simple access to everyday banking services.</p></div>
    <div class="mock-row"><span class="mock-icon">↗</span><div><b>Accounts</b><small>Review account information</small></div><span class="arrow">→</span></div>
    <div class="mock-row"><span class="mock-icon blue">▦</span><div><b>Transactions</b><small>View deposits and withdrawals</small></div><span class="arrow">→</span></div>
    <div class="mock-row"><span class="mock-icon gold">✓</span><div><b>Customer applications</b><small>Submit and track your application</small></div><span class="arrow">→</span></div>
  </div>
</main>
<footer class="site-footer">Bankwise · Banking services made straightforward</footer>
</body>
</html>
