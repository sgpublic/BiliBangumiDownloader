package io.github.sgpublic.bilidownload.ui.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.sgpublic.bilidownload.fragment.follows.Follows

class FollowsFragmentAdapter(context: AppCompatActivity, private val list: ArrayList<Follows>):
    FragmentStateAdapter(context) {
    override fun getItemCount(): Int = list.size

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}