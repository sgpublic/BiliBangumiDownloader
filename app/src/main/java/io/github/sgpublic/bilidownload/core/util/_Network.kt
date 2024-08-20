package io.github.sgpublic.bilidownload.core.util

import com.dtflys.forest.Forest
import com.dtflys.forest.exceptions.ForestNetworkException
import com.dtflys.forest.http.ForestRequest
import com.google.protobuf.GeneratedMessageLite
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.forest.CommonResp
import io.github.sgpublic.bilidownload.core.forest.client.ApiClient
import io.github.sgpublic.bilidownload.core.forest.client.AppClient
import io.github.sgpublic.bilidownload.core.forest.client.GithubClient
import io.github.sgpublic.bilidownload.core.forest.client.PassportClient
import io.github.sgpublic.bilidownload.core.forest.core.BiliApiException
import io.grpc.*
import io.grpc.stub.ClientCalls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Closeable
import java.util.concurrent.TimeUnit

object ForestClients {
    val Passport: PassportClient by lazy { Forest.client(PassportClient::class.java) }
    val App: AppClient by lazy { Forest.client(AppClient::class.java) }
    val Api: ApiClient by lazy { Forest.client(ApiClient::class.java) }
    val Github: GithubClient by lazy { Forest.client(GithubClient::class.java) }
}

/** ForestRequest 封装异步请求 */
inline fun <reified T: CommonResp<Data>, Data> ForestRequest<T>.biliapi(callback: RequestCallback<Data>, viewModelScope: CoroutineScope) {
    enqueue(object : RequestCallback<T>() {
        override suspend fun onFailure(code: Int, message: String?) {
            callback.onFailure(code, message)
        }

        override suspend fun onResponse(data: T) {
            callback.onResponse(data.data)
        }
    }, viewModelScope)
}

inline fun <reified T> ForestRequest<T>.enqueue(callback: RequestCallback<T>, viewModelScope: CoroutineScope) {
    viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val data = try {
                execute(T::class.java)
            } catch (ex: Exception) {
                log.error("资源请求出错", ex)
                when (ex) {
                    is ForestNetworkException -> callback.onFailure(
                        RequestCallback.CODE_NETWORK_ERROR,
                        Application.getString(R.string.error_network)
                    )
                    is BiliApiException -> callback.onFailure(ex)
                    else -> callback.onFailure(
                        RequestCallback.CODE_NETWORK_UNKNOWN,
                        ex.requiredMessage()
                    )
                }
                return@withContext
            }
//            try {
                callback.onResponse(data)
//            } catch (e: Exception) {
//                log.error("资源处理出错", e)
//                callback.onFailure(RequestCallback.CODE_RESOURCE, e.requiredMessage())
//            }
        }
    }
}

fun Exception?.requiredMessage(): String {
    return this?.message ?: "Unknown error"
}

abstract class RequestCallback<Data> {
    abstract suspend fun onFailure(code: Int, message: String?)
    abstract suspend fun onResponse(data: Data)

    companion object {
        const val CODE_NETWORK_ERROR = -1001
        const val CODE_NETWORK_UNKNOWN = -1002
        const val CODE_RESOURCE = -1003
        const val CODE_ACCOUNT_CAPTCHA = -1004
        const val CODE_GRPC = -1005
        const val CODE_PLAYER_QUALITY = -1011
    }
}

suspend fun <T> RequestCallback<T>.onFailure(ex: BiliApiException) {
    onFailure(ex.code, ex.requiredMessage())
}

class GrpcRequest<ReqT: GeneratedMessageLite<ReqT, *>,
        ReplyT: GeneratedMessageLite<ReplyT, *>>(
    private val channel: Channel,
    private val method: MethodDescriptor<ReqT, ReplyT>,
    private val req: ReqT,
) {
    fun enqueue(callback: RequestCallback<ReplyT>, viewModelScope: CoroutineScope) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = try {
                    execute()
                } catch (e: Exception) {
                    val case = e.cause ?: return@withContext
                    val status = when (case) {
                        is StatusRuntimeException -> case.status to case.trailers
                        is StatusException -> case.status to case.trailers
                        else -> Status.UNKNOWN to null
                    }
                    // TODO 解析错误信息
                    return@withContext
                }
                callback.onResponse(data)
            }
        }
    }

    fun execute(): ReplyT {
        return ClientCalls.blockingUnaryCall(channel, method, CallOptions.DEFAULT, req)
    }
}

class LazyChannel<T: ManagedChannel>(initializer: () -> T): Lazy<T>, ManagedChannel(), Closeable {
    private val channel: Lazy<T> = lazy(initializer)

    override val value: T by channel

    override fun isInitialized() = channel.isInitialized()

    override fun <RequestT : Any?, ResponseT : Any?> newCall(
        methodDescriptor: MethodDescriptor<RequestT, ResponseT>?,
        callOptions: CallOptions?
    ): ClientCall<RequestT, ResponseT> {
        return value.newCall(methodDescriptor, callOptions)
    }

    override fun authority() = value.authority()

    override fun shutdown(): LazyChannel<T> {
        if (isInitialized()) {
            value.shutdown()
        }
        return this
    }

    override fun isShutdown(): Boolean {
        return if (!isInitialized()) {
            true
        } else {
            value.isShutdown
        }
    }

    override fun isTerminated() = value.isTerminated

    override fun shutdownNow(): ManagedChannel {
        if (isInitialized()) {
            value.shutdown()
        }
        return this
    }

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
        return value.awaitTermination(timeout, unit)
    }

    override fun getState(requestConnection: Boolean): ConnectivityState {
        return value.getState(requestConnection)
    }

    override fun notifyWhenStateChanged(source: ConnectivityState?, callback: Runnable?) {
        return value.notifyWhenStateChanged(source, callback)
    }

    override fun resetConnectBackoff() {
        return value.resetConnectBackoff()
    }

    override fun enterIdle() {
        return value.enterIdle()
    }

    override fun close() {
        shutdown()
    }
}