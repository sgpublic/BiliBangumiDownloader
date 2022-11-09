package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.ui.list.LicenseListAdapter
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.core.util.toSortedList
import io.github.sgpublic.bilidownload.databinding.ActivityLicenseBinding
import java.util.PriorityQueue

class License: BaseActivity<ActivityLicenseBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.licenseToolbar)
        supportActionBar?.run {
            setTitle(R.string.title_about_license)
            setDisplayHomeAsUpEnabled(true)
        }

        val arrayList = PriorityQueue(Comparator<LicenseListAdapter.LicenseListItem> { o1, o2 ->
            return@Comparator o1.projectTitle.compareTo(o2.projectTitle)
        })
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "BannerViewPager",
                "Android，Base on ViewPager2. 这可能是全网最好用的ViewPager轮播图。简单、高效，一行代码实现循环轮播，一屏三页任意变，指示器样式任你挑。",
                "zhpanvip",
                "https://github.com/zhpanvip/BannerViewPager"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "Sofia",
                "Android沉浸式效果的实现，状态栏和导航栏均支持设置颜色、渐变色、图片、透明度、内容入侵和状态栏深色字体；兼容竖屏、横屏，当屏幕旋转时会自动适配。",
                "yanzhenjie",
                "https://github.com/yanzhenjie/Sofia"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "MultiWaveHeader",
                "Wave,水波,Android 炫酷的多重水波纹 MultiWaveHeader",
                "scwang90",
                "https://github.com/scwang90/MultiWaveHeader"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "XPopup",
                "\uD83D\uDD25XPopup2.0版本重磅来袭，2倍以上性能提升，带来可观的动画性能优化和交互细节的提升！！！功能强大，交互优雅，动画丝滑的通用弹窗！可以替代Dialog，PopupWindow，PopupMenu，BottomSheet，DrawerLayout，Spinner等组件，自带十几种效果良好的动画， 支持完全的UI和动画自定义！(Powerful and Beautiful Popup for Android，can absolutely replace Dialog，PopupWindow，PopupMenu，BottomSheet，DrawerLayout，Spinner. With built-in animators , very easy to custom popup vi…",
                "li-xiaojun",
                "https://github.com/li-xiaojun/XPopup"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "zxing",
                "ZXing (\"Zebra Crossing\") barcode scanning library for Java, Android",
                "zxing",
                "https://github.com/zxing/zxing"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "ExSharedPreference",
                "Manage SharedPreference with annotation!",
                "sgpublic",
                "https://github.com/sgpublic/ExSharedPreference"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "glide",
                "An image loading and caching library for Android focused on smooth scrolling.",
                "bumptech",
                "https://github.com/bumptech/glide"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "glide-transformations",
                "An Android transformation library providing a variety of image transformations for Glide.",
                "wasabeef",
                "https://github.com/wasabeef/glide-transformations"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "okhttp",
                "Square’s meticulous HTTP client for Java and Kotlin.",
                "square",
                "https://github.com/square/okhttp"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "ExoPlayer",
                "An extensible media player for Android",
                "google",
                "https://github.com/google/ExoPlayer"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "logback-android",
                "\uD83D\uDCC4The reliable, generic, fast and flexible logging framework for Android",
                "tony19",
                "https://github.com/tony19/logback-android"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "forest",
                "A high-level and lightweight HTTP client framework for Java. it makes sending HTTP requests in Java easier.",
                "dromara",
                "https://github.com/dromara/forest"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "Aria",
                "下载可以很简单",
                "AriaLyy",
                "https://github.com/AriaLyy/Aria"
            )
        )
        ViewBinding.licenseList.adapter = LicenseListAdapter(this@License, arrayList.toSortedList())
    }


    override val ViewBinding: ActivityLicenseBinding by viewBinding()
    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, License::class.java)
            context.startActivity(intent)
        }
    }
}