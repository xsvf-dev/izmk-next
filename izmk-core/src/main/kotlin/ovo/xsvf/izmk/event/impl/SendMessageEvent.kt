package ovo.xsvf.izmk.event.impl

import net.minecraft.network.chat.Component
import ovo.xsvf.izmk.event.CancellableEvent

class SendMessageEvent(val component: Component) : CancellableEvent()
