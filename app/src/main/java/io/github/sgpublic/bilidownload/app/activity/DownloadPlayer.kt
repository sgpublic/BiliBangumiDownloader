package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.fragment.player.LocalPlayer
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding

class DownloadPlayer: BaseActivity<ActivityPlayerBinding>() {
    override fun beforeCreate() {
        supportFragmentManager.fragmentFactory = object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                val clazz = loadFragmentClass(classLoader, className)
                return if (LocalPlayer::class.java.isAssignableFrom(clazz)) {
                    clazz.getConstructor(
                        Long::class.java, Long::class.java,
                        AppCompatActivity::class.java,
                    ).newInstance(
                        intent.getLongExtra(KEY_SEASON_ID, -1),
                        intent.getLongExtra(KEY_EPISODE_ID, -1),
                        this@DownloadPlayer,
                    )
                } else {
                    super.instantiate(classLoader, className)
                }
            }
        }
        super.beforeCreate()
    }

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        supportFragmentManager.beginTransaction().apply {
            replace(ViewBinding.playerOrigin.id, LocalPlayer::class.java, null, "LocalPlayer")
        }.commit()
    }

    private var last: Long = -1
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        val now = System.currentTimeMillis()
        if (last == -1L) {
            Application.onToast(this, R.string.text_back_local_player_exit)
            last = now
        } else {
            if (now - last < 2000) {
                @Suppress("DEPRECATION")
                super.onBackPressed()
            } else {
                last = now
                Application.onToast(this, R.string.text_back_local_player_exit_notice)
            }
        }
    }

    override val ViewBinding: ActivityPlayerBinding by viewBinding()
    companion object {
        const val KEY_SEASON_ID = "season_id"
        const val KEY_EPISODE_ID = "ep_id"

        fun startActivity(context: Context, sid: Long, epid: Long) {
            val intent = Intent(context, DownloadPlayer::class.java)
            intent.putExtra(KEY_SEASON_ID, sid)
            intent.putExtra(KEY_EPISODE_ID, epid)
            log.info("DownloadPlayer(sid: $sid, epid: $epid)")
            context.startActivity(intent)
        }
    }
}