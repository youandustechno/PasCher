package com.monasoftware.pascher.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.view.View
import kotlin.math.abs

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onImmersiveFullscreenChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var isFullscreen by remember { mutableStateOf(false) }
    var isImmersiveFullscreen by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    val toggleFullscreen = {
        isFullscreen = !isFullscreen
        val activity = context.findActivity()
        if (activity != null) {
            if (isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                hideSystemUi(activity)
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                showSystemUi(activity)
                isImmersiveFullscreen = false
                onImmersiveFullscreenChanged(false)
            }
        }
    }

    val toggleImmersiveFullscreen = {
        isImmersiveFullscreen = !isImmersiveFullscreen
        onImmersiveFullscreenChanged(isImmersiveFullscreen)
        val activity = context.findActivity()
        if (activity != null) {
            if (isImmersiveFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                hideSystemUiImmersive(activity)
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                showSystemUi(activity)
            }
        }
    }

    BackHandler(isFullscreen || isImmersiveFullscreen) {
        if (isImmersiveFullscreen) {
            toggleImmersiveFullscreen()
        } else if (isFullscreen) {
            toggleFullscreen()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val activity = context.findActivity()
            if (activity != null && (isFullscreen || isImmersiveFullscreen)) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                showSystemUi(activity)
            }
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setFullscreenButtonClickListener {
                    toggleFullscreen()
                }

                // Set up gesture detector for swipe immersive fullscreen
                val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                        if (e1 == null) return false

                        val deltaY = e2.y - e1.y
                        val deltaX = abs(e2.x - e1.x)

                        // Swipe up with velocity > 100 and vertical movement > horizontal
                        if (abs(velocityY) > 500 && deltaY < -100 && deltaY < -deltaX) {
                            toggleImmersiveFullscreen()
                            return true
                        }

                        // Swipe down to exit immersive mode
                        if (isImmersiveFullscreen && abs(velocityY) > 500 && deltaY > 100 && deltaY > deltaX) {
                            toggleImmersiveFullscreen()
                            return true
                        }

                        return false
                    }
                })

                setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                }
            }
        },
        update = { view ->
            view.player = exoPlayer
        },
        modifier = if (isFullscreen || isImmersiveFullscreen) {
            Modifier.fillMaxSize()
        } else {
            modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        }
    )
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun hideSystemUi(activity: Activity) {
    val window = activity.window
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

private fun hideSystemUiImmersive(activity: Activity) {
    val window = activity.window
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    // Add immersive fullscreen flags for 100% edge-to-edge display
    @Suppress("DEPRECATION")
    window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    )
}

private fun showSystemUi(activity: Activity) {
    val window = activity.window
    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
    // Clear immersive flags
    @Suppress("DEPRECATION")
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
}