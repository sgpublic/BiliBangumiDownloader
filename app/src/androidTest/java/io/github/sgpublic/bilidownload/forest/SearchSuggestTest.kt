package io.github.sgpublic.bilidownload.forest

import io.github.sgpublic.bilidownload.core.util.ForestClients
import org.junit.Test

class SearchSuggestTest {
    @Test
    fun getSuggest() {
        ForestClients.API.getSearchSuggestRequest("超炮").execute()
    }
}