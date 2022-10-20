package io.github.sgpublic.bilidownload.core.forest.annotations

import com.dtflys.forest.annotation.MethodLifeCycle
import io.github.sgpublic.bilidownload.core.forest.core.GeetestApiCycle

/**
 *
 * @author Madray Haven
 * @date 2022/10/19 12:37
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@MethodLifeCycle(GeetestApiCycle::class)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class GeetestApi
