package ovo.xsvf.izmk.module.impl.render

import net.minecraft.world.effect.MobEffectInstance
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.module.RenderableModule
import java.text.SimpleDateFormat
import java.util.*

object PotionStatus: RenderableModule(
    name = "potion-status",
    defaultX = 0f,
    defaultY = 0f
) {
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        var height0 = 0f
        var width0 = 0f
        var y0 = y
        mc.player?.activeEffectsMap?.forEach { (effect, instance) ->
            var x0 = x
            val amplifier = getAmplifier(instance)
            val duration = getDuration(instance)
            val displayName = effect.displayName.string + " " + amplifier + " "
            FontRenderers.drawString(
                displayName,
                x0, y0, ColorRGB.WHITE
            )
            width0 += FontRenderers.getStringWidth(displayName)
            x0 += FontRenderers.getStringWidth(displayName)
            FontRenderers.drawString(
                "(${duration})",
                x0, y0, ColorRGB.WHITE
            )
            width0 += FontRenderers.getStringWidth("(${duration})")
            y0 += FontRenderers.DRAW_FONT_SIZE

            height0 += FontRenderers.DRAW_FONT_SIZE
        }
        height = height0
        width = width0
    }

    private fun getAmplifier(mobEffectInstance: MobEffectInstance): String {
        return when (mobEffectInstance.amplifier + 1) {
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            5 -> "V"
            else -> (mobEffectInstance.amplifier + 1).toString()
        }
    }

    private fun getDuration(mobeffectinstance: MobEffectInstance): String {
        val duration = mobeffectinstance.duration / 20 // ticks
        return if (duration >= 60 * 60) secondToDate(duration.toLong(), "hh:mm:ss")
        else secondToDate(duration.toLong(), "mm:ss")
    }

    private fun secondToDate(second: Long, patten: String): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = second * 1000
        val date = calendar.time
        val format = SimpleDateFormat(patten)
        return format.format(date)
    }
}