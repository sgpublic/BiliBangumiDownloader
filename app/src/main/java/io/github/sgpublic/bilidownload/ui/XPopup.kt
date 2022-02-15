package io.github.sgpublic.bilidownload.ui

import androidx.annotation.StringRes
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.ConfirmPopupView
import io.github.sgpublic.bilidownload.Application

fun XPopup.Builder.asConfirm(@StringRes titleId: Int, @StringRes contentId: Int,
                             onConfirm: () -> Unit): ConfirmPopupView {
    val title = Application.getString(titleId)
    val content = Application.getString(contentId)
    return asConfirm(title, content, null, null, onConfirm, null, false, 0)
}

fun XPopup.Builder.asConfirm(title: String, @StringRes contentId: Int,
                             onConfirm: () -> Unit): ConfirmPopupView {
    val content = Application.getString(contentId)
    return asConfirm(title, content, null, null, onConfirm, null, false, 0)
}

fun XPopup.Builder.asConfirm(@StringRes titleId: Int, content: String,
                             onConfirm: () -> Unit): ConfirmPopupView {
    val title = Application.getString(titleId)
    return asConfirm(title, content, null, null, onConfirm, null, false, 0)
}