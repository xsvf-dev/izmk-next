package ovo.xsvf.izmk.graphics

interface GlObject {

    var id: Int

    fun bind()
    fun unbind()
    fun delete()

}