package com.example.ui

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

@Composable
fun OsmMapScreen(
    modifier: Modifier = Modifier,
    initialLocation: GeoPoint = GeoPoint(24.7136, 46.6753), // افتراضياً الرياض، السعودية
    onLocationSelected: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // تهيئة مكتبة الخرائط
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }

    // إدارة دورة حياة الخريطة
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView?.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                mapView = this
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true) // تفعيل التقريب باللمس
                controller.setZoom(13.0)
                controller.setCenter(initialLocation)

                // إضافة مستمع للنقرات على الخريطة
                val mapEventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        onLocationSelected(p.latitude, p.longitude)
                        updateMarker(this@apply, p, ctx)
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean {
                        return false
                    }
                }
                overlays.add(MapEventsOverlay(mapEventsReceiver))
            }
        },
        update = { view ->
            // هنا نقدر نحدث الخريطة لو فيه بيانات جديدة
        }
    )
}

// دالة لإضافة دبوس على الخريطة ومسح الدبوس القديم
private fun updateMarker(mapView: MapView, point: GeoPoint, context: Context) {
    // إزالة الدبابيس القديمة
    mapView.overlays.removeAll { it is Marker }
    
    // إضافة دبوس جديد
    val marker = Marker(mapView)
    marker.position = point
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    marker.title = "الموقع المحدد"
    
    mapView.overlays.add(marker)
    mapView.invalidate() // تحديث الشاشة
}
