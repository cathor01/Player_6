package cn.cathor.selfview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button

/**
 * Created by Cathor on 2016/3/3 20:00.
 */
class GeniusButton
constructor(context: Context, attrs: AttributeSet?, defStyle: Int): Button(context, attrs, defStyle)
{
    private var touchEffectAnimator: TouchEffectAnimator? = null

    init{
        setTouchEffect(TouchEffectAnimator.TouchEffect.Ripple)
    }



    fun setTouchEffect(touchEffect: TouchEffectAnimator.TouchEffect) {
        if (touchEffect === TouchEffectAnimator.TouchEffect.None)
            touchEffectAnimator = null
        else {
            if (touchEffectAnimator == null) {
                touchEffectAnimator = TouchEffectAnimator(this)
                touchEffectAnimator!!.setTouchEffect(touchEffect)
                touchEffectAnimator!!.setEffectColor(Color.WHITE)
                touchEffectAnimator!!.setClipRadius(20)
            }
        }
    }

    override  fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (touchEffectAnimator != null)
            touchEffectAnimator!!.onMeasure()
    }

    override fun onDraw(canvas: Canvas) {
        if (touchEffectAnimator != null)
            touchEffectAnimator!!.onDraw(canvas)
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (touchEffectAnimator != null)
            touchEffectAnimator!!.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
}