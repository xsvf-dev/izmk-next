package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.gui.screen.TestScreen
import ovo.xsvf.izmk.module.Module

object TestScreen : Module("test-screen", false) {
    override fun onEnable() {
        mc.setScreen(TestScreen())
    }
}