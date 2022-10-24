package io.github.sgpublic.bilidownload.core.util

import com.dtflys.forest.Forest
import com.dtflys.forest.exceptions.ForestNetworkException
import com.dtflys.forest.http.ForestRequest
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.forest.CommonResp
import io.github.sgpublic.bilidownload.core.forest.client.ApiClient
import io.github.sgpublic.bilidownload.core.forest.client.AppClient
import io.github.sgpublic.bilidownload.core.forest.client.PassportClient
import io.github.sgpublic.bilidownload.core.forest.core.BiliApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ForestClients {
    val PASSPORT: PassportClient by lazy { Forest.client(PassportClient::class.java) }
    val APP: AppClient by lazy { Forest.client(AppClient::class.java) }
    val API: ApiClient by lazy { Forest.client(ApiClient::class.java) }
}

/** ForestRequest 封装异步请求 */
inline fun <reified T: CommonResp<Data>, Data> ForestRequest<T>.biliapi(callback: ForestCallback<Data>, viewModelScope: CoroutineScope) {
    enqueue(object : ForestCallback<T>() {
        override fun onFailure(code: Int, message: String?) {
            callback.onFailure(code, message)
        }

        override fun onResponse(data: T) {
            callback.onResponse(data.data)
        }
    }, viewModelScope)
}

inline fun <reified T> ForestRequest<T>.enqueue(callback: ForestCallback<T>, viewModelScope: CoroutineScope) {
    viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val data = try {
                execute(T::class.java)
            } catch (ex: Exception) {
                log.error("资源请求出错", ex)
                when (ex) {
                    is ForestNetworkException -> callback.onFailure(
                        ForestCallback.CODE_NETWORK_ERROR,
                        Application.getString(R.string.error_network)
                    )
                    is BiliApiException -> callback.onFailure(ex)
                    else -> callback.onFailure(
                        ForestCallback.CODE_NETWORK_UNKNOWN,
                        ex.requiredMessage()
                    )
                }
                return@withContext
            }
            try {
                callback.onResponse(data)
            } catch (e: Exception) {
                log.error("资源处理出错", e)
                callback.onFailure(ForestCallback.CODE_RESOURCE, e.requiredMessage())
            }
        }
    }
}

fun Exception.requiredMessage(): String {
    return message ?: "Unknown error"
}

abstract class ForestCallback<Data> {
    abstract fun onFailure(code: Int, message: String?)
    abstract fun onResponse(data: Data)

    companion object {
        const val CODE_NETWORK_ERROR = -101
        const val CODE_NETWORK_UNKNOWN = -102
        const val CODE_RESOURCE = -103
        const val CODE_ACCOUNT_VALIDATE = -104
    }
}

fun <T> ForestCallback<T>.onFailure(ex: BiliApiException) {
    onFailure(ex.code, ex.requiredMessage())
}