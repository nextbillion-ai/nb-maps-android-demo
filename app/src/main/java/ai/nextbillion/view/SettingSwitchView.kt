package ai.nextbillion.view

import ai.nextbillion.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.DialogTitle
import androidx.appcompat.widget.SwitchCompat

/**
 * @author qiuyu
 * @Date 2022/6/2
 **/
class SettingSwitchView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    LinearLayout(context, attributeSet, defStyle) {

    private lateinit var switchTitle: TextView
    lateinit var switchButton: SwitchCompat
    private var title: String = ""
    private var onSwitchChangedLister: OnSwitchChangedLister? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.setting_switch_button, this, true)

        initView()
    }

    private fun initView() {
        switchTitle = findViewById(R.id.switchTitle)
        switchButton = findViewById(R.id.switchButton)
        switchTitle.text = title
        switchButton.setOnCheckedChangeListener { _, isChecked ->
            onSwitchChangedLister?.onSwitchChanged(isChecked)
        }
    }

    fun setTitle(title: String) {
        switchTitle.text = title
    }

    fun defaultValue(isOpen: Boolean) {
        switchButton.isChecked = isOpen;
    }


    fun setOnSwitchChangedLister(onSwitchChangedLister: OnSwitchChangedLister) {
        this.onSwitchChangedLister = onSwitchChangedLister
    }

    interface OnSwitchChangedLister {
        fun onSwitchChanged(status: Boolean)
    }

}