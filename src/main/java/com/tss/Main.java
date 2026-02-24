package com.tss;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import com.tss.db.tables.Person;

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

            ctx.selectFrom(Person.PERSON)
               .fetch()
               .forEach(r -> System.out.println(r.getName() + " is " + r.getAge()));
        }
    }
}
