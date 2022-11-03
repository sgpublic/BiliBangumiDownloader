package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bilibili.pgc.gateway.player.v2.Playurl.PlayViewReply
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.forest.data.SeasonRecommendResp
import io.github.sgpublic.bilidownload.core.forest.find
import io.github.sgpublic.bilidownload.core.grpc.client.AppClient
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity
import io.github.sgpublic.bilidownload.core.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.closeQuietly
import java.util.*

class OnlinePlayerModel(sid: Long, epid: Long): BasePlayerModel() {
    val SID: MutableLiveData<Long> by lazy {
        getSeasonInfo(sid)
        MutableLiveData(sid)
    }
    val EpisodeId: MutableLiveData<Pair<Long, Long>> = MutableLiveData(epid to -1)
    val SeasonData: MutableLiveData<SeasonInfoResp.SeasonInfoData> = MutableLiveData()
    val EpisodeList: HashMap<Long, SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem> = LinkedHashMap()
    fun getSeasonInfo(sid: Long) {
        ForestClients.API.seasonInfoV2(
            sid, TokenPreference.accessToken
        ).biliapi(newRequestCallback { data ->
            for (episodes in data.find<SeasonInfoResp.SeasonInfoData.Episodes>()) {
                for (item in episodes.data.episodes) {
                    EpisodeList[item.id] = item
                }
            }
            SeasonData.postValue(data)
            (EpisodeList[EpisodeId.value ?: -1] ?: ArrayList(EpisodeList.values)[0]).let { episode ->
                getPlayUrl(episode.id, episode.cid!!)
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

    private var onResolvePlayData: (PlayViewReply) -> Unit = { }
    fun setOnResolvePlayDataListener(onResolvePlayData: (PlayViewReply) -> Unit = { }) {
        this.onResolvePlayData = onResolvePlayData
    }
    val QualityData: HashMap<Int, String> = LinkedHashMap()
    val QualityDesc: MutableLiveData<String> = MutableLiveData()
    fun getPlayUrl(epid: Long, cid: Long) {
        log.info("getPlayUrl(epid: $epid, cid: $cid)")
        EpisodeId.postValue(epid to cid)
        AppClient.getPlayUrl(cid, epid, FittedQuality).enqueue(object : RequestCallback<PlayViewReply>() {
            override fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override fun onResponse(data: PlayViewReply) {
                for (stream in data.videoInfo.streamListList) {
                    QualityData[stream.info.quality] = stream.info.newDescription
                }
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        onResolvePlayData.invoke(data)
                    }
                }
            }
        }, viewModelScope)
    }

    val FittedQuality: Int get() {
        val pq = PriorityQueue<Int> { o1, o2 ->
            o2 - o1
        }.addIf(QualityData.keys) {
            it <= BangumiPreference.quality
        }
        return pq.peek()?.also {
            pq.clear()
        } ?: 80
    }

    val DownloadTasks: LiveData<List<DownloadTaskEntity>> by lazy {
        Application.Database.DownloadTaskDao().observeBySid(sid)
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