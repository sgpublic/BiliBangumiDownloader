package io.github.sgpublic.bilidownload.core.util

import com.arialyy.aria.core.common.HttpOption
import com.arialyy.aria.core.download.target.GroupBuilderTarget
import com.arialyy.aria.core.download.target.GroupNormalTarget
import com.arialyy.aria.core.download.target.HttpBuilderTarget
import io.github.sgpublic.bilidownload.core.forest.core.UrlEncodedInterceptor

private val header = HttpOption()
    .addHeader("User-Agent", UrlEncodedInterceptor.UserAgent)

fun GroupBuilderTarget.customs(): GroupBuilderTarget {
    return option(header)
        .ignoreCheckPermissions()
        .ignoreFilePathOccupy()
        .unknownSize()
}

fun GroupNormalTarget.customs(): GroupNormalTarget {
    return ignoreCheckPermissions()
        .unknownSize()
}

fun HttpBuilderTarget.customs(): HttpBuilderTarget {
    return option(header)
        .ignoreCheckPermissions()
        .ignoreFilePathOccupy()
}