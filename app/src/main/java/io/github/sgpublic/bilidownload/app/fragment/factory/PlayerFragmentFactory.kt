package io.github.sgpublic.bilidownload.app.fragment.factory

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import io.github.sgpublic.bilidownload.base.app.BaseFragment
import io.github.sgpublic.bilidownload.base.app.BaseViewModelActivity
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding

class PlayerFragmentFactory(
    private val context: BaseViewModelActivity<ActivityPlayerBinding, *>
): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return loadFragmentClass(classLoader, className)
            .getConstructor(AppCompatActivity::class.java)
            .newInstance(context)
    }
}