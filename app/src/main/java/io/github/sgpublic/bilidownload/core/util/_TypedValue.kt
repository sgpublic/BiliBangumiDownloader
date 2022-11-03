package io.github.sgpublic.bilidownload.core.util

import android.util.TypedValue
import io.github.sgpublic.bilidownload.Application

val Int.dp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(),
    Application.ApplicationContext.resources.displayMetrics).toInt()
val Float.dp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this,
    Application.ApplicationContext.resources.displayMetrics).toInt()

val Int.sp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(),
    Application.ApplicationContext.resources.displayMetrics).toInt()
val Float.sp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this,
    Application.ApplicationContext.resources.displayMetrics).toInt()