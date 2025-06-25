package sample.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.DrawableResource
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun App() {
    //Your data collection
    val imageMap = remember { mutableStateMapOf<String, DrawableResource>() }
    val navController = rememberNavController()
    MaterialTheme {
        NavHost(
            navController,
            startDestination = "Home",
            Modifier.systemBarsPadding()
        ) {
            composable("Home") {
                SampleScreen(navController)
            }
            composable("FixedSingleTouch") {
                FixedSingleTouch(navController, imageMap)
            }
            composable("FixedSingleTouchPainter") {
                FixedSingleTouchPainter(navController, imageMap)
            }
            composable("PagerWithSingleTouch") {
                PagerWithSingleTouch(navController, imageMap)
            }
            composable("Test") {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        navController.navigateUp()
                    }) {
                        Text("Navigate Back")
                    }
                    Text("Test")
                }
            }
        }
    }
}