package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.forest.data.BangumiPageResp
import io.github.sgpublic.bilidownload.core.forest.data.BannerResp
import io.github.sgpublic.bilidownload.core.forest.find
import io.github.sgpublic.bilidownload.core.util.*

/**
 *
 * @author Madray Haven
 * @date 2022/10/21 10:52
 */
class HomeModel : BaseViewModel() {
    val BannerInfo: MutableLiveData<List<BannerResp.BannerData.BannerItem.Item>> by lazy {
        getBannerInfo()
        MutableLiveData()
    }
    fun getBannerInfo() {
        Loading.postValue(true)
        ForestClients.Api.banner(TokenPreference.accessToken).biliapi(object : RequestCallback<BannerResp.BannerData>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: BannerResp.BannerData) {
                val banners: List<BannerResp.BannerData.BannerItem> = data.find()
                BannerInfo.postValue(banners[0].items)
            }
        }, viewModelScope)
    }

    val BangumiItems: MutableLiveData<Pair<Boolean, ArrayList<BangumiPageResp.BangumiPageData.AbstractFeed<*>>>> by lazy {
        getBangumiItems(true)
        MutableLiveData()
    }
    private var cursor = 0
    fun getBangumiItems(isRefresh: Boolean) {
        if (isRefresh) {
            Loading.postValue(true)
        }
        ForestClients.Api.bangumi(
            TokenPreference.accessToken, cursor, isRefresh.take(1, 0)
        ).biliapi(object : RequestCallback<BangumiPageResp.BangumiPageData>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: BangumiPageResp.BangumiPageData) {
                cursor = data.nextCursor

                if (isRefresh) {
                    BangumiItems.value?.second?.clear()
                }
                val list = BangumiItems.value?.second ?: ArrayList()

                val doubleFeed: List<BangumiPageResp.BangumiPageData.DoubleFeed> = data.find()
                val tmp: ArrayList<BangumiPageResp.BangumiPageData.DoubleFeed.Item> = ArrayList()
                for (feed in doubleFeed) {
                    for (item in feed.items) {
                        if (item.type != "PGC") {
                            continue
                        }
                        tmp.add(item)
                    }
                }
                list.addAll(tmp.advSub(2).map {
                    BangumiPageResp.BangumiPageData.AbstractFeed(it, 1)
                })

                data.find<BangumiPageResp.BangumiPageData.FallFeed>()
                    .takeIf { it.isNotEmpty() }
                    ?.get(0)?.items?.takeIf { it.isNotEmpty() }
                    ?.get(0)?.let {
                        list.add(BangumiPageResp.BangumiPageData.AbstractFeed(it, 2))
                    }

                BangumiItems.postValue((data.hasNext == 1) to list)
            }
        }, viewModelScope)
    }
}