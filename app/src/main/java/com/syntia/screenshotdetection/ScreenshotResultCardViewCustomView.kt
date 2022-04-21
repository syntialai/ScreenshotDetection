package com.syntia.screenshotdetection

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.syntia.screenshotdetection.databinding.CustomViewScreenshotCardViewBinding
import java.io.File

class ScreenshotResultCardViewCustomView constructor(context: Context) : FrameLayout(context) {

  private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

  private val viewBinding by lazy {
    CustomViewScreenshotCardViewBinding.inflate(layoutInflater, null, false)
  }

  init {
    addView(viewBinding.root)
  }

  fun setImageBitmap(bitmap: Bitmap?) {
    viewBinding.ivScreenshot.setImageBitmap(bitmap)
  }

  fun setImageUri(uri: Uri?) {
    viewBinding.ivScreenshot.setImageURI(uri)
  }

  fun setOnReportButtonClickListener(onClickListener: () -> Unit) {
    viewBinding.bReportIssue.setOnClickListener {
      onClickListener.invoke()
    }
  }

  fun setOnCloseListener(onCloseListener: () -> Unit) {
    viewBinding.ivClose.setOnClickListener {
      removeView(onCloseListener)
    }
  }

  fun showView() {
    val deviceWidth = resources.displayMetrics.widthPixels
    val posX = deviceWidth - getScreenshotResultCardViewWidth()
    this.x = deviceWidth.toFloat()
    showWithAnimation(posX)
  }

  fun showWithAnimation(posX: Float) {
    visibility = View.VISIBLE
    this.animate()
        .translationX(posX)
        .setDuration(200L)
        .start()
  }

  fun removeView(onCloseListener: (() -> Unit)?) {
    this.animate()
        .translationY(getScreenshotResultCardViewHeight() * -1)
        .withEndAction {
          visibility = View.GONE
          onCloseListener?.invoke()
        }
        .setDuration(200L)
        .start()
  }

  private fun getScreenshotResultCardViewWidth(): Float {
    with(viewBinding.cvScreenshot.layoutParams) {
      return width + ((this as? MarginLayoutParams)?.topMargin ?: 0).toFloat()
    }
  }

  private fun getScreenshotResultCardViewHeight(): Float {
    with(viewBinding.cvScreenshot.layoutParams) {
      return height + ((this as? MarginLayoutParams)?.topMargin ?: 0).toFloat()
    }
  }
}