package ai.nextbillion

import ai.nextbillion.databinding.ActivityBoundsBinding
import ai.nextbillion.maps.annotations.PolygonOptions
import ai.nextbillion.maps.camera.CameraUpdateFactory
import ai.nextbillion.maps.core.NextbillionMap
import ai.nextbillion.maps.core.OnMapReadyCallback
import ai.nextbillion.maps.core.Style
import ai.nextbillion.maps.geometry.LatLng
import ai.nextbillion.maps.geometry.LatLngBounds
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity


/**
 * @author qiuyu
 * @Date 2022/6/30
 **/
class BoundsActivity : AppCompatActivity(){

    private lateinit var binding: ActivityBoundsBinding
    private lateinit var mMap: NextbillionMap
    private var boundsPoints: MutableList<LatLng> = mutableListOf()
    private var boundsArea: PolygonOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoundsBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map: NextbillionMap -> onMapSync(map) }
        binding.removeBounds.setOnClickListener {
            removeBounds()
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun removeBounds() {
        mMap.clear()
        boundsPoints.clear()
        boundsArea = null
        binding.removeBounds.hide()
    }

    private fun onMapSync(map: NextbillionMap) {
        mMap = map
        mMap.addOnMapLongClickListener {
            checkBounds(it)
            false
        }
        map.setStyle( Style.Builder().fromUri(StyleConstants.LIGHT))
    }

    private fun checkBounds(latLng: LatLng) {
        mMap.addMarker(latLng)
        boundsPoints.add(latLng)

        if (boundsPoints.size >= 4) {
            binding.removeBounds.show()
            boundsArea?.let {
                mMap.removePolygon(it.polygon)
            }
            val bounds = LatLngBounds.Builder().includes(boundsPoints).build()
            mMap.setLatLngBoundsForCameraTarget(bounds);
            boundsArea = PolygonOptions()
                .add(bounds.northWest)
                .add(bounds.northEast)
                .add(bounds.southEast)
                .add(bounds.southWest)
            boundsArea?.let {
                it.alpha(0.25f)
                it.fillColor(Color.RED)
                mMap.addPolygon(it)
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}