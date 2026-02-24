buildscript{
    repositories{
        mavenCentral()
    }
    dependencies{
        //KEEP THESE IN SYNC WITH VERSION IN libs.versions.toml FILE
        val flyway = "12.0.2"
        val postgres_driver = "42.7.10"

        classpath("org.flywaydb:flyway-core:$flyway")
        classpath("org.flywaydb:flyway-database-postgresql:$flyway")
        classpath("org.postgresql:postgresql:$postgres_driver")
    }
}

plugins{
    java
    alias(libs.plugins.shadow)
    alias(libs.plugins.jooq.codegen)
}

dependencies{
    implementation(libs.postgres.driver)
    implementation(libs.jooq)
    implementation(libs.jooq.postgres.extensions)

    jooqCodegen(libs.postgres.driver)
    jooqCodegen(libs.jooq.meta)
    jooqCodegen(libs.jooq.codegen)
    jooqCodegen(libs.jooq.postgres.extensions)

    compileOnly(libs.jakarta.xml.bind)
    testCompileOnly(libs.jakarta.xml.bind)
}

testing{
    suites{
        val test by getting(JvmTestSuite::class){
            useJUnitJupiter("5.12.1")
        }
    }
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

tasks.register("flywayMigrate"){
    doLast{
        org.flywaydb.core.Flyway.configure()
            .dataSource(
                System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/devdb",
                System.getenv("DB_USER") ?: "dev",
                System.getenv("DB_PASSWORD") ?: "devpass"
            )
            .locations("filesystem:src/main/resources/db/migration")
            .load()
            .migrate()
    }
}

tasks.named("jooqCodegen"){
    dependsOn("flywayMigrate")
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
