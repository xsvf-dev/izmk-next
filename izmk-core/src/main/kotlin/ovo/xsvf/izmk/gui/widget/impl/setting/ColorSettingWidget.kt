package ovo.xsvf.izmk.gui.widget.impl.setting

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.color.ColorUtils
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.settings.ColorSetting

class ColorSettingWidget(
    screen: GuiScreen,
    override val setting: ColorSetting
) : AbstractSettingWidget(screen, setting) {
    private var isExtended = false
    private var draggingAlpha = false
    private var draggingHue = false
    private var draggingColor = false

    // 尺寸参数
    private val colorPickerSize = 100f
    private val alphaBarWidth = 16f
    private val hueBarWidth = 16f
    private val previewWidth = 80f
    private val elementHeight = 20f
    private val padding = 4f
    private val componentSpacing = 8f

    private lateinit var colorPickerArea: Area
    private lateinit var alphaBarArea: Area
    private lateinit var hueBarArea: Area
    private lateinit var previewArea: Area

    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        val startY = renderY + elementHeight + padding

        // 布局计算
        colorPickerArea = Area(
            renderX + padding,
            startY,
            colorPickerSize,
            colorPickerSize
        )

        hueBarArea = Area(
            colorPickerArea.right + componentSpacing,
            startY,
            hueBarWidth,
            colorPickerSize
        )

        alphaBarArea = Area(
            hueBarArea.right + componentSpacing,
            startY,
            alphaBarWidth,
            colorPickerSize
        )

        previewArea = Area(
            alphaBarArea.right + componentSpacing,
            startY,
            previewWidth,
            colorPickerSize
        )

        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)

        // 头部显示
        fontMulti.addText(
            "${setting.name.translation}: ${setting.value}",
            renderX + padding,
            renderY + padding,
            ColorRGB.WHITE
        )

        if (isExtended) {
            // 交互更新
            if (draggingAlpha) updateAlphaFromMouse(mouseY)
            else if (draggingHue) updateHueFromMouse(mouseY)
            else if (draggingColor) updateColorFromPicker(mouseX, mouseY)

            // 绘制组件
            drawColorPicker(colorPickerArea, rectMulti)
            drawHueBar(hueBarArea, rectMulti)
            drawAlphaBar(alphaBarArea, rectMulti)
            drawPreview(previewArea, fontMulti, rectMulti)
        }
    }

    private fun drawPreview(area: Area, fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw) {
        // 背景和边框
        rectMulti.addRect(area.left, area.top, area.width, area.height, ColorRGB(40, 40, 40))

        // 颜色方块
        val colorBoxSize = 32f
        rectMulti.addRect(
            area.centerX - colorBoxSize/2,
            area.top + 10f,
            colorBoxSize,
            colorBoxSize,
            setting.value
        )

        // 颜色值显示
        val hexText = setting.value.run { "#%02X%02X%02X%02X".format(a, r, g, b) }
        val alphaText = setting.value.run { "${(aFloat * 100).toInt()}%" }
        fontMulti.addText(
            hexText,
            area.left + 14f,
            area.top + colorBoxSize + 16f,
            ColorRGB.WHITE
        )
        fontMulti.addText(
            alphaText,
            area.left + 30f,
            area.top + colorBoxSize + 16f + FontRenderers.DRAW_FONT_SIZE,
            ColorRGB.WHITE
        )
    }

    private fun drawColorPicker(area: Area, rectMulti: PosColor2DMultiDraw) {
        val hsColor = ColorUtils.hsbToRGB(setting.value.hue, 1f, 1f)
        rectMulti.addRectGradientHorizontal(area.left, area.top, area.width, area.height, ColorRGB.WHITE, hsColor)
        rectMulti.addRectGradientVertical(area.left, area.top, area.width, area.height, ColorRGB.BLACK.alpha(0), ColorRGB.BLACK)

        val indicatorX = area.left + setting.value.saturation * area.width
        val indicatorY = area.top + (1f - setting.value.brightness) * area.height
        rectMulti.addRect(indicatorX - 2f, indicatorY - 2f, 4f, 4f, ColorRGB.WHITE)
    }

    private fun drawAlphaBar(area: Area, rectMulti: PosColor2DMultiDraw) {
        rectMulti.addRectGradientVertical(
            area.left, area.top, area.width, area.height,
            setting.value.alpha(1f),
            setting.value.alpha(0f)
        )
        val indicatorY = area.top + (1f - setting.value.aFloat) * area.height
        rectMulti.addRect(area.left - 1f, indicatorY - 1f, area.width + 2f, 2f, ColorRGB.WHITE)
    }

    private fun drawHueBar(area: Area, rectMulti: PosColor2DMultiDraw) {
        val segmentHeight = area.height / 6f
        val colors = arrayOf(
            ColorRGB(255, 0, 0), ColorRGB(255, 0, 255),
            ColorRGB(0, 0, 255), ColorRGB(0, 255, 255),
            ColorRGB(0, 255, 0), ColorRGB(255, 255, 0),
            ColorRGB(255, 0, 0)
        )

        for (i in 0 until 6) {
            rectMulti.addRectGradientVertical(
                area.left,
                area.top + i * segmentHeight,
                area.width,
                segmentHeight,
                colors[i],
                colors[i + 1]
            )
        }
        val indicatorY = area.top + (1f - setting.value.hue) * area.height
        rectMulti.addRect(area.left - 1f, indicatorY - 1f, area.width + 2f, 2f, ColorRGB.WHITE)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        if (!isLeftClick) {
            isExtended = !isExtended
            return
        }
        if (!isExtended) return

        when {
            alphaBarArea.contains(mouseX, mouseY) -> draggingAlpha = true
            hueBarArea.contains(mouseX, mouseY) -> draggingHue = true
            colorPickerArea.contains(mouseX, mouseY) -> {
                draggingColor = true
                updateColorFromPicker(mouseX, mouseY)
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, isLeftClick: Boolean): Boolean {
        if (draggingAlpha || draggingHue || draggingColor) {
            draggingAlpha = false
            draggingHue = false
            draggingColor = false
            return true
        }
        return false
    }

    override fun getHeight(): Float = if (isExtended) elementHeight + padding + colorPickerSize + padding else elementHeight

    private fun updateColorFromPicker(mouseX: Float, mouseY: Float) {
        val saturation = (mouseX - colorPickerArea.left) / colorPickerArea.width
        val brightness = 1f - (mouseY - colorPickerArea.top) / colorPickerArea.height
        setting.value(ColorUtils.hsbToRGB(
            setting.value.hue,
            saturation.coerceIn(0f, 1f),
            brightness.coerceIn(0f, 1f),
            setting.value.aFloat
        ))
    }

    private fun updateAlphaFromMouse(mouseY: Float) {
        if (mouseY in alphaBarArea.top..alphaBarArea.bottom) {
            val alpha = 1f - (mouseY - alphaBarArea.top) / alphaBarArea.height
            setting.value(setting.value.alpha(alpha.coerceIn(0f, 1f)))
        }
    }

    private fun updateHueFromMouse(mouseY: Float) {
        if (mouseY in hueBarArea.top..hueBarArea.bottom) {
            val hue = 1f - (mouseY - hueBarArea.top) / hueBarArea.height
            setting.value(ColorUtils.hsbToRGB(
                hue,
                setting.value.saturation,
                setting.value.brightness,
                setting.value.aFloat
            ))
        }
    }

    private data class Area(val left: Float, val top: Float, val width: Float, val height: Float) {
        val right get() = left + width
        val bottom get() = top + height
        val centerX get() = left + width/2

        fun contains(x: Float, y: Float) = x in left..right && y in top..bottom
    }
}
