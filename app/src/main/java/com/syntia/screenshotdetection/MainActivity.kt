package com.syntia.screenshotdetection

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.syntia.screenshotdetection.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  companion object {
    private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1022
  }

  private val screenshotObserver: ScreenshotObserver by lazy {
    ScreenshotObserver(this)
  }

  private val viewBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(viewBinding.root)

    viewBinding.bTakeScreenshot.setOnClickListener {
//      getScreenshots()
    }

    screenshotObserver.registerViewObserver(viewBinding.root, this, window.decorView)
  }

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
      READ_EXTERNAL_STORAGE_REQUEST -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          getScreenshots()
        }
        return
      }
    }
  }

  private fun getScreenshots() {
    if (haveStoragePermission()) {
      Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT)
      captureScreen()
    } else {
      requestPermission()
    }
  }

  private fun captureScreen() {
    val captureBitmap = ImageUtils.captureScreen(window.decorView)
    captureBitmap?.let { safeCaptureBitmap ->
      ImageUtils.saveCapturedScreen(this, safeCaptureBitmap)
      screenshotObserver.showScreenshotResultCardView(safeCaptureBitmap)
    }
  }

  private fun haveStoragePermission() =
      ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.READ_EXTERNAL_STORAGE
      ) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
      ) == PackageManager.PERMISSION_GRANTED

  private fun requestPermission() {
    if (!haveStoragePermission()) {
      val permissions = arrayOf(
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
      )
      ActivityCompat.requestPermissions(this, permissions, READ_EXTERNAL_STORAGE_REQUEST)
    }
  }
}