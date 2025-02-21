package ovo.xsvf.izmk.graphics.font

import ovo.xsvf.izmk.graphics.font.FontRenderers.fontMode
import ovo.xsvf.izmk.graphics.font.general.FontChunks
import ovo.xsvf.izmk.graphics.font.general.GlyphChunk
import ovo.xsvf.izmk.graphics.font.sparse.SparseFontGlyph
import java.awt.Font

class FontAdapter(
    font: Font
) {
    val general = FontChunks(font)
    val sparse = SparseFontGlyph(font, FontRenderers.DRAW_FONT_SIZE)

    fun getHeight(): Float = when (fontMode) {
        FontMode.GENERAL -> {
            general.getHeight()
        }

        FontMode.SPARSE -> {
            sparse.height
        }
    }

    fun getCharData(char: Char): CharData? {
        return when (fontMode) {
            FontMode.GENERAL -> {
                if (canDisplay(char)) general.getChunk(char.code / GlyphChunk.CHUNK_SIZE).charData[char] else null
            }

            FontMode.SPARSE -> {
                if (canDisplay(char)) sparse.getCharData(char) else null
            }
        }
    }

    fun canDisplay(char: Char): Boolean = when (fontMode) {
        FontMode.GENERAL -> {
            general.canDisplay(char)
        }
        FontMode.SPARSE -> {
            sparse.canDisplay(char)
        }
    }

}