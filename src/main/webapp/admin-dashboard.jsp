<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,in.bankportal.util.Web" %>
<%
List<Map<String,Object>> pendingEmployees=(List<Map<String,Object>>)request.getAttribute("pendingEmployees");
List<Map<String,Object>> employees=(List<Map<String,Object>>)request.getAttribute("employees");
List<Map<String,Object>> pendingCustomers=(List<Map<String,Object>>)request.getAttribute("pendingCustomers");
List<Map<String,Object>> customers=(List<Map<String,Object>>)request.getAttribute("customers");
Map<String,Object> stats=(Map<String,Object>)request.getAttribute("stats");
String token=Web.csrf(session); Object flash=session.getAttribute("flashMessage"); Object kind=session.getAttribute("flashKind");
session.removeAttribute("flashMessage"); session.removeAttribute("flashKind"); String ctx=request.getContextPath();
%>
<!doctype html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Admin dashboard · Bankwise</title><link rel="stylesheet" href="<%=ctx%>/assets/css/style.css">
<style>
.admin-actions{display:flex;flex-wrap:wrap;gap:6px}.admin-actions form{display:inline-flex;margin:0}.admin-section{margin-top:24px}.status-text{font-weight:600}
.customer-modal{display:none;position:fixed;z-index:1000;inset:0;background:rgba(20,35,55,.55);padding:24px;align-items:center;justify-content:center}
.customer-modal.open{display:flex}.customer-modal-card{background:#fff;border-radius:16px;padding:25px;width:min(680px,100%);max-height:90vh;overflow:auto;box-shadow:0 20px 60px rgba(0,0,0,.2)}
.customer-modal-head{display:flex;align-items:center;justify-content:space-between;gap:16px;margin-bottom:18px}.customer-modal-head h2{margin:0}.modal-close{border:0;background:#edf1f7;border-radius:8px;padding:8px 12px;cursor:pointer;font-size:18px}
.customer-detail-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:13px;margin-bottom:20px}.customer-detail{padding:12px;border:1px solid #e4eaf2;border-radius:10px;overflow-wrap:anywhere}.customer-detail small{display:block;color:#8290a2;margin-bottom:5px}.customer-detail strong{font-size:13px;color:#263d58}.modal-actions{display:flex;flex-wrap:wrap;gap:8px;margin-top:16px}.modal-edit-form{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}.modal-edit-form label{font-size:12px;color:#53657b}.modal-edit-form input,.modal-edit-form textarea{display:block;width:100%;box-sizing:border-box;margin-top:5px;border:1px solid #dce4ee;border-radius:9px;padding:10px;color:#24374d}.modal-edit-form .full{grid-column:1/-1}
@media(max-width:600px){.customer-detail-grid,.modal-edit-form{grid-template-columns:1fr}.modal-edit-form .full{grid-column:1}.customer-modal{padding:12px}.customer-modal-card{padding:18px}}
</style></head><body>
<header class="appbar"><a class="brand" href="<%=ctx%>/admin/dashboard"><span class="brand-mark">B</span> Bank<span>wise</span></a><nav><a class="active" href="<%=ctx%>/admin/dashboard">Admin dashboard</a><span class="user-chip"><%=session.getAttribute("username")%></span><a href="<%=ctx%>/logout">Sign out</a></nav></header>
<main class="page-shell"><div class="welcome-row"><div><span class="eyebrow">ADMINISTRATOR WORKSPACE</span><h1>Admin dashboard</h1><p>Manage applications, customer records and banking performance.</p></div></div>
<% if(flash!=null){ %><div class="alert alert-<%=kind%>"><%=flash%></div><% } %>
<div class="metric-grid employee-metric-grid">
 <article class="metric-card metric-accent"><span>Total employees</span><strong><%=stats.get("employeeCount")%></strong><small>Registered employee records</small></article>
 <article class="metric-card metric-blue"><span>Total customers</span><strong><%=stats.get("customerCount")%></strong><small>Registered customer records</small></article>
 <article class="metric-card metric-green"><span>Transaction records</span><strong><%=stats.get("transactionCount")%></strong><small>All recorded transactions</small></article>
 <article class="metric-card"><span>Total deposits</span><strong>₹<%=String.format(java.util.Locale.US,"%,.2f",((java.math.BigDecimal)stats.get("totalDeposits")).doubleValue())%></strong><small>Amount deposited</small></article>
 <article class="metric-card metric-red"><span>Total withdrawals</span><strong>₹<%=String.format(java.util.Locale.US,"%,.2f",((java.math.BigDecimal)stats.get("totalWithdrawals")).doubleValue())%></strong><small>Amount withdrawn</small></article>
 <article class="metric-card metric-accent"><span>Pending applications</span><strong><%=stats.get("pendingCustomers")%></strong><small>Customer requests awaiting review</small></article>
</div>

<section class="panel admin-section"><div class="panel-heading"><div><h2>Pending customer requests</h2><p>View the application details in a pop-up before approving or rejecting.</p></div></div><div class="table-wrap"><table><thead><tr><th>Customer</th><th>Contact</th><th>Application date</th><th>Status</th><th>Details</th></tr></thead><tbody>
<% if(pendingCustomers!=null&&!pendingCustomers.isEmpty()) for(Map<String,Object> r:pendingCustomers){ %>
<tr><td><b><%=r.get("full_name")%></b><br><small>Customer ID: <%=r.get("customer_id")%></small></td><td><%=r.get("email")%><br><small><%=r.get("mobile")%></small></td><td><%=r.get("created_at")%></td><td><span class="pill pill-pending">PENDING</span></td><td><button type="button" class="btn btn-primary btn-small" onclick="viewCustomer(this)"
 data-id="<%=r.get("customer_id")%>" data-name="<%=r.get("full_name")%>" data-username="<%=r.get("username")%>" data-email="<%=r.get("email")%>" data-mobile="<%=r.get("mobile")%>" data-gender="<%=r.get("gender")%>" data-dob="<%=r.get("dob")==null?"":r.get("dob")%>" data-address="<%=r.get("address")%>" data-identity="<%=r.get("identity_type")%>" data-file="<%=r.get("identity_file_name")==null?"Not uploaded":r.get("identity_file_name")%>" data-account="<%=r.get("account_number")==null?"Not assigned":r.get("account_number")%>" data-status="<%=r.get("account_status")%>">View details</button></td></tr>
<% } else { %><tr><td colspan="5" class="empty-cell">No pending customer requests.</td></tr><% } %>
</tbody></table></div></section>

<section class="panel admin-section"><div class="panel-heading"><div><h2>Customer management</h2><p>Edit registered customer details or enable and disable login access.</p></div></div><div class="table-wrap"><table><thead><tr><th>Customer</th><th>Contact</th><th>Account number</th><th>Application status</th><th>Login access</th><th>Actions</th></tr></thead><tbody>
<% if(customers!=null&&!customers.isEmpty()) for(Map<String,Object> r:customers){ %>
<tr><td><b><%=r.get("full_name")%></b><br><small>ID: <%=r.get("customer_id")%> · <%=r.get("username")%></small></td><td><%=r.get("email")%><br><small><%=r.get("mobile")%></small></td><td><%=r.get("account_number")==null?"Not assigned":r.get("account_number")%></td><td><span class="pill pill-<%=String.valueOf(r.get("account_status")).toLowerCase()%>"><%=r.get("account_status")%></span></td><td><b><%=r.get("login_status")%></b></td><td><div class="admin-actions"><button type="button" class="btn btn-light btn-small" onclick="editCustomer(this)"
 data-id="<%=r.get("customer_id")%>" data-name="<%=r.get("full_name")%>" data-email="<%=r.get("email")%>" data-mobile="<%=r.get("mobile")%>" data-address="<%=r.get("address")%>" data-gender="<%=r.get("gender")%>" data-dob="<%=r.get("dob")==null?"":r.get("dob")%>" data-identity="<%=r.get("identity_type")%>" data-account="<%=r.get("account_number")==null?"":r.get("account_number")%>">Edit</button>
<% if("ACTIVE".equals(r.get("login_status"))){ %><form method="post" action="<%=ctx%>/admin/customer-action"><input type="hidden" name="csrfToken" value="<%=token%>"><input type="hidden" name="customerId" value="<%=r.get("customer_id")%>"><button class="btn btn-danger btn-small" name="action" value="DEACTIVATE">Disable login</button></form><% } else { %><form method="post" action="<%=ctx%>/admin/customer-action"><input type="hidden" name="csrfToken" value="<%=token%>"><input type="hidden" name="customerId" value="<%=r.get("customer_id")%>"><button class="btn btn-success btn-small" name="action" value="ACTIVATE">Enable login</button></form><% } %></div></td></tr>
<% } else { %><tr><td colspan="6" class="empty-cell">No customer records found.</td></tr><% } %>
</tbody></table></div></section>

<section class="panel admin-section"><div class="panel-heading"><div><h2>Employee registration requests</h2><p>Approve or reject new employee registrations.</p></div></div><div class="table-wrap"><table><thead><tr><th>Employee</th><th>Contact</th><th>Employee details</th><th>Registered</th><th>Decision</th></tr></thead><tbody>
<% if(pendingEmployees!=null&&!pendingEmployees.isEmpty()) for(Map<String,Object> r:pendingEmployees){ %><tr><td><b><%=r.get("username")%></b><br><small>Code: <%=r.get("employee_code")%></small></td><td><%=r.get("email")%><br><small><%=r.get("mobile")%></small></td><td><%=r.get("designation")%><br><small><%=r.get("department")%></small></td><td><%=r.get("created_at")%></td><td><div class="admin-actions"><form method="post" action="<%=ctx%>/admin/employee-action"><input type="hidden" name="csrfToken" value="<%=token%>"><input type="hidden" name="employeeId" value="<%=r.get("employee_id")%>"><button class="btn btn-success btn-small" name="action" value="APPROVE">Approve</button></form><form method="post" action="<%=ctx%>/admin/employee-action"><input type="hidden" name="csrfToken" value="<%=token%>"><input type="hidden" name="employeeId" value="<%=r.get("employee_id")%>"><button class="btn btn-danger btn-small" name="action" value="REJECT">Reject</button></form></div></td></tr><% } else { %><tr><td colspan="5" class="empty-cell">No pending employee requests.</td></tr><% } %>
</tbody></table></div></section>

<section class="panel admin-section"><div class="panel-heading"><div><h2>Employee access management</h2><p>Activate or deactivate login permission for approved employees.</p></div></div><div class="table-wrap"><table><thead><tr><th>Employee</th><th>Contact</th><th>Department</th><th>Registration</th><th>Login access</th><th>Action</th></tr></thead><tbody>
<% if(employees!=null) for(Map<String,Object> r:employees){ %><tr><td><b><%=r.get("username")%></b><br><small><%=r.get("employee_code")%></small></td><td><%=r.get("email")%><br><small><%=r.get("mobile")%></small></td><td><%=r.get("department")%><br><small><%=r.get("designation")%></small></td><td><%=r.get("application_status")%></td><td><span class="status-text"><%=r.get("status")%></span></td><td><% if("APPROVED".equals(r.get("application_status"))){ %><form method="post" action="<%=ctx%>/admin/employee-action"><input type="hidden" name="csrfToken" value="<%=token%>"><input type="hidden" name="employeeId" value="<%=r.get("employee_id")%>"><% if("ACTIVE".equals(r.get("status"))){ %><button class="btn btn-danger btn-small" name="action" value="DEACTIVATE">Deactivate</button><% } else { %><button class="btn btn-success btn-small" name="action" value="ACTIVATE">Activate</button><% } %></form><% } else { %><small>No access until approved</small><% } %></td></tr><% } %>
</tbody></table></div></section>
</main>

<div class="customer-modal" id="customerViewModal" role="dialog" aria-modal="true" aria-labelledby="customerViewTitle"><div class="customer-modal-card"><div class="customer-modal-head"><h2 id="customerViewTitle">Customer application details</h2><button type="button" class="modal-close" onclick="closeModal('customerViewModal')">×</button></div><div class="customer-detail-grid" id="customerViewFields"></div><div class="modal-actions" id="customerViewActions"></div></div></div>
<div class="customer-modal" id="customerEditModal" role="dialog" aria-modal="true" aria-labelledby="customerEditTitle"><div class="customer-modal-card"><div class="customer-modal-head"><h2 id="customerEditTitle">Edit customer details</h2><button type="button" class="modal-close" onclick="closeModal('customerEditModal')">×</button></div><form class="modal-edit-form" method="post" action="<%=ctx%>/admin/customer-action" data-validate><input type="hidden" name="csrfToken" value="<%=token%>"><input type="hidden" name="customerId" id="editCustomerId"><input type="hidden" name="action" value="UPDATE"><label>Full name<input id="editCustomerName" name="fullName" required maxlength="100"></label><label>Email<input id="editCustomerEmail" name="email" type="email" required maxlength="100"></label><label>Mobile<input id="editCustomerMobile" name="mobile" required pattern="[0-9+ -]{8,15}" maxlength="15"></label><label>Gender<select id="editCustomerGender" name="gender" required><option>Female</option><option>Male</option><option>Other</option></select></label><label>Date of birth<input id="editCustomerDob" name="dob" type="date"></label><label>Identity type<select id="editCustomerIdentity" name="identityType" required><option>Aadhaar</option><option>Passport</option><option>Driving Licence</option><option>Voter ID</option><option>Other</option></select></label><label>Account number (optional)<input id="editCustomerAccount" name="accountNumber" maxlength="20" pattern="[A-Za-z0-9-]{1,20}"></label><label class="full">Address<textarea id="editCustomerAddress" name="address" required rows="3" maxlength="1000"></textarea></label><div class="modal-actions full"><button class="btn btn-primary" type="submit">Save changes</button><button class="btn btn-light" type="button" onclick="closeModal('customerEditModal')">Cancel</button></div></form></div></div>
<script>
function putField(container, label, value) { var box=document.createElement('div'); box.className='customer-detail'; var small=document.createElement('small'); small.textContent=label; var strong=document.createElement('strong'); strong.textContent=value || 'Not provided'; box.appendChild(small); box.appendChild(strong); container.appendChild(box); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }
function viewCustomer(button) {
 var fields=document.getElementById('customerViewFields'); var actions=document.getElementById('customerViewActions'); fields.innerHTML=''; actions.innerHTML='';
 var names=[['Customer ID','id'],['Full name','name'],['Username','username'],['Email','email'],['Mobile','mobile'],['Gender','gender'],['Date of birth','dob'],['Address','address'],['Identity type','identity'],['Identity document','file'],['Account number','account'],['Application status','status']];
 names.forEach(function(item){putField(fields,item[0],button.getAttribute('data-'+item[1]));});
 var id=button.getAttribute('data-id'); var status=button.getAttribute('data-status');
 if(status==='PENDING'){
  ['APPROVE','REJECT'].forEach(function(action){var form=document.createElement('form');form.method='post';form.action='<%=ctx%>/admin/customer-action';var csrf=document.createElement('input');csrf.type='hidden';csrf.name='csrfToken';csrf.value='<%=token%>';var cid=document.createElement('input');cid.type='hidden';cid.name='customerId';cid.value=id;var btn=document.createElement('button');btn.type='submit';btn.name='action';btn.value=action;btn.className='btn '+(action==='APPROVE'?'btn-success':'btn-danger');btn.textContent=action==='APPROVE'?'Approve request':'Reject request';form.appendChild(csrf);form.appendChild(cid);form.appendChild(btn);actions.appendChild(form);});
 }
 var close=document.createElement('button');close.type='button';close.className='btn btn-light';close.textContent='Close';close.onclick=function(){closeModal('customerViewModal');};actions.appendChild(close);
 document.getElementById('customerViewModal').classList.add('open');
}
function editCustomer(button) {
 document.getElementById('editCustomerId').value=button.getAttribute('data-id');
 document.getElementById('editCustomerName').value=button.getAttribute('data-name')||'';
 document.getElementById('editCustomerEmail').value=button.getAttribute('data-email')||'';
 document.getElementById('editCustomerMobile').value=button.getAttribute('data-mobile')||'';
 document.getElementById('editCustomerAddress').value=button.getAttribute('data-address')||'';
 document.getElementById('editCustomerGender').value=button.getAttribute('data-gender')||'Other';
 document.getElementById('editCustomerDob').value=button.getAttribute('data-dob')||'';
 document.getElementById('editCustomerIdentity').value=button.getAttribute('data-identity')||'Other';
 document.getElementById('editCustomerAccount').value=button.getAttribute('data-account')||'';
 document.getElementById('customerEditModal').classList.add('open');
}
document.querySelectorAll('.customer-modal').forEach(function(modal){modal.addEventListener('click',function(event){if(event.target===modal){modal.classList.remove('open');}});});
document.addEventListener('keydown',function(event){if(event.key==='Escape'){closeModal('customerViewModal');closeModal('customerEditModal');}});
</script><script src="<%=ctx%>/assets/js/app.js"></script></body></html>
