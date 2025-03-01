package ovo.xsvf.izmk.config.impl

import com.google.gson.JsonObject
import ovo.xsvf.izmk.config.Config
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.izmk.module.RenderableModule

class ModuleConfig : Config("Module") {
    override fun saveConfig(): JsonObject {
        return JsonObject().apply {
            ModuleManager.modules().forEach { module ->
                add(module.name, JsonObject().apply {
                    if (module is RenderableModule) {
                        addProperty("x", module.x)
                        addProperty("y", module.y)
                    }
                    module.settings.forEach {
                        add(it.name.key.fullKey, it.toJson())
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
                    module.settings.forEach { setting ->
                        runCatching {
                            setting.fromJson(moduleObject.get(setting.name.key.fullKey))
                        }.onFailure {
                            log.warn("Failed to load setting ${setting.name.translation} for module ${module.name}")
                        }
                    }
                }
            }
    }
}