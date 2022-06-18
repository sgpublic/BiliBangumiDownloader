@file:Suppress("JcenterRepositoryObsolete")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://jitpack.io")
        @Suppress("DEPRECATION") jcenter()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        @Suppress("DEPRECATION") jcenter()
    }
}

include(":app")
rootProject.name = "BiliBangumiDownloader"
