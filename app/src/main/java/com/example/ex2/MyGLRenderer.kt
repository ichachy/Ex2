package com.example.ex2

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer: GLSurfaceView.Renderer {

    private lateinit var triangle: Triangle

    private var angle: Float = 0f

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix: FloatArray = FloatArray(16)
    private val projectionMatrix: FloatArray = FloatArray(16)
    private val viewMatrix: FloatArray = FloatArray(16)
    private val rotationMatrix: FloatArray = FloatArray(16)
    private val scratch: FloatArray = FloatArray(16)

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        triangle = Triangle()
        // Set the background frame color
        GLES20.glClearColor(0f, 0f, 0f, 0f)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() /height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    fun addToAngle(angle: Float){
        this.angle += angle
    }

    override fun onDrawFrame(p0: GL10?) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Create a rotation transformation for the triangle
        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1f)

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        // Draw triangle
        triangle.draw(scratch)
    }

    companion object {
        fun loadShader(type: Int, shaderCode: String): Int {

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            return GLES20.glCreateShader(type).also { shader ->

                // add the source code to the shader and compile it
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)

                val compiled = IntArray(1)
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
                if (compiled[0] == 0) {
                    println("Could not compile shader $type: " + GLES20.glGetShaderInfoLog(shader))
                }
            }
        }
    }
}