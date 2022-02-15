package io.github.sgpublic.bilidownload.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.base.postValue
import io.github.sgpublic.bilidownload.data.ComicData
import io.github.sgpublic.bilidownload.data.SeasonData
import io.github.sgpublic.bilidownload.data.parcelable.DashIndexJson
import io.github.sgpublic.bilidownload.data.parcelable.EntryJson
import io.github.sgpublic.bilidownload.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.module.PlayModule
import io.github.sgpublic.bilidownload.module.SeasonModule

@Suppress("PropertyName")
class OnlinePlayerViewModel(sid: Long): BasePlayerViewModel<EpisodeData>() {
    val SEASON_DATA: MutableLiveData<SeasonData> by lazy {
        MutableLiveData<SeasonData>().also {
            getInfoBySid()
            getRecommend()
        }
    }

    fun getSeasonId() = SEASON_ID.value ?: -1
    val SEASON_ID: MutableLiveData<Long> = MutableLiveData(sid)

    fun getInfoBySid() {
        val sid = getSeasonId()
        if (sid < 0) {
            EXCEPTION.postValue(-450, "未知番剧")
            return
        }
        val module = SeasonModule(Application.APPLICATION_CONTEXT, sid, ConfigManager.ACCESS_TOKEN)
        module.getInfo(object : SeasonModule.SeasonCallback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                EXCEPTION.postValue(code, message)
            }

            override fun onResolveSeasonData(episodeData: List<EpisodeData>, seasonData: SeasonData) {
                EPISODE_LIST.postValue(episodeData)
                SEASON_DATA.postValue(seasonData)
            }
        })
    }

    fun getRecommend() {
        val sid = getSeasonId()
        if (sid < 0) {
            return
        }
        val model = SeasonModule(Application.APPLICATION_CONTEXT, sid)
        model.getRecommend(object : SeasonModule.RecommendCallback {
            override fun onResolveRecommendData(comic: List<ComicData>, season: List<SeasonData>) {
                RECOMMEND_SEASON_LIST.postValue(season)
                RECOMMEND_COMIC_LIST.postValue(comic)
            }
        })
    }

    val RECOMMEND_SEASON_LIST: MutableLiveData<List<SeasonData>> = MutableLiveData()
    fun getRecommendSeasonList() = RECOMMEND_SEASON_LIST.value ?: listOf()
    val RECOMMEND_COMIC_LIST: MutableLiveData<List<ComicData>> = MutableLiveData()
    fun getRecommendComicList() = RECOMMEND_COMIC_LIST.value ?: listOf()

    val QUALITY_LIST: MutableLiveData<Map<Int, String>> = MutableLiveData()
    fun getQualityList() = QUALITY_LIST.value ?: ConfigManager.QUALITIES
    val ENTRY_JSON: MutableLiveData<EntryJson> = MutableLiveData()
    fun getEntryJson() = ENTRY_JSON.value
    val DASH_INDEX_JSON: MutableLiveData<DashIndexJson> = MutableLiveData()
    fun getCurrentQuality() = ENTRY_JSON.value?.video_quality ?: ConfigManager.QUALITY
    fun getPlayData(data: EpisodeData) {
        val module = PlayModule(Application.APPLICATION_CONTEXT,
            ConfigManager.ACCESS_TOKEN, data.toEntryJson())
        module.getPlayUrl(object : PlayModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                EXCEPTION.postValue(code, message)
            }

            override fun onResolveAvailableQuality(qualities: Map<Int, String>) {
                QUALITY_LIST.postValue(qualities)
            }

            override fun onResolvePlayData(entry: EntryJson, index: DashIndexJson) {
                this@OnlinePlayerViewModel.DASH_INDEX_JSON.postValue(index)
                this@OnlinePlayerViewModel.ENTRY_JSON.postValue(entry)
            }
        })
    }

    class Factory(private val sid: Long) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when(modelClass) {
                OnlinePlayerViewModel::class.java -> OnlinePlayerViewModel(sid) as T
                else -> super.create(modelClass)
            }
        }
    }
}