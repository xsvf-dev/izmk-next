package ovo.xsvf.izmk.event.impl

import ovo.xsvf.izmk.event.Event

class KeyEvent(val keyCode: Int, val scanCode: Int, val action: Int, val modifiers: Int): Event()