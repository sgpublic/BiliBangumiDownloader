@file:Suppress("PropertyName")

import android.databinding.tool.util.StringUtils
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.kotlin.ir.backend.js.toByteArray
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}

val GIT_HEAD: String get() = Runtime.getRuntime()
    .exec("git rev-parse --short HEAD")
    .inputStream.reader().readLines()[0]

val GITHUB_REPO: String get() {
    val remote = Runtime.getRuntime()
        .exec("git remote get-url origin")
        .inputStream.reader().readText()
    val repo = Pattern.compile("github.com/(.*?).git")
    val matcher = repo.matcher(remote)
    if (!matcher.find()) {
        throw IllegalStateException()
    }
    val result = matcher.group(0)
    return result.substring(11, result.length - 4)
}

val DATED_VERSION: Int get() = Integer.parseInt(
    SimpleDateFormat("yyMMdd").format(Date())
)

val TIME_MD5: String get() {
    val md5 = MessageDigest.getInstance("MD5")
    val digest = md5.digest(System.currentTimeMillis().toByteArray())
    val pre = BigInteger(1, digest)
    return pre.toString(16)
        .padStart(32, '0')
        .substring(8, 18)
}

val TYPE_RELEASE: String = "release"
val TYPE_DEV: String = "dev"
val TYPE_SNAPSHOT: String = "snapshot"

val VERSION_PROPERTIES get() =
    File(rootDir, "version.properties").apply {
        if (!exists()) {
            createNewFile()
        }
    }

android {
    compileSdk = 32
    buildToolsVersion = "32.0.0"

    val signInfoExit: Boolean = file("./gradle.properties").exists()

    if (signInfoExit){
        signingConfigs {
            @Suppress("LocalVariableName")
            create("sign") {
                val SIGN_DIR: String by project
                val SIGN_PASSWORD_STORE: String by project
                val SIGN_ALIAS: String by project
                val SIGN_PASSWORD_KEY: String by project
                storeFile = file(SIGN_DIR)
                storePassword = SIGN_PASSWORD_STORE
                keyAlias = SIGN_ALIAS
                keyPassword = SIGN_PASSWORD_KEY
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "io.github.sgpublic.bilidownload"
        minSdk = 26
        targetSdk = 32
        versionCode = DATED_VERSION
        versionName = "3.4.0"

        renderscriptTargetApi = 26
        renderscriptSupportModeEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (file("./gradle.properties").exists()) {
            signingConfig = signingConfigs.getByName("sign")
        }

        GITHUB_REPO.let {
            buildConfigField("String", "GITHUB_REPO", "\"$it\"")
            val repo = it.split("/")
            buildConfigField("String", "GITHUB_AUTHOR", "\"${repo[0]}\"")
            buildConfigField("String", "GITHUB_REPO_NAME", "\"${repo[1]}\"")
        }
        buildConfigField("String", "TYPE_RELEASE", "\"$TYPE_RELEASE\"")
        buildConfigField("String", "TYPE_DEV", "\"$TYPE_DEV\"")
        buildConfigField("String", "TYPE_SNAPSHOT", "\"$TYPE_SNAPSHOT\"")
    }

    buildTypes {
        val versionProps = Properties().apply {
            load(VERSION_PROPERTIES.inputStream())
        }

        all {
            isMinifyEnabled = false
        }
        named(TYPE_RELEASE) {
            versionNameSuffix = "-$name"
            versionProps[TYPE_RELEASE] = "BiliBangumiDownloader V${
                defaultConfig.versionName
            }(${defaultConfig.versionCode})"
        }
        named("debug") {
            versionNameSuffix = "-$TIME_MD5-$name"
        }
        register(TYPE_DEV) {
            versionNameSuffix = "-$GIT_HEAD-$name"
            isDebuggable = true
            versionProps[TYPE_DEV] = "BiliBangumiDownloader_${
                defaultConfig.versionName
            }_$GIT_HEAD"
        }
        register(TYPE_SNAPSHOT) {
            val suffix = TIME_MD5
            versionNameSuffix = "-$suffix-$name"
            versionProps[TYPE_SNAPSHOT] = "BiliBangumiDownloader_${
                defaultConfig.versionName
            }_$suffix"
            isDebuggable = true
        }

        versionProps.store(VERSION_PROPERTIES.writer(), null)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.github.zhpanvip.BannerViewPager:bannerview:2.6.6")
    val glideVer = "4.12.0"
    implementation("com.github.bumptech.glide:glide:$glideVer")
    annotationProcessor("com.github.bumptech.glide:compiler:$glideVer")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.4")
    implementation("com.yanzhenjie:sofia:1.0.5")
    implementation("com.github.SGPublic:Blur-Fix-AndroidX:1.1.2")
    implementation("com.github.SGPublic:SwipeBackLayoutX:1.2.1")
    implementation("com.github.SGPublic:MultiWaveHeaderX:1.0.0")
    implementation("com.github.li-xiaojun:XPopup:2.7.5")
    implementation("com.github.ssseasonnn:RxDownload:1.0.9")
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.kongzue.dialog_v3x:dialog:3.2.4")
    implementation("com.google.code.gson:gson:2.8.9")
    val exoVer = "2.16.1"
    implementation("com.google.android.exoplayer:exoplayer-core:$exoVer")
    implementation("com.google.android.exoplayer:exoplayer-dash:$exoVer")
    implementation("com.google.android.exoplayer:exoplayer-ui:$exoVer")
}

android.applicationVariants.all {
    outputs.forEach {
        if (it.name == "debug") {
            return@forEach
        }
        (it as BaseVariantOutputImpl).outputFileName = "${Properties().apply {
            load(VERSION_PROPERTIES.inputStream())
        }[it.name] as String}.apk"
        val name = StringUtils.capitalize(it.name)
        tasks.create("package${name}AndLocate") {
            dependsOn("assemble$name")
            doLast {
                val path = it.outputFile.absolutePath
                if (!File(path).exists()) {
                    return@doLast
                }
                Runtime.getRuntime().exec("explorer.exe /select, $path")
            }
        }
    }
}