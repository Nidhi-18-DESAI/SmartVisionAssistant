package com.example.smartvisionassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.smartvisionassistant.network.RetrofitClient

class MainActivity : ComponentActivity() {

    private var hasCameraPermission = mutableStateOf(false)

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasCameraPermission.value = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        hasCameraPermission.value =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        setContent {

            MaterialTheme {

                if (hasCameraPermission.value) {

                    CameraPreviewWithBackend()

                } else {

                    PermissionScreen {
                        requestPermissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }

                }

            }

        }

    }

}

@Composable
fun PermissionScreen(onClick: () -> Unit) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Button(onClick = onClick) {
            Text("Grant Camera Permission")
        }

    }

}

@Composable
fun CameraPreviewWithBackend() {

    var backendMessage by remember {
        mutableStateOf("Connecting...")
    }

    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {

        try {

            val response = withContext(Dispatchers.IO) {
                RetrofitClient.api.checkBackend()
            }

            backendMessage = response.message

        } catch (e: Exception) {

            backendMessage = "Connection Failed"

        }

    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        CameraPreview(imageCapture)

        Text(
            text = backendMessage,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Button(
            onClick = {

                CameraCapture.captureImage(
                    context,
                    imageCapture
                )

            },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {

            Text("Capture")

        }

    }

}

@Composable
fun CameraPreview(
    imageCapture: ImageCapture
) {

    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->

            val previewView = PreviewView(ctx)

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({

                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build()

                preview.surfaceProvider =
                    previewView.surfaceProvider

                val cameraSelector =
                    CameraSelector.DEFAULT_BACK_CAMERA

                try {

                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                } catch (e: Exception) {

                    e.printStackTrace()

                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView

        }
    )

}