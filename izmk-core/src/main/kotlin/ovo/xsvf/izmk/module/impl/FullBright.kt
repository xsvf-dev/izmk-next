package ovo.xsvf.izmk.module.impl

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.PreTickEvent
import ovo.xsvf.izmk.module.Module

object FullBright : Module("full-bright") {
    private var gamma0 = mc.options.gamma().get()
    private val mode by setting("mode", Mode.GAMMA)
    val gamma by setting("gamma", gamma0, 0.1..15.0, 0.1)
        .visibility { mode == Mode.GAMMA }

    override fun onEnable() {
        gamma0 = mc.options.gamma().get()
    }

    @EventTarget
    fun onTick(e: PreTickEvent) {
        if (mode == Mode.NIGHT_VISION && mc.player?.hasEffect(MobEffects.NIGHT_VISION) == false) {
            mc.player?.addEffect(
                MobEffectInstance(
                    MobEffects.NIGHT_VISION, Int.MAX_VALUE,
                    233, false, false
                )
            )
        } else if (mode != Mode.NIGHT_VISION) {
            removeNightVision()
        }
    }

    override fun onDisable() {
        removeNightVision()
    }

    private fun removeNightVision() {
        if (mode == Mode.NIGHT_VISION) {
            mc.player?.activeEffects?.removeIf { it.effect == MobEffects.NIGHT_VISION && it.amplifier == 233 }
        }
    }

    enum class Mode {
        GAMMA,
        NIGHT_VISION
    }
}