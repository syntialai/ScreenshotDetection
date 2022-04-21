package com.syntia.screenshotdetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

import android.os.Environment
import android.text.format.DateFormat
import java.io.File
import java.io.FileOutputStream
import java.util.Date

object ImageUtils {

  fun captureScreen(view: View): Bitmap? {
    view.setLayerType(View.LAYER_TYPE_HARDWARE, Paint())
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
  }

  fun saveCapturedScreen(context: Context, bitmap: Bitmap): File? {
    val currentDate = Date()
    DateFormat.format("yyyy-MM-dd_hh:mm:ss", currentDate)
    return try {
      val externalFileDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
      val imageFile = File.createTempFile(currentDate.toString(), ".jpg", externalFileDir)
      val outputStream = FileOutputStream(imageFile)
      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
      outputStream.flush()
      outputStream.close()
      imageFile
    } catch (e: Throwable) {
      e.printStackTrace()
      null
    }
  }
}