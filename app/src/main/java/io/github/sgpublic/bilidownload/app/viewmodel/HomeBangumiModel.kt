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
class HomeBangumiModel : BaseViewModel() {
    val BannerInfo: MutableLiveData<List<BannerResp.BannerData.BannerItem.Item>> = MutableLiveData()
    fun getBannerInfo(accessToken: String) {
        ForestClients.API.banner(accessToken).biliapi(object : ForestCallback<BannerResp.BannerData>() {
            override fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override fun onResponse(data: BannerResp.BannerData) {
                val banners: List<BannerResp.BannerData.BannerItem> = data.find()
                BannerInfo.postValue(banners[0].items)
            }
        }, viewModelScope)
    }

    val BangumiItems: MutableLiveData<Pair<Boolean, ArrayList<BangumiPageResp.BangumiPageData.AbstractFeed<*>>>> = MutableLiveData()
    private var cursor = 0
    fun getBangumiItems(accessToken: String, isRefresh: Boolean) {
        ForestClients.API.bangumi(
            accessToken, cursor, isRefresh.take(1, 0)
        ).biliapi(object : ForestCallback<BangumiPageResp.BangumiPageData>() {
            override fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override fun onResponse(data: BangumiPageResp.BangumiPageData) {
                cursor = data.nextCursor

                val list = BangumiItems.value?.second.takeIf { isRefresh } ?: ArrayList()

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

                val fallFeed: List<BangumiPageResp.BangumiPageData.FallFeed> = data.find()
                list.add(BangumiPageResp.BangumiPageData.AbstractFeed(fallFeed[0].items[0], 2))

                BangumiItems.postValue((data.hasNext == 1) to list)
            }
        }, viewModelScope)
    }
}