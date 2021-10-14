package io.agora.edu.core.internal.rte.data

import android.graphics.PointF

/**
 * dragSrcPoints->must follow this order: topLeft bottomLeft topRight bottomRight
 * dragDstPoints->must follow this order: topLeft bottomLeft topRight bottomRight
 * */
data class RteTrapezoidCorrectionOptions(
        val dragSrcPoint: PointF,
        val dragDstPoint: PointF,
        val dragFinished: Int,
        val assistLine: Int,
        val autoCorrect: Boolean,
        val resetDragPoints: Int,
        val dragSrcPoints: MutableList<PointF>,
        val dragDstPoints: MutableList<PointF>
) {
}
