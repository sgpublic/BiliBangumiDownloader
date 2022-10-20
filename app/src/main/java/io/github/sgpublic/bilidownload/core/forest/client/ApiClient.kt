package io.github.sgpublic.bilidownload.core.forest.client

import com.dtflys.forest.annotation.Address
import com.dtflys.forest.annotation.Get
import com.dtflys.forest.annotation.Query
import com.dtflys.forest.http.ForestRequest
import io.github.sgpublic.bilidownload.app.activity.Search
import io.github.sgpublic.bilidownload.core.forest.annotations.BiliSearchReferer
import io.github.sgpublic.bilidownload.core.forest.annotations.BiliSign
import io.github.sgpublic.bilidownload.core.forest.data.SearchSuggestResp

@Address(
    scheme = "https",
    host = "api.bilibili.com"
)
interface ApiClient {
    @BiliSign
    @Get("/pgc/app/follow/v2/bangumi")
    fun getFollowsRequest(
        @Query("pn") pageIndex: Int,
        @Query("status") status: Int,
        @Query("access_key") accessToken: String,
        @Query("ps") ps: Int = 18,
    )

    @Get("/x/web-interface/search/type")
    fun getSearchResultRequest(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("search_type") searchType: String = "media_bangumi",
    )

    @BiliSearchReferer
    @Get("https://s.search.bilibili.com/main/suggest")
    fun getSearchSuggestRequest(
        @Query("term") keyword: String,
        @Query("main_ver") mainVer: String = "v1",
        @Query("special_acc_num") specialAccNum: Int = 1,
        @Query("topic_acc_num") topicAccNum: Int = 1,
        @Query("upuser_acc_num") upuserAccNum: Int = 3,
        @Query("tag_num") tagNum: Int = 10,
        @Query("special_num") specialNum: Int = 10,
        @Query("bangumi_num") bangumiNum: Int = 10,
        @Query("upuser_num") upuserNum: Int = 10,
    ): ForestRequest<SearchSuggestResp>

    @BiliSign
    @Get("/pgc/view/app/season")
    fun getSeasonInfoAppRequest(
        @Query("access_key") accessToken: String,
        @Query("season_id") seasonId: Int,
    )
}