package ovo.xsvf.izmk.resource

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.util.EncryptUtil
import java.io.InputStream
import java.nio.file.Path

object ResourceUtil {
    private val resources = HashMap<Path, ByteArray>()

    fun init(jar: Path) {
        IZMK.logger.info("Loading resources from ${jar.toAbsolutePath()}")
        val func: (String, ByteArray) -> Unit = { name, data ->
            resources[Path.of(name)] = data
        }
        if (IZMK.obfuscated) EncryptUtil.getBinaryFilesEncrypted(jar.toFile(), func)
        else EncryptUtil.getBinaryFiles(jar.toFile(), func)
    }

    fun has(name: Path): Boolean {
        return resources.containsKey(name)
    }

    fun getByName(name: Path): ByteArray? {
        return resources[name]
    }

    fun getAsStream(name: Path): InputStream? {
        return resources[name]?.inputStream()
    }
}