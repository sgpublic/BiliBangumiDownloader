package io.github.sgpublic.bilidownload.core.grpc.client

import bilibili.pgc.gateway.player.v2.PlayURLGrpc
import bilibili.pgc.gateway.player.v2.Playurl.PlayViewReply
import bilibili.pgc.gateway.player.v2.Playurl.PlayViewReq
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.core.grpc.GrpcModule
import io.github.sgpublic.bilidownload.core.grpc.customBuild
import io.github.sgpublic.bilidownload.core.util.GrpcRequest
import io.github.sgpublic.bilidownload.core.util.log
import io.grpc.ManagedChannel
import io.grpc.cronet.CronetChannelBuilder
import okio.Closeable
import org.chromium.net.CronetEngine
import org.chromium.net.impl.JavaCronetProvider

/**
 *
 * @author Madray Haven
 * @date 2022/10/25 16:08
 */
class AppClient : Closeable {
    private val Channel: ManagedChannel by lazy {
        val engine = try {
            CronetEngine.Builder(Application.ApplicationContext)
        } catch (e: Exception) {
            log.warn("CronetEngine create failed", e)
            JavaCronetProvider(Application.ApplicationContext).createBuilder()
        }.build()
        CronetChannelBuilder.forAddress("app.bilibili.com", 443, engine)
            .intercept(GrpcModule.AuthInterceptor)
            .customBuild()
    }

    fun getPlayUrl(cid: Long, epid: Long, qn: Int, dash: Boolean = true): GrpcRequest<PlayViewReq, PlayViewReply> {
        PlayViewReq.newBuilder().let {
            it.cid = cid
            it.epid = epid
            it.qn = qn.toLong()
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