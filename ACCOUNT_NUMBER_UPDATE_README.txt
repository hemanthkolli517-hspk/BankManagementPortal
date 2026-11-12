BANKWISE ACCOUNT NUMBER UPDATE

Files changed:
- database/schema.sql: adds nullable unique customers.account_number.
- src/main/webapp/register.jsp: adds optional account-number field.
- src/main/java/in/bankportal/servlet/CustomerRegisterServlet.java: validates and saves the optional field.
- src/main/java/in/bankportal/servlet/EmployeeCustomersServlet.java: retrieves account_number for employees.
- src/main/webapp/employee-customers.jsp: displays the account number.
- src/main/java/in/bankportal/servlet/EmployeeAccountActionServlet.java: synchronizes the customer field with the account number when an account is created; if blank, the system generates one as before.
- database/migration_add_customer_account_number.sql: migration for an existing database.

For an existing database, run the migration SQL only once. If the schema.sql is used to create a fresh database, the column is already included, so do not run the migration too.

The registration account number is optional. If supplied, it is used when the employee creates the customer's account; otherwise, the application generates an account number. The customer must be approved before account creation, as in the original project.
