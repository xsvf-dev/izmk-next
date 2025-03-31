package ovo.xsvf.izmk

import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.objectweb.asm.Type
import ovo.xsvf.common.status.Status
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
import ovo.xsvf.izmk.injection.patch.MinecraftPatch
import ovo.xsvf.izmk.misc.ClassUtil
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.patchify.ASMUtil
import ovo.xsvf.patchify.PatchLoader
import ovo.xsvf.patchify.annotation.Patch
import ovo.xsvf.patchify.asm.ReflectionUtil
import kotlin.properties.Delegates
import com.mojang.blaze3d.systems.RenderSystem as MojangRenderSystem

object IZMK {
    lateinit var classes: MutableMap<String, ByteArray>
    lateinit var mc: Minecraft
    lateinit var statusReporter: (Status) -> Unit
    val log: Logger = LogManager.getLogger(javaClass)
    var obfuscated by Delegates.notNull<Boolean>()

    const val ASSETS_DIRECTORY = "/assets/izmk"

    fun init() {
        log.info("Start initializing IZMK...")
        PreInitEvent().post()

        statusReporter.invoke(Status.CORE_PATCH)
        classes
            .filter { ASMUtil.isVisibleAnnotationPresent(ASMUtil.node(it.value), Patch::class.java) }
            .filter { it.key != Type.getInternalName(MinecraftPatch::class.java) }
            .map { ReflectionUtil.forName(it.key) }
            .let {
                it.forEach { patch ->
                    PatchLoader.INSTANCE.loadPatch(
                        patch,
                        { clazz -> ClassUtil.getClassBytes(clazz) },
                        { clazz, bytes: ByteArray? -> ClassUtil.redefineClass(clazz!!, bytes!!) }
                    )
                }
                log.info("Loaded {} patch, total classes: {}", it.size, classes.size)
            }

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

        PostInitEvent().post()
        statusReporter.invoke(Status.SUCCESS)
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
