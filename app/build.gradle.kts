@file:Suppress("PropertyName")

import com.android.build.api.dsl.VariantDimension
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.google.protobuf.gradle.proto
import com.google.protobuf.gradle.remove
import io.github.sgpublic.gradle.core.BuildTypes
import io.github.sgpublic.gradle.core.SignConfig
import io.github.sgpublic.gradle.core.VersionGen
import io.github.sgpublic.gradle.util.ApkUtil
import java.util.*
import io.github.sgpublic.gradle.Dep

plugins {
    id("bilidl-version")

    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.kapt")

    id("org.jetbrains.kotlin.plugin.lombok")
    id("io.freefair.lombok") version "5.3.0"

    id("com.google.protobuf")
}

fun VariantDimension.buildConfigField(name: String, value: String) {
    buildConfigField("String", name, "\"$value\"")
}
fun VariantDimension.buildConfigField(name: String, value: Int) {
    buildConfigField("int", name, value.toString())
}

android {
    compileSdk = 33
    buildToolsVersion = "33.0.0"
    namespace = "io.github.sgpublic.bilidownload"

    val properties = file("./sign/sign.properties")
    val signInfoExit: Boolean = properties.exists()

    if (signInfoExit){
        val keyProps = Properties()
        keyProps.load(properties.inputStream())
        signingConfigs {
            create(SignConfig.NAME) {
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
        minSdk = 26
        targetSdk = 33
        versionCode = VersionGen.COMMIT_VERSION
        versionName = "3.5.0".also {
            buildConfigField("ORIGIN_VERSION_NAME", it)
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }

        "sgpublic/BiliBangumiDownloader_Kotlin".let {
            buildConfigField("GITHUB_REPO", it)
            val repo = it.split("/")
            buildConfigField("GITHUB_AUTHOR", repo[0])
            buildConfigField("GITHUB_REPO_NAME", repo[1])
        }
        buildConfigField("PROJECT_NAME", rootProject.name)
        buildConfigField("TYPE_RELEASE", BuildTypes.TYPE_RELEASE)
        buildConfigField("LEVEL_RELEASE", BuildTypes.LEVEL_RELEASE)
        buildConfigField("TYPE_DEV", BuildTypes.TYPE_DEV)
        buildConfigField("LEVEL_DEV", BuildTypes.LEVEL_DEV)
        buildConfigField("TYPE_SNAPSHOT", BuildTypes.TYPE_SNAPSHOT)
        buildConfigField("LEVEL_SNAPSHOT", BuildTypes.LEVEL_SNAPSHOT)
        buildConfigField("TYPE_DEBUG", BuildTypes.TYPE_DEBUG)
        buildConfigField("LEVEL_DEBUG", BuildTypes.LEVEL_DEBUG)
    }

    buildTypes {
        all {
            isMinifyEnabled = false
            if (signInfoExit) {
                signingConfig = signingConfigs.getByName(SignConfig.NAME)
            }
        }

        /** 自动化版本命名 */
        named(BuildTypes.TYPE_RELEASE) {
            versionNameSuffix = "-$name"
            buildConfigField( "BUILD_LEVEL", BuildTypes.LEVEL_RELEASE)
        }
        named(BuildTypes.TYPE_DEBUG) {
            defaultConfig.versionCode = VersionGen.DATED_VERSION
            isDebuggable = true
            versionNameSuffix = "-${VersionGen.TIME_MD5}-$name"
            buildConfigField( "BUILD_LEVEL", BuildTypes.LEVEL_DEBUG)
        }
        if (signInfoExit) {
            register(BuildTypes.TYPE_DEV) {
                versionNameSuffix = "-${VersionGen.GIT_HEAD}-$name"
                isDebuggable = true
                buildConfigField( "BUILD_LEVEL", BuildTypes.LEVEL_DEV)
            }
            register(BuildTypes.TYPE_SNAPSHOT) {
                defaultConfig.versionCode = VersionGen.DATED_VERSION
                isDebuggable = true
                versionNameSuffix = "-${VersionGen.TIME_MD5}-$name"
                buildConfigField( "BUILD_LEVEL", BuildTypes.LEVEL_SNAPSHOT)
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packagingOptions {
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
        artifact = "com.google.protobuf:protoc:${Dep.Proto}"
    }
    plugins {
        register("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Dep.GrpcJava}"
        }
    }
    generateProtoTasks {
        for(task in all()) {
            task.builtins {
                register("java") {
                    option("lite")
                }
            }
            task.plugins {
                register("grpc") {
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
    implementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    implementation(kotlin("reflect"))

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.2")

    /* https://github.com/zhpanvip/BannerViewPager */
    implementation("com.github.zhpanvip:BannerViewPager:3.5.5")
    /* https://github.com/yanzhenjie/Sofia */
    implementation("com.yanzhenjie:sofia:1.0.5")
    /* https://github.com/scwang90/MultiWaveHeader */
    implementation("com.scwang.wave:MultiWaveHeader:1.0.0")
    /* https://github.com/li-xiaojun/XPopup */
    implementation("com.github.li-xiaojun:XPopup:2.9.1")
    /* https://github.com/zxing/zxing qrcode */
    implementation("com.google.zxing:core:3.5.0")
    /* https://github.com/KwaiAppTeam/AkDanmaku */
    implementation("com.kuaishou:akdanmaku:1.0.3")
    /* https://github.com/AnJiaoDe/TabLayoutNiubility */
    implementation("com.github.AnJiaoDe:TabLayoutNiubility:V1.3.0")
    /* https://github.com/lihangleo2/ShadowLayout */
    implementation("com.github.lihangleo2:ShadowLayout:3.2.4")
    /* https://docs.geetest.com/sensebot/deploy/client/android */
    implementation("com.geetest.sensebot:sensebot:4.3.7")

    /* https://github.com/sgpublic/ExSharedPreference */
    implementation("io.github.sgpublic:exsp-runtime:${Dep.EXSP}")
    kapt("io.github.sgpublic:exsp-compiler:${Dep.EXSP}")

    compileOnly("org.projectlombok:lombok:${Dep.Lombok}")
    annotationProcessor("org.projectlombok:lombok:${Dep.Lombok}")
    androidTestCompileOnly("org.projectlombok:lombok:${Dep.Lombok}")
    androidTestAnnotationProcessor("org.projectlombok:lombok:${Dep.Lombok}")

    implementation("androidx.room:room-runtime:${Dep.Room}")
    implementation("androidx.room:room-ktx:${Dep.Room}")
    kapt("androidx.room:room-compiler:${Dep.Room}")
    testImplementation("androidx.room:room-testing:${Dep.Room}")

    /* https://github.com/google/protobuf-gradle-plugin */
    implementation("com.google.protobuf:protobuf-javalite:${Dep.Proto}")
    implementation("io.grpc:grpc-okhttp:${Dep.GrpcJava}")
    implementation("io.grpc:grpc-android:${Dep.GrpcJava}")
    implementation("io.grpc:grpc-protobuf-lite:${Dep.GrpcJava}")
    implementation("io.grpc:grpc-stub:${Dep.GrpcJava}")
    implementation("org.apache.tomcat:annotations-api:6.0.53")

    /* https://github.com/bumptech/glide */
    implementation("com.github.bumptech.glide:glide:${Dep.Glide}")
    kapt("com.github.bumptech.glide:compiler:${Dep.Glide}")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    /* https://github.com/google/ExoPlayer */
    implementation("com.google.android.exoplayer:exoplayer-core:${Dep.ExoPlayer}")
    implementation("com.google.android.exoplayer:exoplayer-dash:${Dep.ExoPlayer}")
    implementation("com.google.android.exoplayer:exoplayer-ui:${Dep.ExoPlayer}")

    /* https://github.com/AriaLyy/Aria */
    implementation("me.laoyuyu.aria:core:${Dep.Aria}")
    kapt("me.laoyuyu.aria:compiler:${Dep.Aria}")

    /* https://github.com/tony19/logback-android */
    implementation("com.github.tony19:logback-android:2.0.0")
    implementation("org.slf4j:slf4j-api:1.7.36")

    /* https://github.com/dromara/forest */
    implementation("com.dtflys.forest:forest-core:1.5.26")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}

/** 自动修改输出文件名并定位文件 */
android.applicationVariants.all {
    for (output in outputs) {
        if (output !is BaseVariantOutputImpl) {
            continue
        }
        val name = output.name.split("-")
            .joinToString("") { it.capitalize() }
        val taskName = "assemble${name}AndLocate"
        tasks.register(taskName) {
            dependsOn("assemble${name}")
            doLast {
                ApkUtil.assembleAndLocate(output.name, output.outputFile, "./build/assemble")
            }
        }
    }
}
