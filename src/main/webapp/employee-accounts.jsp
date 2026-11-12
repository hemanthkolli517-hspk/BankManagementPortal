<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,in.bankportal.util.Web" %>
<%
List<Map<String,Object>> rows=(List<Map<String,Object>>)request.getAttribute("accounts");
List<Map<String,Object>> customers=(List<Map<String,Object>>)request.getAttribute("approvedCustomers");
String token=Web.csrf(session);
Object flash=session.getAttribute("flashMessage");
Object kind=session.getAttribute("flashKind");
session.removeAttribute("flashMessage"); session.removeAttribute("flashKind");
String ctx=request.getContextPath();
String accountStatusFilter=(String)request.getAttribute("accountStatusFilter");
int eligible=customers==null?0:customers.size();
%>
<!doctype html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Account management · Bankwise</title><link rel="stylesheet" href="<%=ctx%>/assets/css/style.css"></head>
<body>
<header class="appbar">
  <a class="brand" href="<%=ctx%>/employee/dashboard"><span class="brand-mark">B</span> Bank<span>wise</span></a>
  <nav><a href="<%=ctx%>/employee/dashboard">Dashboard</a><a href="<%=ctx%>/employee/customers">Customers</a><a class="active" href="<%=ctx%>/employee/accounts">Accounts</a><a href="<%=ctx%>/transactions">Transactions</a><a href="<%=ctx%>/profile">My profile</a><a href="<%=ctx%>/logout">Sign out</a></nav>
</header>
<main class="page-shell">
  <div class="welcome-row">
    <div><span class="eyebrow">ACCOUNT OPERATIONS</span><h1>Account management</h1><p>Create an account after a customer application has been approved, then maintain account status.</p></div>
    <a class="btn btn-light" href="<%=ctx%>/employee/dashboard">Back to dashboard</a>
  </div>
  <% if(flash!=null){ %><div class="alert alert-<%=kind%>"><%=flash%></div><% } %>

  <section class="panel" id="create-account">
    <div class="panel-heading">
      <div><h2>Create account for an approved customer</h2><p>Only approved customers without an existing account are listed here.</p></div>
      <div class="metric-inline"><span>Eligible customers</span><strong><%=eligible%></strong></div>
    </div>
    <% if(eligible>0) { %>
    <form method="post" action="<%=ctx%>/employee/account-action" class="form-grid compact-grid" data-validate>
      <input type="hidden" name="csrfToken" value="<%=token%>"><input type="hidden" name="action" value="CREATE">
      <label>Approved customer *
        <select name="customerId" required>
          <option value="">Select approved customer</option>
          <% for(Map<String,Object> c:customers){ %><option value="<%=c.get("id")%>"><%=c.get("name")%> · Customer ID <%=c.get("id")%></option><% } %>
        </select>
      </label>
      <label>Account type *
        <select name="accountType" required><option value="SAVINGS">Savings</option><option value="CURRENT">Current</option></select>
      </label>
      <label>Opening balance (₹) *
        <input type="number" name="openingBalance" min="0" max="100000000" step="0.01" value="0.00" required>
        <small>Use 0.00 if no opening deposit is being made.</small>
      </label>
      <div class="form-actions align-end"><button class="btn btn-primary" type="submit">Create account</button><button class="btn btn-light" type="reset">Reset</button></div>
    </form>
    <% } else { %>
    <div class="empty-state">
      <div class="empty-state-icon">✓</div><h3>No customers waiting for account creation</h3>
      <p>Every approved customer currently has an account, or there are no approved customers yet.</p>
      <a class="btn btn-light btn-small" href="<%=ctx%>/employee/customers">View customer applications</a>
    </div>
    <% } %>
  </section>

  <section class="panel">
    <div class="panel-heading">
      <div><h2><%="ACTIVE".equals(accountStatusFilter)?"Active accounts":("INACTIVE".equals(accountStatusFilter)?"Inactive accounts":"All accounts")%></h2><p>Update account type or status. Deactivation retains transaction history.</p></div>
      <div class="account-list-tools">
        <span class="subtle-note"><%=rows==null?0:rows.size()%> accounts shown</span>
        <form method="get" action="<%=ctx%>/employee/accounts" class="inline-filter">
          <label class="sr-only" for="account-status-filter">Filter accounts by status</label>
          <select id="account-status-filter" name="status" onchange="this.form.submit()">
            <option value="ALL" <%= "ALL".equals(accountStatusFilter)?"selected":""%>>All statuses</option>
            <option value="ACTIVE" <%= "ACTIVE".equals(accountStatusFilter)?"selected":""%>>Active only</option>
            <option value="INACTIVE" <%= "INACTIVE".equals(accountStatusFilter)?"selected":""%>>Inactive only</option>
          </select>
        </form>
      </div>
    </div>
    <div class="table-wrap">
      <table><thead><tr><th>Account</th><th>Customer</th><th>Balance</th><th>Status</th><th>Operations</th></tr></thead><tbody>
      <% if(rows!=null && !rows.isEmpty()) for(Map<String,Object> r:rows){ %>
        <tr>
          <td><b class="mono"><%=r.get("account_number")%></b><br><small><%=r.get("account_type")%></small></td>
          <td><%=r.get("full_name")%><br><small>Customer ID <%=r.get("customer_id")%></small></td>
          <td class="money">₹<%=r.get("balance")%></td>
          <td><span class="pill <%=String.valueOf(r.get("status")).equals("ACTIVE")?"pill-green":"pill-gray"%>"><%=r.get("status")%></span></td>
          <td><details class="row-details"><summary>View / edit</summary><div class="details-panel">
            <form method="post" action="<%=ctx%>/employee/account-action" class="stack-form">
              <input type="hidden" name="csrfToken" value="<%=token%>"><input type="hidden" name="action" value="UPDATE"><input type="hidden" name="accountId" value="<%=r.get("account_id")%>">
              <label>Account type<select name="accountType"><option value="SAVINGS" <%= "SAVINGS".equals(r.get("account_type"))?"selected":""%>>Savings</option><option value="CURRENT" <%= "CURRENT".equals(r.get("account_type"))?"selected":""%>>Current</option></select></label>
              <label>Status<select name="status"><option value="ACTIVE" <%= "ACTIVE".equals(r.get("status"))?"selected":""%>>Active</option><option value="INACTIVE" <%= "INACTIVE".equals(r.get("status"))?"selected":""%>>Inactive</option></select></label>
              <button class="btn btn-primary btn-small" type="submit">Save changes</button>
            </form>
            <form method="post" action="<%=ctx%>/employee/account-action" onsubmit="return confirm('Deactivate this account?');">
              <input type="hidden" name="csrfToken" value="<%=token%>"><input type="hidden" name="action" value="DELETE"><input type="hidden" name="accountId" value="<%=r.get("account_id")%>">
              <button class="btn btn-light btn-small" type="submit">Deactivate account</button>
            </form>
          </div></details></td>
        </tr>
      <% } else { %><tr><td colspan="5" class="empty-cell">No accounts have been created yet.</td></tr><% } %>
      </tbody></table>
    </div>
  </section>
</main>
<script src="<%=ctx%>/assets/js/app.js"></script>
</body></html>
