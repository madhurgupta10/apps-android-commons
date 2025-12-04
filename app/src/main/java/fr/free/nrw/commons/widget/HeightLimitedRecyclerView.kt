package fr.free.nrw.commons.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.util.DisplayMetrics

import androidx.recyclerview.widget.RecyclerView


/**
 * Created by Ilgaz Er on 8/7/2018.
 */
class HeightLimitedRecyclerView : RecyclerView {
    private var height: Int = 0

    constructor(context: Context) : super(context) {
        initializeHeight(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initializeHeight(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initializeHeight(context)
    }

    private fun initializeHeight(context: Context) {
        val displayMetrics = DisplayMetrics()
        val activity = getActivity(context)
        if (activity != null) {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            height = displayMetrics.heightPixels
        } else {
            // Fallback to resources display metrics if activity is not available
            height = context.resources.displayMetrics.heightPixels
        }
    }

    /**
     * Unwraps the context to find the Activity, handling Hilt's FragmentContextWrapper
     */
    private fun getActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val limitedHeightSpec = MeasureSpec.makeMeasureSpec(
            (height * 0.3).toInt(),
            MeasureSpec.AT_MOST
        )
        super.onMeasure(widthSpec, limitedHeightSpec)
    }
}
