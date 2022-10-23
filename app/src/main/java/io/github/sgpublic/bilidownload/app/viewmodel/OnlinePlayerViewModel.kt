package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.MutableLiveData
import io.github.sgpublic.bilidownload.base.app.BaseViewModel

class OnlinePlayerViewModel: BaseViewModel() {
    val SID: MutableLiveData<Int> = MutableLiveData()
}