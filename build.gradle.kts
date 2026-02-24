plugins{
    java
    alias(libs.plugins.shadow)
    alias(libs.plugins.jooq.codegen)
}

dependencies{
    implementation(libs.postgres.driver)
    implementation(libs.jooq)

    jooqCodegen(libs.postgres.driver)
    jooqCodegen(libs.jooq.meta)
    jooqCodegen(libs.jooq.codegen)
}

jooq{
    configuration{
        jdbc{
            driver = "org.postgresql.Driver"
            url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/devdb"
            user = System.getenv("DB_USER") ?: "dev"
            password = System.getenv("DB_PASSWORD") ?: "devpass"
        }
        generator{
            database{
                name = "org.jooq.meta.postgres.PostgresDatabase"
                includes = ".*"
                excludes = ""
                inputSchema = "public"
            }
            target{
                packageName = "com.tss.db"
                directory = "src/main/generated"
            }
        }
    }
}

sourceSets{
    main{
        java{
            srcDir("src/main/generated")
        }
    }
}

tasks.named("compileJava"){
    dependsOn(tasks.named("jooqCodegen"))
}

java{
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.named<Jar>("jar"){
    manifest{
        attributes["Main-Class"] = "com.tss.Main"
    }
}
