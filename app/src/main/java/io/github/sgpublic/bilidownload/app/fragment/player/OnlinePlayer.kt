package io.github.sgpublic.bilidownload.app.fragment.player

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import io.github.sgpublic.bilidownload.app.viewmodel.OnlinePlayerModel
import io.github.sgpublic.bilidownload.databinding.FragmentPlayerBinding

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 14:29
 */
class OnlinePlayer(activity: AppCompatActivity): BasePlayer<FragmentPlayerBinding, OnlinePlayerModel>(activity) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    override val ViewModel: OnlinePlayerModel by activityViewModels()
    override fun onCreateViewBinding(container: ViewGroup?) =
        FragmentPlayerBinding.inflate(layoutInflater, container, false)
}