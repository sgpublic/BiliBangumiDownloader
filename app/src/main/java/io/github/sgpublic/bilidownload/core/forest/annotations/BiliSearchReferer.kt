package io.github.sgpublic.bilidownload.core.forest.annotations

import com.dtflys.forest.annotation.Header

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Header(name = "Referer", defaultValue = "https://search.bilibili.com")
annotation class BiliSearchReferer()
