package com.wzq.singletouchbox.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wzq.singletouchbox.state.SingleTouchState
import com.wzq.singletouchbox.state.TouchTransformData

/**
 * The box can hold multiple transform components, if you want the box itself to be zoomable and movable,
 * you should use [SingleTouchParentZoomBox].
 * @param state The state to control this Box
 * @param index Used in multi-page scenarios, such as in HorizontalPager, to indicate the number of pages where the Item.
 * @param modifier A modifier instance to be applied to this Box outer layout
 * @param controlDpSize Controller button size, default is 24dp
 * @param borderColor Item's border color
 * @param borderWidth Item's border width
 * @param removeControl This RemoveController's Composable.
 * @param zoomRotateControl This ZoomRotateController's Composable.
 * @param topEndControl Customized controller in the upper right corner's Composable.
 * @param bottomStartControl Customized controller in the lower left corner's Composable.
 * @param itemContent Transforms the component's Composable.
 * @param content Transforms the parent component of a component's Composable.
 */
@Composable
fun SingleTouchBox(
    state: SingleTouchState,
    modifier: Modifier = Modifier,
    index: Int = 0,
    controlDpSize: Dp = 24.dp,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 1.dp,
    removeControl: @Composable (String) -> Unit,
    zoomRotateControl: @Composable (String) -> Unit,
    topEndControl: (@Composable (String) -> Unit)? = null,
    bottomStartControl: (@Composable (String) -> Unit)? = null,
    itemContent: @Composable (TouchTransformData) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    TouchBoxParent(state, index, modifier, content) { item ->
        TouchBox(
            state,
            item,
            controlDpSize,
            borderColor,
            borderWidth,
            removeControl,
            zoomRotateControl,
            topEndControl,
            bottomStartControl,
            itemContent
        )
    }
}

/**
 * The box can hold multiple transform components, The Box can hold multiple transformations,
 * and the Box itself can be enlarged and moved.
 * @param state The state to control this Box
 * @param index Used in multi-page scenarios, such as in HorizontalPager, to indicate the number of pages where the Item.
 * @param modifier A modifier instance to be applied to this Box outer layout
 * @param controlDpSize Controller button size, default is 24dp
 * @param borderColor Item's border color
 * @param borderWidth Item's border width
 * @param removeControl This RemoveController's Composable.
 * @param zoomRotateControl This ZoomRotateController's Composable.
 * @param topEndControl Customized controller in the upper right corner's Composable.
 * @param bottomStartControl Customized controller in the lower left corner's Composable.
 * @param itemContent Transforms the component's Composable.
 * @param content Transforms the parent component of a component's Composable.
 */
@Composable
fun SingleTouchParentZoomBox(
    state: SingleTouchState,
    modifier: Modifier = Modifier,
    index: Int = 0,
    controlDpSize: Dp = 24.dp,
    borderColor: Color = Color.Red,
    borderWidth: Dp = 1.dp,
    removeControl: @Composable (String) -> Unit,
    zoomRotateControl: @Composable (String) -> Unit,
    topEndControl: (@Composable (String) -> Unit)? = null,
    bottomStartControl: (@Composable (String) -> Unit)? = null,
    onParentGestureChange: (Float, Offset) -> Unit = { _, _ -> },
    itemContent: @Composable (TouchTransformData) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    ZoomTransformBoxParent(state, index, modifier, onParentGestureChange, content) { item ->
        TouchBox(
            state,
            item,
            controlDpSize,
            borderColor,
            borderWidth,
            removeControl,
            zoomRotateControl,
            topEndControl,
            bottomStartControl,
            itemContent
        )
    }
}