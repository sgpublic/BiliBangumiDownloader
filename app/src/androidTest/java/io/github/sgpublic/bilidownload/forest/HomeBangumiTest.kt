package io.github.sgpublic.bilidownload.forest

import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.util.ForestClients
import io.github.sgpublic.exsp.ExPreference
import org.junit.Test

class HomeBangumiTest {
    @Test
    fun getBangumi() {
        ForestClients.API.bangumi(ExPreference.get<TokenPreference>().accessToken, 0).execute()
    }
}