package ai.nextbillion.dialog

import ai.nextbillion.R
import ai.nextbillion.databinding.ColorPickerDialogLayoutBinding
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.*
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

/**
 * @author qiuyu
 * @Date 2022/3/21
 */
class ColorPickerDialog(
    var mContext: Context,
    private var initialColor: String,
    private var colorPickerChangedListener: ColorPickerChangedListener?
) : Dialog(mContext), View.OnClickListener {

    private lateinit var binding: ColorPickerDialogLayoutBinding

    init {
        init()
    }

    private fun init() {
        this.window?.requestFeature(Window.FEATURE_NO_TITLE)
        binding = ColorPickerDialogLayoutBinding.inflate(LayoutInflater.from(mContext))
        setContentView(binding.root)
        this.window?.decorView?.setPadding(0, 0, 0, 0)
        val lp = this.window?.attributes
        lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        lp?.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp?.gravity = Gravity.CENTER
        this.window?.attributes = lp
        this.window?.setBackgroundDrawableResource(R.color.transparent)
        initView()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        binding.cancelBtn.setOnClickListener(this)
        binding.selectBtn.setOnClickListener(this)
        binding.colorPickerView.setColorListener(ColorEnvelopeListener { envelope, _ ->
            binding.colorValue.text = String.format("#%s", envelope.hexCode)
            binding.alphaTileView.setBackgroundColor(envelope.color)
        })
        binding.colorPickerView.setInitialColor(Color.parseColor(initialColor))
        // attach alphaSlideBar
        binding.colorPickerView.attachAlphaSlider(binding.alphaSlideBar)

        // attach brightnessSlideBar
        binding.colorPickerView.attachBrightnessSlider(binding.brightnessSlideBar)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.cancel_btn) {
            dismiss()
        } else if (view.id == R.id.select_btn) {
            if (colorPickerChangedListener != null) {
                colorPickerChangedListener!!.onColorPick(
                    String.format(
                        "#%s",
                        binding.colorPickerView.colorEnvelope.hexCode
                    )
                )
                dismiss()
            }
        }
    }

    interface ColorPickerChangedListener {
        fun onColorPick(color: String)
    }


}