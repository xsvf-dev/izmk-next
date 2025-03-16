package ovo.xsvf.izmk.gui.screen

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.MousePositionConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.GradientEffect
import gg.essential.elementa.state.v2.stateOf
import java.awt.Color

class TestScreen : WindowScreen(ElementaVersion.V8) {
    private val text = UIWrappedText("Hello, world!")
        .constrain {
            x = MousePositionConstraint() + 10.pixels()
            y = MousePositionConstraint() + 10.pixels()
        } effect GradientEffect(
        stateOf(Color.WHITE), stateOf(Color.BLUE),
        stateOf(Color.WHITE), stateOf(Color.BLUE)
    ) childOf window

    init {

    }

    override fun isPauseScreen() = false
}