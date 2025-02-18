package ovo.xsvf.izmk.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.IZMK.mc
import ovo.xsvf.izmk.config.impl.ModuleConfig
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object ConfigManager {
    private val configs = CopyOnWriteArrayList<Config>()
    private val dir = File(mc.gameDirectory, "IZMK")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val executor: ExecutorService = Executors.newCachedThreadPool()
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
            val file = File(dir, "$name.json")
            if (file.exists()) {
                val json = runCatching {
                    Gson().fromJson(file.readText(), JsonObject::class.java)
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

        val future = executor.submit {
            val file = File(dir, "$name.json")
            if (!file.exists()) file.createNewFile()

            configs.find { it.name == name }?.let {
                file.outputStream().use { stream ->
                    stream.write(gson.toJson(it.saveConfig()).toByteArray(StandardCharsets.UTF_8))
                }
                logConfigAction(name, "Saved client config")
            } ?: IZMK.logger.error("Failed to save config: $name")
        }

        try {
            future.get()
        } catch (e: Exception) {
            IZMK.logger.error("Failed to execute saveConfig for $name: ${e.message}")
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

    fun saveAllConfig() {
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

        executor.shutdown()
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                IZMK.logger.warn("Forcefully shutting down executor...")
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
        }
    }
}
