Please write a command line tool. It can use Windows domain username and password to connect MS SQL Server.

- use java, support version 8+
- use MS SQL Server JDBC driver
- On the command line we  can specify the parameters like MS SQL server address, host port, and username and password.

# Note

The file `mssql-jdbc_auth-12.6.3.x64.dll` may need to be specified by `-Djava.library.path="C:\path\to\sqljdbc_enu\auth\x64"`