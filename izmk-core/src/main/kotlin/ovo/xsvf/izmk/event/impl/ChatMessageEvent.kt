package ovo.xsvf.izmk.event.impl

import net.minecraft.network.chat.Component
import ovo.xsvf.izmk.event.CancellableEvent

class ChatMessageEvent(val message: Component) : CancellableEvent()

