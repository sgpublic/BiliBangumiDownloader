package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.LiveData
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 11:07
 */
class DownloadEpisodeModel(sid: Long): BaseViewModel() {
    val EpisodeTasks: LiveData<List<DownloadTaskEntity>> by lazy {
        Application.Database.DownloadTaskDao().observeBySid(sid)
    }

    fun saveTask(taskEntity: DownloadTaskEntity) {
        DownloadTaskDao.set(taskEntity)
    }
}