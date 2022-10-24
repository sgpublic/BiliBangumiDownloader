package io.github.sgpublic.bilidownload.grpc

import bilibili.pgc.gateway.player.v2.PlayURLGrpc
import bilibili.pgc.gateway.player.v2.Playurl
import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.forest.core.UrlEncodedInterceptor
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.exsp.ExPreference
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import org.junit.After
import org.junit.Test

/**
 *
 * @author Madray Haven
 * @date 2022/10/24 17:18
 */
class PlayUrlTest {
    private val Channel: ManagedChannel by lazy {
        ManagedChannelBuilder.forTarget("app.bilibili.com")
            .useTransportSecurity()
            .userAgent(UrlEncodedInterceptor.UserAgent)
            .build()
    }

    @Test
    fun getUrl() {
        val interceptor =
            MetadataUtils.newAttachHeadersInterceptor(Metadata().also {
                it.put(
                    Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER),
                    "identify_v1 " + ExPreference.get<TokenPreference>().accessToken
                )
            })
        val stub = PlayURLGrpc.newFutureStub(Channel)
            .withInterceptors(interceptor)
        val req = Playurl.PlayViewReq.newBuilder()
            .setCid(771780492)
            .setEpid(562695)
            .build()
        val view = stub.playView(req).get()
        log.debug("view: $view")
    }

    @After
    fun close() {
        Channel.shutdown()
    }
}