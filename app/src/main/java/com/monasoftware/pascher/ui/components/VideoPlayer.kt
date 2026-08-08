package com.monasoftware.pascher.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.View
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val lifecycleOwner = LocalLifecycleOwner.current
    //var isFullscreen by remember { mutableStateOf(false) }

    // Pause/Resume on app lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val activity = context.findActivity()
            if (activity != null) {
                showSystemUi(activity)
            }
        }
    }

    val toggleFullscreen = {
        val activity = context.findActivity()
        if (activity != null) {
            // Read actual orientation from the Activity at click time
            val isCurrentlyLandscape = activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            if (isCurrentlyLandscape) {
                // Exit fullscreen -> go back to portrait (or unspecified)
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                showSystemUi(activity)
            } else {
                // Enter fullscreen -> force landscape and hide system UI
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                hideSystemUi(activity)
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true

                // Keep controller visible for a while so user can see the fullscreen button
                controllerShowTimeoutMs = 3000

                // When the view is created, find the fullscreen button inside the controller layout
                // Use the runtime id to avoid direct dependency on generated R
                val fullBtnId = ctx.resources.getIdentifier("exo_fullscreen", "id", ctx.packageName)
                val enterDrawableId = ctx.resources.getIdentifier("exo_ic_fullscreen_enter", "drawable", ctx.packageName)
                val exitDrawableId = ctx.resources.getIdentifier("exo_ic_fullscreen_exit", "drawable", ctx.packageName)

                // set click on the controller's fullscreen button if present
                findViewById<View?>(fullBtnId)?.let { btn ->
                    btn.setOnClickListener { toggleFullscreen() }

                    // Set initial icon depending on current orientation
                    val isCurrentlyLandscape = ctx.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                    val drawableId = if (isCurrentlyLandscape) exitDrawableId else enterDrawableId
                    (btn as? ImageView)?.setImageDrawable(
                        if (drawableId != 0) ContextCompat.getDrawable(ctx, drawableId) else null
                    )
                }

                // Also set the PlayerView's built-in fullscreen listener (optional, keeps compatibility)
                setFullscreenButtonClickListener { toggleFullscreen() }
            }
        },
        update = { view ->
            // Keep player attached
            view.player = exoPlayer

            // Update fullscreen icon on recomposition (orientation changed)
            val fullBtnId = view.context.resources.getIdentifier("exo_fullscreen", "id", view.context.packageName)
            val enterDrawableId = view.context.resources.getIdentifier("exo_ic_fullscreen_enter", "drawable", view.context.packageName)
            val exitDrawableId = view.context.resources.getIdentifier("exo_ic_fullscreen_exit", "drawable", view.context.packageName)
            view.findViewById<View?>(fullBtnId)?.let { btn ->
                val isCurrentlyLandscape = view.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                val drawableId = if (isCurrentlyLandscape) exitDrawableId else enterDrawableId
                (btn as? ImageView)?.setImageDrawable(
                    if (drawableId != 0) ContextCompat.getDrawable(view.context, drawableId) else null
                )
            }
        },
        modifier = if (isLandscape) {
            Modifier.fillMaxSize()
        } else {
            modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        }
    )
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun hideSystemUi(activity: Activity) {
    val window = activity.window
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

private fun showSystemUi(activity: Activity) {
    val window = activity.window
    WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
}