package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.forest.data.SeasonRecommendResp
import io.github.sgpublic.bilidownload.core.grpc.client.AppClient
import io.github.sgpublic.bilidownload.core.util.ForestClients
import io.github.sgpublic.bilidownload.core.util.RequestCallback
import io.github.sgpublic.bilidownload.core.util.biliapi
import io.github.sgpublic.bilidownload.core.util.log
import okhttp3.internal.closeQuietly

class OnlinePlayerModel(sid: Long): BasePlayerModel() {
    val SID: MutableLiveData<Long> by lazy {
        getSeasonInfo(sid)
        MutableLiveData(sid)
    }
    val SeasonData: MutableLiveData<SeasonInfoResp.SeasonInfoData> = MutableLiveData()

    fun getSeasonInfo(sid: Long) {
        ForestClients.API.seasonInfoV2(sid, TokenPreference.accessToken).biliapi(object : RequestCallback<SeasonInfoResp.SeasonInfoData>() {
            override fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override fun onResponse(data: SeasonInfoResp.SeasonInfoData) {
                SeasonData.postValue(data)
            }
        }, viewModelScope)
    }

    val SeasonRecommend: MutableLiveData<SeasonRecommendResp.SeasonRecommend> by lazy {
        getSeasonRecommend(sid)
        MutableLiveData()
    }

    fun getSeasonRecommend(sid: Long) {
        ForestClients.API.seasonRecommend(sid, TokenPreference.accessToken).biliapi(object : RequestCallback<SeasonRecommendResp.SeasonRecommend>() {
            override fun onFailure(code: Int, message: String?) {

            }

            override fun onResponse(data: SeasonRecommendResp.SeasonRecommend) {
                SeasonRecommend.postValue(data)
            }
        }, viewModelScope)
    }

    private val AppClient: AppClient by lazy { AppClient() }

    fun getPlayUrl(epid: Long, cid: Long) {

    }

    override fun onCleared() {
        super.onCleared()
        try {
            AppClient.closeQuietly()
        } catch (e: Exception) {
            log.warn("Channel shutdown failed.", e)
        }
    }
}