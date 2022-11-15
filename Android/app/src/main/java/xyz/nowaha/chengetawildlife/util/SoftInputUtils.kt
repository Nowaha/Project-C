package xyz.nowaha.chengetawildlife.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity

object SoftInputUtils {

    /**
     * Show the soft input on an already focused view.
     * @param view An already focused view to show the soft input for.
     */
    fun show(activity: Activity, view: View): Boolean {
        return getIMM(activity)?.showSoftInput(view, 0) ?: false
    }

    /**
     * Request focus on a view and then request the soft input to be shown.
     * @param view The view that should be focused.
     */
    fun focusAndShow(activity: Activity, view: View): Boolean {
        if (view.requestFocus()) {
            return show(activity, view)
        }
        return false
    }

    fun hide(activity: Activity, view: View): Boolean {
        return getIMM(activity)?.hideSoftInputFromWindow(view.windowToken, 0) ?: false
    }

    fun hideAndClearFocus(activity: Activity, view: View): Boolean {
        if (hide(activity, view)) {
            view.clearFocus()
            return true
        }
        return false
    }

    fun getIMM(activity: Activity): InputMethodManager? {
        return activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    }

    // Extension functions
    fun FragmentActivity.showSoftInput(view: View): Boolean = show(this, view)
    fun FragmentActivity.focusAndShowSoftInput(view: View): Boolean = focusAndShow(this, view)
    fun FragmentActivity.hideSoftInput(view: View): Boolean = hide(this, view)
    fun FragmentActivity.hideSoftInputAndClearFocus(view: View): Boolean =
        hideAndClearFocus(this, view)

}