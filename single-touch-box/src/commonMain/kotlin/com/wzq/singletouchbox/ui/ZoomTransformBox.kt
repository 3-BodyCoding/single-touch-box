package com.wzq.singletouchbox.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged

/**
 * @param modifier A modifier instance to be applied to this Box outer layout
 * @param touchSingleBox Whether the changeable item inside the Box is being transformed.
 * @param onGestureChange This Box Zoom and Move Parameters
 * @param content This Box's page Composable.
 */
@Composable
fun ZoomTransformBox(
    modifier: Modifier,
    touchSingleBox: Boolean,
    onGestureChange: (Float, Offset) -> Unit,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    val maxScale by remember {
        mutableFloatStateOf(5f)
    }
    var scale by remember {
        mutableFloatStateOf(1f)
    }
    var offset by remember {
        mutableStateOf(Offset.Zero)
    }
    BoxWithConstraints(
        modifier
            .clipToBounds()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
            .pointerInput(touchSingleBox) {
                if (!touchSingleBox) awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val zoomChange = event.calculateZoom()
                        scale = (scale * zoomChange).coerceIn(1f, maxScale)
                        if (scale > 1f) {
                            var panChange = event.calculatePan()
                            panChange *= scale
                            var x = offset.x
                            val deltaX = scale * size.width - size.width
                            x = (offset.x + panChange.x).coerceIn(-deltaX / 2, deltaX / 2)
                            var y = offset.y
                            val deltaY = scale * size.height - size.height
                            y = (offset.y + panChange.y).coerceIn(-deltaY / 2, deltaY / 2)
                            val isEdgeX = x != offset.x
                            if (isEdgeX || y != offset.y) {
                                offset = Offset(x, y)
                            }
                            if (event.changes.size > 1 || isEdgeX) {
                                onGestureChange(scale, offset)
                                event.changes.forEach {
                                    if (it.positionChanged()) {
                                        it.consume()
                                    }
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    if (scale == 1f) {
                        offset = Offset.Zero
                    }
                }
            }
//        .pointerInput(Unit) {
//            detectTapGestures(
//                onDoubleTap = {
//                    offset = Offset.Zero
//                    scale = if (scale != 1.0f) {
//                        1.0f
//                    } else {
//                        min(2f, maxScale)
//                    }
//                }, onTap = {
//                    onGestureTap()
//                }
//            )
//        }
    ) {
        content()
    }
}