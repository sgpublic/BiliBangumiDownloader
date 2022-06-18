@file:Suppress("PropertyName")

import android.databinding.tool.util.StringUtils
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.ir.backend.js.toByteArray
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.kapt")
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

val COMMIT_VERSION: Int get() {
    return Runtime.getRuntime()
        .exec("git log -n 1 --pretty=format:%cd --date=format:%y%m%d")
        .inputStream.reader().readLines()[0]
        .toInt()
}

val TIME_MD5: String get() {
    val md5 = MessageDigest.getInstance("MD5")
    val digest = md5.digest(System.currentTimeMillis().toByteArray())
    val pre = BigInteger(1, digest)
    return pre.toString(16)
        .padStart(32, '0')
        .substring(8, 18)
}

val TYPE_RELEASE: String = "release"
val TYPE_DEBUG: String = "debug"
val TYPE_DEV: String = "dev"
val TYPE_SNAPSHOT: String = "snapshot"
val SIGN_CONFIG: String = "sign"

val VERSION_PROPERTIES get() =
    File(rootDir, "version.properties").apply {
        if (!exists()) {
            createNewFile()
        }
    }

android {
    compileSdk = 32
    buildToolsVersion = "32.1.0-rc1"

    val signInfoExit: Boolean = file("./gradle.properties").exists()

    if (signInfoExit){
        signingConfigs {
            @Suppress("LocalVariableName")
            create(SIGN_CONFIG) {
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
        targetSdk = 31
        versionCode = COMMIT_VERSION
        versionName = "3.4.0"

        renderscriptTargetApi = 26
        renderscriptSupportModeEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (file("./gradle.properties").exists()) {
            signingConfig = signingConfigs.getByName("sign")
        }

        kapt {
            arguments {
                arg("room.schemaLocation", "$rootDir/schemas")
            }
        }

        fun buildConfigStringField(name: String, value: String) {
            buildConfigField("String", name, "\"$value\"")
        }
        GITHUB_REPO.let {
            buildConfigStringField("GITHUB_REPO", it)
            val repo = it.split("/")
            buildConfigStringField("GITHUB_AUTHOR", repo[0])
            buildConfigStringField("GITHUB_REPO_NAME", repo[1])
        }
        buildConfigStringField("PROJECT_NAME", rootProject.name)
        buildConfigStringField("TYPE_RELEASE", TYPE_RELEASE)
        buildConfigStringField("TYPE_DEV", TYPE_DEV)
        buildConfigStringField("TYPE_SNAPSHOT", TYPE_SNAPSHOT)
    }

    buildTypes {
        val versionProps = Properties().apply {
            load(VERSION_PROPERTIES.inputStream())
        }

        all {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName(SIGN_CONFIG)
        }

        /** 自动化版本命名 */
        named(TYPE_RELEASE) {
            versionNameSuffix = "-$name"
            versionProps[TYPE_RELEASE] = "${rootProject.name} V${
                defaultConfig.versionName
            }(${defaultConfig.versionCode})"
        }
        named(TYPE_DEBUG) {
            defaultConfig.versionCode = DATED_VERSION
            isDebuggable = true
            versionNameSuffix = "-$TIME_MD5-$name"
        }
        register(TYPE_DEV) {
            versionNameSuffix = "-$GIT_HEAD-$name"
            isDebuggable = true
            isTestCoverageEnabled = true
            versionProps[TYPE_DEV] = "${rootProject.name}_${
                defaultConfig.versionName
            }_$GIT_HEAD"
        }
        register(TYPE_SNAPSHOT) {
            defaultConfig.versionCode = DATED_VERSION
            isDebuggable = true
            val suffix = TIME_MD5
            versionNameSuffix = "-$suffix-$name"
            versionProps[TYPE_SNAPSHOT] = "${rootProject.name}_${
                defaultConfig.versionName
            }_$suffix"
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
    implementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")

    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.2")

    val roomVer = "2.4.2"
    implementation("androidx.room:room-runtime:$roomVer")
    implementation("androidx.room:room-ktx:$roomVer")
    annotationProcessor("androidx.room:room-compiler:$roomVer")
    kapt("androidx.room:room-compiler:$roomVer")
    testImplementation("androidx.room:room-testing:$roomVer")

    /* https://github.com/zhpanvip/BannerViewPager */
    implementation("com.github.zhpanvip:BannerViewPager:3.5.5")
    /* https://github.com/square/okhttp */
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.7")
    /* https://github.com/yanzhenjie/Sofia */
    implementation("com.yanzhenjie:sofia:1.0.5")
    /* https://github.com/sgpublic/Blur-Fix-AndroidX */
    implementation("com.github.SGPublic:Blur-Fix-AndroidX:1.1.2")
    /* https://github.com/scwang90/MultiWaveHeader */
    implementation("com.scwang.wave:MultiWaveHeader:1.0.0")
    /* https://github.com/li-xiaojun/XPopup */
    implementation("com.github.li-xiaojun:XPopup:2.7.7")
    /* https://github.com/zxing/zxing qrcode */
    implementation("com.google.zxing:core:3.5.0")
    /* https://github.com/google/gson */
    implementation("com.google.code.gson:gson:2.9.0")
    /* https://github.com/KwaiAppTeam/AkDanmaku */
    implementation("com.kuaishou:akdanmaku:1.0.3")
    /* https://github.com/AnJiaoDe/TabLayoutNiubility */
    implementation("com.github.AnJiaoDe:TabLayoutNiubility:V1.3.0")
    /* https://github.com/lihangleo2/ShadowLayout */
    implementation("com.github.lihangleo2:ShadowLayout:3.2.4")

    /* https://github.com/bumptech/glide */
    val glideVer = "4.13.2"
    implementation("com.github.bumptech.glide:glide:$glideVer")
    annotationProcessor("com.github.bumptech.glide:compiler:$glideVer")
    kapt("com.github.bumptech.glide:compiler:$glideVer")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    /* https://github.com/google/ExoPlayer */
    val exoVer = "2.17.1"
    implementation("com.google.android.exoplayer:exoplayer-core:$exoVer")
    implementation("com.google.android.exoplayer:exoplayer-dash:$exoVer")
    implementation("com.google.android.exoplayer:exoplayer-ui:$exoVer")

    /* https://github.com/AriaLyy/Aria */
    val ariaVer = "3.8.16"
    implementation("me.laoyuyu.aria:core:$ariaVer")
    annotationProcessor("me.laoyuyu.aria:compiler:$ariaVer")
    kapt("me.laoyuyu.aria:compiler:$ariaVer")
}

/** 自动修改输出文件名并定位文件 */
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
                when (true) {
                    OperatingSystem.current().isWindows ->
                        Runtime.getRuntime().exec("explorer.exe /select, $path")
                }
            }
        }
    }
}
