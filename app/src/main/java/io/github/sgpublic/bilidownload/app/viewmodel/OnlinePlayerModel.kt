package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bilibili.pgc.gateway.player.v2.Playurl.PlayViewReply
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.forest.data.SeasonRecommendResp
import io.github.sgpublic.bilidownload.core.forest.find
import io.github.sgpublic.bilidownload.core.grpc.client.AppClient
import io.github.sgpublic.bilidownload.core.util.*
import okhttp3.internal.closeQuietly
import java.util.PriorityQueue

class OnlinePlayerModel(sid: Long, var EpisodeId: Long): BasePlayerModel() {
    val SID: MutableLiveData<Long> by lazy {
        getSeasonInfo(sid)
        MutableLiveData(sid)
    }
    val SeasonData: MutableLiveData<SeasonInfoResp.SeasonInfoData> = MutableLiveData()
    val EpisodeList: HashMap<Long, SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem> = HashMap()
    fun getSeasonInfo(sid: Long) {
        ForestClients.API.seasonInfoV2(
            sid, TokenPreference.accessToken
        ).biliapi(newRequestCallback { data ->
            for (episodes in data.find<SeasonInfoResp.SeasonInfoData.Episodes>()) {
                for (item in episodes.data.episodes) {
                    EpisodeList[item.id] = item
                }
            }
            initPlayerData(data, EpisodeList[EpisodeId] ?: ArrayList(EpisodeList.values)[0])
        }, viewModelScope)
    }

    private fun initPlayerData(
        season: SeasonInfoResp.SeasonInfoData,
        episode: SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem,
    ) {
        AppClient.getPlayUrl(episode.cid!!, episode.id, 80).enqueue(object : RequestCallback<PlayViewReply>() {
            override fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override fun onResponse(data: PlayViewReply) {
                setPlayerData(data)
                SeasonData.postValue(season)
            }
        }, viewModelScope)
    }

    val SeasonRecommend: MutableLiveData<SeasonRecommendResp.SeasonRecommend> by lazy {
        getSeasonRecommend(sid)
        MutableLiveData()
    }

    fun getSeasonRecommend(sid: Long) {
        ForestClients.API.seasonRecommend(
            sid, TokenPreference.accessToken
        ).biliapi(object : RequestCallback<SeasonRecommendResp.SeasonRecommend>() {
            override fun onFailure(code: Int, message: String?) {

            }

            override fun onResponse(data: SeasonRecommendResp.SeasonRecommend) {
                SeasonRecommend.postValue(data)
            }
        }, viewModelScope)
    }

    private val AppClient: AppClient by lazy { AppClient() }

    val QualityData: HashMap<Int, String> = HashMap()
    fun setPlayerData(data: PlayViewReply) {
        for (stream in data.videoInfo.streamListList) {
            QualityData[stream.info.quality] = stream.info.newDescription
        }
        PlayerData.postValue(data)
    }
    val PlayerData: MutableLiveData<PlayViewReply> = MutableLiveData()
    fun getPlayUrl(epid: Long, cid: Long) {
        AppClient.getPlayUrl(cid, epid, FittedQuality).enqueue(object : RequestCallback<PlayViewReply>() {
            override fun onFailure(code: Int, message: String?) {

            }

            override fun onResponse(data: PlayViewReply) {

            }
        }, viewModelScope)
    }
    val FittedQuality: Int get() {
        val pq = PriorityQueue<Int> { o1, o2 ->
            o2 - o1
        }.addIf(QualityData.keys) {
            it <= BangumiPreference.quality
        }
        return pq.peek()!!.also {
            pq.clear()
        }
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