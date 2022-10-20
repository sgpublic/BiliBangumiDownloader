package io.github.sgpublic.bilidownload.app.ui.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.sgpublic.bilidownload.app.fragment.home.HomeBangumi
import io.github.sgpublic.bilidownload.app.fragment.home.HomeMine

class HomeFragmentAdapter(private val context: AppCompatActivity): FragmentStateAdapter(context) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> HomeBangumi(context)
            1 -> HomeMine(context)
            else -> throw NullPointerException()
        }
    }
}