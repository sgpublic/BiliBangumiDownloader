package io.github.sgpublic.bilidownload.core.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import io.github.sgpublic.bilidownload.core.util.take

open class AutoLevelFilter @JvmOverloads constructor(
    private var level: Level = Level.INFO
): Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        return event.level.isGreaterOrEqual(level).take(FilterReply.NEUTRAL, FilterReply.DENY)
    }
}