@file:Suppress("ObjectLiteralToLambda")

package io.github.sgpublic.bilidownload.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

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
