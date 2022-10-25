package io.github.sgpublic.bilidownload.core.grpc.client

import bilibili.pgc.gateway.player.v2.PlayURLGrpc
import bilibili.pgc.gateway.player.v2.Playurl.PlayViewReply
import bilibili.pgc.gateway.player.v2.Playurl.PlayViewReq
import io.github.sgpublic.bilidownload.core.grpc.GrpcModule
import io.github.sgpublic.bilidownload.core.grpc.customBuild
import io.github.sgpublic.bilidownload.core.util.GrpcRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import okio.Closeable

/**
 *
 * @author Madray Haven
 * @date 2022/10/25 16:08
 */
class AppClient: Closeable {
    private val Channel: ManagedChannel by lazy {
        ManagedChannelBuilder.forTarget("app.bilibili.com")
            .intercept(GrpcModule.AuthInterceptor)
            .customBuild()
    }

    fun getPlayUrl(cid: Long, epid: Long, qn: Long, dash: Boolean = true): GrpcRequest<PlayViewReq, PlayViewReply> {
        PlayViewReq.newBuilder().let {
            it.cid = cid
            it.epid = epid
            it.qn = qn
            if (dash) {
                it.fnval = 976
            }
            return GrpcRequest(Channel, PlayURLGrpc.getPlayViewMethod(), it.build())
        }
    }

    override fun close() {
        Channel.shutdownNow()
    }
}