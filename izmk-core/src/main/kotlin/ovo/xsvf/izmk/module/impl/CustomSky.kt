package ovo.xsvf.izmk.module.impl

import net.optifine.CustomSky
import ovo.xsvf.izmk.module.Module

object CustomSky : Module("custom-sky") {
    override fun onEnable() {
        CustomSky.update()
    }

    override fun onDisable() {
        CustomSky.update()
    }
}