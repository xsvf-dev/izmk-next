package ovo.xsvf.izmk.config

import com.google.gson.JsonObject

open class Config(val name: String) {

    open fun saveConfig(): JsonObject {
        return JsonObject().apply {
            addProperty("exampleKey", "exampleValue")
        }
    }

    open fun loadConfig(jsonObject: JsonObject) {}
}
