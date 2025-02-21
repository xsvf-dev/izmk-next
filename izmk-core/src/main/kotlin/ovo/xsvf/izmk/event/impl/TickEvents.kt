package ovo.xsvf.izmk.event.impl

import ovo.xsvf.izmk.event.Event

sealed class TickEvents {

    object Pre: Event()
    object Post: Event()

}