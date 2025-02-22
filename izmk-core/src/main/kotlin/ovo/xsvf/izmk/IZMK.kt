package ovo.xsvf.izmk

import net.minecraft.client.Minecraft
import ovo.xsvf.izmk.config.ConfigManager
import ovo.xsvf.izmk.graphics.buffer.VertexBufferObjects
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.graphics.utils.RenderUtils
import ovo.xsvf.izmk.injection.mixin.impl.MixinMinecraft
import ovo.xsvf.izmk.misc.ClassUtil
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.izmk.module.hud.HUDManager
import ovo.xsvf.logging.Logger
import kotlin.properties.Delegates

object IZMK {
    lateinit var logger: Logger
    lateinit var mc: Minecraft
    var Obfuscated by Delegates.notNull<Boolean>()
    var runHeypixel: Boolean = false

    const val ASSETS_DIRECTORY = "assets/izmk"

    val excludedLoading: List<Class<*>> = listOf<Class<*>>(
        MixinMinecraft::class.java
    )

    fun init() {
        logger.info("Start initializing IZMK...")

        val classes = ClassUtil.getInstrumentation().allLoadedClasses
        ModuleManager.init(classes)
        HUDManager.init(classes)
        ConfigManager.init()

        com.mojang.blaze3d.systems.RenderSystem.recordRenderCall {
            // Systems
            VertexBufferObjects
            ovo.xsvf.izmk.graphics.RenderSystem
            // Fonts
            FontRenderers
            // Utils
            RenderUtils
        }
    }

    fun shutdown() {
        ConfigManager.shutdown()
    }

    fun isNull(): Boolean {
        return mc.player == null
    }
}
