package ai.nextbillion.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import android.widget.Toast
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.regex.Pattern

/**
 * @author qiuyu
 * @Date 2022/5/18
 **/

fun Activity.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun dpToPx(context: Context?, dp: Double): Float {
    val scale = context?.resources?.displayMetrics?.density
    scale?.let {
        return (dp * scale + 0.5f).toFloat()
    }
    return 0f
}

fun spToPx(context: Context, spValue: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        spValue.toFloat(), context.resources.displayMetrics).toInt()
}

fun getVersion(context: Context): String? {
    return try {
        val manager = context.packageManager
        val info = manager.getPackageInfo(context.packageName, 0)
        info.versionName
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun Context.screenW(): Int {
    val displayMetrics = DisplayMetrics()
    val windowManager: WindowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

fun Context.screenH(): Int {
    val displayMetrics = DisplayMetrics()
    val windowManager: WindowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}


fun getJsonFromAssets(context: Context, fileName: String): String {
    val stringBuilder = StringBuilder()
    try {
        val assetManager = context.assets
        if (assetManager != null) {
            val input = InputStreamReader(assetManager.open(fileName))
            val bf = BufferedReader(input)
            var line: String?
            while (bf.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        }
    } catch (e: IOException) {
    }
    return stringBuilder.toString()
}

fun isCoordinatePoint(value: String): Boolean {
    val latPattern = "^[\\-\\+]?((0|([1-8]\\d?))(\\.\\d{1,20})?|90(\\.0{1,20})?)$"
    val lngPattern = "^[\\-\\+]?(0(\\.\\d{1,20})?|([1-9](\\d)?)(\\.\\d{1,20})?|1[0-7]\\d{1}(\\.\\d{1,20})?|180(\\.0{1,20})?)$"
    if (TextUtils.isEmpty(value)) {
        return false
    }
    val split = value.split(",").toTypedArray()
    return if (split.size < 2) {
        false
    } else
        Pattern.matches(latPattern, split[0]) && Pattern.matches(lngPattern, split[1]
    )
}

fun isLandScapeMode(context: Context): Boolean {
    val configuration = context.resources.configuration;
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}


