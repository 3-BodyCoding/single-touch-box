package com.wzq.singletouchbox.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.util.fastForEach
import com.wzq.singletouchbox.ex.ZOOM_RANGE
import com.wzq.singletouchbox.exception.MinZoomException
import com.wzq.singletouchbox.state.SingleTouchState
import com.wzq.singletouchbox.state.SingleTouchState.Companion.Saver
import com.wzq.singletouchbox.state.TouchTransformData

/**
 * @param initItemSize Initial size when new Item is added.
 * @param zoomRange Maximum and minimum range for Item scaling.
 */
@Composable
fun rememberSingleTouchState(
    initItemSize: Size = Size.Zero,
    zoomRange: ClosedFloatingPointRange<Float> = ZOOM_RANGE
): SingleTouchState {
    return rememberSaveable(saver = Saver) {
        SingleTouchState().apply {
            initContentSize = initItemSize
            if (zoomRange.start <= 0f) throw MinZoomException()
            this.zoomRange = zoomRange
        }
    }
}

@Composable
internal fun ZoomTransformBoxParent(
    state: SingleTouchState,
    index: Int = 0,
    modifier: Modifier = Modifier,
    onGestureChange: (Float, Offset) -> Unit,
    content: @Composable BoxScope.() -> Unit,
    itemContent: @Composable (TouchTransformData) -> Unit
) {
    ZoomTransformBox(modifier, state.gestureChanging, onGestureChange) {
        content()
        BoxParent(state, index, itemContent)
    }
}

@Composable
internal fun TouchBoxParent(
    state: SingleTouchState,
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    itemContent: @Composable (TouchTransformData) -> Unit
) {
    BoxWithConstraints(modifier) {
        content()
        BoxParent(state, index, itemContent)
    }
}

@Composable
internal inline fun BoxWithConstraintsScope.BoxParent(
    state: SingleTouchState,
    index: Int = 0,
    itemContent: @Composable (TouchTransformData) -> Unit
) {
    val density = LocalDensity.current
    val parentDpSize = DpSize(this.maxWidth, this.maxHeight)
    val parentSize = with(density) { parentDpSize.toSize() }
    LaunchedEffect(parentSize) {
        state.updateParentSize(parentSize)
    }
    val transformDataMap = state.transformDataMapState.value
    transformDataMap[index]?.fastForEach { item ->
        itemContent(item)
    }
}

