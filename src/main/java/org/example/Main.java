package org.example;

import java.sql.*; // Import all SQL utilities
import java.util.ArrayList; // To store the list of tables
import java.util.InputMismatchException; // To handle bad number input
import java.util.List; // Interface for ArrayList
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // 1. Use this Scanner for all user inputs
        Scanner input = new Scanner(System.in);

        System.out.println("Welcome to Oracle Metadata Explorer!");

        // 2. Prompt for username in console
        System.out.print("Enter username: ");
        String username = input.nextLine();

        // 3. Prompt for password in console
        System.out.print("Enter password: ");
        String password = input.nextLine();

        String dbUrl = "jdbc:oracle:thin:@localhost:8521/freepdb1";
        System.out.println("Connecting to database as " + username + "...");

        // 4. Attempt connection (try-with-resources auto-closes the connection)
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {

            System.out.println("-----------------------------------");
            System.out.println("SUCCESS! Connected to the database.");
            System.out.println("-----------------------------------");

            // 5. Create the main menu loop
            boolean running = true;
            while (running) {
                // Display menu options
                System.out.println("\n--- Main Menu ---");
                System.out.println("1. View Tables");
                System.out.println("2. View Views");
                System.out.println("3. View Sequences");
                System.out.println("4. View Users"); // As per your sheet requirements
                System.out.println("5. Exit");
                System.out.print("Select an option (1-5): ");

                String choice = input.nextLine(); // Read user's choice

                // 6. Handle user's choice
                switch (choice) {
                    case "1":
                        // (Important change: We now pass the Scanner to the method)
                        listTables(connection, input);
                        break;
                    case "2":
                        listViews(connection);
                        break;
                    case "3":
                        listSequences(connection);
                        break;
                    case "4":
                        listUsers(connection);
                        break;
                    case "5":
                        running = false; // Break the loop
                        System.out.println("Exiting... Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please enter a number between 1 and 5.");
                        break;
                }
            }

        } catch (SQLException e) {
            System.err.println("Connection Failed! Check your username/password.");
            System.err.println("Error: " + e.getMessage());
        }

        // 7. Close the input scanner at the very end
        input.close();
    }

    // --- 1. Method to list tables (and ask for details) ---
    private static void listTables(Connection connection, Scanner scanner) {
        String sql = "SELECT table_name FROM USER_TABLES ORDER BY table_name";
        // Use a List to store table names to select them by number
        List<String> tables = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            System.out.println("\n--- Available Tables ---");
            int count = 1;
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                System.out.println(count + ". " + tableName);
                tables.add(tableName); // Add table name to the list
                count++;
            }
            System.out.println("------------------------");

            // If no tables, return to main menu
            if (tables.isEmpty()) {
                System.out.println("No tables found for this user.");
                return; // Return to main menu
            }

            // (New) Ask the user if they want to inspect a table or go back
            System.out.print("Enter a table number to inspect (or 0 to go back): ");
            try {
                int tableChoice = scanner.nextInt(); // Read the number
                scanner.nextLine(); // (Important: Consume the trailing newline character)

                if (tableChoice == 0) {
                    return; // Return to main menu
                }

                if (tableChoice > 0 && tableChoice <= tables.size()) {
                    // User selected a valid table, get its name from the list
                    String selectedTable = tables.get(tableChoice - 1); // (Subtract 1 because the list is 0-indexed)
                    inspectTable(connection, scanner, selectedTable); // Call the sub-menu method
                } else {
                    System.out.println("Invalid table number.");
                }

            } catch (InputMismatchException e) { // If the user entered "abc" instead of a number
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // (Important: Clear the invalid input)
            }

        } catch (SQLException e) {
            System.err.println("Error fetching tables: " + e.getMessage());
        }
    }

    // --- 2. Method to list Views ---
    private static void listViews(Connection connection) {
        String sql = "SELECT view_name FROM USER_VIEWS ORDER BY view_name";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            System.out.println("\n--- Available Views ---");
            int count = 1;
            while (rs.next()) {
                String viewName = rs.getString("VIEW_NAME");
                System.out.println(count + ". " + viewName);
                count++;
            }
            if (count == 1) {
                System.out.println("No views found for this user.");
            }
            System.out.println("-----------------------");

        } catch (SQLException e) {
            System.err.println("Error fetching views: " + e.getMessage());
        }
    }

    // --- 3. Method to list Sequences ---
    private static void listSequences(Connection connection) {
        String sql = "SELECT sequence_name FROM USER_SEQUENCES ORDER BY sequence_name";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            System.out.println("\n--- Available Sequences ---");
            int count = 1;
            while (rs.next()) {
                String seqName = rs.getString("SEQUENCE_NAME");
                System.out.println(count + ". " + seqName);
                count++;
            }
            if (count == 1) {
                System.out.println("No sequences found for this user.");
            }
            System.out.println("-----------------------------");

        } catch (SQLException e) {
            System.err.println("Error fetching sequences: " + e.getMessage());
        }
    }

    // --- 4. Method to list Users ---
    // (Note: This will return a long list of all database users)
    private static void listUsers(Connection connection) {
        String sql = "SELECT username FROM ALL_USERS ORDER BY username";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            System.out.println("\n--- Database Users (All) ---");
            int count = 1;
            while (rs.next()) {
                String userName = rs.getString("USERNAME");
                System.out.println(count + ". " + userName);
                count++;
            }
            System.out.println("------------------------------");

        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
    }

    // ---------------------------------------------------
    // (New: Sub-menu methods - Step 5 from the sheet)
    // ---------------------------------------------------

    /**
     * Sub-menu method (for Step 5 in the sheet).
     * This menu runs when a user selects a specific table.
     */
    private static void inspectTable(Connection connection, Scanner scanner, String tableName) {
        boolean inspecting = true;
        while (inspecting) {
            System.out.println("\n--- Inspecting Table: " + tableName + " ---");
            System.out.println("1. View Columns");
            System.out.println("2. View Constraints");
            System.out.println("3. View Indexes");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select an option (1-4): ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    listTableColumns(connection, tableName);
                    break;
                case "2":
                    listTableConstraints(connection, tableName);
                    break;
                case "3":
                    listTableIndexes(connection, tableName);
                    break;
                case "4":
                    inspecting = false; // Break the sub-menu loop
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 4.");
                    break;
            }
        }
    }

    /**
     * Method to list table columns.
     */
    private static void listTableColumns(Connection connection, String tableName) {
        // (Use PreparedStatement here to protect against SQL Injection)
        String sql = "SELECT column_name, data_type, data_length " +
                "FROM USER_TAB_COLUMNS " +
                "WHERE table_name = ? " +
                "ORDER BY column_id";

        // (Use try-with-resources so the PreparedStatement is auto-closed)
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, tableName); // Set the table name in the first question mark

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- Columns for: " + tableName + " ---");
                // (Print the table header)
                System.out.printf("%-30s | %-20s | %s%n", "COLUMN_NAME", "DATA_TYPE", "LENGTH");
                System.out.println(new String(new char[62]).replace("\0", "-")); // Separator line

                while (rs.next()) {
                    System.out.printf("%-30s | %-20s | %s%n",
                            rs.getString("COLUMN_NAME"),
                            rs.getString("DATA_TYPE"),
                            rs.getString("DATA_LENGTH")
                    );
                }
                System.out.println("--------------------------");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching columns: " + e.getMessage());
        }
    }

    /**
     * Method to list table constraints.
     */
    private static void listTableConstraints(Connection connection, String tableName) {
        String sql = "SELECT constraint_name, constraint_type " +
                "FROM USER_CONSTRAINTS " +
                "WHERE table_name = ? " +
                "ORDER BY constraint_name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tableName);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- Constraints for: " + tableName + " ---");
                System.out.printf("%-30s | %s%n", "CONSTRAINT_NAME", "TYPE (C=Check, P=Primary, R=Foreign)");
                System.out.println(new String(new char[50]).replace("\0", "-"));

                while (rs.next()) {
                    System.out.printf("%-30s | %s%n",
                            rs.getString("CONSTRAINT_NAME"),
                            rs.getString("CONSTRAINT_TYPE")
                    );
                }
                System.out.println("-------------------------------");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching constraints: " + e.getMessage());
        }
    }

    /**
     * Method to list table indexes.
     */
    private static void listTableIndexes(Connection connection, String tableName) {
        String sql = "SELECT index_name, index_type " +
                "FROM USER_INDEXES " +
                "WHERE table_name = ? " +
                "ORDER BY index_name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tableName);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- Indexes for: " + tableName + " ---");
                System.out.printf("%-30s | %s%n", "INDEX_NAME", "TYPE");
                System.out.println(new String(new char[40]).replace("\0", "-"));

                while (rs.next()) {
                    System.out.printf("%-30s | %s%n",
                            rs.getString("INDEX_NAME"),
                            rs.getString("INDEX_TYPE")
                    );
                }
                System.out.println("---------------------------");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching indexes: " + e.getMessage());
        }
    }
}