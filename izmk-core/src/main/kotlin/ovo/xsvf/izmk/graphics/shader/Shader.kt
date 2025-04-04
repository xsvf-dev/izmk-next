package ovo.xsvf.izmk.graphics.shader

import org.apache.logging.log4j.LogManager
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL45.*
import ovo.xsvf.izmk.graphics.GlHelper
import ovo.xsvf.izmk.graphics.GlObject
import java.io.InputStream
import java.io.StringWriter
import java.nio.FloatBuffer
import java.nio.charset.Charset
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class Shader(
    vertShaderPath: String,
    fragShaderPath: String,
    geomShaderPath: String? = null,
    shouldBeCompiled: () -> Boolean = { true }
) : GlObject {
    private val buffer: FloatBuffer = BufferUtils.createFloatBuffer(4 * 4)

    final override var id: Int

    init {
        if (shouldBeCompiled()) {
            val vertexShaderID = createShader(vertShaderPath, GL_VERTEX_SHADER)
            val fragShaderID = createShader(fragShaderPath, GL_FRAGMENT_SHADER)
            val geomShaderID = geomShaderPath?.let { createShader(it, GL_GEOMETRY_SHADER) }
            val id = glCreateProgram()

            glAttachShader(id, vertexShaderID)
            glAttachShader(id, fragShaderID)
            geomShaderID?.let { glAttachShader(id, it) }

            glLinkProgram(id)
            val linked = glGetProgrami(id, GL_LINK_STATUS)
            if (linked == 0) {
                log.error(glGetProgramInfoLog(id, 1024))
                glDeleteProgram(id)
                throw ShaderCompileException(
                    "Failed to link shader: $vertShaderPath, $fragShaderPath" + geomShaderPath?.let { ", $geomShaderPath" }
                )
            }
            this.id = id

            glDetachShader(id, vertexShaderID)
            glDetachShader(id, fragShaderID)
            geomShaderID?.let { glDetachShader(id, it) }
            glDeleteShader(vertexShaderID)
            glDeleteShader(fragShaderID)
            geomShaderID?.let { glDeleteShader(it) }
        } else {
            this.id = 0
        }
    }

    private fun createShader(path: String, shaderType: Int): Int {
        val srcString = javaClass.getResourceAsStream(path)?.use { it.readText() } ?: run {
            throw IllegalArgumentException("Shader source not found: $path")
        }
        val id = glCreateShader(shaderType)

        glShaderSource(id, srcString)
        glCompileShader(id)

        val compiled = glGetShaderi(id, GL_COMPILE_STATUS)
        if (compiled == 0) {
            log.error("$path\n" + glGetShaderInfoLog(id, 1024))
            glDeleteShader(id)
            throw ShaderCompileException("Failed to compile shader: $path")
        }

        return id
    }

    override fun bind() {
        GlHelper.program = id
    }

    override fun unbind() {
        GlHelper.program = 0
    }

    override fun delete() {
        glDeleteProgram(id)
    }

    open fun default() {}

    /* uniform functions */
    protected fun matrix4f(location: Int, mat: Matrix4f) {
        mat.get(buffer)
        glProgramUniformMatrix4fv(id, location, false, buffer)
    }

    protected fun vec3f(location: Int, vec: Vector3f) {
        glProgramUniform3f(id, location, vec.x, vec.y, vec.z)
    }

    protected fun int1(location: Int, textureUnit: Int) {
        glProgramUniform1i(id, location, textureUnit)
    }

    protected fun float1(location: Int, value: Float) {
        glProgramUniform1f(id, location, value)
    }

    class ShaderCompileException(message: String) : Exception(message)

    companion object {
        private val log = LogManager.getLogger(Shader::class.java)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T : Shader> T.use(block: T.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    bind()
    block.invoke(this)
    glUseProgram(0)
}

fun InputStream.readText(charset: Charset = Charsets.UTF_8, bufferSize: Int = DEFAULT_BUFFER_SIZE): String {
    val stringWriter = StringWriter()
    buffered(bufferSize / 2).reader(charset).copyTo(stringWriter, bufferSize / 2)
    return stringWriter.toString()
}