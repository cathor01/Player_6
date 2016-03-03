package cn.cathor.selfview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Property
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import com.cathor.n_6.R

class MaterialButton : Button {

    companion object{
        val defaultColor = Color.GREEN
        val defaultMaxTime = 600L
        val defaultShadowColor: Int = Color.BLACK
        val ANIMATION_INTERPOLATOR = DecelerateInterpolator();

    }

    val backColor : Int

    val maxTime : Long

    private var paint :Paint = Paint()

    private var minRadius: Int = 0
    private var maxRadius: Int = 0

    private var currentRadius = 0f

    private var runningAnimation : ObjectAnimator? = null

    private var drawShadow = false

    private var point: Pointf = Pointf(0f, 0f)

    private val radiusProperty = object : Property<MaterialButton, Float>(Float::class.java, "radius"){
        override fun get(p0: MaterialButton?): Float? {
            return currentRadius
        }

        override fun set(`object`: MaterialButton?, value: Float?) {
            currentRadius = value!!
            invalidate()
        }
    }

    private var currentColor : Int = Color.TRANSPARENT

    private val colorProperty = object : Property<MaterialButton, Int>(Int::class.java, "color"){
        override fun get(p0: MaterialButton?): Int? {
            return currentColor
        }

        override fun set(`object`: MaterialButton?, value: Int?) {
            currentColor = value!!.toInt()
        }
    }

    private val clearProperty = object : Property<MaterialButton, Int>(Int::class.java, "color"){
        override fun get(p0: MaterialButton?): Int? {
            return currentColor
        }

        override fun set(`object`: MaterialButton?, value: Int?) {
            currentColor = value!!
            invalidate()
        }
    }

    private val mPaintXProperty = object : Property<MaterialButton, Float>(Float::class.java, "paintX") {
        override fun get(`object`: MaterialButton): Float? {
            return `object`.point.x
        }

        override fun set(`object`: MaterialButton, value: Float?) {
            `object`.point.x = value!!
        }
    }

    private val mPaintYProperty = object : Property<MaterialButton, Float>(Float::class.java, "paintY") {
        override fun get(`object`: MaterialButton): Float? {
            return `object`.point.y
        }

        override fun set(`object`: MaterialButton, value: Float?) {
            `object`.point.y = value!!
        }
    }

    private class IntArgbEvaluator : TypeEvaluator<Int> {
        private constructor()

        /**
         * This function returns the calculated in-between value for a color
         * given integers that represent the start and end values in the four
         * bytes of the 32-bit int. Each channel is separately linearly interpolated
         * and the resulting calculated values are recombined into the return value.

         * @param fraction The fraction from the starting to the ending values
         * *
         * @param startValue A 32-bit int value representing colors in the
         * * separate bytes of the parameter
         * *
         * @param endValue A 32-bit int value representing colors in the
         * * separate bytes of the parameter
         * *
         * @return A value that is calculated to be the linearly interpolated
         * * result, derived by separating the start and end values into separate
         * * color channels and interpolating each one separately, recombining the
         * * resulting values in the same way.
         */
        override fun evaluate(fraction: Float, startValue: Int, endValue: Int): Int {
            val startInt = startValue
            val startA = startInt shr 24 and 0xff
            val startR = startInt shr 16 and 0xff
            val startG = startInt shr 8 and 0xff
            val startB = startInt and 0xff

            val endInt = endValue
            val endA = endInt shr 24 and 0xff
            val endR = endInt shr 16 and 0xff
            val endG = endInt shr 8 and 0xff
            val endB = endInt and 0xff

            return (startA + (fraction * (endA - startA)).toInt() shl 24).toInt() or
                    (startR + (fraction * (endR - startR)).toInt() shl 16).toInt() or
                    (startG + (fraction * (endG - startG)).toInt() shl 8).toInt() or
                    (startB + (fraction * (endB - startB)).toInt()).toInt()
        }

        companion object {
            /**
             * Returns an instance of `ArgbEvaluator` that may be used in
             * [ValueAnimator.setEvaluator]. The same instance may
             * be used in multiple `Animator`s because it holds no state.
             * @return An instance of `ArgbEvalutor`.
             * *
             * *
             * @hide
             */
            val instance = IntArgbEvaluator()
        }
    }

