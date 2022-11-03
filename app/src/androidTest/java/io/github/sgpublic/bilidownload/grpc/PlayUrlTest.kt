package io.github.sgpublic.bilidownload.grpc

import io.github.sgpublic.bilidownload.core.grpc.client.AppClient
import io.github.sgpublic.bilidownload.core.util.log
import okhttp3.internal.closeQuietly
import org.junit.AfterClass
import org.junit.Test

/**
 *
 * @author Madray Haven
 * @date 2022/10/24 17:18
 */
class PlayUrlTest {
    @Test
    fun getUrl() {
        val execute = AppClient.getPlayUrl(866123996, 691172, 80).execute()
        log.debug("result: $execute")
    }

    companion object {
        private val AppClient: AppClient by lazy {
            AppClient()
        }

        @AfterClass
        @JvmStatic
        fun cleanup() {
            AppClient.closeQuietly()
        }
    }
}