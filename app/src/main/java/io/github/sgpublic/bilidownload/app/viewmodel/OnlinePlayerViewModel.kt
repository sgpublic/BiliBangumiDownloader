package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.MutableLiveData
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.core.forest.ApiModule
import io.github.sgpublic.bilidownload.core.forest.data.common.SeasonEpisodeBean
import io.github.sgpublic.bilidownload.core.grpc.client.AppClient
import io.github.sgpublic.bilidownload.core.util.log
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import okhttp3.internal.closeQuietly

class OnlinePlayerViewModel(sid: Long): BaseViewModel() {
    val SID: MutableLiveData<Long> = MutableLiveData(sid)

    val AppClient: AppClient by lazy { AppClient() }

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