package games.kapow.utils.ui.custom_views

import android.content.Context
import android.view.animation.Interpolator
import android.widget.Scroller

class FixedSpeedScroller(context: Context, interpolator: Interpolator? = null) :
        Scroller(context, interpolator) {

    var scrollDuration = 300

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, scrollDuration)
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
        super.startScroll(startX, startY, dx, dy, scrollDuration)
    }
}
