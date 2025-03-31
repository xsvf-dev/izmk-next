package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.module.Module

object Particles : Module("particles") {
    val showFirstPerson by setting("show-first-person", false)
    val blockBreaking by setting("block-breaking", true)
}