package ovo.xsvf.izmk

import net.minecraft.client.Minecraft
import ovo.xsvf.izmk.config.ConfigManager
import ovo.xsvf.izmk.graphics.buffer.VertexBufferObjects
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.graphics.utils.RenderUtils
import ovo.xsvf.izmk.injection.mixin.MixinLoader
import ovo.xsvf.izmk.injection.mixin.impl.MixinMinecraft
import ovo.xsvf.izmk.misc.ClassUtil
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.logging.Logger

object IZMK {
    lateinit var logger: Logger
    lateinit var mc: Minecraft
    var runHeypixel: Boolean = false

    const val ASSETS_DIRECTORY = "/assets/izmk/"

    val excludedLoading: List<Class<*>> = listOf<Class<*>>(
        MixinMinecraft::class.java
    )

    fun init() {
        logger.info("Start initializing IZMK...")

        val classes = ClassUtil.getInstrumentation().allLoadedClasses
        MixinLoader.loadMixins(classes.toMutableList())
        ModuleManager.init(classes)
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
