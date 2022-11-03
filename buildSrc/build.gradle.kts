plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/net.dongliu/apk-parser
    implementation("net.dongliu:apk-parser:2.6.10")
}