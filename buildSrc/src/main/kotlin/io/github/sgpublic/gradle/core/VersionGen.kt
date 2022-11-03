package io.github.sgpublic.gradle.core

import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

object VersionGen {
    val GIT_HEAD: String get() {
        val lines = Runtime.getRuntime()
                .exec("git rev-parse --short HEAD")
                .inputStream.reader()
                .readLines()
        if (lines.isEmpty()) {
            return TIME_MD5
        }
        return lines[0]
    }

    val DATED_VERSION: Int @Suppress("SimpleDateFormat") get() {
        return Integer.parseInt(SimpleDateFormat("yyMMdd").format(Date()))
    }

    val COMMIT_VERSION: Int get() {
        val lines = Runtime.getRuntime()
                .exec("git log -n 1 --pretty=format:%cd --date=format:%y%m%d")
                .inputStream.reader()
                .readLines()
        if (lines.isEmpty()) {
            return DATED_VERSION
        }
        return Integer.parseInt(lines[0])
    }

    val TIME_MD5: String get() {
        val digest = MessageDigest.getInstance("MD5")
                .digest(System.currentTimeMillis().toString().toByteArray())
        val pre = BigInteger(1, digest)
        return pre.toString(16).padStart(32, '0').substring(8, 18)
    }
}