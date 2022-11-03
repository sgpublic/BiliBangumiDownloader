package io.github.sgpublic.bilidownload.core.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val Any.log: Logger get() = LoggerFactory.getLogger(this::class.isCompanion.take(javaClass.enclosingClass, javaClass))