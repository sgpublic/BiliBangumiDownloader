package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.*
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.forest.data.FollowsResp
import io.github.sgpublic.bilidownload.core.util.ForestClients
import io.github.sgpublic.bilidownload.core.util.RequestCallback
import io.github.sgpublic.bilidownload.core.util.advSub
import io.github.sgpublic.bilidownload.core.util.biliapi
import kotlin.collections.ArrayList

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 9:11
 */
class FollowModel(private val status: FollowStatus): BaseViewModel() {
    val Follows: MutableLiveData<Pair<ArrayList<FollowsResp.FollowsData.FollowItem>, Boolean>> by lazy {
        getFollows(true)
        MutableLiveData(ArrayList<FollowsResp.FollowsData.FollowItem>() to true)
    }
    private var pageIndex: Int = 1

    fun getFollows(isRefresh: Boolean) {
        if (isRefresh) {
            pageIndex = 1
            Loading.postValue(true)
        }
        ForestClients.Api.follow(
            status.value, pageIndex, TokenPreference.accessToken
        ).biliapi(object : RequestCallback<FollowsResp.FollowsData>() {
            override fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override fun onResponse(data: FollowsResp.FollowsData) {
                pageIndex += 1
                val liveData = Follows.value?.takeIf { !isRefresh }
                    ?: (ArrayList<FollowsResp.FollowsData.FollowItem>() to true)
                liveData.first.addAll(data.followList.advSub(3))
                Follows.postValue(liveData.first to data.isHasNext)
            }
        }, viewModelScope)
    }

    class Factory(private val status: FollowStatus): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(FollowStatus::class.java).newInstance(status)
        }
    }

    enum class FollowStatus(val value: Int, @StringRes val title: Int) {
        Want(1, R.string.title_follows_want),
        Watching(2, R.string.title_follows_watching),
        Watched(3, R.string.title_follows_has_watched);
    }
}