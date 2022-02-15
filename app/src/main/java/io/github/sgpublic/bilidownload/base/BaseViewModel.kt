package io.github.sgpublic.bilidownload.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel: ViewModel() {
    val EXCEPTION: MutableLiveData<ExceptionData> = MutableLiveData()
    fun getExceptionData() = EXCEPTION.value
    data class ExceptionData(var code: Int, var message: String?)
}

fun MutableLiveData<BaseViewModel.ExceptionData>.postValue(
    code: Int, message: String?
) {
    postValue(BaseViewModel.ExceptionData(code, message))
}