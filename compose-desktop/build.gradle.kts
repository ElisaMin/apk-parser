import org.jetbrains.compose.compose
plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}


group = "me.heizi.apk.parser"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}
dependencies {
    implementation(project(":"))
    api(compose.desktop.currentOs)
}

