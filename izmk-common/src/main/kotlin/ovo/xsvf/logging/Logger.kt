package ovo.xsvf.logging

import com.google.gson.JsonObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author xsvf, LangYa466
 */
class Logger private constructor(private val writer: BufferedWriter, private val socket: Socket, private val name: String) {
    companion object {
        fun of(name: String, port: Int): Logger {
            val socket = Socket("localhost", port)
            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
            val logger = Logger(writer, socket, name)

            logger.sendJson(
                JsonObject().apply {
                    addProperty("type", "init")
                    addProperty("name", name)
                }
            )

            return logger
        }
    }

    private fun sendJson(jsonObject: JsonObject) {
        val base64 = Base64.getEncoder().encodeToString(jsonObject.toString().toByteArray(StandardCharsets.UTF_8))
        writer.write(base64)
        writer.newLine() // 确保每条消息完整
        writer.flush()
    }

    private fun log(level: Int, message: String) {
        sendJson(
            JsonObject().apply {
                addProperty("type", "log")
                addProperty("level", level)
                addProperty("message", message)
            }
        )
    }

    fun debug(message: String, vararg args: Any) = log(Level.DEBUG, message.format2(*args))

    fun info(message: String, vararg args: Any) = log(Level.INFO, message.format2(*args))

    fun warn(message: String, vararg args: Any) = log(Level.WARN, message.format2(*args))

    fun error(message: String, vararg args: Any) = log(Level.ERROR, message.format2(*args))

    fun error(msg: String, throwable: Throwable, vararg args: Any) {
        error(buildErrorMessage(msg.format2(*args), throwable))
    }

    fun error(throwable: Throwable) {
        error(buildErrorMessage("Exception Occurred", throwable))
    }

    private fun buildErrorMessage(msg: String, throwable: Throwable): String {
        return buildString {
            appendLine(msg)
            append("    Exception: ${throwable::class.simpleName}")
            throwable.message?.let { append(": $it") }
            appendLine()
            throwable.cause?.let { appendLine("    Cause: ${it.message ?: it::class.simpleName}") }
            appendLine("    Stack Trace:")
            append(throwable.stackTraceToString().prependIndent("    "))
        }
    }

    private fun String.format2(vararg args: Any): String {
        var index = 0
        return this.replace(Regex("\\{}")) {
            if (index < args.size) args[index++].toString() else "{}"
        }
    }

    fun close() {
        writer.close()
        socket.close()
    }

    object Level {
        const val DEBUG = 0
        const val INFO = 1
        const val WARN = 2
        const val ERROR = 3
    }
}
