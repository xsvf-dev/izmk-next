package ovo.xsvf.izmk.util

import net.minecraft.network.chat.Component
import ovo.xsvf.izmk.IZMK.isNull
import ovo.xsvf.izmk.IZMK.mc

object ChatUtil {
    fun addMessageWithClient(message: String) {
        if (isNull()) return
        mc.player?.sendSystemMessage(Component.literal(message))
    }
}
