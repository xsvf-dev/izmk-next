package ovo.xsvf.izmk.graphics.font

import org.lwjgl.opengl.GL45.GL_TRIANGLES
import ovo.xsvf.izmk.graphics.buffer.VertexBufferObjects
import ovo.xsvf.izmk.graphics.buffer.drawArrays
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers.fontMode
import ovo.xsvf.izmk.graphics.font.general.GlyphChunk
import ovo.xsvf.izmk.graphics.shader.impl.FontShader
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D

class FontRenderer(
    internal val font: FontAdapter
) {
    fun drawString(
        text: String, x: Float, y: Float,
        color0: ColorRGB,
        shadow: Boolean = false,
        scale0: Float = 1f,
        backFont: FontRenderer? = null
    ): Float {
        var continueIndex = -1
        var color = color0

        val scale = scale0 / 40f * FONT_SIZE

        var width = 0f

        when (fontMode) {
            FontMode.GENERAL -> {
                text.forEachIndexed { index, ch ->
                    if (index == continueIndex) {
                        continueIndex = -1
                        color = getColor(ch)
                        return@forEachIndexed
                    }

                    if (ch == '§') {
                        continueIndex = index + 1
                        return@forEachIndexed
                    }

                    val prevWidth =
                        if (canDisplay(ch)) drawChar(ch, x + width, y, color, shadow, scale)
                        else backFont?.drawChar(ch, x + width, y, color, shadow, scale) ?: 0f

                    width += prevWidth
                }
            }

            FontMode.SPARSE -> {
                font.sparse.tex.bind()
                FontShader.textureUnit = font.sparse.tex.handle

                VertexBufferObjects.RenderFont.drawArrays(GL_TRIANGLES) {
                    text.forEachIndexed { index, ch ->
                        if (index == continueIndex) {
                            continueIndex = -1
                            color = getColor(ch)
                            return@forEachIndexed
                        }

                        if (ch == '§') {
                            continueIndex = index + 1
                            return@forEachIndexed
                        }

                        val prevWidth =
                            if (canDisplay(ch)) drawCharSparse(ch, x + width, y, color, shadow, scale)
                            else 0f // fixme: backFont?.drawCharSparse(ch, x + width, y, color, shadow, scale) ?: 0f

                        width += prevWidth
                    }
                }

                font.sparse.tex.unbind()
                FontShader.textureUnit = null
            }
        }

        return width
    }

    /**
     * Draw a string with a custom font, but reversed.
     */
    fun drawStringRev(
        text: String, x: Float, y: Float,
        color0: ColorRGB,
        shadow: Boolean = false,
        scale0: Float = 1f,
        backFont: FontRenderer? = null
    ): Float {
        val width = getStringWidth(text, scale0, backFont)
        drawString(text, x - width, y, color0, shadow, scale0, backFont)
        return width
    }

    /**
     * Get the width of a string with a custom font.
     * @param text The text to measure.
     * @param scale0 The scale of the text.
     * @param backFont The font to use for characters that cannot be displayed.
     * @return The width of the text.
     */
    fun getStringWidth(text: String, scale0: Float = 1f, backFont: FontRenderer? = null): Float {
        var width = 0f
        var shouldContinue = false

        val scale = scale0 / 40f * FONT_SIZE

        for (i in 0 until text.length) {
            if (shouldContinue) {
                shouldContinue = false
                continue
            }
            if (text[i] == '§' && i < text.length - 1) {
                shouldContinue = true
                continue
            }

            val ch = text[i]

            width += if (font.canDisplay(ch)) {
                font.getCharData(ch)?.let { it.width * scale } ?: 0f
            } else {
                backFont?.font?.getCharData(ch)?.let { it.width * scale } ?: 0f
            }
        }
        return width
    }

    fun canDisplay(ch: Char): Boolean = font.canDisplay(ch)

    fun drawChar(ch: Char, x: Float, y: Float, color: ColorRGB, shadow: Boolean, scale: Float): Float {
        val chunk = font.general.getChunk(ch.code / GlyphChunk.CHUNK_SIZE)

        val charData = chunk.charData[ch] ?: return 0f

        val width = charData.width * scale
        val height = charData.height * scale

        if (shadow)
            RenderUtils2D.drawTextureRect(x + 1, y + 1, width, height,
                charData.uStart, charData.vStart, charData.uEnd, charData.vEnd,
                chunk.texture, ColorRGB(0, 0, 0))

        RenderUtils2D.drawTextureRect(x, y, width, height,
            charData.uStart, charData.vStart, charData.uEnd, charData.vEnd,
            chunk.texture, color)

        return width
    }

    fun VertexBufferObjects.RenderFont.drawCharSparse(
        ch: Char, x: Float, y: Float,
        color: ColorRGB, shadow: Boolean, scale: Float
    ): Float {
        val charData = font.getCharData(ch) ?: return 0f

        val width = charData.width * scale
        val height = charData.height * scale

        val startX = x
        val startY = y
        val endX = x + width
        val endY = y + height

        val startU = charData.uStart
        val startV = charData.vStart
        val endU = charData.uEnd
        val endV = charData.vEnd

        val chunk = font.sparse.getChunk(ch).toFloat()

        if (shadow) {
            val startX = x + 1
            val startY = y + 1
            val endX = x + width + 1
            val endY = y + height + 1

            texture(startX, startY, startU, startV, chunk, ColorRGB(0, 0, 0))
            texture(endX, startY, endU, startV, chunk, ColorRGB(0, 0, 0))
            texture(endX, endY, endU, endV, chunk, ColorRGB(0, 0, 0))
            texture(startX, startY, startU, startV, chunk, ColorRGB(0, 0, 0))
            texture(startX, endY, startU, endV, chunk, ColorRGB(0, 0, 0))
            texture(endX, endY, endU, endV, chunk, ColorRGB(0, 0, 0))
        }

        // Triangles mode
        texture(startX, startY, startU, startV, chunk, color)
        texture(endX, startY, endU, startV, chunk, color)
        texture(endX, endY, endU, endV, chunk, color)
        texture(startX, startY, startU, startV, chunk, color)
        texture(startX, endY, startU, endV, chunk, color)
        texture(endX, endY, endU, endV, chunk, color)

        return width
    }

    fun getHeight(scale: Float = 1f): Float {
        return font.getHeight() * scale / 40f * FONT_SIZE
    }

    fun getColor(ch: Char): ColorRGB =
        ci[ch]?.let { return ColorRGB(it) } ?: ColorRGB.WHITE

    companion object {
        internal const val FONT_SIZE = 12f
        
        val ci: MutableMap<Char, Int> = mutableMapOf<Char, Int>().also {
            it['0'] = 0x000000FF.toInt()
            it['1'] = 0x0000AAFF.toInt()
            it['2'] = 0x00AA00FF.toInt()
            it['3'] = 0x00AAAAFF.toInt()
            it['4'] = 0xAA0000FF.toInt()
            it['5'] = 0xAA00AAFF.toInt()
            it['6'] = 0xFFAA00FF.toInt()
            it['7'] = 0xAAAAAAFF.toInt()
            it['8'] = 0x555555FF.toInt()
            it['9'] = 0x5555FFFF.toInt()

            it['a'] = 0x55FF55FF.toInt()
            it['b'] = 0x55FFFFFF.toInt()
            it['c'] = 0xFF5555FF.toInt()
            it['d'] = 0xFF55FFFF.toInt()
            it['e'] = 0xFFFF55FF.toInt()
            it['f'] = 0xFFFFFFFF.toInt()

            it['A'] = 0x55FF55FF.toInt()
            it['B'] = 0x55FFFFFF.toInt()
            it['C'] = 0xFF5555FF.toInt()
            it['D'] = 0xFF55FFFF.toInt()
            it['E'] = 0xFFFF55FF.toInt()
            it['F'] = 0xFFFFFFFF.toInt()
        }
    }

}
