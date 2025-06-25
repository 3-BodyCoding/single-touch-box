package com.wzq.singletouchbox.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wzq.singletouchbox.state.SingleTouchState
import com.wzq.singletouchbox.state.TouchTransformData
import kotlin.math.max

/**
 * The box can hold multiple transform components, if you want the box itself to be zoomable and movable,
 * you should use [SingleTouchParentZoomBox].
 * @param state The state to control this Box
 * @param index Used in multi-page scenarios, such as in HorizontalPager, to indicate the number of pages where the Item.
 * @param modifier A modifier instance to be applied to this Box outer layout
 * @param removeControlPainter “RemoveController” to draw Painter
 * @param zoomRotateControlPainter “ZoomRotateControlPainter” to draw Painter
 * @param topEndControlPainter Customized controller in the upper right corner to draw Painter.
 * @param bottomStartControlPainter Customized controller in the lower left corner to draw Painter.
 * @param onClickRemove Click event of “RemoveController”.
 * @param onClickTopEndControl Click event for the controller button in the upper right corner
 * @param onClickBottomStartControl Click event for the controller button in the lower left corner
 * @param borderColor Item's border color
 * @param borderWidth Item's border width
 * @param itemContent Transforms the component's Composable.
 * @param content Transforms the parent component of a component's Composable.
 */
@Composable
fun SingleTouchBox(
    state: SingleTouchState,
    removeControlPainter: Painter,
    zoomRotateControlPainter: Painter,
    onClickRemove: (String) -> Unit,
    index: Int = 0,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 1.dp,
    topEndControlPainter: Painter? = null,
    bottomStartControlPainter: Painter? = null,
    onClickTopEndControl: (String) -> Unit = {},
    onClickBottomStartControl: (String) -> Unit = {},
    itemContent: @Composable (TouchTransformData) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    TouchBoxParent(state, index, modifier, content) { item ->
        TouchImage(
            state,
            item,
            removeControlPainter,
            zoomRotateControlPainter,
            onClickRemove,
            borderColor,
            borderWidth,
            topEndControlPainter,
            bottomStartControlPainter,
            onClickTopEndControl,
            onClickBottomStartControl,
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
 * @param removeControlPainter “RemoveController” to draw Painter
 * @param zoomRotateControlPainter “ZoomRotateControlPainter” to draw Painter
 * @param topEndControlPainter Customized controller in the upper right corner to draw Painter.
 * @param bottomStartControlPainter Customized controller in the lower left corner to draw Painter.
 * @param onClickRemove Click event of “RemoveController”.
 * @param onClickTopEndControl Click event for the controller button in the upper right corner
 * @param onClickBottomStartControl Click event for the controller button in the lower left corner
 * @param borderColor Item's border color
 * @param borderWidth Item's border width
 * @param itemContent Transforms the component's Composable.
 * @param content Transforms the parent component of a component's Composable.
 */
@Composable
fun SingleTouchParentZoomBox(
    state: SingleTouchState,
    removeControlPainter: Painter,
    zoomRotateControlPainter: Painter,
    onClickRemove: (String) -> Unit,
    index: Int = 0,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 1.dp,
    topEndControlPainter: Painter? = null,
    bottomStartControlPainter: Painter? = null,
    onClickTopEndControl: (String) -> Unit = {},
    onClickBottomStartControl: (String) -> Unit = {},
    onParentGestureChange: (Float, Offset) -> Unit = { _, _ -> },
    itemContent: @Composable (TouchTransformData) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    ZoomTransformBoxParent(state, index, modifier, onParentGestureChange, content) { item ->
        TouchImage(
            state,
            item,
            removeControlPainter,
            zoomRotateControlPainter,
            onClickRemove,
            borderColor,
            borderWidth,
            topEndControlPainter,
            bottomStartControlPainter,
            onClickTopEndControl,
            onClickBottomStartControl,
            itemContent
        )
    }
}

@Composable
private fun TouchImage(
    state: SingleTouchState,
    item: TouchTransformData,
    removeControlPainter: Painter,
    zoomRotateControl: Painter,
    onClickRemove: (String) -> Unit,
    borderColor: Color = Color.Red,
    borderWidth: Dp = 1.dp,
    topEndControl: Painter? = null,
    bottomStartControl: Painter? = null,
    onClickTopEndControl: (String) -> Unit = {},
    onClickBottomStartControl: (String) -> Unit = {},
    itemContent: @Composable (TouchTransformData) -> Unit
) {
    val density = LocalDensity.current
    val removeIconSize = removeControlPainter.intrinsicSize.maxDimension
    val zoomRotateIconSize = zoomRotateControl.intrinsicSize.maxDimension
    val controlDpSize = with(density) { max(removeIconSize, zoomRotateIconSize).toDp() }
    val topEndControlImage: @Composable (String) -> Unit = {
        if (topEndControl != null) Image(
            topEndControl,
            contentDescription = null,
            modifier = Modifier.clip(CircleShape).clickable { onClickTopEndControl(it) })
    }
    val bottomStartControlImage: @Composable (String) -> Unit = {
        if (bottomStartControl != null) Image(
            bottomStartControl,
            contentDescription = null,
            modifier = Modifier.clip(CircleShape)
                .clickable { onClickBottomStartControl(it) })
    }
    TouchBox(
        state,
        item,
        controlDpSize,
        borderColor,
        borderWidth,
        removeControl = {
            Image(
                removeControlPainter,
                contentDescription = "Remove Control",
                modifier = Modifier.clip(CircleShape).clickable { onClickRemove(it) }
            )
        },
        zoomRotateControl = {
            Image(
                zoomRotateControl,
                contentDescription = "Zoom Rotate Control"
            )
        },
        if (topEndControl != null) topEndControlImage else null,
        if (bottomStartControl != null) bottomStartControlImage else null,
        itemContent
    )
}