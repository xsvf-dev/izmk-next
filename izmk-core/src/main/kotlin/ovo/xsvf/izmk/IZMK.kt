package ovo.xsvf.izmk

import net.minecraft.client.Minecraft
import ovo.xsvf.izmk.config.ConfigManager
import ovo.xsvf.izmk.injection.mixin.MixinLoader
import ovo.xsvf.izmk.injection.mixin.impl.special.MixinMinecraft
import ovo.xsvf.izmk.misc.ClassUtil
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.logging.Logger

object IZMK {
    var logger: Logger? = null
    val excludedLoading: List<Class<*>> = listOf<Class<*>>(
        MixinMinecraft::class.java
    )
    var mc: Minecraft? = null
    var runHeypixel: Boolean = false

    fun init() {
        logger!!.info("Start initializing IZMK...")

        MixinLoader.loadMixins(*ClassUtil.getInstrumentation().allLoadedClasses)
        ModuleManager.init(ClassUtil.getInstrumentation().allLoadedClasses)

        ConfigManager.init()
    }

    fun shutdown() {
        ConfigManager.shutdown()
    }
}
