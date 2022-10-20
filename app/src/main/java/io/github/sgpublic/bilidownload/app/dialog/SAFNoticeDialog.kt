package io.github.sgpublic.bilidownload.app.dialog

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lxj.xpopup.core.BottomPopupView
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.databinding.DialogSafNoticeBinding

@SuppressLint("ViewConstructor")
class SAFNoticeDialog(private val context: AppCompatActivity): BottomPopupView(context) {
    override fun onCreate() {
        val binding = DialogSafNoticeBinding.bind(popupImplView)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            binding.dialogSafP.visibility = View.GONE
        } else {
            binding.dialogSafContentC.visibility = View.GONE
        }
        binding.dialogSafImageC.setOnClickListener {
            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clip.setPrimaryClip(ClipData.newPlainText("dir", BuildConfig.PROJECT_NAME))
            Application.onToast(context, R.string.text_copy_success)
        }
        binding.dialogSafAction.setOnClickListener { onConfirm() }
    }

    private var onConfirm: () -> Unit = { }
    fun setOnConfirmListener(onConfirm: () -> Unit) {
        this.onConfirm = onConfirm
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_saf_notice
}