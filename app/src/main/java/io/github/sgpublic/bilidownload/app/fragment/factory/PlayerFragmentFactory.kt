package io.github.sgpublic.bilidownload.app.fragment.factory

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import io.github.sgpublic.bilidownload.base.app.BaseViewModelActivity
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding

class PlayerFragmentFactory(
    private val context: BaseViewModelActivity<ActivityPlayerBinding, *>
): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(loadFragmentClass(classLoader, className)) {
            else -> super.instantiate(classLoader, className)
        }
    }
}