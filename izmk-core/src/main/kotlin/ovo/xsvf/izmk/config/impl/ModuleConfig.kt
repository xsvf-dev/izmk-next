package ovo.xsvf.izmk.config.impl

import com.google.gson.JsonObject
import ovo.xsvf.izmk.config.Config
import ovo.xsvf.izmk.module.ModuleManager

class ModuleConfig : Config("Module") {
    override fun saveConfig(): JsonObject {
        return JsonObject().apply {
            ModuleManager.modules().forEach { module ->
                add(module.name, JsonObject().apply {
                    addProperty("enabled", module.enabled)
                    addProperty("keyCode", module.keyCode)
                })
            }
        }
    }

    override fun loadConfig(jsonObject: JsonObject) {
        ModuleManager.modules().forEach { module ->
            jsonObject.getAsJsonObject(module.name)?.let { moduleObject ->
                module.enabled = moduleObject["enabled"].asBoolean
                module.keyCode = moduleObject["keyCode"].asInt
            }
        }
    }
}