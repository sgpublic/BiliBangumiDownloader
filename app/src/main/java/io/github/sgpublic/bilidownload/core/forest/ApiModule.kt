package io.github.sgpublic.bilidownload.core.forest

class ApiModule {
    companion object {
        const val USER_AGENT = "Mozilla/5.0 (sgpublic2002@gmail.com)"

        val TS: Long get() = System.currentTimeMillis() / 1000
        val TS_FULL: Long get() = System.currentTimeMillis()
    }
}