package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.module.Module

object Particles : Module("particles") {
    val showFirstPerson by setting("show_first_person", false)
    val blockBreaking by setting("block_breaking", true)
}