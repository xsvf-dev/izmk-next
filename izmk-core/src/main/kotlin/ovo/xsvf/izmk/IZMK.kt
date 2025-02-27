package ovo.xsvf.izmk

import net.minecraft.client.Minecraft
import ovo.xsvf.izmk.config.ConfigManager
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.PostInitEvent
import ovo.xsvf.izmk.event.impl.PreInitEvent
import ovo.xsvf.izmk.event.impl.ShutdownEvent
import ovo.xsvf.izmk.graphics.RenderSystem
import ovo.xsvf.izmk.graphics.buffer.VertexBufferObjects
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.graphics.utils.RenderUtils
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.logging.Logger
import kotlin.properties.Delegates
import com.mojang.blaze3d.systems.RenderSystem as MojangRenderSystem

object IZMK {
    lateinit var logger: Logger
    lateinit var mc: Minecraft
    var obfuscated by Delegates.notNull<Boolean>()

    const val ASSETS_DIRECTORY = "assets/izmk"

    fun init() {
        logger.info("Start initializing IZMK...")
        PreInitEvent().post()

        ModuleManager.init()
        ConfigManager.init()

        MojangRenderSystem.recordRenderCall {
            // Systems
            VertexBufferObjects
            RenderSystem
            // Fonts
            FontRenderers
            // Utils
            RenderUtils
        }

        PostInitEvent().post()
        logger.info("IZMK has been successfully initialized!")
    }

    @EventTarget
    fun onShutdown(event: ShutdownEvent) {
        ConfigManager.shutdown()
    }

    fun isNull(): Boolean {
        return mc.player == null
    }
}
