package com.example.wcljdbc;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class SqlConnectTool {
    private SqlConnectTool() {
    }

    public static void main(String[] args) {
        Map<String, String> options = parseArgs(args);
        if (options.containsKey("error")) {
            System.err.println(options.get("error"));
            printUsage();
            System.exit(1);
        }

        if (options.containsKey("help")) {
            printUsage();
            return;
        }

        String host = options.get("host");
        String username = options.get("username");
        String password = options.get("password");

        if (host == null || host.isEmpty()) {
            System.err.println("Missing required option: --host");
            printUsage();
            System.exit(1);
        }

        if (username == null || username.isEmpty()) {
            System.err.println("Missing required option: --username");
            printUsage();
            System.exit(1);
        }

        String portValue = options.getOrDefault("port", "1433");
        int port;
        try {
            port = Integer.parseInt(portValue);
        } catch (NumberFormatException ex) {
            System.err.println("Invalid port: " + portValue);
            System.exit(1);
            return;
        }

        String database = options.getOrDefault("database", "");
        String domain = options.get("domain");

        if (password == null) {
            password = promptForPassword();
        }

        if (password == null) {
            System.err.println("Password not provided.");
            System.exit(1);
        }

        String encrypt = options.getOrDefault("encrypt", "true");
        String trustCert = options.getOrDefault("trustServerCertificate", "false");
        String authScheme = options.get("authenticationScheme");
        if (authScheme == null && domain != null && !domain.isEmpty()) {
            authScheme = "NativeAuthentication";
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("jdbc:sqlserver://").append(host).append(":").append(port);
        if (!database.isEmpty()) {
            urlBuilder.append(";databaseName=").append(database);
        }
        urlBuilder.append(";encrypt=").append(encrypt);
        urlBuilder.append(";trustServerCertificate=").append(trustCert);
        if (authScheme != null && !authScheme.isEmpty()) {
            urlBuilder.append(";authenticationScheme=").append(authScheme);
        }
        if (domain != null && !domain.isEmpty()) {
            urlBuilder.append(";domain=").append(domain);
        }

        String url = urlBuilder.toString();

        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        if (domain != null && !domain.isEmpty()) {
            props.setProperty("domain", domain);
        }

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            System.err.println("SQL Server JDBC driver not found: " + ex.getMessage());
            System.exit(2);
        }

        System.out.println("Connecting to " + url + " as " + username + "...");
        try (Connection connection = DriverManager.getConnection(url, props)) {
            System.out.println("Connection established successfully.");
        } catch (SQLException ex) {
            System.err.println("Failed to connect: " + ex.getMessage());
            System.exit(3);
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                options.put("error", "Unexpected argument: " + arg);
                break;
            }

            String key = arg.substring(2);
            if ("help".equals(key)) {
                options.put("help", "true");
                continue;
            }

            if (i + 1 >= args.length) {
                options.put("error", "Missing value for option: " + arg);
                break;
            }

            String value = args[++i];
            options.put(key, value);
        }
        return options;
    }

    private static void printUsage() {
    System.out.println("Usage: java -jar wcljdbc.jar --host <hostname> [--port <port>] "
        + "[--database <db>] [--domain <domain>] --username <username> [--password <password>] "
        + "[--encrypt <true|false>] [--trustServerCertificate <true|false>] "
        + "[--authenticationScheme <scheme>]");
        System.out.println();
        System.out.println("Examples:");
    System.out.println("  java -jar wcljdbc.jar --host sqlserver.example.com --port 1433 --database Sales "
        + "--domain CONTOSO --username alice --password S3cret");
    System.out.println("  java -jar wcljdbc.jar --host 10.0.0.4 --domain CONTOSO --username bob");
        System.out.println();
        System.out.println("If --password is omitted, the tool prompts securely when possible.");
    }

    private static String promptForPassword() {
        Console console = System.console();
        if (console == null) {
            return null;
        }
        char[] passwordChars = console.readPassword("Enter password: ");
        if (passwordChars == null) {
            return null;
        }
        String password = new String(passwordChars);
        Arrays.fill(passwordChars, '\0');
        return password;
    }
}
