package io.github.sgpublic.bilidownload.core.forest.client

import com.dtflys.forest.annotation.Address
import com.dtflys.forest.annotation.Get
import com.dtflys.forest.annotation.Query
import com.dtflys.forest.http.ForestRequest
import io.github.sgpublic.bilidownload.core.forest.annotations.BiliSearchReferer
import io.github.sgpublic.bilidownload.core.forest.annotations.BiliSign
import io.github.sgpublic.bilidownload.core.forest.data.HotwordResp
import io.github.sgpublic.bilidownload.core.forest.data.UserInfoResp

@Address(
    scheme = "https",
    host = "app.bilibili.com"
)
interface AppClient {
    @BiliSign
    @Get("/x/v2/account/myinfo")
    fun getUserInfoRequest(
        @Query("access_key") accessToken: String,
    ): ForestRequest<UserInfoResp>

    @BiliSearchReferer
    @Get("/x/v2/search/trending/ranking")
    fun getHotWordRequest(): ForestRequest<HotwordResp>
}