package ovo.xsvf.izmk.config

import com.google.gson.JsonObject
import org.apache.logging.log4j.LogManager

abstract class Config(val name: String) {
    val log = LogManager.getLogger(javaClass)

    abstract fun saveConfig(): JsonObject
    abstract fun loadConfig(jsonObject: JsonObject)
}
