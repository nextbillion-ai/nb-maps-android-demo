package ai.nextbillion.view

import ai.nextbillion.R
import ai.nextbillion.dialog.ColorPickerDialog
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.skydoves.colorpickerview.AlphaTileView

/**
 * @author qiuyu
 * @Date 2022/6/30
 **/
class ColorSelectorView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attributeSet, defStyle) {

    private lateinit var title: TextView
    private lateinit var colorValue: TextView
    private lateinit var alphaTileView: AlphaTileView
    private var defaultColor: String = ""
    private var onColorChangedListener: OnColorChangedListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_color_selector, this, true)
        initView()
    }

    private fun initView() {
        title = findViewById(R.id.optionsAttrName)
        colorValue = findViewById(R.id.colorValue)
        alphaTileView = findViewById(R.id.alphaTileView)
        setOnClickListener {
            val dialog = ColorPickerDialog(context, defaultColor, object :
                ColorPickerDialog.ColorPickerChangedListener {
                override fun onColorPick(color: String) {
                    updateColorView(color)
                    onColorChangedListener?.onColorChanged(color)
                }

            })
            dialog.show()
        }
    }

    fun setTitle(itemTitle: String) {
        title.text = itemTitle
    }


    fun initColor(color: String) {
        defaultColor = color
        updateColorView(color)
    }

    private fun updateColorView(color: String) {
        colorValue.text = color
        alphaTileView.setBackgroundColor(Color.parseColor(color))
    }

    fun setOnColorChangedListener(onColorChangedListener: OnColorChangedListener){
        this.onColorChangedListener = onColorChangedListener
    }

    interface OnColorChangedListener {
        fun onColorChanged(color: String)
    }
}