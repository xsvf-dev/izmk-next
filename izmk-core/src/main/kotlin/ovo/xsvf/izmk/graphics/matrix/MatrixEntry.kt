package ovo.xsvf.izmk.graphics.matrix

import org.joml.Matrix4f

data class MatrixEntry(
    val mvpMatrix: Matrix4f,
    val positionMatrix: Matrix4f
) {

    constructor(): this(Matrix4f(), Matrix4f())
    constructor(matrixEntry: MatrixEntry):
            this(matrixEntry.mvpMatrix, Matrix4f(matrixEntry.positionMatrix))

}