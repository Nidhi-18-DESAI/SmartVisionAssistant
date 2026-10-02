package com.example.smartvisionassistant

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream

object GallerySaver {

    fun saveToGallery(
        context: Context,
        imageFile: File
    ): Boolean {

        return try {

            val values = ContentValues().apply {

                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "SmartVision_${System.currentTimeMillis()}.jpg"
                )

                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/SmartVisionAssistant"
                    )
                }
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: return false

            context.contentResolver.openOutputStream(uri)?.use { output ->

                FileInputStream(imageFile).use { input ->
                    input.copyTo(output)
                }

            } ?: return false

            true

        } catch (e: Exception) {

            e.printStackTrace()
            false

        }

    }

}
