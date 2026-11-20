package ai.nextbillion

import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import ai.nextbillion.maps.Nextbillion
import ai.nextbillion.maps.camera.CameraUpdateFactory
import ai.nextbillion.maps.core.MapView
import ai.nextbillion.maps.core.NextbillionMap
import ai.nextbillion.maps.core.Style
import ai.nextbillion.databinding.ActivityHouseNumberBinding
import ai.nextbillion.styles.HouseNumberLayer
import ai.nextbillion.maps.geometry.LatLng
import ai.nextbillion.maps.style.NGLWellKnownTileServer
import android.util.Log

/**
 * Displays house numbers already present in the selected provider's vector tiles.
 *
 * The controls select a provider and base style; [HouseNumberLayer] maps each provider's
 * address fields to a text layer. No geocoding request or generated address data is needed.
 */
class HouseNumberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHouseNumberBinding
    // Tile-server and authentication settings are global, so preserve the entry values
    // and restore them when the user leaves this example.
    private lateinit var previousTileServer: NGLWellKnownTileServer
    private var previousHeaderName: String? = null
    private var map: NextbillionMap? = null
    private var styleIndex = 0
    private var ready = false
    private var destroyed = false
    private val providers = listOf(
        NGLWellKnownTileServer.NGLTomTom,
        NGLWellKnownTileServer.NGLTomTomOrbis,
        NGLWellKnownTileServer.NGLMapTiler
    )
    private val styleNames = listOf("Bright", "Dark", "Satellite")

    // Wait for the map to settle before counting the labels in the current viewport.
    private val idleListener = MapView.OnDidBecomeIdleListener { updateStatus() }
    private val styleListener = MapView.OnDidFinishLoadingStyleListener {
        if (!destroyed) {
            map?.style?.let { style ->
                val provider = providers[binding.provider.selectedItemPosition.coerceIn(providers.indices)]
                // Runtime layers belong to one Style. Recreate ours after every reload,
                // then reapply the switch state because visibility is also a layer property.
                ready = HouseNumberLayer.addTo(style, provider) != null
                if (ready) {
                    HouseNumberLayer.setVisible(style, binding.showNumbers.isChecked)
                } else {
                    binding.status.setText(R.string.house_numbers_unavailable)
                }
            }
        }
    }
    private val failureListener = MapView.OnDidFailLoadingMapListener { error ->
        if (!destroyed) {
            ready = false
            binding.status.text = getString(R.string.house_numbers_failed, error.orEmpty())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Preserve the original entry settings across Activity recreation. Taking a new
        // snapshot after rotation would capture this example's temporary provider instead.
        previousTileServer = savedInstanceState?.getString("previous_server")
            ?.let { runCatching { NGLWellKnownTileServer.valueOf(it) }.getOrNull() }
            ?: Nextbillion.getWellKnownTileServer()
        previousHeaderName = if (savedInstanceState?.containsKey("previous_header") == true) {
            savedInstanceState.getString("previous_header")
        } else {
            Nextbillion.getApiKeyHeaderName()
        }
        styleIndex = (savedInstanceState?.getInt("style_index") ?: 0).coerceIn(styleNames.indices)
        val providerIndex = (savedInstanceState?.getInt("provider_index") ?: 0).coerceIn(providers.indices)
        // Clear any custom API-key header for the predefined provider styles used here.
        Nextbillion.setApiKeyHeaderName(null)
        if (Nextbillion.getWellKnownTileServer() != providers[providerIndex]) {
            Nextbillion.setTileServer(providers[providerIndex])
        }

        binding = ActivityHouseNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.provider.adapter = ArrayAdapter.createFromResource(
            this, R.array.house_number_providers, R.layout.item_house_number_provider
        ).apply { setDropDownViewResource(R.layout.item_house_number_provider) }
        binding.provider.setSelection(providerIndex)
        binding.showNumbers.isChecked = savedInstanceState?.getBoolean("show_numbers") ?: true
        binding.mapView.onCreate(savedInstanceState)
        binding.provider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (map != null && Nextbillion.getWellKnownTileServer() != providers[position]) loadStyle()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.showNumbers.setOnCheckedChangeListener { _, checked ->
            map?.style?.let { HouseNumberLayer.setVisible(it, checked) }
            map?.triggerRepaint()
        }
        binding.streetZoom.setOnClickListener { map?.moveCamera(CameraUpdateFactory.zoomTo(16.0)) }
        binding.detailZoom.setOnClickListener { map?.moveCamera(CameraUpdateFactory.zoomTo(19.0)) }
        binding.switchStyle.setOnClickListener {
            styleIndex = (styleIndex + 1) % styleNames.size
            loadStyle()
        }
        // A persistent listener also catches style reloads initiated by the SDK when the
        // tile server changes. A callback attached to one setStyle call would cover only that load.
        // Keep listener instances so onDestroy can unregister the same objects.
        binding.mapView.addOnDidBecomeIdleListener(idleListener)
        binding.mapView.addOnDidFinishLoadingStyleListener(styleListener)
        binding.mapView.addOnDidFailLoadingMapListener(failureListener)
        binding.mapView.getMapAsync { nextbillionMap ->
            if (destroyed) return@getMapAsync
            map = nextbillionMap
            // MapView restores its saved camera; only use the demo location on a fresh launch.
            if (savedInstanceState == null) {
                nextbillionMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEMO_CENTER, 19.0))
            }
            loadStyle()
        }
    }

    private fun loadStyle() {
        if (destroyed) return
        val nextbillionMap = map ?: return
        ready = false
        val provider = providers[binding.provider.selectedItemPosition]
        if (Nextbillion.getWellKnownTileServer() != provider) {
            Nextbillion.setTileServer(provider)
        }
        binding.status.setText(R.string.house_numbers_loading)
        // Resolve the style after switching providers so its tiles and glyphs match that provider.
        // The styleListener adds house-number labels once the new style has finished loading.
        nextbillionMap.setStyle(Style.Builder().fromUri(Style.getPredefinedStyle(styleNames[styleIndex])))
    }

    private fun updateStatus() {
        if (destroyed) return
        val nextbillionMap = map ?: return
        if (!ready || nextbillionMap.style?.getLayer(HouseNumberLayer.ID) == null) return
        // Query only our layer in screen coordinates. The result reflects zoom, visibility,
        // tile coverage, and label placement; it is not a count of every address in the area.
        val features = nextbillionMap.queryRenderedFeatures(
            RectF(0f, 0f, binding.mapView.width.toFloat(), binding.mapView.height.toFloat()),
            HouseNumberLayer.ID
        )
        val zoom = nextbillionMap.cameraPosition.zoom
        val provider = binding.provider.selectedItem.toString()
        binding.status.text = getString(
            R.string.house_numbers_status, provider, styleNames[styleIndex], zoom, features.size
        )
        Log.i("HouseNumberActivity", String.format(
            "HouseNumbers provider=%s style=%s zoom=%.1f enabled=%s rendered=%d samples=%s",
            provider, styleNames[styleIndex], zoom, binding.showNumbers.isChecked, features.size,
            features.take(5).map { it.properties()?.toString() }
        ))
    }

    // Forward the Activity lifecycle to MapView so rendering and map resources follow the screen.
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("previous_server", previousTileServer.name)
        outState.putString("previous_header", previousHeaderName)
        outState.putInt("provider_index", binding.provider.selectedItemPosition)
        outState.putInt("style_index", styleIndex)
        outState.putBoolean("show_numbers", binding.showNumbers.isChecked)
        binding.mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        // Guard late asynchronous callbacks and detach listeners before destroying native map resources.
        destroyed = true
        ready = false
        binding.mapView.removeOnDidBecomeIdleListener(idleListener)
        binding.mapView.removeOnDidFinishLoadingStyleListener(styleListener)
        binding.mapView.removeOnDidFailLoadingMapListener(failureListener)
        map = null
        binding.mapView.onDestroy()
        // Rotation recreates this example and should keep its current provider. Restore
        // the original global settings only when the Activity is actually finishing.
        if (isFinishing) {
            Nextbillion.setApiKeyHeaderName(previousHeaderName)
            Nextbillion.setTileServer(previousTileServer)
        }
        super.onDestroy()
    }

    companion object {
        // Amsterdam sample area used to compare real address coverage across providers.
        private val DEMO_CENTER = LatLng(52.35273, 4.91638)
    }
}
