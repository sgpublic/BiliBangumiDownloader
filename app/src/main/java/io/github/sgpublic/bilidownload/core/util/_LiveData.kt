@file:Suppress("ObjectLiteralToLambda")

package io.github.sgpublic.bilidownload.core.util

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * fix 'Candidate resolution will be changed soon, please use fully qualified name to invoke the following closer candidate explicitly
 */
fun <T> MutableLiveData<T>.newObserve(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner, object : Observer<T> {
        override fun onChanged(t: T) {
            observer(t)
        }
    })
}

fun <T: Any> ViewModel.launchWithIOContext(block: suspend CoroutineScope.() -> T) {
    viewModelScope.launch {
        withContext(Dispatchers.IO, block)
    }
}