package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivityLicenseBinding
import io.github.sgpublic.bilidownload.ui.list.LicenseListAdapter

class License: BaseActivity<ActivityLicenseBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onCreateViweBinding(): ActivityLicenseBinding =
        ActivityLicenseBinding.inflate(layoutInflater)

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.licenseToolbar)
        supportActionBar?.run {
            setTitle(R.string.title_about_license)
            setDisplayHomeAsUpEnabled(true)
        }

        val arrayList = ArrayList<LicenseListAdapter.LicenseListItem>()
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
                "Blur-Fix-AndroidX",
                "Fork from https://github.com/ThomasCookDeveloperInfo/BlurKit-Fix, add AndroidX support, move to JitPack.",
                "SGPublic",
                "https://github.com/SGPublic/Blur-Fix-AndroidX"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "DialogX",
                "\uD83D\uDCACDialogX对话框组件库，更加方便易用，可自定义程度更高，扩展性更强，轻松实现各种对话框、菜单和提示效果，更有iOS、MIUI等主题扩展可选",
                "kongzue",
                "https://github.com/kongzue/DialogX"
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
                "Sofia",
                "Android沉浸式效果的实现，状态栏和导航栏均支持设置颜色、渐变色、图片、透明度、内容入侵和状态栏深色字体；兼容竖屏、横屏，当屏幕旋转时会自动适配。",
                "yanzhenjie",
                "https://github.com/yanzhenjie/Sofia"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "SmartTabLayout",
                "A custom ViewPager title strip which gives continuous feedback to the user when scrolling",
                "ogaclejapan",
                "https://github.com/ogaclejapan/SmartTabLayout"
            )
        )
        arrayList.add(
            LicenseListAdapter.LicenseListItem(
                "SwipeBackLayoutX",
                "Fork from https://github.com/ikew0ng/SwipeBackLayout, add AndroidX support, move to JitPack.",
                "SGPublic",
                "https://github.com/SGPublic/SwipeBackLayoutX"
            )
        )
        ViewBinding.licenseList.adapter = LicenseListAdapter(this@License, arrayList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, License::class.java)
            context.startActivity(intent)
        }
    }
}