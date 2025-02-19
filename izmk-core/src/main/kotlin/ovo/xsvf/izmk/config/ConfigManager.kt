package ovo.xsvf.izmk.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.IZMK.mc
import ovo.xsvf.izmk.config.impl.ModuleConfig
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ConfigManager {
    private val configs = CopyOnWriteArrayList<Config>()
    private val dir = File(mc.gameDirectory, "IZMK")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var isFirst = false

    fun init() {
        if (!dir.exists()) {
            dir.mkdir()
            isFirst = true
        }
        configs.add(ModuleConfig())
        loadAllConfig()
    }

    private fun loadConfig(name: String) {
        if (executor.isShutdown || executor.isTerminated) return
        executor.execute {
            val file = File(dir, "$name.izmk")
            if (file.exists()) {
                val json = runCatching {
                    val base64Text = file.readText(StandardCharsets.UTF_8)
                    val decodedJson = String(Base64.getDecoder().decode(base64Text), StandardCharsets.UTF_8)
                    Gson().fromJson(decodedJson, JsonObject::class.java)
                }.getOrNull()

                configs.find { it.name == name }?.loadConfig(json ?: JsonObject())
                logConfigAction(name, "Loaded client config")
            } else {
                IZMK.logger.warn("Config $name doesn't exist, creating a new one...")
                saveConfig(name)
            }
        }
    }

    private fun saveConfig(name: String) {
        if (executor.isShutdown || executor.isTerminated) {
            IZMK.logger.warn("Attempted to save config $name but executor is shut down!")
            return
        }

        executor.execute {
            val file = File(dir, "$name.izmk")
            if (!file.exists()) file.createNewFile()

            configs.find { it.name == name }?.let {
                val jsonData = gson.toJson(it.saveConfig())
                val base64Data = Base64.getEncoder().encodeToString(jsonData.toByteArray(StandardCharsets.UTF_8))

                file.writeText(base64Data, StandardCharsets.UTF_8)
                logConfigAction(name, "Saved client config")
            } ?: IZMK.logger.error("Failed to save config: $name")
        }
    }

    private fun loadAllConfig() {
        if (executor.isShutdown || executor.isTerminated) {
            IZMK.logger.warn("Attempted to load all configs but executor is shut down!")
            return
        }

        executor.execute {
            configs.forEach { loadConfig(it.name) }
            logConfigAction("All configs", "Successfully loaded all configs")
        }
    }

    private fun saveAllConfig() {
        if (executor.isShutdown || executor.isTerminated) {
            IZMK.logger.warn("Attempted to save all configs but executor is shut down!")
            return
        }

        executor.execute {
            configs.forEach { saveConfig(it.name) }
            logConfigAction("All configs", "Successfully saved all configs")
        }
    }

    private fun logConfigAction(name: String, message: String) {
        IZMK.logger.info("$message: $name")
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
