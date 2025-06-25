package com.wzq.singletouchbox.ex

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.wzq.singletouchbox.state.TouchCorner
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Get the size of the outer rectangle after rotating the rectangle.
 */
internal fun calculateRotatedSize(
    originalWidth: Float,
    originalHeight: Float,
    cos: Double,
    sin: Double
): Size {
    val cosTheta = abs(cos)
    val sinTheta = abs(sin)
    val newWidth = originalWidth * cosTheta + originalHeight * sinTheta
    val newHeight = originalWidth * sinTheta + originalHeight * cosTheta
    return Size(newWidth.toFloat(), newHeight.toFloat())
}

/**
 * Distance traveled after point rotation
 */
internal fun Offset.rotateBy(cosTheta: Double, sinTheta: Double): Offset {
    return Offset((x * cosTheta - y * sinTheta).toFloat(), (x * sinTheta + y * cosTheta).toFloat())
}

internal fun rotate(angle: Float): Pair<Double, Double> {
    val angleInRadians = angle * (PI / 180)
    val cos = cos(angleInRadians)
    val sin = sin(angleInRadians)
    return Pair(cos, sin)
}

/**
 * One-finger zoom: change based on distance from rotationCenter (only works in the corners)
 */
private fun PointerEvent.calculateZoomSingle(
    change: PointerInputChange,
    rotationCenter: Offset
): Float {
    val curr = change.position
    val prev = change.previousPosition
    val prevDist = (prev - rotationCenter).getDistance().coerceAtLeast(1f)
    val currDist = (curr - rotationCenter).getDistance()
    return currDist / prevDist
}

/**
 * Single-finger rotation: Angle change based on relative rotationCenter (only works in the four corners)
 */
private fun PointerEvent.calculateRotationSingle(
    change: PointerInputChange,
    rotationCenter: Offset
): Float {
    val curr = change.position
    val prev = change.previousPosition
    val prevOffset = prev - rotationCenter
    val currOffset = curr - rotationCenter
    val prevAngle = atan2(prevOffset.y, prevOffset.x).toDegrees()
    val currAngle = atan2(currOffset.y, currOffset.x).toDegrees()
    var diff = currAngle - prevAngle
    if (diff > 180f) diff -= 360f
    if (diff < -180f) diff += 360f
    return diff
}

/**
 * Whether the detection point is in any corner area of the component
 */
private fun Offset.isInCorners(size: IntSize, cornerSize: Float): TouchCorner {
    val rightEdge = size.width - cornerSize
    val bottomEdge = size.height - cornerSize
    val inLeftRegion = x <= cornerSize
    val inRightRegion = x >= rightEdge
    val inTopRegion = y <= cornerSize
    val inBottomRegion = y >= bottomEdge
    return when {
        inLeftRegion && inTopRegion -> TouchCorner.TopStart
        inRightRegion && inTopRegion -> TouchCorner.TopEnd
        inLeftRegion && inBottomRegion -> TouchCorner.BottomStart
        inRightRegion && inBottomRegion -> TouchCorner.BottomEnd
        else -> TouchCorner.Other
    }
}

/**
 * radian number of revolutions (math.)
 */
private fun Float.toDegrees(): Float = this * 180f / PI.toFloat()

/**
 * Single-finger rotation, scaling and translation, known rotation center rotationCenter
 */
suspend fun PointerInputScope.detectTransformGestures(
    cornerSize: Float,
    hasTopEndControl: Boolean = false,
    hasBottomStartControl: Boolean = false,
//    onClickStartTop: (() -> Unit)? = null,
    onGestureStart: () -> Unit,
    onGestureEnd: () -> Unit,
    onGesture: (
        pan: Offset,
        zoom: Float,
        rotation: Float
    ) -> Unit
) {
    awaitEachGesture {
        var rotationAcc = 0f
        var zoomAcc = 1f
        var panAcc = Offset.Zero
        var pastTouchSlop = false
        val rotationCenter = Offset(size.width / 2f, size.height / 2f)
        val touchSlop = 1f //viewConfiguration.touchSlop
        val firstDown = awaitFirstDown(requireUnconsumed = false)
//        val longPressTimeout = viewConfiguration.longPressTimeoutMillis
//        val startTime = firstDown.uptimeMillis
//        val startPos = firstDown.position
//        val clickCandidate = startPos.x <= cornerSize && startPos.y <= cornerSize
//        if (clickCandidate && onClickStartTop != null) {
//            val event = awaitPointerEvent()
//            val upPointer = event.changes.firstOrNull { !it.pressed }
//            val endTime = upPointer?.uptimeMillis ?: startTime
//            val duration = endTime - startTime
//            val endPos = upPointer?.position ?: startPos
//            if (duration < longPressTimeout
//                && endPos.x <= cornerSize && endPos.y <= cornerSize
//            ) {
//                onClickStartTop()
//            }
//        } else {
        onGestureStart()
        val touchCorner = firstDown.position.isInCorners(size, cornerSize)
        val touchBottomEnd = touchCorner == TouchCorner.BottomEnd
        val touchTopStart = touchCorner == TouchCorner.TopStart
        val touchTopEnd = hasTopEndControl && touchCorner == TouchCorner.TopEnd
        val touchBottomStart = hasBottomStartControl && touchCorner == TouchCorner.BottomStart
        val noPan = touchTopStart || touchTopEnd || touchBottomStart
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.fastAny { it.isConsumed }
            if (!canceled) {
                val change =
                    event.changes.firstOrNull { it.pressed && it.previousPressed } ?: break
                // Single-finger mode: rotation/zoom based on distance from center, bottom right area only
                val rotationChange = if (touchBottomEnd) event.calculateRotationSingle(
                    change,
                    rotationCenter
                ) else 0f
                val zoomChange = if (touchBottomEnd) event.calculateZoomSingle(
                    change,
                    rotationCenter
                ) else 1f

                val isTransform = rotationChange != 0f || zoomChange != 1f
                val panChange =
                    if (isTransform || noPan) Offset.Zero else event.calculatePan()

                if (!pastTouchSlop) {
                    rotationAcc += rotationChange
                    zoomAcc *= zoomChange
                    panAcc += panChange
                    val centroidSize = (change.position - rotationCenter).getDistance()
                        .coerceAtLeast(1f)
                    val zoomMotion = abs(1 - zoomAcc) * centroidSize
                    val rotationMotion = abs(
                        rotationAcc * PI.toFloat() * centroidSize / 180f
                    )
                    val panMotion = panAcc.getDistance()
                    if (
                        zoomMotion > touchSlop ||
                        rotationMotion > touchSlop ||
                        panMotion > touchSlop
                    ) {
                        pastTouchSlop = true
                    }
                }

                if (pastTouchSlop) {
                    val effectiveRotation = rotationChange
                    if (
                        effectiveRotation != 0f || zoomChange != 1f || panChange != Offset.Zero
                    ) {
                        onGesture(panChange, zoomChange, effectiveRotation)
                    }
                    event.changes.fastForEach {
                        if (it.positionChanged()) it.consume()
                    }
                }
            }
        } while (!canceled && event.changes.fastAny { it.pressed })
        onGestureEnd()
//        }
    }
}