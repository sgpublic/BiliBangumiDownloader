package io.github.sgpublic.bilidownload.forest

import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.util.ForestClients
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.toGson
import io.github.sgpublic.exsp.ExPreference
import org.junit.Test

/**
 *
 * @author Madray Haven
 * @date 2022/10/21 11:17
 */
class SeasonInfoTest {
    private val Token: TokenPreference by lazy { ExPreference.get() }

    /** 租借女友 */
    @Test
    fun season_42688() {
        val realSeason = realSeason(42068)
        log.debug("租借女友: ${realSeason.toGson()}")
    }

    private fun realSeason(sid: Int): Any {
        return ForestClients.API.seasonInfoV2(sid, Token.accessToken).execute()
    }
}