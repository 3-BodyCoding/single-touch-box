package com.wzq.singletouchbox.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastForEach
import com.wzq.singletouchbox.ex.ZOOM_RANGE
import com.wzq.singletouchbox.exception.KeyBlankException
import com.wzq.singletouchbox.exception.KeyUniqueException

@Stable
class SingleTouchState {

    private val _transformDataList = mutableStateListOf<TouchTransformData>()
    val transformDataMapState: State<Map<Int, List<TouchTransformData>>>
        get() = derivedStateOf { _transformDataList.groupBy { it.page } }
    val transformDataListState: State<List<TouchTransformData>>
        get() = derivedStateOf { _transformDataList.toList() }

    var parentSize = Size.Zero
        private set

    var initContentSize = Size.Zero
        internal set

    var zoomRange = ZOOM_RANGE
        internal set

    var gestureChanging by mutableStateOf(false)
        internal set

    internal fun updateParentSize(parentSize: Size) {
        this.parentSize = parentSize
    }

    /**
     * @param key The value that ensures the uniqueness of the Item.
     * @param page Used in multi-page scenarios, such as in HorizontalPager, to indicate the number of pages where the Item.
     * @param centerPoint Add the location in the page. The default value is the center of the page.
     * @param onlyCurrentEditable Whether to set other Item editable to false when adding an Item.
     */
    fun addItem(
        key: String,
        page: Int = 0,
        centerPoint: Offset? = null,
        onlyCurrentEditable: Boolean = true
    ) {
        if (key.isBlank()) throw KeyBlankException()
        if (_transformDataList.any { it.key == key }) throw KeyUniqueException()
        val centerPoint = centerPoint
            ?: if (parentSize != Size.Zero) {
                val parentSizeHalf = parentSize / 2f
                Offset(parentSizeHalf.width, parentSizeHalf.height)
            } else throw Exception("parentSize cannot be Zero")
        val item = TouchTransformData(key, centerPoint, page)
        if (onlyCurrentEditable) disableEditAll()
        item.editable = true
        _transformDataList.add(item)
    }

    /**
     * @param list A list of data to be restored. Typically used to restore the UI state after locally persisting a List<[TouchTransformData]>
     */
    fun restoreList(list: List<TouchTransformData>) {
        if (list.any { it.key.isBlank() })
            throw KeyBlankException()
        if (list.distinctBy { it.key }.size != list.size)
            throw KeyUniqueException()
        _transformDataList.clear()
        _transformDataList.addAll(list)
    }

    /**
     * @param key of [TouchTransformData]
     * @param value Sets the editable state of the [TouchTransformData].
     */
    fun setEditable(key: String, value: Boolean) {
        try {
            val item = _transformDataList.fastFirst { it.key == key }
            item.editable = value
        } catch (_: NoSuchElementException) {
        }
    }

    /**
     * Set all [TouchTransformData] to be non-editable.
     */
    fun disableEditAll() {
        _transformDataList.fastForEach { it.editable = false }
    }

    /**
     * @param key Remove the TouchTransformData corresponding to key.
     */
    fun removeItem(key: String) {
        _transformDataList.removeAll { it.key == key }
    }

    companion object {
        val TouchTransformDataSaver = listSaver<TouchTransformData, Any>(
            save = { data ->
                listOf(
                    data.key,
                    data.page,
                    data.centerPoint.x,
                    data.centerPoint.y,
                    data.rotate,
                    data.zoom,
                    data.offset.x,
                    data.offset.y,
                    data.contentSize.width,
                    data.contentSize.height,
                    data.editable
                )
            },
            restore = { list ->
                TouchTransformData(
                    key = list[0] as String,
                    page = list[1] as Int,
                    center = Offset(list[2] as Float, list[3] as Float),
                    rotate = list[4] as Float,
                    zoom = list[5] as Float
                ).apply {
                    offset = Offset(list[6] as Float, list[7] as Float)
                    contentSize = Size(list[8] as Float, list[9] as Float)
                    editable = list[10] as Boolean
                }
            }
        )

        val Saver = listSaver<SingleTouchState, Any>(
            save = {
                val parentSize = it.parentSize
                val initContentSize = it.initContentSize
                val zoomRange = it.zoomRange
                listOf(
                    parentSize.width,
                    parentSize.height,
                    initContentSize.width,
                    initContentSize.height,
                    zoomRange.start,
                    zoomRange.endInclusive,
                ) + it._transformDataList.map {
                    with(TouchTransformDataSaver) { save(it) } as Any
                }
            },
            restore = { savedList ->
                val state = SingleTouchState()
                val takeList = savedList.take(6)
                state.apply {
                    parentSize = Size(takeList[0] as Float, takeList[1] as Float)
                    initContentSize = Size(takeList[2] as Float, takeList[3] as Float)
                    zoomRange = takeList[4] as Float..takeList[5] as Float
                }
                val dataList = savedList.takeLast(savedList.size - 6)
                val list = mutableListOf<TouchTransformData>()
                dataList.fastForEach {
                    val item = TouchTransformDataSaver.restore(it)
                    if (item != null) list.add(item)
                }
                state._transformDataList.clear()
                state._transformDataList.addAll(list)
                state
            }
        )
    }
}