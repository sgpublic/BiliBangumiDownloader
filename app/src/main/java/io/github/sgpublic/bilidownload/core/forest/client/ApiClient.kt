package io.github.sgpublic.bilidownload.core.forest.client

import com.dtflys.forest.annotation.Address
import com.dtflys.forest.annotation.Get
import com.dtflys.forest.annotation.Query
import com.dtflys.forest.http.ForestRequest
import io.github.sgpublic.bilidownload.core.forest.annotations.BiliSearchReferer
import io.github.sgpublic.bilidownload.core.forest.annotations.BiliSign
import io.github.sgpublic.bilidownload.core.forest.data.*

@Address(
    scheme = "https",
    host = "api.bilibili.com"
)
interface ApiClient {
    /**
     * 追番列表
     * @param status 状态，1：想看，2：在看，3：看过
     * @param pageIndex 页数
     * @param accessToken access_token
     * @param ps 每页记录数量
     */
    @BiliSign
    @Get("/pgc/app/follow/v2/bangumi")
    fun follow(
        @Query("status") status: Int,
        @Query("pn") pageIndex: Int,
        @Query("access_key") accessToken: String,
        @Query("ps") ps: Int = 18,
    ): ForestRequest<FollowsResp>

    @Get("/x/web-interface/search/type")
    fun searchResult(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("search_type") searchType: String = "media_bangumi",
    )

    @BiliSearchReferer
    @Get("https://s.search.bilibili.com/main/suggest")
    fun searchSuggest(
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

    @Deprecated(
        message = "Use v2 instead.",
        replaceWith = ReplaceWith("seasonInfoV2(seasonId, accessToken)")
    )
    @BiliSign
    @Get("/pgc/view/app/season")
    fun seasonInfo(
        @Query("season_id") seasonId: Long,
        @Query("access_key") accessToken: String,
    ): ForestRequest<String>

    /**
     * 番剧信息 v2 接口
     * @param seasonId sid
     * @param accessToken access_token
     */
    @BiliSign
    @Get("/pgc/view/v2/app/season")
    fun seasonInfoV2(
        @Query("season_id") seasonId: Long,
        @Query("access_key") accessToken: String,
    ): ForestRequest<SeasonInfoResp>

    /**
     * 番剧推荐接口
     * @param seasonId sid
     * @param accessToken access_token
     */
    @BiliSign
    @Get("/pgc/season/app/related/recommend")
    fun seasonRecommend(
        @Query("season_id") seasonId: Long,
        @Query("access_key") accessToken: String,
    ): ForestRequest<SeasonRecommendResp>

    /**
     * 首页 banner
     * @param accessToken access_token
     * @param tabId 固定为 8
     */
    @BiliSign
    @Get("/pgc/page/")
    fun banner(
        @Query("access_key") accessToken: String,
        @Query("tab_id") tabId: Int = 8,
    ): ForestRequest<BannerResp>

    /**
     * 首页推荐数据
     * @param accessToken access_token
     */
    @BiliSign
    @Get("/pgc/page/bangumi")
    fun bangumi(
        @Query("access_key") accessToken: String,
        @Query("cursor") cursor: Int,
        @Query("is_refresh") isRefresh: Int,
    ): ForestRequest<BangumiPageResp>
}