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
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("junit:junit:4.13.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}