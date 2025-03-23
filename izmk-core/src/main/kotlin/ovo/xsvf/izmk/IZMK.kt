package ovo.xsvf.izmk

import net.minecraft.client.Minecraft
import net.minecraft.server.packs.resources.ReloadableResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.optifine.CustomSky
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ovo.xsvf.izmk.config.ConfigManager
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.PostInitEvent
import ovo.xsvf.izmk.event.impl.PreInitEvent
import ovo.xsvf.izmk.event.impl.ShutdownEvent
import ovo.xsvf.izmk.graphics.RenderSystem
import ovo.xsvf.izmk.graphics.buffer.VertexBufferObjects
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.graphics.utils.RenderUtils
import ovo.xsvf.izmk.gui.screen.ClickGUIScreen
import ovo.xsvf.izmk.module.ModuleManager
import kotlin.properties.Delegates
import com.mojang.blaze3d.systems.RenderSystem as MojangRenderSystem

object IZMK {
    lateinit var classes: MutableMap<String, ByteArray>
    lateinit var mc: Minecraft
    val log: Logger = LogManager.getLogger(javaClass)
    var obfuscated by Delegates.notNull<Boolean>()

    const val ASSETS_DIRECTORY = "/assets/izmk"

    fun init() {
        log.info("Start initializing IZMK...")
        PreInitEvent().post()

        ModuleManager.init()
        ConfigManager.init()

        MojangRenderSystem.recordRenderCall {
            // GUI
            ClickGUIScreen
            // Systems
            VertexBufferObjects
            RenderSystem
            // Fonts
            FontRenderers
            // Utils
            RenderUtils
        }

        CustomSky.update()
        val resourceManager = mc.resourceManager
        if (resourceManager is ReloadableResourceManager) {
            resourceManager.registerReloadListener(ResourceManagerReloadListener {
                CustomSky.update()
            })
        }

        PostInitEvent().post()
        log.info("IZMK has been successfully initialized!")
    }

    @EventTarget
    fun onShutdown(event: ShutdownEvent) {
        ConfigManager.shutdown()
    }

    fun isNull(): Boolean {
        return mc.player == null
    }
}
