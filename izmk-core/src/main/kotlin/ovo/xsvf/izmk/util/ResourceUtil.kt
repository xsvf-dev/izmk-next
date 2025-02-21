package ovo.xsvf.izmk.util

import ovo.xsvf.izmk.IZMK
import java.io.InputStream
import java.nio.file.Path

object ResourceUtil {
    private val resources = HashMap<String, ByteArray>()

    fun init(jar: Path) {
        IZMK.logger.info("Loading resources from ${jar.toAbsolutePath()}")
        val func: (String, ByteArray) -> Unit = { name, data ->
            resources[name] = data
            IZMK.logger.info("Loading resource: $name")
        }
        if (IZMK.Obfuscated) EncryptUtil.getBinaryFilesEncrypted(jar.toFile(), func)
        else EncryptUtil.getBinaryFiles(jar.toFile(), func)
    }

    fun has(name: String): Boolean {
        return resources.containsKey(name)
    }

    fun getByName(name: String): ByteArray? {
        return resources[name]
    }

    fun getAsStream(name: String): InputStream? {
        return resources[name]?.inputStream()
    }
}