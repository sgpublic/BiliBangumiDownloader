package io.github.sgpublic.bilidownload.forest

import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.forest.data.BangumiPageResp
import io.github.sgpublic.bilidownload.core.util.ForestClients
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.toGson
import io.github.sgpublic.exsp.ExPreference
import org.junit.Test

class HomeBangumiTest {
    @Test
    fun getBangumi() {
        val execute = ForestClients.API.bangumi(ExPreference.get<TokenPreference>().accessToken, 0, 1)
            .execute(BangumiPageResp::class.java)
        log.debug(execute.data.toGson())
    }
}