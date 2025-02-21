package ovo.xsvf.izmk

import net.minecraft.client.Minecraft
import ovo.xsvf.izmk.config.ConfigManager
import ovo.xsvf.izmk.injection.mixin.MixinLoader
import ovo.xsvf.izmk.injection.mixin.impl.MixinMinecraft
import ovo.xsvf.izmk.misc.ClassUtil
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.logging.Logger
import kotlin.properties.Delegates

object IZMK {
    lateinit var logger: Logger
    lateinit var mc: Minecraft
    var Obfuscated by Delegates.notNull<Boolean>()
    var runHeypixel: Boolean = false

    val excludedLoading: List<Class<*>> = listOf<Class<*>>(
        MixinMinecraft::class.java
    )

    fun init() {
        logger.info("Start initializing IZMK...")

        val classes = ClassUtil.getInstrumentation().allLoadedClasses
        MixinLoader.loadMixins(classes.toMutableList())
        ModuleManager.init(classes)
        ConfigManager.init()
    }

    fun shutdown() {
        ConfigManager.shutdown()
    }

    fun isNull(): Boolean {
        return mc.player == null
    }
}
