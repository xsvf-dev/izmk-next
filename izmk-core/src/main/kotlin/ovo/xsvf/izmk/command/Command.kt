package ovo.xsvf.izmk.command

open class Command(val name: String, val usage: String) {
    open fun run(args: Array<String>) {}
}
