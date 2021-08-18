package com.example.ex2

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// number of values per vertex in this array for each attribute
const val VALUES = 4

var verticesPositions = floatArrayOf(     // in counterclockwise order:
    0f, .5f, 0f, 1f,   // top vertex
    -.5f, 0f, 0f, 1f, // bottom left vertex
    .5f, 0f, 0f, 1f  // bottom right vertex
)

var verticesColors = floatArrayOf(     // in counterclockwise order:
    1f, 0f ,0f, 1f,    // top vertex, red
    0f, 1f ,0f, 1f,   // bottom left vertex, green
    0f, 0f ,1f, 1f   // bottom right vertex, blue
)


class Triangle {

    private val vertexShaderCode =
    // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +
                "attribute vec4 aPosition;" +
                "attribute vec4 aColor;" +
                "varying vec4 vColor;" +
                "void main() {" +
                // the matrix must be included as a modifier of gl_Position
                // Note that the uMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                "vColor = aColor;" +
                "gl_Position = uMVPMatrix * aPosition;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "varying vec4 vColor;" +
                "void main() {" +
                "gl_FragColor = vColor;" +
                "}"


    private val verticesPositionsBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(verticesPositions.size * Float.SIZE_BYTES).run {

            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(verticesPositions)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private val verticesColorsBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(verticesColors.size * Float.SIZE_BYTES).run {

            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(verticesColors)
                // set the buffer to read the first coordinate
                position(0)
            }
        }


    private var mProgram: Int

    init {
        val vertexShader: Int = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {

            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
    }

    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var vPMatrixHandle: Int = 0

    private val vertexCount: Int = 3
    private val vertexStride: Int = Float.SIZE_BYTES * VALUES

    fun draw(mvpMatrix: FloatArray) {

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition").also {

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(
                it,
                VALUES,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                verticesPositionsBuffer
            )
        }

        colorHandle = GLES20.glGetAttribLocation(mProgram, "aColor").also {

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the triangle color data
            GLES20.glVertexAttribPointer(
                it,
                VALUES,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                verticesColorsBuffer
            )
        }

        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix").also {
            // Pass the projection and view transformation to the shader
            GLES20.glUniformMatrix4fv(it, 1, false, mvpMatrix, 0)
        }

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
    }
}
