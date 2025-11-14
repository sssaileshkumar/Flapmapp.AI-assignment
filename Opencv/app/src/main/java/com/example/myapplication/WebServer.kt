package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class WebServer(context: Context, port: Int) : NanoHTTPD(port) {

    private val assets = context.assets
    private var latestFrame: ByteArray? = null

    fun setFrame(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        latestFrame = stream.toByteArray()
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        return when (uri) {
            "/" -> newFixedLengthResponse(Response.Status.OK, "text/html", assets.open("index.html"), assets.open("index.html").available().toLong())
            "/viewer.js" -> newFixedLengthResponse(Response.Status.OK, "application/javascript", assets.open("viewer.js"), assets.open("viewer.js").available().toLong())
            "/frame" -> {
                if (latestFrame != null) {
                    newFixedLengthResponse(Response.Status.OK, "image/jpeg", ByteArrayInputStream(latestFrame), latestFrame!!.size.toLong())
                } else {
                    newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "No frame available")
                }
            }
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
        }
    }
}
