package ai.nextbillion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ai.nextbillion.databinding.ActivityPolylineBinding
import ai.nextbillion.maps.annotations.Marker
import ai.nextbillion.maps.annotations.Polyline
import ai.nextbillion.maps.annotations.PolylineOptions
import ai.nextbillion.maps.camera.CameraUpdateFactory
import ai.nextbillion.maps.core.NextbillionMap
import ai.nextbillion.maps.core.Style
import ai.nextbillion.maps.geometry.LatLng
import ai.nextbillion.maps.geometry.LatLngBounds
import ai.nextbillion.view.ColorSelectorView
import ai.nextbillion.view.SliderBarView
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import java.text.Bidi

class PolylineActivity : AppCompatActivity() {
    private lateinit var mMap: NextbillionMap
    private lateinit var binding: ActivityPolylineBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var points: MutableList<LatLng> = mutableListOf()
    private var polyline: Polyline? = null
    private var originMarker: Marker? = null
    private var lineColor = "#1E58A5"
    private var lineWidth = 5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPolylineBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.mapView.getMapAsync { map: NextbillionMap -> onMapSync(map) }
        initBottomSheet()
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun onMapSync(map: NextbillionMap) {
        showSnackBar()
        mMap = map
        mMap.addOnMapLongClickListener {
            applyPolyline(it)
            false
        }
        map.setStyle( Style.Builder().fromUri(StyleConstants.LIGHT))
    }

    private fun showSnackBar() {
        val snackBar = Snackbar.make(binding.root,getString(R.string.snack_long_press),Snackbar.LENGTH_LONG)
        snackBar.setTextColor(Color.WHITE)
        snackBar.view.setBackgroundColor(Color.BLACK)
        snackBar.show()
    }

    private fun applyPolyline(latLng: LatLng) {
        points.add(latLng)

        if (points.size < 2) {
            originMarker = mMap.addMarker(latLng)
        } else {
            originMarker?.let {
                mMap.removeMarker(it)
                originMarker = null
            }
            recreatePoleLine()
        }

        updateFloatButtonStatus()
    }

    private fun removeSingleLine() {
        if (points.size <= 2) {
            removeAllPolyline()
            return
        } else {
            points.removeLast()
            recreatePoleLine()
        }
    }

    private fun recreatePoleLine() {
        polyline?.let {
            mMap.removePolyline(it)
        }
//        polyline = mMap.addPolyline(points, lineColor)
        val options =  PolylineOptions().color(Color.parseColor(lineColor)).width(lineWidth)
        options.addAll(points)
        mMap.addPolylines(listOf(options))
        polyline?.width = lineWidth
        animateBound(1f)
    }

    private fun removeAllPolyline() {
        points.clear()
        mMap.clear()
        polyline = null
        updateFloatButtonStatus()
    }

    private fun updateFloatButtonStatus() {
        if (points.size < 2) {
            bottomSheetBehavior.isHideable = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            binding.removeSingleLine.visibility = View.GONE
            binding.removeAllLine.visibility = View.GONE
        } else {
            bottomSheetBehavior.isHideable = false
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.peekHeight = dpToPx(this, 60.0)

            binding.removeSingleLine.visibility = View.VISIBLE
            binding.removeAllLine.visibility = View.VISIBLE
        }

    }


    private fun initBottomSheet() {
        binding.removeSingleLine.setOnClickListener {
            removeSingleLine()
        }

        binding.removeAllLine.setOnClickListener {
            removeAllPolyline()
        }

        binding.lineColor.setTitle(getString(R.string.lineColor))
        binding.lineColor.initColor(lineColor)
        binding.lineColor.setOnColorChangedListener(object : ColorSelectorView.OnColorChangedListener {
            override fun onColorChanged(color: String) {
                lineColor = color
                polyline?.color = Color.parseColor(color)
            }
        })

        binding.lineWidth.setTitle(getString(R.string.lineWidth))
        binding.lineWidth.initSeekBar(5f, 1f, 15f, "", 1f)
        binding.lineWidth.setOnSliderChangedListener(object : SliderBarView.OnSliderChangedListener {
            override fun onSliderChanged(value: Float) {
                lineWidth = value
                polyline?.width = value
            }
        })

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                animateBound(slideOffset)
            }
        })

    }


    private fun animateBound(slideOffset: Float) {
        if (points.size < 2) {
            return
        }
        val bounds = LatLngBounds.Builder().includes(points).build()
        animateCameraBox(bounds, createPadding(this, slideOffset))
    }

    private fun animateCameraBox(
        bounds: LatLngBounds,
        padding: IntArray
    ) {
        val position = mMap.getCameraForLatLngBounds(bounds, padding)
        position?.let {
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(position)
            mMap.animateCamera(cameraUpdate)
        }

    }

    private fun createPadding(context: Context, cameraBoundFactor: Float): IntArray {
        val horPadding = dpToPx(context, 50.0)
        val topPadding = dpToPx(context, 50.0)
        val bottomPadding = (dpToPx(context, 260.0) * cameraBoundFactor).toInt()
        return intArrayOf(horPadding, topPadding, horPadding, bottomPadding)
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

    fun dpToPx(context: Context, dp: Double): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}