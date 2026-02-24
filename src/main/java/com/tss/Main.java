package com.tss;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import com.tss.db.tables.Projects;
import com.tss.db.tables.WeeklySchedules;
import com.tss.db.tables.ScheduleEntries;
import com.tss.db.tables.ScheduleAnalytics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.LogManager;

public class Main {
    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();

        String url  = System.getenv("DB_URL")      != null ? System.getenv("DB_URL")      : "jdbc:postgresql://localhost:5432/devdb";
        String user = System.getenv("DB_USER")     != null ? System.getenv("DB_USER")     : "dev";
        String pass = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "devpass";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            DSLContext ctx = DSL.using(conn, SQLDialect.POSTGRES);

            System.out.println("=== jOOQ + Schema Sanity Check ===\n");

            // 1. Projects
            int projectCount = ctx.fetchCount(Projects.PROJECTS);
            System.out.println("[projects]          row count: " + projectCount);

            // 2. Weekly Schedules
            int scheduleCount = ctx.fetchCount(WeeklySchedules.WEEKLY_SCHEDULES);
            System.out.println("[weekly_schedules]  row count: " + scheduleCount);

            // 3. Schedule Entries
            int entryCount = ctx.fetchCount(ScheduleEntries.SCHEDULE_ENTRIES);
            System.out.println("[schedule_entries]  row count: " + entryCount);

            // 4. Schedule Analytics
            int analyticsCount = ctx.fetchCount(ScheduleAnalytics.SCHEDULE_ANALYTICS);
            System.out.println("[schedule_analytics] row count: " + analyticsCount);

            System.out.println("\nAll tables reachable — jOOQ is wired correctly.");
        }
    }
}
