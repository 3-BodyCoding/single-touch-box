package com.wzq.singletouchbox.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.toSize
import com.wzq.singletouchbox.ex.calculateRotatedSize
import com.wzq.singletouchbox.ex.detectTransformGestures
import com.wzq.singletouchbox.ex.rotate
import com.wzq.singletouchbox.ex.rotateBy
import com.wzq.singletouchbox.exception.MinInitContentSizeException
import com.wzq.singletouchbox.state.SingleTouchState
import com.wzq.singletouchbox.state.TouchTransformData
import kotlin.math.min

@Composable
internal fun TouchBox(
    state: SingleTouchState,
    item: TouchTransformData,
    controlDpSize: Dp,
    borderColor: Color,
    borderWidth: Dp,
    removeControl: @Composable (String) -> Unit,
    zoomRotateControl: @Composable (String) -> Unit,
    topEndControl: (@Composable (String) -> Unit)? = null,
    bottomStartControl: (@Composable (String) -> Unit)? = null,
    itemContent: @Composable (TouchTransformData) -> Unit
) {
    val parentSize = state.parentSize
    val initContentSize = state.initContentSize
    val zoomRange = state.zoomRange
    val density = LocalDensity.current
    val controlSize = with(density) { controlDpSize.toPx() }
    Box(
        Modifier
            .graphicsLayer {
                scaleX = item.zoom
                scaleY = item.zoom
                rotationZ = item.rotate
                translationX = item.offset.x
                translationY = item.offset.y
            }
            .pointerInput(item.editable) {
                if (item.editable) detectTransformGestures(
                    controlSize,
                    topEndControl != null,
                    bottomStartControl != null,
                    onGestureStart = {
                        state.gestureChanging = true
                    },
                    onGestureEnd = {
                        state.gestureChanging = false
                    }
                ) { pan, gestureZoom, gestureRotate ->
                    val contentSize = item.contentSize
                    val rotate = item.rotate
                    val zoom = item.zoom
                    val center = item.centerPoint
                    val offset = item.offset
                    val (cos, sin) = rotate(rotate)
                    val size = contentSize * zoom
                    val newBoxSize = calculateRotatedSize(
                        size.width,
                        size.height,
                        cos, sin
                    )
                    val tempSize = newBoxSize / 2f
                    val boxRect =
                        Rect(
                            center - Offset(tempSize.width, tempSize.height),
                            newBoxSize
                        )
                    boxRect.translate(
                        offset.x - (tempSize.width - contentSize.width / 2f),
                        offset.y - (tempSize.height - contentSize.height / 2f)
                    )
                    if (pan != Offset.Zero) {
                        val minLeft = center.x - boxRect.size.width / 2
                        val maxRight =
                            parentSize.width - (center.x + boxRect.size.width / 2)
                        val minTop = center.y - boxRect.size.height / 2
                        val maxBottom =
                            parentSize.height - (center.y + boxRect.size.height / 2)
                        var newPan = (pan * zoom).rotateBy(cos, sin)
                        newPan = Offset(
                            newPan.x.coerceIn(-minLeft, maxRight),
                            newPan.y.coerceIn(-minTop, maxBottom)
                        )
                        item.updateOffset(newPan)
                    } else {
                        val maxScaleSize =
                            minOf(
                                parentSize.width - boxRect.right,
                                parentSize.height - boxRect.bottom,
                                boxRect.left,
                                boxRect.top
                            )
                        val maxSize =
                            Size(
                                newBoxSize.width + maxScaleSize,
                                newBoxSize.height + maxScaleSize
                            )
                        val newZoom =
                            min(
                                maxSize.width / newBoxSize.width,
                                maxSize.height / newBoxSize.height
                            )
                        item.updateZoom(
                            (zoom * if (gestureZoom <= newZoom) gestureZoom else newZoom).coerceIn(
                                zoomRange
                            )
                        )
                        item.updateRotate(gestureRotate)
                    }
                }
            }
    ) {
        val controlPadding = controlDpSize / 2f
        val key = item.key
        Box(Modifier.padding(controlPadding).onSizeChanged {
            if (it.width == 1 && it.height == 1) return@onSizeChanged //Wasm and JS first load 1px * 1px
            val contentSize = it.toSize()
            val size = if (initContentSize == Size.Zero) parentSize / 2f
            else {
                if (initContentSize.width <= controlSize || initContentSize.height <= controlSize)
                    throw MinInitContentSizeException()
                initContentSize
            }
            val scale = min(size.width / contentSize.width, size.height / contentSize.height)
            item.initContentSize(contentSize, controlSize, scale)
        }) {
            itemContent(item)
        }
        if (item.editable) {
            val zoom = item.zoom
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(controlPadding)
                    .border(borderWidth / zoom, borderColor)
            )
            val offsetScale = 1 / zoom
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .size(controlDpSize)
                    .graphicsLayer {
                        scaleX = offsetScale
                        scaleY = offsetScale
                    },
                contentAlignment = Alignment.Center
            ) { removeControl(key) }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(controlDpSize)
                    .graphicsLayer {
                        scaleX = offsetScale
                        scaleY = offsetScale
                    },
                contentAlignment = Alignment.Center
            ) { zoomRotateControl(key) }
            if (topEndControl != null) Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(controlDpSize)
                    .graphicsLayer {
                        scaleX = offsetScale
                        scaleY = offsetScale
                    },
                contentAlignment = Alignment.Center
            ) { topEndControl(key) }
            if (bottomStartControl != null) Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .size(controlDpSize)
                    .graphicsLayer {
                        scaleX = offsetScale
                        scaleY = offsetScale
                    },
                contentAlignment = Alignment.Center
            ) { bottomStartControl(key) }
        }
    }
}