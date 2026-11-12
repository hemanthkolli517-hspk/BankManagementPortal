# Bank Management Portal — Fresh Eclipse Project

A new JSP + Servlet + HTML/CSS/JavaScript + MySQL educational bank-management website for **Eclipse IDE and Apache Tomcat 10.1**. This project was generated as a separate fresh project; it does not reuse the prior project source files.

## Requirements
- JDK 17
- Apache Tomcat 10.1.x
- MySQL 8.x
- Eclipse IDE for Enterprise Java and Web Developers with Maven integration
- Internet access on the first Maven build to download dependencies

## Included features
- Customer registration with profile fields, identity document type and file picker; new applications begin as `PENDING`.
- Login/logout, BCrypt password hashing, role checks, session tracking in `user_sessions`, 15-minute idle session timeout.
- Customer dashboard: account status, balance, deposit, withdrawal and transaction history.
- Employee registration with email OTP field in the middle of the form. OTP is six digits, expires in 10 minutes, and has a five-attempt limit.
- Employee dashboard: live operation counts for pending applications, approved customers, all customers, approved customers awaiting account creation, total accounts, active accounts, transactions and rejected applications. Each count has one View button to open the related operation, with consistent card heights, restrained status colors and responsive spacing.
- Account creation is available only for approved customers who do not yet have an account; after account creation, that customer is removed from the eligible creation list.
- Customer approval/rejection, profile/contact update and login/account deactivation.
- Account creation for approved customers, account type/status updates and safe deactivation (preserves transaction history).
- Transaction ledger, form validation, responsive light UI, aligned two-column forms on desktop and single-column forms on mobile, consistent field sizing, accessible focus states, reset/submit controls, radio/checkbox/select/textarea/file inputs.
- SQL uses prepared statements. Database schema is included as `database/schema.sql` and is kept as supplied.

## Database setup
1. Open MySQL Workbench.
2. Run `database/schema.sql` exactly as included.
3. Default local DB settings are `root` / `manager` at `localhost:3306`.
4. If needed, override using environment variables:
   - `BANK_DB_URL` (default `jdbc:mysql://localhost:3306/bank_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`)
   - `BANK_DB_USER` (default `root`)
   - `BANK_DB_PASSWORD` (default `manager`)

The schema has no administrator account or account status for employees beyond the `employees.status` field. Employee registration is enabled as requested and creates an active employee after successful OTP verification. For any real deployment, replace open employee registration with administrator invitation/approval.

## SMTP OTP setup (employee registration only)
Gmail requires a Google App Password, not your normal Gmail password.
1. Enable 2-Step Verification for the sender Gmail account.
2. Create an App Password in Google Account security settings.
3. Set the environment variable `BANK_SMTP_APP_PASSWORD` to that App Password before starting Eclipse/Tomcat.
4. Optional override: `BANK_SMTP_EMAIL` (default `ganeshbajaru41@gmail.com`).
5. Restart Eclipse/Tomcat after setting environment variables.
6. Open Employee registration, enter the email, click **Send OTP**, check that inbox, enter the six-digit code in the OTP field, then submit.

Windows PowerShell example for the current terminal:
```powershell
$env:BANK_SMTP_EMAIL="ganeshbajaru41@gmail.com"
$env:BANK_SMTP_APP_PASSWORD="YOUR_GMAIL_APP_PASSWORD"
```
Then launch Eclipse from that same environment, or set these variables in the Tomcat server launch configuration. Never commit your app password to source control or put it in JSP/Java source code.

## Import and run in Eclipse
1. Extract this ZIP to a writable folder.
2. Eclipse → **File → Import → Maven → Existing Maven Projects**.
3. Select the extracted `BankManagementPortal` folder; select `pom.xml`; Finish.
4. Right-click project → **Maven → Update Project**.
5. In Project Properties → Java Compiler, select Java 17. Add Apache Tomcat 10.1 in the Servers view.
6. Run the project using **Run As → Run on Server**.
7. Open `http://localhost:8080/BankManagementPortal/`.

If Eclipse does not map the project to the server, right-click project → Properties → Project Facets and set Dynamic Web Module to 6.0 and Java to 17, then check Deployment Assembly includes `src/main/webapp` and Maven Dependencies.

## Default URL paths
- `/` — Home page
- `/login.jsp` — Login
- `/register.jsp` — Customer registration
- `/employee-register.jsp` — Employee registration and OTP
- `/customer/dashboard` — Customer dashboard
- `/employee/dashboard` — Employee dashboard
- `/employee/customers` — Customer operations
- `/employee/accounts` — Account operations
- `/transactions` — Transaction history
- `/profile` — Profile and password update
- `/logout` — End session

## Important safety notes
This is an educational demo ledger, not a real banking system. Do not use real customer identity documents, real account credentials, or real funds. The uploaded identity file is saved in a private local folder configured by `BANK_UPLOAD_DIR` (default is a folder under the current user's home directory); only its generated filename is stored in the database. The demo needs HTTPS, rate limiting, stricter employee provisioning, independent security review and operational controls before any real-world use. No production banking deployment should rely on a classroom sample.


## Admin approval, audit history and login OTP

### Admin credentials
- Username: `Admin`
- Password: `Admin@123`
- Admin login OTP is sent to `ganeshbajaru41@gmail.com`.

The Admin user record is created automatically on the first successful Admin sign-in. The fixed credentials are intended for this educational/demo project; change them before any real deployment.

### Database
For a **new database**, run `database/schema.sql`.

For an **existing database**, back it up first, then run `database/migration_admin_otp_audit.sql` exactly once. This adds employee application status and the activity log. Existing employees are marked approved by the migration so their current access is preserved. New employee registrations are marked pending until Admin approval.

### Gmail OTP setup
Configure these environment variables for the process running Tomcat:
- `BANK_SMTP_EMAIL` — Gmail address used to send messages (defaults to `ganeshbajaru41@gmail.com`)
- `BANK_SMTP_APP_PASSWORD` — Gmail App Password, not the regular Google password

Restart Eclipse/Tomcat after changing environment variables. Login OTPs are sent to the email address stored on each user's account. Employee registration OTP is still required as before; a separate login OTP is now required for every role.

### Added features
- Admin approves/rejects employee registration requests.
- Admin activates/deactivates approved employee login access.
- Employees can activate/deactivate customer login access. The original customer deactivation action is retained.
- Employee/Admin requests are recorded in `audit_log`, and Admin can review the latest 200 records in the Admin dashboard.
- Admin dashboard is available at `/admin/dashboard` after OTP-verified login.
- Login is completed only after OTP verification. OTP expires after 10 minutes and has a limited number of attempts.

### Eclipse / Tomcat
Import as an existing Maven project, run Maven > Update Project, and deploy to Tomcat 10.1 with Java 17. Check the Eclipse Console if SMTP configuration or database migration is missing.


## Admin dashboard updates
- Dashboard metrics now show total employees, total customers, transaction count, total deposits and total withdrawals.
- Admin can review pending customer applications in a detail pop-up and approve/reject them.
- Admin can edit customer contact/personal details and activate/deactivate customer login.
- Customer profile fields are read-only; registered customer details can only be changed by the administrator.
- Activity-log table has been removed from the admin dashboard. Existing tables and CSS design system are retained.
