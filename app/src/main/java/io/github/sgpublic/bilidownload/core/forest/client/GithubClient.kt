package io.github.sgpublic.bilidownload.core.forest.client

import com.dtflys.forest.annotation.Address
import com.dtflys.forest.annotation.Get
import com.dtflys.forest.http.ForestRequest
import com.google.gson.JsonObject
import io.github.sgpublic.bilidownload.BuildConfig

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
    @Get("/repos/${BuildConfig.GITHUB_REPO}/releases/latest")
    fun getRelease(): ForestRequest<JsonObject>
}