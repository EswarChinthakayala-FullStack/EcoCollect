package com.wastereporting.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    onMapReady: (MapLibreMap) -> Unit
) {
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val lifecycleObserver = remember {
        androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_CREATE -> mapViewRef?.onCreate(null)
                androidx.lifecycle.Lifecycle.Event.ON_START -> mapViewRef?.onStart()
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> mapViewRef?.onStop()
                androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDestroy()
                else -> {}
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).also { view ->
                mapViewRef = view
                // Call onCreate directly when creating the view to ensure MapLibre starts correctly
                view.onCreate(null)
                view.getMapAsync { map ->
                    onMapReady(map)
                }
            }
        },
        modifier = modifier
    )
}
