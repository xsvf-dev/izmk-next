package ovo.xsvf.izmk.util.resources

import ovo.xsvf.izmk.IZMK

class Resource(
    path: String
) {
    val byteArr: ByteArray

    val data: String get() = String(byteArr, Charsets.UTF_8)

    init {
        val stream = javaClass.getResourceAsStream("${IZMK.ASSETS_DIRECTORY}/${path}")
            ?: throw IllegalArgumentException("Resource not found: ${IZMK.ASSETS_DIRECTORY}/${path}")

        byteArr = stream.readBytes()

        stream.close()
    }

}
