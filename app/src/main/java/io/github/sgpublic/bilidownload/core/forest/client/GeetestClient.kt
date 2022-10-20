package io.github.sgpublic.bilidownload.core.forest.client

import com.dtflys.forest.annotation.Address
import com.dtflys.forest.annotation.Get
import com.dtflys.forest.annotation.Header
import com.dtflys.forest.annotation.Query
import com.dtflys.forest.http.ForestRequest
import com.google.gson.JsonObject
import io.github.sgpublic.bilidownload.core.forest.annotations.GeetestApi

/**
 *
 * @author Madray Haven
 * @date 2022/10/19 11:35
 */
@Address(
    scheme = "https",
    host = "api.geetest.com"
)
@GeetestApi
interface GeetestClient {
    @Get("/ajax.php")
    fun ajax(
        @Query("gt") gt: String,
        @Query("challenge") challenge: String,
    ): ForestRequest<JsonObject>

    @Get("/get.php")
    fun get(
        @Query("gt") gt: String,
        @Query("challenge") challenge: String,
    ): ForestRequest<String>
}