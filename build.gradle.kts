
import  java.util.Properties

apply("gradle/genLocal.gradle.kts")

plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "net.dongliu.apk.parser"

allprojects {
    version = rootProject.extra["apk-parser.version"] as String
    apply( plugin = "maven-publish")
    apply( plugin = "org.jetbrains.kotlin.jvm")
    kotlin {
        jvmToolchain(19)
    }
    publishing {
        val anotherLocal = Properties().apply {
            rootProject.file("local.properties").inputStream().use(::load)
        }["maven_repo_dir"] as String
        repositories {
            maven {
                url = uri(anotherLocal)
            }
        }
        publications {
            create("toLocal", MavenPublication::class.java){
                components.forEach(::println)
                from(components["kotlin"])
            }
        }
    }
}
repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk18on
    implementation("org.bouncycastle:bcprov-jdk18on:1.76")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.76")
    testImplementation(kotlin("test"))
}