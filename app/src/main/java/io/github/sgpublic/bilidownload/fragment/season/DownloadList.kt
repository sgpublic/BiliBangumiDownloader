package io.github.sgpublic.bilidownload.fragment.season

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import io.github.sgpublic.bilidownload.base.BaseViewModelFragment
import io.github.sgpublic.bilidownload.databinding.FragmentDownloadListBinding
import io.github.sgpublic.bilidownload.viewmodel.LocalPlayerViewModel

class DownloadList(contest: AppCompatActivity)
    : BaseViewModelFragment<FragmentDownloadListBinding, LocalPlayerViewModel>(contest) {
    override val ViewModel: LocalPlayerViewModel by activityViewModels()

    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onCreateViweBinding(container: ViewGroup?): FragmentDownloadListBinding =
        FragmentDownloadListBinding.inflate(layoutInflater, container, false)
}