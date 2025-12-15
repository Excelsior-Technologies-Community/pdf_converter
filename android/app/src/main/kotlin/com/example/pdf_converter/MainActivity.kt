package com.example.pdf_converter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.io.FileOutputStream

class MainActivity : FlutterActivity() {

    private val CHANNEL = "native_pdf"
    private val PICK_IMAGES = 1001
    private var pendingResult: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "pickImagesAndConvertToPdf" -> {
                        pendingResult = result
                        pickMultipleImages()
                    }
                    else -> result.notImplemented()
                }
            }
    }

    private fun pickMultipleImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Enable multiple selection
        }
        startActivityForResult(intent, PICK_IMAGES)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES && resultCode == Activity.RESULT_OK) {
            val imageUris = mutableListOf<Uri>()

            // Handle multiple images
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    imageUris.add(clipData.getItemAt(i).uri)
                }
            } ?: data?.data?.let { singleUri ->
                // Handle single image
                imageUris.add(singleUri)
            }

            if (imageUris.isNotEmpty()) {
                Thread {
                    val pdfPath = convertImagesToPdf(imageUris)
                    runOnUiThread {
                        if (pdfPath != null) {
                            openPdf(pdfPath)
                            pendingResult?.success(pdfPath)
                        } else {
                            pendingResult?.error("CONVERSION_ERROR", "Failed to create PDF", null)
                        }
                        pendingResult = null
                    }
                }.start()
            }
        } else {
            pendingResult?.success(null)
            pendingResult = null
        }
    }

    private fun convertImagesToPdf(imageUris: List<Uri>): String? {
        return try {
            val pdfDocument = PdfDocument()
            var pageNumber = 1

            for (uri in imageUris) {
                val inputStream = contentResolver.openInputStream(uri) ?: continue
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap != null) {
                    // Scale bitmap if too large
                    val scaledBitmap = scaleBitmapIfNeeded(bitmap)

                    val pageInfo = PdfDocument.PageInfo.Builder(
                        scaledBitmap.width,
                        scaledBitmap.height,
                        pageNumber++
                    ).create()

                    val page = pdfDocument.startPage(pageInfo)
                    page.canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
                    pdfDocument.finishPage(page)

                    if (scaledBitmap != bitmap) {
                        scaledBitmap.recycle()
                    }
                    bitmap.recycle()
                }
            }

            val timestamp = System.currentTimeMillis()
            val file = File(getExternalFilesDir(null), "images_$timestamp.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val maxSize = 2048
        return if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val ratio = minOf(
                maxSize.toFloat() / bitmap.width,
                maxSize.toFloat() / bitmap.height
            )
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }
    }

    private fun openPdf(filePath: String) {
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}