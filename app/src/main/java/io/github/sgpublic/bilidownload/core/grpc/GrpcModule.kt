package io.github.sgpublic.bilidownload.core.grpc

import bilibili.metadata.device.DeviceOuterClass
import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.forest.annotations.BiliSign
import io.github.sgpublic.bilidownload.core.forest.core.UrlEncodedInterceptor
import io.github.sgpublic.exsp.ExPreference
import io.grpc.*
import io.grpc.cronet.CronetChannelBuilder
import io.grpc.stub.MetadataUtils
import okio.Closeable
import java.util.concurrent.TimeUnit
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 *
 * @author Madray Haven
 * @date 2022/10/25 13:52
 */
object GrpcModule {
    val Compress: CompressorRegistry by lazy {
        CompressorRegistry.newEmptyInstance().also {
            it.register(Codec.Identity.NONE)
        }
    }

    val Decompress: DecompressorRegistry by lazy {
        DecompressorRegistry.emptyInstance()
            .with(Codec.Identity.NONE, true)
    }

    val DeviceBin: DeviceOuterClass.Device by lazy {
        DeviceOuterClass.Device.newBuilder()
            .setBuild(BiliSign.Build)
            .setPlatform(BiliSign.Android)
            .setMobiApp(BiliSign.Android)
            .build()
    }

    val AuthInterceptor: ClientInterceptor by lazy {
        MetadataUtils.newAttachHeadersInterceptor(Metadata().also {
            it.put(
                Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER),
                "identify_v1 " + ExPreference.get<TokenPreference>().accessToken,
            )
            it.put(
                Metadata.Key.of("x-bili-device-bin", Metadata.BINARY_BYTE_MARSHALLER),
                DeviceBin.toByteArray(),
            )
        })
    }
}

fun ManagedChannelBuilder<*>.customBuild(): ManagedChannel {
    return useTransportSecurity()
        .compressorRegistry(GrpcModule.Compress)
        .decompressorRegistry(GrpcModule.Decompress)
        .userAgent(UrlEncodedInterceptor.UserAgent)
        .enableFullStreamDecompression()
        .build()
}

fun CronetChannelBuilder.customBuild(): ManagedChannel {
    return compressorRegistry(GrpcModule.Compress)
        .decompressorRegistry(GrpcModule.Decompress)
        .userAgent(UrlEncodedInterceptor.UserAgent)
        .enableFullStreamDecompression()
        .build()
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ManagedChannel.use(unit: (ManagedChannel) -> T): T {
    contract {
        callsInPlace(unit, InvocationKind.EXACTLY_ONCE)
    }
    val closeable: Closeable = CloseableManagedChannel(this)
    return closeable.use {
        unit.invoke(this)
    }
}

class CloseableManagedChannel(
    private val origin: ManagedChannel
): Closeable, ManagedChannel() {
    override fun close() {
        origin.shutdown()
        origin.awaitTermination(200, TimeUnit.MILLISECONDS)
    }

    override fun <RequestT : Any?, ResponseT : Any?> newCall(
        methodDescriptor: MethodDescriptor<RequestT, ResponseT>?,
        callOptions: CallOptions?
    ): ClientCall<RequestT, ResponseT> = origin.newCall(methodDescriptor, callOptions)

    override fun authority(): String = origin.authority()

    override fun shutdown(): ManagedChannel = origin.shutdown()

    override fun isShutdown(): Boolean = origin.isShutdown

    override fun isTerminated(): Boolean = origin.isTerminated

    override fun shutdownNow(): ManagedChannel = origin.shutdownNow()

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean =
        origin.awaitTermination(timeout, unit)
}