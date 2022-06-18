package io.github.sgpublic.bilidownload.fragment.factory

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import io.github.sgpublic.bilidownload.fragment.season.TaskList

class DownloadFragmentFactory(
    private val context: AppCompatActivity
): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(loadFragmentClass(classLoader, className)) {
            TaskList::class.java -> TaskList(context)
            else -> super.instantiate(classLoader, className)
        }
    }
}