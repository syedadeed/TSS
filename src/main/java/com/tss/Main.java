package com.tss;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {

        String url = "jdbc:postgresql://localhost:5432/devdb";
        String user = "dev";
        String password = "devpass";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            System.out.println("✅ Connected successfully!");

            if (rs.next()) {
                System.out.println("Test query result: " + rs.getInt(1));
            }

        } catch (Exception e) {
            System.err.println("❌ Connection failed");
            e.printStackTrace();
        }
    }
}
