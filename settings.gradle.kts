pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://jitpack.io")
        jcenter()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        gradlePluginPortal()
        mavenLocal()
        jcenter()
    }

    versionCatalogs {
        val bilidl by creating {
            from(files(File(rootDir, "./gradle/bilidl.versions.toml")))
        }
    }
}

include(":app")
rootProject.name = "BiliBangumiDownloader"
