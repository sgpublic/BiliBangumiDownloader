package io.github.sgpublic.bilidownload.forest

import io.github.sgpublic.bilidownload.core.forest.data.GetKeyResp
import io.github.sgpublic.bilidownload.core.util.ForestClients
import org.junit.Test

class LoginKeyTest {
    @Test
    fun getKey() {
        ForestClients.PASSPORT.pubKey().execute(GetKeyResp::class.java)
    }
}