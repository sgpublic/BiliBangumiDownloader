package io.github.sgpublic.bilidownload.core.forest.client

import com.dtflys.forest.annotation.Address
import com.dtflys.forest.annotation.Get
import com.dtflys.forest.http.ForestRequest
import com.google.gson.JsonObject

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 14:33
 */
@Address(
    scheme = "https",
    host = "api.github.com"
)
interface GithubClient {
    @Get("/repos/sgpublic/BiliBangumiDownloader/releases/latest")
    fun getRelease(): ForestRequest<JsonObject>
}