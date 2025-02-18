package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.TickEvent
import ovo.xsvf.izmk.module.Module

/**
 * @author LangYa466
 * @since 2025/2/16
 */
class Test : Module("Test") {
    init {
        println("Test module loaded")
        enabled = true
    }

    override fun onTick(event: TickEvent) {
        IZMK.logger.info("tick event")
    }
}
