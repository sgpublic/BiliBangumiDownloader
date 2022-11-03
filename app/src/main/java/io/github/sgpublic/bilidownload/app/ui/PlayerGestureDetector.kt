package io.github.sgpublic.bilidownload.app.ui

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity

abstract class PlayerGestureDetector(context: AppCompatActivity) : View.OnTouchListener {
    private val sample = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            this@PlayerGestureDetector.onDoubleTap(e)
            return super.onDoubleTap(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            this@PlayerGestureDetector.onSingleTap(e)
            return super.onSingleTapConfirmed(e)
        }
    }

    private val gestureDetector = GestureDetector(context, sample)

    final override fun onTouch(v: View, ev: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(ev)
    }

    open fun onDoubleTap(e: MotionEvent) {

    }

    open fun onSingleTap(e: MotionEvent) {

    }
}