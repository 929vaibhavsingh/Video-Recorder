package com.jr.liveclipper

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.jr.liveclipper.common.FileManager
import com.jr.liveclipper.common.FileManagerImpl
import com.jr.liveclipper.common.PermissionsHandler
import com.jr.liveclipper.ffmpeg.FFMpegUtilImpl
import com.jr.liveclipper.screens.landing.EntryScreen
import com.jr.liveclipper.screens.photo.PhotoScreen
import com.jr.liveclipper.screens.photo.PhotoViewModel
import com.jr.liveclipper.screens.photo.PreviewScreen
import com.jr.liveclipper.screens.playback.PlaybackScreen
import com.jr.liveclipper.ui.theme.LiveClipperTheme
import com.jr.liveclipper.screens.recording.RecordingScreen
import com.jr.liveclipper.screens.recording.RecordingViewModel
import com.jr.liveclipper.screens.split.CustomSplitViewModel
import com.jr.liveclipper.screens.split.SplitScreen
import com.jr.liveclipper.splitsManager.SplitsManagerImpl

@OptIn(ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {

    private val fileManager = FileManagerImpl(LiveClipperApp.getApplicationContext())

    private val permissionsHandler = PermissionsHandler()
    private val ffMpegUtilImpl = FFMpegUtilImpl()
    private lateinit var splitsManagerImpl : SplitsManagerImpl

    private val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PhotoViewModel::class.java)) {
                return PhotoViewModel(fileManager, permissionsHandler) as T
            }
            if (modelClass.isAssignableFrom(RecordingViewModel::class.java)) {
                return RecordingViewModel(fileManager, permissionsHandler) as T
            }
            if (modelClass.isAssignableFrom(CustomSplitViewModel::class.java)) {
                return CustomSplitViewModel() as T
            }
//            if (modelClass.isAssignableFrom(SplitsManagerImpl::class.java)) {
//                return SplitsManagerImpl(ffMpegUtilImpl, fileManager) as T
//            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splitsManagerImpl = SplitsManagerImpl(ffMpegUtilImpl, fileManager)
        setContent {
            App()
        }
    }


    private fun showMessage(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    private fun App() {
        LiveClipperTheme {
            Scaffold(topBar = {}) {
                val navController = rememberAnimatedNavController()
                AnimatedNavHost(
                    navController = navController,
                    startDestination = ScreenDestinations.Landing.route
                ) {
                    screen(ScreenDestinations.Landing.route) { EntryScreen(navController) }
                    screen(ScreenDestinations.Photo.route) {
                        PhotoScreen(navController, viewModelFactory) {
                            showMessage(it)
                        }
                    }
                    screen(ScreenDestinations.Video.route) {
                        RecordingScreen(navController, viewModelFactory) {
                            showMessage(it)
                        }
                    }
                    screen(
                        ScreenDestinations.Preview.route, arguments = listOf(
                            navArgument(ScreenDestinations.ARG_FILE_PATH) {
                                nullable = false
                                type = NavType.StringType
                            })
                    ) {
                        PreviewScreen(filePath = ScreenDestinations.Preview.getFilePath(it.arguments))
                    }
                    screen(
                        ScreenDestinations.Playback.route, arguments = listOf(
                            navArgument(ScreenDestinations.ARG_FILE_PATH) {
                                nullable = false
                                type = NavType.StringType
                            })
                    ) {
                        val filePath = ScreenDestinations.Playback.getFilePath(it.arguments)
                        PlaybackScreen(
                            filePath = filePath,
                            navHostController = navController
                        )
                    }
                    screen(
                        ScreenDestinations.SplitVideo.route, arguments = listOf(
                            navArgument(ScreenDestinations.ARG_FILE_PATH) {
                                nullable = false
                                type = NavType.StringType
                            })
                    ) {
                        val filePath = ScreenDestinations.SplitVideo.getFilePath(it.arguments)
                        SplitScreen(
                            filePath = filePath,
                            navHostController = navController,
                            splitsManager = splitsManagerImpl
                        )
                    }
                }

                BackHandler {
                    navController.popBackStack()
                }
            }
        }
    }

    @ExperimentalAnimationApi
    fun NavGraphBuilder.screen(
        route: String,
        arguments: List<NamedNavArgument> = listOf(),
        content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
    ) {
        val animSpec: FiniteAnimationSpec<IntOffset> = tween(500, easing = FastOutSlowInEasing)

        composable(
            route,
            arguments = arguments,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { screenWidth -> screenWidth },
                    animationSpec = animSpec
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { screenWidth -> -screenWidth },
                    animationSpec = animSpec
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { screenWidth -> -screenWidth },
                    animationSpec = animSpec
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { screenWidth -> screenWidth },
                    animationSpec = animSpec
                )
            },
            content = content
        )
    }
}

fun NavHostController.navigateTo(route: String) = navigate(route) {
    popUpTo(route)
    launchSingleTop = true
}

sealed class ScreenDestinations(val route: String) {
    object Landing : ScreenDestinations("landing")
    object Photo : ScreenDestinations("photo")
    object Preview : ScreenDestinations("preview?${ARG_FILE_PATH}={$ARG_FILE_PATH}") {
        fun createRoute(filePath: String): String {
            return "preview?${ARG_FILE_PATH}=${filePath}"
        }

        fun getFilePath(bundle: Bundle?): String {
            return bundle?.getString(ARG_FILE_PATH)!!
        }
    }

    object Video : ScreenDestinations("video")

    object SplitVideo : ScreenDestinations("split?${ARG_FILE_PATH}={$ARG_FILE_PATH}") {
        fun createRoute(filePath: String): String {
            return "split?${ARG_FILE_PATH}=${filePath}"
        }

        fun getFilePath(bundle: Bundle?): String {
            return bundle?.getString(ARG_FILE_PATH)!!
        }
    }

    object Playback : ScreenDestinations("playback?${ARG_FILE_PATH}={$ARG_FILE_PATH}") {
        fun createRoute(filePath: String): String {
            return "playback?${ARG_FILE_PATH}=${filePath}"
        }

        fun getFilePath(bundle: Bundle?): String {
            return bundle?.getString(ARG_FILE_PATH)!!
        }
    }

    companion object {
        const val ARG_FILE_PATH: String = "arg_file_path"
    }
}