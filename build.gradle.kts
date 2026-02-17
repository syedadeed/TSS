plugins{
    java
    alias(libs.plugins.shadow)
}

dependencies{

}

java{
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.named<Jar>("jar"){
    manifest{
        attributes["Main-Class"] = "com.tss.Main"
    }
}
