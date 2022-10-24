package io.github.sgpublic.bilidownload.core.util

/**
 * Collection 扩展函数，判断是否存在所有元素，空指针冗余
 */
fun <T> Collection<T>?.containsAll(list: Collection<T>?): Boolean {
    if (this == null || list == null) return false
    return containsAll(list)
}

/**
 * Collection 扩展函数，判断是否存在元素，空指针冗余
 */
fun <T> Collection<T>?.contains(element: T?): Boolean {
    if (this == null || element == null) return false
    return contains(element)
}

/**
 * MutableCollection 扩展函数，如果元素存在则删除，不存在则添加，空指针冗余
 */
fun <T> MutableCollection<T>?.exchange(element: T?) {
    if (this == null || element == null) return
    contains(element).take({ remove(element) }, { add(element) })
}

/**
 * MutableCollection 扩展函数，如果列出的元素全部存在则删除，否则全部添加，空指针冗余
 */
fun <T> MutableCollection<T>?.exchangeAll(list: Collection<T>?) {
    containsAll(list).take({ removeAll(list) }, { addAll(list) })
}

/**
 * MutableCollection 扩展函数，删除列出的所有元素，空指针冗余
 */
fun <T> MutableCollection<T>?.removeAll(list: Collection<T>?) {
    if (this == null || list == null) return
    list.forEach { remove(it) }
}

/**
 * MutableCollection 扩展函数，如果元素存在则删除，不存在则添加，空指针冗余
 */
fun <T> MutableCollection<T>?.addAll(list: Collection<T>?) {
    if (this == null || list == null) return
    list.forEach { contains(it).take { add(it) } }
}

fun <T> ArrayList<T>.advSub(factor: Int): ArrayList<T> {
    val sub = size - (size % factor)
    return ArrayList(subList(0, sub))
}