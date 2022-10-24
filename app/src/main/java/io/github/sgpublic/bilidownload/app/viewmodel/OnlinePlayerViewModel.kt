package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.MutableLiveData
import bilibili.pgc.gateway.player.v2.PlayURLGrpc
import bilibili.pgc.gateway.player.v2.Playurl.PlayViewReq
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.forest.data.common.AidCidBean
import io.github.sgpublic.bilidownload.core.forest.data.common.SeasonEpisodeBean
import io.github.sgpublic.bilidownload.core.util.ForestCallback
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.requiredMessage
import io.grpc.Channel
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

class OnlinePlayerViewModel: BaseViewModel() {
    val SID: MutableLiveData<Int> = MutableLiveData()

    private val Channel: ManagedChannel = ManagedChannelBuilder
        .forTarget("app.bilibili.com")
        .useTransportSecurity()
        .build()

    fun getPlayUrl(bean: SeasonEpisodeBean) {

    }

    override fun onCleared() {
        super.onCleared()
        try {
            Channel.shutdown()
        } catch (e: Exception) {
            log.warn("Channel shutdown failed.", e)
        }
    }
}