    private fun startAnimator(press: Boolean){
        var time = maxTime
        var start: Float
        var speed = 0.3f
        var end : Float
        if (height < width) {
            start = height.toFloat()
            end = width.toFloat()
        } else {
            start = width.toFloat()
            end = height.toFloat()
        }
        start = if (start / 2 > point.y) start - point.y else point.y;
        end = end * 0.8f / 2f;
        if (start > end) {
            start = end * 0.6f;
            end = end / 0.8f;
            time = (time * 0.65).toLong();
            speed = 1f;
        }

        var startRadius = (if (start / 2 > point.y) start - point.y else point.y) * 1.15f
        var endRadius = (if (end / 2 > point.x) end - point.x else point.x) * 0.85f
        var radiusAni = ObjectAnimator.ofFloat(this, radiusProperty, startRadius, endRadius)
        if(!press){
            var aPaintX = ObjectAnimator.ofFloat(this, mPaintXProperty, point.x, width / 2f)
            aPaintX.duration = time;
            //PaintY
            var aPaintY = ObjectAnimator.ofFloat(this, mPaintYProperty, point.y, height / 2f)
            aPaintY.duration = (time * speed).toLong();
            var colorAni = ObjectAnimator.ofObject(this, colorProperty, IntArgbEvaluator.instance, defaultShadowColor, Color.TRANSPARENT)
            colorAni.duration = time
            radiusAni.duration = time
            var aniset = AnimatorSet()
            aniset.playTogether(aPaintX, aPaintY, colorAni, radiusAni)
            aniset.interpolator = ANIMATION_INTERPOLATOR
            aniset.addListener(object : Animator.AnimatorListener{
                override fun onAnimationEnd(p0: Animator?) {
                    drawShadow = false
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                    drawShadow = true
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
            aniset.start()
        }
        else{
            currentColor = defaultShadowColor
            runningAnimation = radiusAni
            runningAnimation!!.interpolator = DecelerateInterpolator()
            runningAnimation!!.duration = 2 * (maxTime / end * endRadius).toLong()
            runningAnimation!!.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    runningAnimation = null
                }

                override fun onAnimationCancel(p0: Animator?) {
                    runningAnimation = null
                }

                override fun onAnimationStart(p0: Animator?) {
                    drawShadow = true
                }
            })
            runningAnimation!!.start()
        }
    }

    private fun initPaint(){
        paint = Paint()
        paint.color = currentColor
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean{
        if(motionEvent.action == MotionEvent.ACTION_DOWN) {
            point = Pointf(motionEvent.x, motionEvent.y)
            //calculateRadius()
            startAnimator(false)
        }
        return super.onTouchEvent(motionEvent)
    }


    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle){
        if(attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MaterialButton, defStyle, 0)
            this.backColor = a.getColor(R.styleable.MaterialButton_backColor, defaultColor)
            this.maxTime = a.getInteger(R.styleable.MaterialButton_maxTime, defaultMaxTime.toInt()).toLong()
            a.recycle()
        }
        else{
            this.backColor = defaultColor
            this.maxTime = defaultMaxTime
        }
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    override fun onDraw(canvas: Canvas){
        canvas.save()
        var rectPaint = Paint()
        rectPaint.color = backColor
        rectPaint.style = Paint.Style.FILL
        rectPaint.isAntiAlias = true
        canvas.drawRect(Rect(0, 0, width, height), rectPaint)
        if(drawShadow) {
            var rectPaintD = Paint()
            rectPaintD.color = Color.argb(33, 0, 0, 0)
            rectPaintD.style = Paint.Style.FILL
            rectPaintD.isAntiAlias = true
            canvas.drawRect(Rect(0, 0, width, height), rectPaintD)
            initPaint()
            canvas.drawCircle(point.x, point.y, currentRadius.toFloat(), paint)
        }
        canvas.restore()
        background = null
        super.onDraw(canvas)
    }
}

data class Pointf(var x: Float, var y: Float)