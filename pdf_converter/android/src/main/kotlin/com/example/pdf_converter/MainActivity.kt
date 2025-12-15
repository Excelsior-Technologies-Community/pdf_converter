package com.example.pdf_converter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import androidx.core.content.FileProvider
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.io.FileOutputStream

class MainActivity : FlutterActivity() {

    private val CHANNEL = "native_pdf"
    private val PICK_IMAGES = 5001
    private var pendingResult: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "pickImagesAndConvertToPdf" -> {
                        pendingResult = result
                        pickImages()
                    }
                    else -> {
                        result.notImplemented()
                    }
                }
            }
    }

    private fun pickImages() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, PICK_IMAGES)
        } catch (e: Exception) {
            pendingResult?.error("PICKER_ERROR", "Failed to open image picker: ${e.message}", null)
            pendingResult = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES) {
            if (resultCode == Activity.RESULT_OK) {
                val imageUris = mutableListOf<Uri>()

                // Handle multiple images
                data?.clipData?.let { clip ->
                    for (i in 0 until clip.itemCount) {
                        imageUris.add(clip.getItemAt(i).uri)
                    }
                } ?: data?.data?.let {
                    // Handle single image
                    imageUris.add(it)
                }

                if (imageUris.isEmpty()) {
                    pendingResult?.error("NO_IMAGES", "No images selected", null)
                    pendingResult = null
                    return
                }

                // Process in background thread
                Thread {
                    try {
                        val pdfFile = createPdfFromImages(imageUris)
                        openPdf(pdfFile)

                        runOnUiThread {
                            pendingResult?.success(pdfFile.absolutePath)
                            pendingResult = null
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            pendingResult?.error("PDF_ERROR", "Failed to create PDF: ${e.message}", null)
                            pendingResult = null
                        }
                    }
                }.start()
            } else {
                pendingResult?.error("CANCELLED", "Image selection cancelled", null)
                pendingResult = null
            }
        }
    }

    private fun createPdfFromImages(uris: List<Uri>): File {
        val pdfDocument = PdfDocument()

        try {
            uris.forEachIndexed { index, uri ->
                contentResolver.openInputStream(uri)?.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)

                    if (bitmap != null) {
                        val pageInfo = PdfDocument.PageInfo.Builder(
                            bitmap.width,
                            bitmap.height,
                            index + 1
                        ).create()

                        val page = pdfDocument.startPage(pageInfo)
                        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                        pdfDocument.finishPage(page)

                        bitmap.recycle()
                    }
                }
            }

            val pdfFile = File(
                getExternalFilesDir(null),
                "pdf_${System.currentTimeMillis()}.pdf"
            )

            FileOutputStream(pdfFile).use { output ->
                pdfDocument.writeTo(output)
            }

            return pdfFile
        } finally {
            pdfDocument.close()
        }
    }

    private fun openPdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(intent)
        } catch (e: Exception) {
            // No PDF viewer available
            runOnUiThread {
                android.widget.Toast.makeText(
                    this,
                    "No PDF viewer found. File saved at: ${file.absolutePath}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}