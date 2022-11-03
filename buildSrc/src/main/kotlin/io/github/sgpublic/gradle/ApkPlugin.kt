package io.github.sgpublic.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

class ApkPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        rootProject = project.rootProject
        logger = project.logger
    }

    companion object {
        lateinit var rootProject: Project private set
        lateinit var logger: Logger private set
    }
}