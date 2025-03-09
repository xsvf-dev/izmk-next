package ovo.xsvf.izmk.gui.widget.impl.setting

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.color.ColorUtils
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

    private var screenWidth = -1f
    private var screenHeight = -1f
    private var renderX = -1f
    private var renderY = -1f

    // 颜色选择区域尺寸
    private val colorPickerSize = 100f
    private val alphaBarWidth = 12f
    private val hueBarWidth = 12f

    // 布局参数
    private val padding = 2f
    private val elementHeight = 20f

    private lateinit var colorPickerArea: Area
    private lateinit var alphaBarArea: Area
    private lateinit var hueBarArea: Area

    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
        this.renderX = renderX
        this.renderY = renderY

        colorPickerArea = Area(
            renderX + padding,
            renderY + elementHeight + padding,
            colorPickerSize,
            colorPickerSize
        )

        hueBarArea = Area(
            renderX + padding + colorPickerSize + padding,
            renderY + elementHeight + padding,
            hueBarWidth,
            colorPickerSize
        )

        alphaBarArea = Area(
            renderX + padding + colorPickerSize + hueBarWidth + padding * 2,
            renderY + elementHeight + padding,
            alphaBarWidth,
            colorPickerSize
        )

        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)

        // 显示当前颜色值
        fontMulti.addText(
            "${setting.name.translation}: ${setting.value}",
            renderX + 2f,
            renderY + 3f,
            ColorRGB.WHITE
        )

        if (isExtended) {
            // 如果正在拖动，更新颜色或者透明度值
            if (draggingAlpha) {
                updateAlphaFromMouse(mouseY)
            } else if (draggingHue) {
                updateHueFromMouse(mouseY)
            } else if (draggingColor) {
                updateColorFromPicker(mouseX, mouseY)
            }

            // 绘制颜色选择区域
            drawColorPicker(renderX + padding, renderY + elementHeight + padding,
                colorPickerSize, colorPickerSize, rectMulti)

            // 绘制透明度条
            drawAlphaBar(
                renderX + padding + colorPickerSize + hueBarWidth + padding * 2,
                renderY + elementHeight + padding,
                alphaBarWidth,
                colorPickerSize,
                rectMulti
            )

            // 绘制色相条
            drawHueBar(
                renderX + padding + colorPickerSize + padding,
                renderY + elementHeight + padding,
                hueBarWidth,
                colorPickerSize,
                rectMulti
            )
        }
    }

    private fun drawColorPicker(x: Float, y: Float, width: Float, height: Float, rectMulti: PosColor2DMultiDraw) {
        // 绘制饱和度/亮度区域
        val hsColor = ColorUtils.hsbToRGB(setting.value.hue, 1f, 1f)
        rectMulti.addRectGradientHorizontal(x, y, width, height, ColorRGB.WHITE, hsColor)
        rectMulti.addRectGradientVertical(x, y, width, height, ColorRGB.BLACK.alpha(0), ColorRGB.BLACK)

        // 绘制当前颜色指示器
        val pickerX = x + setting.value.saturation * width
        val pickerY = y + (1f - setting.value.brightness) * height
        rectMulti.addRect(pickerX - 2f, pickerY - 2f, 4f, 4f, ColorRGB.WHITE)
    }

    private fun drawAlphaBar(x: Float, y: Float, width: Float, height: Float, rectMulti: PosColor2DMultiDraw) {
        // 背景渐变
        rectMulti.addRectGradientVertical(x, y, width, height,
            setting.value.alpha(1f),
            setting.value.alpha(0f)
        )

        // 当前透明度指示器
        val alphaY = y + (1f - setting.value.aFloat) * height
        rectMulti.addRect(x - 1f, alphaY - 1f, width + 2f, 2f, ColorRGB.WHITE)
    }

    private fun drawHueBar(x: Float, y: Float, width: Float, height: Float, rectMulti: PosColor2DMultiDraw) {
        // 色相渐变条
        val segmentHeight = height / 6f
        val colors = arrayOf(
            ColorRGB(255, 0, 0),   // Red
            ColorRGB(255, 0, 255), // Magenta
            ColorRGB(0, 0, 255),    // Blue
            ColorRGB(0, 255, 255),  // Cyan
            ColorRGB(0, 255, 0),    // Green
            ColorRGB(255, 255, 0), // Yellow
            ColorRGB(255, 0, 0)     // Red
        )

        for (i in 0 until 6) {
            val startY = y + i * segmentHeight
            rectMulti.addRectGradientVertical(
                x, startY, width, segmentHeight,
                colors[i], colors[i + 1]
            )
        }

        // 当前色相指示器
        val hueY = y + (1f - setting.value.hue) * height
        rectMulti.addRect(x - 1f, hueY - 1f, width + 2f, 2f, ColorRGB.WHITE)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        if (!isLeftClick) {
            // 右键切换展开状态
            isExtended = !isExtended
            return
        }
        if (!isExtended) return

        when {
            alphaBarArea.contains(mouseX, mouseY) -> draggingAlpha = true
            hueBarArea.contains(mouseX, mouseY) -> draggingHue = true
            colorPickerArea.contains(mouseX, mouseY) -> {
                draggingColor = true
                updateColorFromPicker(mouseX, mouseY)  // 初始点击时立即更新
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

    private fun updateColorFromPicker(mouseX: Float, mouseY: Float) {
        val saturation = (mouseX - colorPickerArea.left) / colorPickerArea.width
        val brightness = 1f - (mouseY - colorPickerArea.top) / colorPickerArea.height

        val newColor = ColorUtils.hsbToRGB(
            setting.value.hue,
            saturation.coerceIn(0f, 1f),
            brightness.coerceIn(0f, 1f),
            setting.value.aFloat
        )
        setting.value(newColor)
    }

    override fun getHeight0(): Float = if (isExtended) 140f else 20f

    private fun updateAlphaFromMouse(mouseY: Float) {
        if (mouseY in alphaBarArea.top..alphaBarArea.top + alphaBarArea.height) {
            val alpha = 1f - (mouseY - alphaBarArea.top) / (alphaBarArea.height)
            setting.value(setting.value.alpha(alpha.coerceIn(0f, 1f)))
        }
    }

    private fun updateHueFromMouse(mouseY: Float) {
        if (mouseY in hueBarArea.top..hueBarArea.top + hueBarArea.height) {
            val hue = 1f - (mouseY - hueBarArea.top) / (hueBarArea.height)
            setting.value(ColorUtils.hsbToRGB(hue, setting.value.saturation, setting.value.brightness, setting.value.aFloat))
        }
    }

    private data class Area(val left: Float, val top: Float, val width: Float, val height: Float) {
        fun contains(x: Float, y: Float): Boolean {
            return x >= left && x <= left + width && y >= top && y <= top + height
        }
    }
}
