package com.wzq.singletouchbox.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

class TouchTransformData(
    val key: String,
    center: Offset,
    val page: Int = 0,
    rotate: Float = 0F,
    zoom: Float = 1F
) {
    var centerPoint by mutableStateOf(center)
        private set
    var rotate by mutableFloatStateOf(rotate)
        private set
    var zoom by mutableFloatStateOf(zoom)
        private set
    var offset by mutableStateOf(Offset.Zero)
        internal set
    var editable by mutableStateOf(false)
        internal set
    internal var contentSize: Size = Size.Zero

    internal fun updateOffset(newPan: Offset) {
        offset += newPan
        centerPoint += newPan
    }

    internal fun updateRotate(newRotate: Float) {
        rotate += newRotate
    }

    internal fun updateZoom(newZoom: Float) {
        zoom = newZoom
    }

    internal fun initContentSize(size: Size, controlSize: Float, scale: Float) {
        if (contentSize == Size.Zero) {
            contentSize = size
            zoom = scale
            val contentSizeHalf = Size(size.width + controlSize, size.height + controlSize) / 2f
            offset = Offset(
                centerPoint.x - contentSizeHalf.width,
                centerPoint.y - contentSizeHalf.height
            )
        }
    }
}