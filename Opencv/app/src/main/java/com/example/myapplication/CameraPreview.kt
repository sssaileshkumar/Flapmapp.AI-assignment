package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import java.util.*

@SuppressLint("MissingPermission")
class CameraPreview(context: Context) : TextureView(context), TextureView.SurfaceTextureListener, ImageReader.OnImageAvailableListener {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private lateinit var previewSize: Size
    private lateinit var processedBitmap: Bitmap
    private var imageReader: ImageReader? = null
    private var onFrameProcessedListener: OnFrameProcessedListener? = null

    private val cameraThread = HandlerThread("CameraThread").also { it.start() }
    private val cameraHandler = Handler(cameraThread.looper)

    init {
        surfaceTextureListener = this
    }

    interface OnFrameProcessedListener {
        fun onFrameProcessed(bitmap: Bitmap)
    }

    fun setOnFrameProcessedListener(listener: OnFrameProcessedListener) {
        onFrameProcessedListener = listener
    }

    private external fun processFrame(
        width: Int, height: Int,
        yBuffer: java.nio.ByteBuffer, yRowStride: Int,
        uBuffer: java.nio.ByteBuffer, uRowStride: Int,
        vBuffer: java.nio.ByteBuffer, vRowStride: Int,
        bitmapOut: Bitmap)

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        openCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        closeCamera()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    private fun openCamera() {
        val cameraId = getCameraId() ?: return
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        previewSize = map!!.getOutputSizes(SurfaceTexture::class.java)[0]
        processedBitmap = Bitmap.createBitmap(previewSize.width, previewSize.height, Bitmap.Config.ARGB_8888)
        imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.YUV_420_888, 2)
        imageReader?.setOnImageAvailableListener(this, cameraHandler)
        cameraManager.openCamera(cameraId, cameraStateCallback, cameraHandler)
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
    }

    private fun getCameraId(): String? {
        for (cameraId in cameraManager.cameraIdList) {
            if (cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                return cameraId
            }
        }
        return null
    }

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCaptureSession()
        }
        override fun onDisconnected(camera: CameraDevice) { closeCamera() }
        override fun onError(camera: CameraDevice, error: Int) { closeCamera() }
    }

    private fun createCaptureSession() {
        surfaceTexture?.setDefaultBufferSize(previewSize.width, previewSize.height)
        val previewSurface = Surface(surfaceTexture)
        val readerSurface = imageReader!!.surface

        val captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(previewSurface)
        captureRequestBuilder.addTarget(readerSurface)

        cameraDevice?.createCaptureSession(listOf(previewSurface, readerSurface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                session.setRepeatingRequest(captureRequestBuilder.build(), null, cameraHandler)
            }
            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }, cameraHandler)
    }

    override fun onImageAvailable(reader: ImageReader?) {
        val image = reader?.acquireLatestImage() ?: return
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        processFrame(
            image.width, image.height,
            yPlane.buffer, yPlane.rowStride,
            uPlane.buffer, uPlane.rowStride,
            vPlane.buffer, vPlane.rowStride,
            processedBitmap
        )
        onFrameProcessedListener?.onFrameProcessed(processedBitmap)
        image.close()
    }

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}