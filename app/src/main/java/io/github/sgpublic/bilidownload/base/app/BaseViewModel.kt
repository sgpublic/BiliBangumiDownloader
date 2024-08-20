package io.github.sgpublic.bilidownload.base.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.core.exsp.BangumiPreference
import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.exsp.UserPreference
import io.github.sgpublic.bilidownload.core.forest.core.BiliApiException
import io.github.sgpublic.bilidownload.core.room.dao.DownloadTaskDao
import io.github.sgpublic.bilidownload.core.util.RequestCallback
import io.github.sgpublic.exsp.ExPreference

abstract class BaseViewModel: ViewModel() {
    val Exception: MutableLiveData<ExceptionData> = MutableLiveData()
    fun getExceptionData() = Exception.value
    data class ExceptionData(var code: Int, var message: String?)

    open val Loading: MutableLiveData<Boolean> = MutableLiveData()

    protected val DownloadTaskDao: DownloadTaskDao by lazy { Application.Database.DownloadTaskDao() }

    fun <T> newRequestCallback(reply: (T) -> Unit): RequestCallback<T> {
        return object : RequestCallback<T>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: T) {
                reply.invoke(data)
            }
        }
    }

    companion object {
        @JvmStatic
        protected val TokenPreference: TokenPreference by lazy { return@lazy ExPreference.get<TokenPreference>() }
        @JvmStatic
        protected val UserPreference: UserPreference by lazy { return@lazy ExPreference.get<UserPreference>() }
        @JvmStatic
        protected val BangumiPreference: BangumiPreference by lazy { return@lazy ExPreference.get<BangumiPreference>() }
    }
}

fun MutableLiveData<BaseViewModel.ExceptionData>.postValue(
    code: Int, message: String?
) {
    postValue(BaseViewModel.ExceptionData(code, message))
}

fun MutableLiveData<BaseViewModel.ExceptionData>.postValue(e: BiliApiException) {
    postValue(BaseViewModel.ExceptionData(e.code, e.msg))
}