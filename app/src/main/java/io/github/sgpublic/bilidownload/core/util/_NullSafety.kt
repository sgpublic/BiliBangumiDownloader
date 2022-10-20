package io.github.sgpublic.bilidownload.core.util

fun <T> T?.takeOr(def: T): T {
    return this ?: def
}

fun CharSequence?.takeOr(def: String): String {
    return if (this != null && "$this".isNotBlank()) {
        "$this"
    } else {
        def
    }
}