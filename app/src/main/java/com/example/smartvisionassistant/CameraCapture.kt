package com.example.smartvisionassistant

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

object CameraCapture {

    private var tts: TextToSpeech? = null

    fun captureImage(
        context: Context,
        imageCapture: ImageCapture
    ) {

        val photoFile = File(
            context.cacheDir,
            "captured.jpg"
        )

        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            context.mainExecutor,
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults: ImageCapture.OutputFileResults
                ) {

                    // Save to Gallery
                    val gallerySaved =
                        GallerySaver.saveToGallery(
                            context,
                            photoFile
                        )

                    CoroutineScope(Dispatchers.Main).launch {

                        val uploaded =
                            UploadImage.upload(context)

                        if (uploaded) {

                            if (tts == null) {

                                tts = TextToSpeech(context) { status ->

                                    if (status == TextToSpeech.SUCCESS) {

                                        tts?.language = Locale.US

                                        tts?.speak(
                                            UploadImage.latestDescription,
                                            TextToSpeech.QUEUE_FLUSH,
                                            null,
                                            "vision"
                                        )
                                    }
                                }

                            } else {

                                tts?.speak(
                                    UploadImage.latestDescription,
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "vision"
                                )

                            }

                        }

                        if (gallerySaved && uploaded) {

                            Toast.makeText(
                                context,
                                "Image saved to Gallery & uploaded successfully",
                                Toast.LENGTH_LONG
                            ).show()

                        } else if (gallerySaved) {

                            Toast.makeText(
                                context,
                                "Image saved to Gallery, upload failed",
                                Toast.LENGTH_LONG
                            ).show()

                        } else if (uploaded) {

                            Toast.makeText(
                                context,
                                "Image uploaded, Gallery save failed",
                                Toast.LENGTH_LONG
                            ).show()

                        } else {

                            Toast.makeText(
                                context,
                                "Gallery save & upload failed",
                                Toast.LENGTH_LONG
                            ).show()

                        }

                    }

                }

                override fun onError(
                    exception: ImageCaptureException
                ) {

                    Toast.makeText(
                        context,
                        "Capture Failed",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }
        )

    }

}