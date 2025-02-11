import org.jetbrains.compose.compose
plugins {
    `maven-publish`
    kotlin("jvm")
    id("org.jetbrains.compose")
}


group = "me.heizi.apk.parser"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}
dependencies {
    implementation(project(":"))
    api(compose.desktop.currentOs)
}

