package com.sgpublic.bilidownload.base

object Base64Util {
    fun Decode(content: String): ByteArray? {
        val data = content.toCharArray()
        val codes = ByteArray(256)
        for (i in 0..255) {
            codes[i] = -1
        }
        run {
            var i = 'A'.toInt()
            while (i <= 'Z'.toInt()) {
                codes[i] = (i - 'A'.toInt()).toByte()
                i++
            }
        }
        run {
            var i = 'a'.toInt()
            while (i <= 'z'.toInt()) {
                codes[i] = (26 + i - 'a'.toInt()).toByte()
                i++
            }
        }
        var i = '0'.toInt()
        while (i <= '9'.toInt()) {
            codes[i] = (52 + i - '0'.toInt()).toByte()
            i++
        }
        codes['+'.toInt()] = 62
        codes['/'.toInt()] = 63
        var tempLen = data.size
        for (datum in data) {
            if (datum.toInt() > 255 || codes[datum.toInt()] < 0) {
                --tempLen
            }
        }
        var len = tempLen / 4 * 3
        if (tempLen % 4 == 3) {
            len += 2
        }
        if (tempLen % 4 == 2) {
            len += 1
        }
        val out = ByteArray(len)
        var shift = 0
        var accum = 0
        var index = 0
        for (datum in data) {
            val value = if (datum.toInt() > 255) -1 else codes[datum.toInt()].toInt()
            if (value >= 0) {
                accum = accum shl 6
                shift += 6
                accum = accum or value // at the bottom.
                if (shift >= 8) {
                    shift -= 8
                    out[index++] = (accum shr shift and 0xff).toByte()
                }
            }
        }
        return if (index != out.size) {
            ByteArray(0)
        } else {
            out
        }
    }

    fun Encode(content: ByteArray): String? {
        val alphabet =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray()
        val out = CharArray((content.size + 2) / 3 * 4)
        var i = 0
        var index = 0
        while (i < content.size) {
            var quad = false
            var trip = false
            var `val` = 0xFF and content[i].toInt()
            `val` = `val` shl 8
            if (i + 1 < content.size) {
                `val` = `val` or (0xFF and content[i + 1].toInt())
                trip = true
            }
            `val` = `val` shl 8
            if (i + 2 < content.size) {
                `val` = `val` or (0xFF and content[i + 2].toInt())
                quad = true
            }
            out[index + 3] = alphabet[if (quad) `val` and 0x3F else 64]
            `val` = `val` shr 6
            out[index + 2] = alphabet[if (trip) `val` and 0x3F else 64]
            `val` = `val` shr 6
            out[index + 1] = alphabet[`val` and 0x3F]
            `val` = `val` shr 6
            out[index] = alphabet[`val` and 0x3F]
            i += 3
            index += 4
        }
        return String(out)
    }
}