package io.github.sgpublic.bilidownload.core.forest.annotations

import com.dtflys.forest.annotation.MethodLifeCycle
import io.github.sgpublic.bilidownload.core.forest.core.SignLifeCycle

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@MethodLifeCycle(SignLifeCycle::class)
@Target(AnnotationTarget.FUNCTION)
annotation class BiliSign(
    val appKey: String = "1d8b6e7d45233436",
    val appSecret: String = "560c52ccd288fed045859ed18bffd973",
    val mobiApp: String = "android",
    val platform: String = "android",
    val build: String = "7010300",
)