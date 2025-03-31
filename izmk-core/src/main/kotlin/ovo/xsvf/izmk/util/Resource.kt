package ovo.xsvf.izmk.util

import ovo.xsvf.izmk.IZMK

class Resource(path: String) {
    val byteArr: ByteArray = javaClass.getResourceAsStream("${IZMK.ASSETS_DIRECTORY}/${path}")?.use { it.readBytes() }
        ?: throw IllegalArgumentException("Resource not found: ${IZMK.ASSETS_DIRECTORY}/${path}")
    val data: String get() = String(byteArr, Charsets.UTF_8)
}
