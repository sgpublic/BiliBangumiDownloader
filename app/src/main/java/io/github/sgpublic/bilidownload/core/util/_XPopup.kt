package io.github.sgpublic.bilidownload.core.util

import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.ConfirmPopupView
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R

fun XPopup.Builder.showAsOutsideConfirm(confirm: () -> Unit) {
    asConfirm(
        Application.getString(R.string.title_open_other),
        Application.getString(R.string.text_open_other),
    ) {
        confirm.invoke()
    }.show()
}