package ovo.xsvf

/**
 * @author LangYa466
 * @since 2025/2/17
 */
data class Pair<T, U>(
    @JvmField var first: T,
    @JvmField var second: U
) {
    companion object {
        @JvmStatic
        fun <T, U> of(first: T, second: U): Pair<T, U> = Pair(first, second)
    }
}
