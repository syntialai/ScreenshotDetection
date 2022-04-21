package com.syntia.screenshotdetection

import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.lang.ref.WeakReference

class ScreenshotObserver(private val context: Context) : LifecycleEventObserver {

  private var lifecycleOwnerWeakReference: WeakReference<LifecycleOwner>? = null

  private var viewGroupWeakReference: WeakReference<ViewGroup>? = null

  private var contentObserver: ContentObserver? = null

//  private var debounceJob: Job? = null

  private var screenshotResultCardViewCustomView: ScreenshotResultCardViewCustomView? = null

  private var view: View? = null

  override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    when (event) {
      Lifecycle.Event.ON_START -> registerObserver()
      Lifecycle.Event.ON_STOP -> unregisterObserver()
      else -> {}
    }
  }

  fun registerViewObserver(viewGroup: ViewGroup, lifecycleOwner: LifecycleOwner, view: View) {
    lifecycleOwnerWeakReference = WeakReference(lifecycleOwner)
    lifecycleOwnerWeakReference?.get()?.let { safeLifecycleOwner ->
      safeLifecycleOwner.lifecycle.addObserver(this)
    }
    viewGroupWeakReference = WeakReference(viewGroup)
    this.view = view
    registerObserver()
  }

  fun showScreenshotResultCardView(bitmap: Bitmap?) {
    removeScreenshotResultCardView()
    viewGroupWeakReference?.get()?.let { safeViewGroup ->
      screenshotResultCardViewCustomView = ScreenshotResultCardViewCustomView(
          safeViewGroup.context).apply {
        setImageBitmap(bitmap)
        setOnCloseListener(::removeScreenshotResultCardView)
        showView()
      }
      safeViewGroup.addView(screenshotResultCardViewCustomView)
      setDebounceAutoCloseScreenshotResult()
    }
  }

  private fun setDebounceAutoCloseScreenshotResult() {
    lifecycleOwnerWeakReference?.get()?.let { safeLifecycleOwner ->
//      safeLifecycleOwner.lifecycleScope.launch {
//        delay()
//        screenshotResultCardViewCustomView?.removeView(::removeScreenshotResultCardView)
//      }
    }
  }

  private fun registerObserver() {
    initContentObserver()
    contentObserver?.let { safeContentObserver ->
      context.contentResolver.registerContentObserver(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          true,
          safeContentObserver)
    }
  }

  private fun unregisterObserver() {
    lifecycleOwnerWeakReference?.get()?.lifecycle?.removeObserver(this)
    contentObserver?.let { safeContentObserver ->
      context.contentResolver.unregisterContentObserver(safeContentObserver)
    }
    contentObserver = null
  }

  private fun initContentObserver() {
    contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
      override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
//        uri?.let {
//          queryScreenshots(it)
//        }
        view?.let {
          val bitmap = ImageUtils.captureScreen(it)
          showScreenshotResultCardView(bitmap)
        }
      }
    }
  }

  private fun showScreenshotResultCardView(uri: Uri?) {
    removeScreenshotResultCardView()
    viewGroupWeakReference?.get()?.let { safeViewGroup ->
      screenshotResultCardViewCustomView = ScreenshotResultCardViewCustomView(
          safeViewGroup.context).apply {
        setImageUri(uri)
        setOnCloseListener(::removeScreenshotResultCardView)
        showView()
      }
      safeViewGroup.addView(screenshotResultCardViewCustomView)
    }
  }

  private fun removeScreenshotResultCardView() {
    viewGroupWeakReference?.get()?.removeView(screenshotResultCardViewCustomView)
    screenshotResultCardViewCustomView?.let {
      it.visibility = View.GONE
    }
    screenshotResultCardViewCustomView = null
  }

  private fun queryScreenshots(uri: Uri) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      queryRelativeDataColumn(uri)
    } else {
      queryDataColumn(uri)
    }
  }

  private fun queryDataColumn(uri: Uri) {
    val projection = arrayOf(
        MediaStore.Images.Media.DATA
    )
    context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        null
    )?.use { cursor ->
      val dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
      while (cursor.moveToNext()) {
        val path = cursor.getString(dataColumn)
        if (path.contains("screenshot", true)) {
          // do something
          Toast.makeText(context, path, Toast.LENGTH_LONG).show()
        }
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun queryRelativeDataColumn(uri: Uri) {
    val projection = arrayOf(
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.RELATIVE_PATH
    )
    context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        null
    )?.use { cursor ->
      val relativePathColumn =
          cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
      val displayNameColumn =
          cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
      while (cursor.moveToNext()) {
        val name = cursor.getString(displayNameColumn)
        val relativePath = cursor.getString(relativePathColumn)
        if (name.contains("screenshot", true) or
            relativePath.contains("screenshot", true)
        ) {
          // do something
          Toast.makeText(context, name, Toast.LENGTH_LONG).show()
        }
      }
    }
  }
}