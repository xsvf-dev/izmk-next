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
class Logger private constructor(private val writer: BufferedWriter, name: String) {
    companion object {
        fun of(name: String, port: Int): Logger {
            val socket = Socket("localhost", port)
            val logger = Logger(BufferedWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), name)
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
        writer.write(Base64.getEncoder().encodeToString(jsonObject.toString().toByteArray(StandardCharsets.UTF_8)) + "\n")
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

    fun debug(message: String, vararg args: Any) = log(Level.DEBUG, message.format2(args))

    fun info(message: String, vararg args: Any) = log(Level.INFO, message.format2(args))

    fun warn(message: String, vararg args: Any) = log(Level.WARN, message.format2(args))

    fun error(message: String, vararg args: Any) = log(Level.ERROR, message.format2(args))

    fun error(msg: String, throwable: Throwable, vararg args: Any) {
        error(buildErrorMessage(msg.format2(args), throwable))
    }

    fun error(throwable: Throwable) {
        error(buildErrorMessage("Exception Occurred", throwable))
    }

    private fun buildErrorMessage(msg: String, throwable: Throwable): String {
        return buildString {
            appendLine(msg)
            appendLine("    Exception: ${throwable::class.simpleName}: ${throwable.message ?: ""}")
            throwable.cause?.let { appendLine("    Cause: ${it.message ?: it::class.simpleName}") }
            appendLine("    Stack Trace:")
            append(throwable.stackTraceToString().prependIndent("    "))
        }
    }

    private fun String.format2(vararg args: Any) : String {
        args.forEach {
            this.replaceFirst("{}", it.toString())
        }
        return this
    }

    object Level {
        const val DEBUG = 0
        const val INFO = 1
        const val WARN = 2
        const val ERROR = 3
    }
}
