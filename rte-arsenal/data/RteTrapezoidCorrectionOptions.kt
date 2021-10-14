package io.agora.edu.core.internal.rte.data

import android.graphics.PointF
import io.agora.rtc2.TrapezoidCorrectionOptions

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
    fun convert(): TrapezoidCorrectionOptions {
        val options = TrapezoidCorrectionOptions()
        options.setDragSrcPoint(dragSrcPoint)
        options.setDragDstPoint(dragDstPoint)
        options.dragFinished = dragFinished
        options.showAssistLine(assistLine)
        options.autoCorrect = if (autoCorrect) 1 else 0
        options.resetDragPoints = resetDragPoints
        options.setDragSrcPoints(dragSrcPoints[0], dragSrcPoints[1], dragSrcPoints[2], dragSrcPoints[3])
        options.setDragDstPoints(dragDstPoints[0], dragDstPoints[1], dragDstPoints[2], dragDstPoints[3])
        return options
    }

    companion object {
        fun convert(options: TrapezoidCorrectionOptions): RteTrapezoidCorrectionOptions {
            val dragSrcPoint = PointF(options.dragSrcPoint[0], options.dragSrcPoint[1])
            val dragDstPoint = PointF(options.dragDstPoint[0], options.dragDstPoint[1])
            return RteTrapezoidCorrectionOptions(dragSrcPoint, dragDstPoint, options.dragFinished,
                    options.assistLine, options.autoCorrect == 1, options.resetDragPoints, arrayToList(options.dragSrcPoints),
                    arrayToList(options.dragDstPoints))
        }

        private fun arrayToList(array: FloatArray): MutableList<PointF> {
            val result = mutableListOf<PointF>()
            var index = 0
            while (true) {
                if (index > array.size) {
                    return result
                }
                val pointF = PointF(array[index], array[index + 1])
                result.add(pointF)
                index += 2
            }
        }
    }
}
