package io.github.sgpublic.bilidownload.forest

import io.github.sgpublic.bilidownload.core.forest.data.GetKeyResp
import io.github.sgpublic.bilidownload.core.util.ForestClients
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.toGson
import org.junit.Test

class LoginKeyTest {
    @Test
    fun getKey() {
        val execute = ForestClients.PASSPORT.pubKey().execute(GetKeyResp::class.java)
        log.debug(execute.toGson())
    }
}