package io.github.sgpublic.bilidownload.core.logback

import ch.qos.logback.classic.Level
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.core.util.take

class FileFilter: AutoLevelFilter(
    (BuildConfig.BUILD_TYPE != "release").take(Level.DEBUG, Level.INFO)
)