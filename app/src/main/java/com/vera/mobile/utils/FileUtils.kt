package com.vera.mobile.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

fun saveAndSharePdf(context: Context, body: ResponseBody, fileName: String) {
    try {
        val file = File(context.cacheDir, fileName)
        val inputStream = body.byteStream()
        val outputStream = FileOutputStream(file)
        
        inputStream.copyTo(outputStream)
        outputStream.flush()
        outputStream.close()
        inputStream.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Enviar recibo en PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}