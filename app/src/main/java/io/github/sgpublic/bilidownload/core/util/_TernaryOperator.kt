package io.github.sgpublic.bilidownload.core.util

/**
 * Boolean 扩展函数，三元表达式
 * @param ifTrue 如果 Boolean 为 true 执行
 * @param ifFalse 如果 Boolean 为 false 执行
 */
fun Boolean?.take(ifTrue: () -> Unit = { }, ifFalse: () -> Unit = { }) {
    if (true == this) ifTrue() else ifFalse()
}

/**
 * Boolean 扩展函数，三元表达式
 * @param ifTrue 如果 Boolean 为 true 执行
 * @param ifFalse 如果 Boolean 为 false 执行
 */
fun <T> Boolean?.take(ifTrue: () -> T, ifFalse: () -> T): T {
    return if (true == this) ifTrue() else ifFalse()
}

/**
 * Boolean 扩展函数，三元表达式
 * @param ifTrue 如果 Boolean 为 true 时返回
 * @param ifFalse 如果 Boolean 为 false 时返回
 */
fun <T> Boolean?.take(ifTrue: T, ifFalse: T): T {
    return if (true == this) ifTrue else ifFalse
}