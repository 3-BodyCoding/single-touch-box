package sample.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.wzq.singletouchbox.ui.SingleTouchBox
import com.wzq.singletouchbox.ui.rememberSingleTouchState
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import singletouchbox.sample.composeapp.generated.resources.Res
import singletouchbox.sample.composeapp.generated.resources.ic_bg
import singletouchbox.sample.composeapp.generated.resources.ic_test
import singletouchbox.sample.composeapp.generated.resources.ic_touch_remove
import singletouchbox.sample.composeapp.generated.resources.ic_touch_zoom_rotate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun FixedSingleTouchPainter(
    navController: NavHostController,
    imageMap: SnapshotStateMap<String, DrawableResource>
) {
    val touchState = rememberSingleTouchState()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SingleTouchBox(
            touchState,
            removeControlPainter = painterResource(Res.drawable.ic_touch_remove),
            zoomRotateControlPainter = painterResource(Res.drawable.ic_touch_zoom_rotate),
            onClickRemove = { key ->
                touchState.removeItem(key)
                imageMap.remove(key)
            },
            modifier = Modifier.fillMaxWidth().weight(1f).pointerInput(Unit) {
                detectTapGestures {
                    touchState.disableEditAll()
                }
            }, itemContent = { item ->
                val key = item.key
                val image = imageMap[key]
                if (image != null) Image(
                    painterResource(image),
                    contentDescription = null,
                    modifier = Modifier.pointerInput(item.editable) {
                        if (!item.editable) detectTapGestures {
                            touchState.setEditable(key, true)
                        }
                    })
            }
        ) {
            Image(
                painterResource(Res.drawable.ic_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                navController.navigateUp()
            }) {
                Text("Back")
            }
            Button(onClick = {
                val id = Uuid.random().toString()
                imageMap[id] = Res.drawable.ic_test
                touchState.addItem(id)
            }) {
                Text("Add Image")
            }
            Button(onClick = {
                navController.navigate("Test")
            }) {
                Text("Navigate Test")
            }
        }
    }
}