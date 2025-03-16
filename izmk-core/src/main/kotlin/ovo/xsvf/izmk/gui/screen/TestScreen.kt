package ovo.xsvf.izmk.gui.screen

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.effects.OutlineEffect
import java.awt.Color

class TestScreen : WindowScreen(ElementaVersion.V8) {
    private val myTextBox = UIBlock(Color(0, 0, 0, 255))

    init {
        val container = UIContainer().constrain {
            x = RelativeConstraint(.25f)
            y = RelativeConstraint(.25f)
            width = RelativeConstraint(.5f)
            height = RelativeConstraint(.5f)
        } childOf window
        UIBlock(Color.RED /* java.awt.Color */).constrain {
            width = 10.pixels()
            height = 10.pixels()
        } childOf window effect OutlineEffect(Color.BLUE, 1f)
        myTextBox.constrain {
            x = 10.pixels()
            y = 10.pixels()
            width = 100.pixels()
            height = 100.pixels()
        } childOf container
    }
}