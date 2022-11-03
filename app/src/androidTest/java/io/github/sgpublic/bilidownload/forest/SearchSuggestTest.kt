package io.github.sgpublic.bilidownload.forest

import io.github.sgpublic.bilidownload.core.util.ForestClients
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.toGson
import org.junit.Test

class SearchSuggestTest {
    @Test
    fun getSuggest() {
        val execute = ForestClients.API.searchSuggest("超炮").execute()
        log.debug(execute.toGson())
    }
}