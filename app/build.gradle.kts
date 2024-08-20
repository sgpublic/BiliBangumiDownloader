@file:Suppress("PropertyName")

import com.google.protobuf.gradle.proto
import io.github.sgpublic.androidassemble.core.renameRule
import io.github.sgpublic.androidassemble.util.VersionGen
import io.github.sgpublic.androidassemble.util.buildConfigField
import java.util.*

plugins {
    alias(bilidl.plugins.android.application)

    alias(bilidl.plugins.kotlin.android)
    alias(bilidl.plugins.kotlin.plugin.parcelize)
    alias(bilidl.plugins.kotlin.kapt)
    alias(bilidl.plugins.kotlin.plugin.lombok)

    alias(bilidl.plugins.protobuf)
    alias(bilidl.plugins.lombok)
    alias(bilidl.plugins.github.release)
    alias(bilidl.plugins.android.assemble)
}

val mVersion = findProperty("bilidl.version")!!.toString()

android {
    compileSdk = 34
    namespace = "io.github.sgpublic.bilidownload"

    val properties = file("./sign/sign.properties")
    val signInfoExit: Boolean = properties.exists()

    if (signInfoExit){
        val keyProps = Properties()
        keyProps.load(properties.inputStream())
        signingConfigs {
            create("release") {
                val SIGN_DIR: String by keyProps
                val SIGN_PASSWORD_STORE: String by keyProps
                val SIGN_ALIAS: String by keyProps
                val SIGN_PASSWORD_KEY: String by keyProps
                keyPassword = SIGN_PASSWORD_KEY
                keyAlias = SIGN_ALIAS
                storeFile = file(SIGN_DIR)
                storePassword = SIGN_PASSWORD_STORE
            }
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    sourceSets {
        named("main") {
            proto {
                srcDir("src/main/java")
            }
        }
        named("test") {
            proto {
                srcDir("src/test/java")
            }
        }
    }

    defaultConfig {
        applicationId = "io.github.sgpublic.bilidownload"
        minSdk = 29
        targetSdk = 34
        versionCode = VersionGen.COMMIT_COUNT_VERSION
        versionName = mVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        "sgpublic/BiliBangumiDownloader".let {
            buildConfigField("GITHUB_REPO", it)
            val repo = it.split("/")
            buildConfigField("GITHUB_AUTHOR", repo[0])
            buildConfigField("GITHUB_REPO_NAME", repo[1])
        }
        buildConfigField("PROJECT_NAME", rootProject.name)
    }

    buildTypes {
        all {
            isMinifyEnabled = false
            if (signInfoExit) {
                signingConfig = signingConfigs.getByName("release")
            }
        }

        /** 自动化版本命名 */
        release {
            renameRule(project) {
                "${rootProject.name} V${versionName}(${versionCode})"
            }
        }
        debug {
            isDebuggable = true

            renameRule(project) {
                "${rootProject.name}_${versionName}_${versionCode}"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    packaging {
        resources.excludes.addAll(listOf(
            "META-INF/DEPENDENCIES",
            "META-INF/NOTICE",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/NOTICE.txt",
        ))
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${bilidl.versions.protobuf.asProvider().get()}"
    }
    plugins {
        register("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${bilidl.versions.grpc.get()}"
        }
    }
    generateProtoTasks {
        for(task in all()) {
            task.builtins {
                val java by creating {
                    option("lite")
                }
            }
            task.plugins {
                val grpc by creating {
                    option("lite")
                }
            }
        }
    }
}

kapt {
    keepJavacAnnotationProcessors = true
}

dependencies {
    testImplementation(bilidl.junit)
    androidTestImplementation(bilidl.androidx.test.ext)
    androidTestImplementation(bilidl.androidx.test.espresso)
    androidTestImplementation(bilidl.androidx.test.runner)
    androidTestImplementation(bilidl.androidx.test.rules)
    implementation(bilidl.kotlin.reflect)

    implementation(bilidl.androidx.core)
    implementation(bilidl.androidx.appcompat)
    implementation(bilidl.google.material)
    implementation(bilidl.androidx.constraintlayout)
    implementation(bilidl.androidx.swiperefreshlayout)
    implementation(bilidl.androidx.navigation)

    /* https://github.com/zhpanvip/BannerViewPager */
    implementation(bilidl.bannerviewpager)
    /* https://github.com/yanzhenjie/Sofia */
    implementation(bilidl.sofia)
    /* https://github.com/scwang90/MultiWaveHeader */
    implementation(bilidl.multiwaveheader)
    /* https://github.com/li-xiaojun/XPopup */
    implementation(bilidl.xpopup)
    /* https://github.com/zxing/zxing qrcode */
    implementation(bilidl.zxing)
//    /* https://github.com/KwaiAppTeam/AkDanmaku */
//    implementation(bilidl.akdanmaku)
    implementation(bilidl.geetest)

    /* https://github.com/sgpublic/ExSharedPreference */
    implementation(bilidl.exsp.runtime)
    kapt(bilidl.exsp.compiler)

    compileOnly(bilidl.lombok)
    annotationProcessor(bilidl.lombok)
    testCompileOnly(bilidl.lombok)
    testAnnotationProcessor(bilidl.lombok)
    androidTestCompileOnly(bilidl.lombok)
    androidTestAnnotationProcessor(bilidl.lombok)

    implementation(bilidl.androidx.room.runtime)
    annotationProcessor(bilidl.androidx.room.compiler)

    /* https://github.com/google/protobuf-gradle-plugin */
    implementation(bilidl.protobuf)
    // 阿b用的 cronet，如果用 okhttp 会导致 io.grpc.StatusRuntimeException: INTERNAL: Received unexpected EOS on DATA frame from server.
    implementation(bilidl.grpc.cronet)
    implementation(bilidl.google.play.cronet)
    implementation(bilidl.cronet.fallback)
    implementation(bilidl.grpc.android)
    implementation(bilidl.grpc.protobuf)
    implementation(bilidl.grpc.stub)
    implementation(bilidl.tomcat.annotations)

    /* https://github.com/bumptech/glide */
    implementation(bilidl.glide)
    kapt(bilidl.glide.compiler)
    implementation(bilidl.glide.transformations)

    /* https://github.com/google/ExoPlayer */
    implementation(bilidl.exoplayer.core)
    implementation(bilidl.exoplayer.dash)
    implementation(bilidl.exoplayer.ui)

    /* https://github.com/AriaLyy/Aria */
    implementation(bilidl.aria.core)
    kapt(bilidl.aria.compiler)

    /* https://github.com/tony19/logback-android */
    implementation(bilidl.logback.android)
    implementation(bilidl.slf4j.api)

    /* https://github.com/dromara/forest */
    implementation(bilidl.forest)
    implementation(bilidl.gson)
    implementation(bilidl.okhttp3)
}

fun findEnv(name: String) = provider {
    findProperty(name)?.toString()?.takeIf { it.isNotBlank() }
        ?: System.getenv(name.replace(".", "_").uppercase())
}

githubRelease {
    token(findEnv("publishing.github.token"))
    owner = "sgpublic"
    repo = "BiliBangumiDownloader"
    tagName = "v${mVersion}"
    releaseName = "v${mVersion}"
    overwrite = true

    releaseAssets(
        "${rootDir}/assemble/${rootProject.name} V${mVersion}(${VersionGen.COMMIT_COUNT_VERSION}).apk"
    )
}
