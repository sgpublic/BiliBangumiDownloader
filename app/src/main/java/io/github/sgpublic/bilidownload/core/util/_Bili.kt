package io.github.sgpublic.bilidownload.core.util

import java.util.StringJoiner
import kotlin.random.Random

private fun genMacAddr(): String {
    val mac = StringJoiner(":")

    val num = Random.nextInt(0, 0xff + 1) and 0b11111110 or 0b00000010 // 表示这是一个本地管理的单播地址
    mac.add(num.toString(16))

    for (i in 0 until 5) {
        val num = Random.nextInt(0, 0xff + 1)
        mac.add(num.toString(16))
    }

    return mac.toString()
}

fun genBuvid(): String {
    val macMd5 = genMacAddr().MD5_FULL
    return "XY${macMd5[2]}${macMd5[12]}${macMd5[22]}$macMd5"
}