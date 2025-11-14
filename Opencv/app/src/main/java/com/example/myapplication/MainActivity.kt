package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import gl.MyGLRenderer
import java.io.IOException

class MainActivity : ComponentActivity(), CameraPreview.OnFrameProcessedListener {

    private val cameraPermissionRequestCode = 1
    private var hasCameraPermission by mutableStateOf(false)
    private lateinit var renderer: MyGLRenderer
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var webServer: WebServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateCameraPermission()
        renderer = MyGLRenderer()
        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(renderer)
        }

        webServer = WebServer(this, 8080)
        try {
            webServer.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (hasCameraPermission) {
                        CameraWithGLView()
                    } else {
                        PermissionRequestScreen {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraPermissionRequestCode)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webServer.stop()
    }

    private fun updateCameraPermission() {
        hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraPermissionRequestCode) {
            updateCameraPermission()
        }
    }

    override fun onFrameProcessed(bitmap: Bitmap) {
        renderer.setBitmap(bitmap)
        glSurfaceView.requestRender()
        webServer.setFrame(bitmap)
    }

    @Composable
    private fun CameraWithGLView() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AndroidView(
                factory = { context ->
                    CameraPreview(context).apply {
                        setOnFrameProcessedListener(this@MainActivity)
                    }
                },
                modifier = Modifier.fillMaxSize().weight(1f)
            )
            AndroidView(
                factory = { glSurfaceView },
                modifier = Modifier.fillMaxSize().weight(1f)
            )
        }
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onRequestPermission) {
            Text(text = "Request Camera Permission")
        }
    }
}
