package io.github.sgpublic.bilidownload.app.fragment.player

import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import io.github.sgpublic.bilidownload.app.viewmodel.BasePlayerModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelFragment

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 14:30
 */
abstract class BasePlayer<VB: ViewBinding, VM: BasePlayerModel>(
    private val activity: AppCompatActivity
): BaseViewModelFragment<VB, VM>(activity) {
}