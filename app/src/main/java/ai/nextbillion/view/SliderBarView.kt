package ai.nextbillion.view

import ai.nextbillion.R
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnSliderTouchListener
import com.skydoves.colorpickerview.AlphaTileView

/**
 * @author qiuyu
 * @Date 2022/6/30
 **/
class SliderBarView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attributeSet, defStyle) {

    private lateinit var title: TextView
    private lateinit var seekBarValue: TextView
    private lateinit var numberSeekbar: Slider
    private var onSliderChangedListener: OnSliderChangedListener? = null
    private var unit: String = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.item_seek_bar, this, true)
        initView()
    }

    private fun initView() {
        title = findViewById(R.id.optionsAttrName)
        seekBarValue = findViewById(R.id.seekBarValue)
        numberSeekbar = findViewById(R.id.number_seekbar)

        numberSeekbar.addOnChangeListener { slider, _, _ ->
            seekBarValue.text = slider.value.toString()
            onSliderChangedListener?.onSliderChanged(slider.value)
        }

        numberSeekbar.addOnSliderTouchListener(object : OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {

            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {

            }

        })
    }

    fun setTitle(itemTitle: String) {
        title.text = itemTitle
    }

    fun initSeekBar(default: Float, from: Float, to: Float, unit: String, step: Float) {
        numberSeekbar.valueFrom = from
        numberSeekbar.valueTo = to
        numberSeekbar.stepSize = step
        this.unit = unit
        updateSliderValue(default)
    }

    private fun updateSliderValue(value: Float) {
        numberSeekbar.value = value
        seekBarValue.text = value.toString()
    }


    fun setOnSliderChangedListener(onSliderChangedListener: OnSliderChangedListener) {
        this.onSliderChangedListener = onSliderChangedListener
    }

    interface OnSliderChangedListener {
        fun onSliderChanged(value: Float)
    }

}