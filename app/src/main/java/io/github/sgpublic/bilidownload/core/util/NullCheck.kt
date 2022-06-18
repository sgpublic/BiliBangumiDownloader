package io.github.sgpublic.bilidownload.core.util

/**
 * 检查是否为 null
 * @param isNull 若为 null 则调用
 * @param isNotNull 若不为 null 则调用，并传递非 null 类型
 */
fun <T> T?.check(isNull: () -> Unit = { }, isNotNull: (T) -> Unit = { }) {
    if (this != null) isNotNull(this) else isNull()
}