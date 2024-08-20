package io.github.sgpublic.bilidownload.app.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.forest.data.LoginResp
import io.github.sgpublic.bilidownload.core.forest.data.QrcodePollResp
import io.github.sgpublic.bilidownload.core.forest.data.QrcodeResp.QrcodeData
import io.github.sgpublic.bilidownload.core.forest.data.UserInfoResp
import io.github.sgpublic.bilidownload.core.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginQrcodeModel: BaseViewModel() {
    val LoginData: MutableLiveData<LoginResp.LoginData> = MutableLiveData()
    val UserInfo: MutableLiveData<UserInfoResp.UserInfo> = MutableLiveData()

    val Qrcode: MutableLiveData<Bitmap> = MutableLiveData()
    val QrcodeState: MutableLiveData<QrcodeStateEnum> = MutableLiveData()
    private var qrcodeKey: String? = null

    fun getQrcode(width: Int, height: Int) {
        ForestClients.Passport.qrcodeTv().biliapi(object : RequestCallback<QrcodeData>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: QrcodeData) {
                qrcodeKey = data.authCode
                Qrcode.postValue(data.url.createQRCodeBitmap(width, height))
                startQrcodeConfirm()
            }
        }, viewModelScope)
    }

    private var QrcodeConfirmJob: Job? = null
    private fun startQrcodeConfirm() {
        QrcodeConfirmJob?.cancel()
        QrcodeConfirmJob = viewModelScope.launch(Dispatchers.IO) {
            delay(5000)
            while (true) {
                delay(2000)
                val state = checkQrcodeState()
                QrcodeState.postValue(state)
                if (state == QrcodeStateEnum.Success || state == QrcodeStateEnum.Expired || state == QrcodeStateEnum.Error) {
                    break
                }
            }
        }
    }

    private suspend fun checkQrcodeState(): QrcodeStateEnum {
        val qrcodeKey = qrcodeKey
        if (qrcodeKey == null) {
            return QrcodeStateEnum.Error
        }
        val resultChannel = Channel<QrcodeStateEnum>()
        ForestClients.Passport.qrcodeTvPoll(
            authCode = qrcodeKey
        ).biliapi(object : RequestCallback<LoginResp.LoginData>() {
            override suspend fun onFailure(code: Int, message: String?) {
                when (code) {
                    86038 -> resultChannel.send(QrcodeStateEnum.Expired)
                    86039 -> resultChannel.send(QrcodeStateEnum.Waiting)
                    86090 -> resultChannel.send(QrcodeStateEnum.Scanned)
                    else -> resultChannel.send(QrcodeStateEnum.Error)
                }
            }
            override suspend fun onResponse(data: LoginResp.LoginData) {
                resultChannel.send(QrcodeStateEnum.Success)
                LoginData.postValue(data)
            }
        }, viewModelScope)
        return resultChannel.receive()
    }

    enum class QrcodeStateEnum {
        Success,
        Error,
        Waiting,
        Scanned,
        Expired,
    }


    fun accessToken(code: String) {
        ForestClients.Passport.accessToken(code).biliapi(object : RequestCallback<LoginResp.LoginData>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: LoginResp.LoginData) {
                LoginData.postValue(data)
            }
        }, viewModelScope)
    }

    fun getUserInfo(accessToken: String) {
        ForestClients.App.getUserInfoRequest(accessToken).biliapi(object : RequestCallback<UserInfoResp.UserInfo>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: UserInfoResp.UserInfo) {
                UserInfo.postValue(data)
            }
        }, viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        QrcodeConfirmJob?.cancel()
    }
}