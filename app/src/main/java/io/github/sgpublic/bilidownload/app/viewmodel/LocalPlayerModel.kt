package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity

class LocalPlayerModel(sid: Long, epid: Long): BasePlayerModel() {
    val EpisodeList: LiveData<List<DownloadTaskEntity>> by lazy {
        DownloadTaskDao.observeBySidWhereFinished(sid)
    }
    val EpisodeId: MutableLiveData<Long> = MutableLiveData(epid)
}