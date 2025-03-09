package ovo.xsvf.izmk.module.impl

import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import ovo.xsvf.izmk.module.Module
import java.awt.Color

object ChatCopy : Module(name = "chat-copy") {
    fun process(input: Component): Component {
        return input.copy().append(Component.literal(" [C]").apply {
            withStyle(
                Style.EMPTY
                    .withColor(Color(18, 114, 126).rgb)
                    .withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, input.string))
                    .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.nullToEmpty("Copy")))
            )
        })
    }
}