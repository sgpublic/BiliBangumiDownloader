package io.github.sgpublic.bilidownload.ui.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.sgpublic.bilidownload.fragment.home.HomeBangumi
import io.github.sgpublic.bilidownload.fragment.home.HomeMine

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