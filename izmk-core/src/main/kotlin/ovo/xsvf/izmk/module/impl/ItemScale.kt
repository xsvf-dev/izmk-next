package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.module.Module

object ItemScale: Module("item-scale") {
    private val showFirstPerson by setting("show-first-person", true)

    private val instantAxe by setting("instant-axe", false)
    private val instantAxeScale by setting("instant-axe-scale", 1f, 0.1f..5f) { instantAxe }
    private val instantAxeX by setting("instant-axe-x-rotation", 0f, -10f..10f) { instantAxe }
    private val instantAxeY by setting("instant-axe-y-rotation", 0f, -10f..10f) { instantAxe }
    private val instantAxeZ by setting("instant-axe-z-rotation", 0f, -10f..10f) { instantAxe }

    private val kbBall by setting("kb-ball", false)
    private val kbBallScale by setting("kb-ball-scale", 1f, 0.1f..5f) { kbBall }
    private val kbBallX by setting("kb-ball-x-rotation", 0f, -10f..10f) { kbBall }
    private val kbBallY by setting("kb-ball-y-rotation", 0f, -10f..10f) { kbBall }
    private val kbBallZ by setting("kb-ball-z-rotation", 0f, -10f..10f) { kbBall }

    private val totem by setting("totem", false)
    private val totemScale by setting("totem-scale", 1f, 0.1f..5f) { totem }
    private val totemX by setting("totem-x-rotation", 0f, -10f..10f) { totem }
    private val totemY by setting("totem-y-rotation", 0f, -10f..10f) { totem }
    private val totemZ by setting("totem-z-rotation", 0f, -10f..10f) { totem }

    private val enchantedGApple by setting("enchanted-golden-apple", false)
    private val enchantedGAppleScale by setting("enchanted-golden-apple-scale", 1f, 0.1f..5f)  { enchantedGApple }
    private val enchantedGAppleX by setting("enchanted-golden-apple-x-rotation", 0f, -10f..10f)  { enchantedGApple }
    private val enchantedGAppleY by setting("enchanted-golden-apple-y-rotation", 0f, -10f..10f)  { enchantedGApple }
    private val enchantedGAppleZ by setting("enchanted-golden-apple-z-rotation", 0f, -10f..10f)  { enchantedGApple }

    private val power5Bow by setting("power-5-bow", false)
    private val power5BowScale by setting("power-5-bow-scale", 1f, 0.1f..5f) { power5Bow }
    private val power5BowX by setting("power-5-bow-x-rotation", 0f, -10f..10f) { power5Bow }
    private val power5BowY by setting("power-5-bow-y-rotation", 0f, -10f..10f) { power5Bow }
    private val power5BowZ by setting("power-5-bow-z-rotation", 0f, -10f..10f) { power5Bow }

    private val punch3Bow by setting("punch-3-bow", false)
    private val punch3BowScale by setting("punch-3-bow-scale", 1f, 0.1f..5f) { punch3Bow }
    private val punch3BowX by setting("punch-3-bow-x-rotation", 0f, -10f..10f) { punch3Bow }
    private val punch3BowY by setting("punch-3-bow-y-rotation", 0f, -10f..10f) { punch3Bow }
    private val punch3BowZ by setting("punch-3-bow-z-rotation", 0f, -10f..10f) { punch3Bow }

    private val flameBow by setting("flame-bow", false)
    private val flameBowScale by setting("flame-bow-scale", 1f, 0.1f..5f) { flameBow }
    private val flameBowX by setting("flame-bow-x-rotation", 0f, -10f..10f) { flameBow }
    private val flameBowY by setting("flame-bow-y-rotation", 0f, -10f..10f) { flameBow }
    private val flameBowZ by setting("flame-bow-z-rotation", 0f, -10f..10f) { flameBow }

    private val endCrystal by setting("end-crystal", false)
    private val endCrystalScale by setting("end-crystal-scale", 1f, 0.1f..5f) { endCrystal }
    private val endCrystalX by setting("end-crystal-x-rotation", 0f, -10f..10f) { endCrystal }
    private val endCrystalY by setting("end-crystal-y-rotation", 0f, -10f..10f) { endCrystal }
    private val endCrystalZ by setting("end-crystal-z-rotation", 0f, -10f..10f) { endCrystal }

    private val sharp8 by setting("sharp-5", false)
    private val sharp8Scale by setting("sharp-5-scale", 1f, 0.1f..5f) { sharp8 }
    private val sharp8X by setting("sharp-5-x-rotation", 0f, -10f..10f) { sharp8 }
    private val sharp8Y by setting("sharp-5-y-rotation", 0f, -10f..10f) { sharp8 }
    private val sharp8Z by setting("sharp-5-z-rotation", 0f, -10f..10f) { sharp8 }
}
