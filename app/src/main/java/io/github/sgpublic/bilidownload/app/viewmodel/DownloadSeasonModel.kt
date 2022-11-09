package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 11:07
 */
class DownloadSeasonModel: ViewModel() {
    val SeasonTasks: LiveData<List<DownloadTaskEntity>> by lazy {
        Application.Database.DownloadTaskDao().observeAll()
    }
}