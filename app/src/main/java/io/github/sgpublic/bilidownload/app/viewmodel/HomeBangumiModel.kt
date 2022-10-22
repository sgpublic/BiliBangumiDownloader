package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dtflys.forest.Forest
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.core.forest.data.BannerResp
import io.github.sgpublic.bilidownload.core.forest.find
import io.github.sgpublic.bilidownload.core.util.ForestCallback
import io.github.sgpublic.bilidownload.core.util.ForestClients
import io.github.sgpublic.bilidownload.core.util.biliapi

/**
 *
 * @author Madray Haven
 * @date 2022/10/21 10:52
 */
class HomeBangumiModel : BaseViewModel() {
    val BannerInfo: MutableLiveData<List<BannerResp.BannerData.BannerItem.Item>> = MutableLiveData()
    fun getBannerInfo(accessToken: String) {
        ForestClients.API.banner(accessToken).biliapi(object : ForestCallback<JsonArray>() {
            override fun onFailure(code: Int, message: String?) {

            }

            override fun onResponse(data: JsonArray) {
                val banners: List<BannerResp.BannerData.BannerItem> = data.find()
                BannerInfo.postValue(banners[0].items)
            }
        }, viewModelScope)
    }
}