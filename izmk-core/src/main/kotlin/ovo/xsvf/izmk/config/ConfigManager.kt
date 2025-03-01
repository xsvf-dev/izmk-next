package ovo.xsvf.izmk.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.apache.logging.log4j.LogManager
import ovo.xsvf.izmk.IZMK.mc
import ovo.xsvf.izmk.config.impl.ModuleConfig
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ConfigManager {
    private val log = LogManager.getLogger(javaClass)
    private val configs = CopyOnWriteArrayList<Config>()
    private val dir = File(mc.gameDirectory, "IZMK")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var isFirst = false

    fun init() {
        if (!dir.exists()) {
            dir.mkdirs()
            isFirst = true
        }
        configs.add(ModuleConfig())
        loadAllConfig()
    }

    @Synchronized
    private fun loadConfig(name: String) {
        if (executor.isShutdown) return

        executor.execute {
            val file = File(dir, "$name.izmk")
            if (file.exists()) {
                runCatching {
                    val base64Text = file.readText(StandardCharsets.UTF_8)
                    val decodedJson = String(Base64.getDecoder().decode(base64Text), StandardCharsets.UTF_8)
                    gson.fromJson(decodedJson, JsonObject::class.java)
                }.onSuccess { json ->
                    configs.find { it.name == name }?.loadConfig(json ?: JsonObject())
                    logConfigAction(name, "Loaded successfully")
                }.onFailure {
                    log.error("Failed to load config $name: ${it.message}")
                }
            } else {
                log.warn("Config $name doesn't exist, creating a new one...")
                saveConfig(name)
            }
        }
    }

    @Synchronized
    private fun saveConfig(name: String) {
        if (executor.isShutdown) {
            log.warn("Attempted to save config $name but executor is shut down!")
            return
        }

        executor.execute {
            val file = File(dir, "$name.izmk")
            file.takeIf { !it.exists() }?.createNewFile()

            configs.find { it.name == name }?.let { config ->
                runCatching {
                    val jsonData = gson.toJson(config.saveConfig())
                    val base64Data = Base64.getEncoder().encodeToString(jsonData.toByteArray(StandardCharsets.UTF_8))
                    file.writeText(base64Data, StandardCharsets.UTF_8)
                }.onSuccess {
                    logConfigAction(name, "Saved successfully")
                }.onFailure {
                    log.error("Failed to save config $name: ${it.message}")
                }
            } ?: log.error("Config $name not found!")
        }
    }

    fun loadAllConfig() {
        if (executor.isShutdown) {
            log.warn("Attempted to load all configs but executor is shut down!")
            return
        }

        executor.execute {
            configs.forEach { loadConfig(it.name) }
            logConfigAction("All configs", "Successfully loaded")
        }
    }

    fun saveAllConfig() {
        if (executor.isShutdown) {
            log.warn("Attempted to save all configs but executor is shut down!")
            return
        }

        executor.execute {
            configs.forEach { saveConfig(it.name) }
            logConfigAction("All configs", "Successfully saved")
        }
    }

    private fun logConfigAction(name: String, message: String) {
        log.info("$message: $name")
    }

    fun shutdown() {
        saveAllConfig()

        try {
            Thread.sleep(500) // 让配置(子弹)飞一会
            executor.shutdownNow()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
