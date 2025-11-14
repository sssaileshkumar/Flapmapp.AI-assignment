package gl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {

    private var program = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0

    private val vertices = floatArrayOf(
        -1.0f, -1.0f,  // bottom left
         1.0f, -1.0f,  // bottom right
        -1.0f,  1.0f,  // top left
         1.0f,  1.0f   // top right
    )

    private val texCoords = floatArrayOf(
        0.0f, 1.0f,  // bottom left
        1.0f, 1.0f,  // bottom right
        0.0f, 0.0f,  // top left
        1.0f, 0.0f   // top right
    )

    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply { put(vertices) }
    }

    private val texCoordBuffer: FloatBuffer = ByteBuffer.allocateDirect(texCoords.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply { put(texCoords) }
    }

    private val textures = IntArray(1)
    private var bitmapToRender: Bitmap? = null
    private var bitmapChanged = false

    fun setBitmap(bitmap: Bitmap) {
        bitmapToRender = bitmap
        bitmapChanged = true
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (bitmapChanged && bitmapToRender != null) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapToRender, 0)
            bitmapChanged = false
        }

        GLES20.glUseProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            vertexBuffer.position(0)
            GLES20.glVertexAttribPointer(it, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        }

        texCoordHandle = GLES20.glGetAttribLocation(program, "a_texCoord").also {
            GLES20.glEnableVertexAttribArray(it)
            texCoordBuffer.position(0)
            GLES20.glVertexAttribPointer(it, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        }

        textureHandle = GLES20.glGetUniformLocation(program, "s_texture").also {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glUniform1i(it, 0)
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, shaderCode)
            GLES20.glCompileShader(it)
        }
    }

    private val vertexShaderCode = """
        attribute vec4 vPosition;
        attribute vec2 a_texCoord;
        varying vec2 v_texCoord;
        void main() {
            gl_Position = vPosition;
            v_texCoord = a_texCoord;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec2 v_texCoord;
        uniform sampler2D s_texture;
        void main() {
            gl_FragColor = texture2D(s_texture, v_texCoord);
        }
    """
}
