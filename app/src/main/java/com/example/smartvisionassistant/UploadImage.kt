package com.example.smartvisionassistant

import android.content.Context
import com.example.smartvisionassistant.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object UploadImage {

    var latestDescription: String = ""

    suspend fun upload(context: Context): Boolean {

        return withContext(Dispatchers.IO) {

            try {

                val imageFile = File(
                    context.cacheDir,
                    "captured.jpg"
                )

                if (!imageFile.exists()) {
                    return@withContext false
                }

                val requestBody =
                    imageFile.asRequestBody(
                        "image/jpeg".toMediaTypeOrNull()
                    )

                val imagePart =
                    MultipartBody.Part.createFormData(
                        "image",
                        imageFile.name,
                        requestBody
                    )

                val response =
                    RetrofitClient.api.uploadImage(imagePart)

                if (response.isSuccessful && response.body() != null) {

                    latestDescription =
                        response.body()!!.description ?: ""

                    true

                } else {

                    false

                }

            } catch (e: Exception) {

                e.printStackTrace()
                false

            }

        }

    }

}

