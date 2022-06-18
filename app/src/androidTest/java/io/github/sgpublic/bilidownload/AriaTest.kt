package io.github.sgpublic.bilidownload

import com.arialyy.aria.core.Aria
import org.junit.After
import org.junit.Before
import org.junit.Test

class AriaTest: ApplicationText() {
    @Before
    fun register() {
        Aria.init(applicationContext)
        Aria.download(this).register()
    }

    @After
    fun unRegister() {
        Aria.download(this).unRegister()
    }

    @Test
    fun download() {
        val task = Aria.download(this)
            .load("http://oi.sgpublic.xyz/?/Bangumi/%E7%81%B0%E8%89%B2%E4%B8%89%E9%83%A8%E6%9B%B2/" +
                    "%E7%81%B0%E8%89%B2%E7%9A%84%E6%9E%9C%E5%AE%9E/" +
                    "%5BKissSub%26FZSD%5D%5BGrisaia_no_Kajitsu%5D%5BBDRip%5D%5B01%5D%5BCHS%5D%5B720P%5D%5BAVC_AAC%5D%28E141FF2A%29.mp4")
            .ignoreFilePathOccupy()
            .ignoreCheckPermissions()
            .create()
        Thread.sleep(20000)
        Aria.download(this).load(task).stop()
    }
}