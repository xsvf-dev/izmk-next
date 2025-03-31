package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.module.Module

object FullBright : Module("full-bright") {
    val gamma by setting("gamma", mc.options.gamma().get(), 0.1..15.0, 0.1)
}