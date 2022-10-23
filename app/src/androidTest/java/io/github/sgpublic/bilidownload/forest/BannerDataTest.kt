package io.github.sgpublic.bilidownload.forest

import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.util.ForestClients
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.toGson
import io.github.sgpublic.exsp.ExPreference
import org.junit.Test

class BannerDataTest {
    @Test
    fun getBanner() {
        val execute = ForestClients.API.banner(ExPreference.get<TokenPreference>().accessToken).execute()
        log.debug(execute.toGson())
    }
}