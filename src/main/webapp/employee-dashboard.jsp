<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%
Map<String,Object> stats=(Map<String,Object>)request.getAttribute("stats");
List<Map<String,Object>> latest=(List<Map<String,Object>>)request.getAttribute("latest");
Object flash=session.getAttribute("flashMessage");
Object kind=session.getAttribute("flashKind");
session.removeAttribute("flashMessage");
session.removeAttribute("flashKind");
String ctx=request.getContextPath();
%>
<!doctype html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Employee dashboard · Bankwise</title>
<link rel="stylesheet" href="<%=ctx%>/assets/css/style.css">
</head>
<body>
<header class="appbar">
  <a class="brand" href="<%=ctx%>/employee/dashboard"><span class="brand-mark">B</span> Bank<span>wise</span></a>
  <nav>
    <a class="active" href="<%=ctx%>/employee/dashboard">Dashboard</a>
    <a href="<%=ctx%>/employee/customers">Customers</a>
    <a href="<%=ctx%>/employee/accounts">Accounts</a>
    <a href="<%=ctx%>/transactions">Transactions</a>
    <a href="<%=ctx%>/profile">My profile</a>
    <span class="user-chip"><%=session.getAttribute("username")%></span>
    <a href="<%=ctx%>/logout">Sign out</a>
  </nav>
</header>

<main class="page-shell">
  <div class="welcome-row">
    <div>
      <span class="eyebrow">EMPLOYEE WORKSPACE</span>
      <h1>Good day, <%=session.getAttribute("username")%></h1>
      <p>Every operation has a count and a single View button to open its workspace.</p>
    </div>
  </div>

  <% if(flash!=null){ %><div class="alert alert-<%=kind%>"><%=flash%></div><% } %>

  <div class="section-heading-row">
    <div><h2>Operations overview</h2><p>Live counts from the bank database</p></div>
    <span class="subtle-note">Counts refresh when you open this dashboard.</span>
  </div>

  <div class="metric-grid employee-metric-grid">
    <article class="metric-card metric-accent">
      <span>Pending applications</span><strong><%=stats.get("pending")%></strong>
      <small>Applications waiting for a decision</small>
      <a class="btn btn-light btn-small metric-view" href="<%=ctx%>/employee/customers?status=PENDING">View</a>
    </article>
    <article class="metric-card">
      <span>Approved customers</span><strong><%=stats.get("approved")%></strong>
      <small>Customers approved by an employee</small>
      <a class="btn btn-light btn-small metric-view" href="<%=ctx%>/employee/customers?status=APPROVED">View</a>
    </article>
    <article class="metric-card">
      <span>Customers</span><strong><%=stats.get("customers")%></strong>
      <small>All registered customer profiles</small>
      <a class="btn btn-light btn-small metric-view" href="<%=ctx%>/employee/customers">View</a>
    </article>
    <article class="metric-card metric-blue">
      <span>Approved customers without account</span><strong><%=stats.get("customersWithoutAccount")%></strong>
      <small>Ready for employee account creation</small>
      <a class="btn btn-light btn-small metric-view" href="<%=ctx%>/employee/accounts#create-account">View</a>
    </article>
    <article class="metric-card">
      <span>Total accounts</span><strong><%=stats.get("accounts")%></strong>
      <small>All savings and current accounts</small>
      <a class="btn btn-light btn-small metric-view" href="<%=ctx%>/employee/accounts">View</a>
    </article>
    <article class="metric-card">
      <span>Active accounts</span><strong><%=stats.get("activeAccounts")%></strong>
      <small>Accounts currently enabled</small>
      <a class="btn btn-light btn-small metric-view" href="<%=ctx%>/employee/accounts?status=ACTIVE">View</a>
    </article>
    <article class="metric-card metric-green">
      <span>Transactions</span><strong><%=stats.get("transactions")%></strong>
      <small>Deposit and withdrawal ledger entries</small>
      <a class="btn btn-light btn-small metric-view" href="<%=ctx%>/transactions">View</a>
    </article>
    <article class="metric-card metric-red">
      <span>Rejected applications</span><strong><%=stats.get("rejected")%></strong>
      <small>Customer applications declined</small>
      <a class="btn btn-light btn-small metric-view" href="<%=ctx%>/employee/customers?status=REJECTED">View</a>
    </article>
  </div>

  <section class="panel">
    <div class="panel-heading">
      <div><h2>Latest customer applications</h2><p>Pending applications are shown first for faster processing.</p></div>
      <a class="btn btn-light btn-small" href="<%=ctx%>/employee/customers">View customers</a>
    </div>
    <div class="table-wrap">
      <table>
        <thead><tr><th>Customer</th><th>Username</th><th>Contact</th><th>Status</th><th>Registered</th><th>Operation</th></tr></thead>
        <tbody>
        <% if(latest!=null && !latest.isEmpty()) { for(Map<String,Object> r:latest) { %>
          <tr>
            <td><b><%=r.get("full_name")%></b><br><small>ID <%=r.get("customer_id")%></small></td>
            <td><%=r.get("username")%></td>
            <td><%=r.get("email")%><br><small><%=r.get("mobile")%></small></td>
            <td><span class="pill pill-<%=String.valueOf(r.get("account_status")).toLowerCase()%>"><%=r.get("account_status")%></span></td>
            <td><%=r.get("created_at")%></td>
            <td><a class="btn btn-light btn-small" href="<%=ctx%>/employee/customers">View</a></td>
          </tr>
        <% } } else { %>
          <tr><td colspan="6" class="empty-cell">No customer applications found.</td></tr>
        <% } %>
        </tbody>
      </table>
    </div>
  </section>


</main>
<script src="<%=ctx%>/assets/js/app.js"></script>
</body>
</html>
