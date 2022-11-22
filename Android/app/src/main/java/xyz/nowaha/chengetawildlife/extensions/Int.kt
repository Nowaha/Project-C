package xyz.nowaha.chengetawildlife.extensions

import android.view.View
import kotlin.math.roundToInt

fun View.dp(dp: Int): Int {
    return (resources.displayMetrics.density * dp).roundToInt()
}