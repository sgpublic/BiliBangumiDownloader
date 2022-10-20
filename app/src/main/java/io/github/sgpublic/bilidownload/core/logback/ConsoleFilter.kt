package io.github.sgpublic.bilidownload.core.logback

import ch.qos.logback.classic.Level
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.core.util.take

class ConsoleFilter: AutoLevelFilter(
    BuildConfig.DEBUG.take(Level.DEBUG, Level.INFO)
)