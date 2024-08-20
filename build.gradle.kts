// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val androidVer = "8.5.2"
    id("com.android.application") version androidVer apply false
    id("com.android.library") version androidVer apply false

    val kotlinVer = "2.0.10"
    id("org.jetbrains.kotlin.android") version kotlinVer apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version kotlinVer apply false
    id("org.jetbrains.kotlin.kapt") version kotlinVer apply false
    id("org.jetbrains.kotlin.plugin.lombok") version kotlinVer apply false

    id("com.google.protobuf") version "0.9.4" apply false
}
