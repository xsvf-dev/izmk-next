package ovo.xsvf.izmk.config.impl

import com.google.gson.JsonObject
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.config.Config
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.izmk.module.RenderableModule
import ovo.xsvf.izmk.settings.*

class ModuleConfig : Config("Module") {
    override fun saveConfig(): JsonObject {
        return JsonObject().apply {
            ModuleManager.modules().forEach { module ->
                add(module.name, JsonObject().apply {
                    if (module is RenderableModule) {
                        addProperty("x", module.x)
                        addProperty("y", module.y)
                    }
                    module.settings.forEach { setting ->
                        when (setting) {
                            is IntSetting -> addProperty(setting.key.key.key, setting.value)
                            is LongSetting -> addProperty(setting.key.key.key, setting.value)
                            is FloatSetting -> addProperty(setting.key.key.key, setting.value)
                            is DoubleSetting -> addProperty(setting.key.key.key, setting.value)
                            is BooleanSetting -> addProperty(setting.key.key.key, setting.value)
                            is KeyBindSetting -> addProperty(setting.key.key.key, setting.value.keyCode)
                            is ColorSetting -> addProperty(setting.key.key.key, setting.value.rgba)
                            is EnumSetting<*> -> addProperty(setting.key.key.key, setting.value.name)
                        }
                    }
                })
            }
        }
    }

    override fun loadConfig(jsonObject: JsonObject) {
        ModuleManager.modules()
            .filter { it.loadFromConfig }
            .forEach { module ->
                jsonObject.getAsJsonObject(module.name)?.let { moduleObject ->
                    try {
                        if (module is RenderableModule) {
                            module.x = moduleObject.get("x").asFloat
                            module.y = moduleObject.get("y").asFloat
                        }
                    } catch (e: Exception) {
                        IZMK.logger.error("Failed to load module config for ${module.name}", e)
                    }

                    module.settings.forEach { setting ->
                        try {
                            if (!module.loadFromConfig) return@forEach

                            setting.setWithJson(moduleObject.get(setting.key.key.key))
                        } catch (e: Exception) {
                            IZMK.logger.error("Failed to load setting ${setting.key.key.key} for ${module.name}", e)
                        }
                    }
                }
            }
    }
}