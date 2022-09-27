plugins {
    kotlin("jvm") version "1.7.10"
}

group = "net.dongliu.apk.parser"
version = "1.0-SNAPSHOT"



repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk18on
    implementation("org.bouncycastle:bcprov-jdk18on:1.71.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.71.1")
    testImplementation(kotlin("test"))
}