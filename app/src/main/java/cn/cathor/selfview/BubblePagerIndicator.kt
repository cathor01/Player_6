package cn.cathor.selfview

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.util.Property
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.cathor.n_6.R
import com.cathor.n_6.debug
import org.jetbrains.anko.dip
import java.util.*

/**
 * Created by Cathor on 2016/3/2 22:55.
 */

class BubblePagerIndicator : View, ViewPager.OnPageChangeListener {

    companion object{
        private val MAX_DURATION = 300L
    }

    private var y :Int = 0

    private var color : Int = Color.GREEN

    private var pageNumber : Int = 4

    private var offset : Int = 0

    private var distance : Int = 0

    private var maxRadius : Int = 0

    private var minRadius : Int = 0

    private var start: Int = 0

    private var end: Int = 0

    private var progress: Float = 0f

    private var farest : Float = 0f

    private var xArray: Array<Int> = Array(4, {0})

    private var paint: Paint = Paint()

    private var responseRadius: Float = 0f

    private var nowPage: Int = 0

    private var playingAnimator = false

    private val progressProperty : Property<BubblePagerIndicator, Float> = object : Property<BubblePagerIndicator, Float>(Float::class.java, "progress"){
        override fun get(p0: BubblePagerIndicator?): Float? {
            return p0!!.progress
        }

        override fun set(`object`: BubblePagerIndicator?, value: Float?) {
            `object`!!.progress = value!!
            invalidate()
        }
    }

    constructor(context: Context):super(context){
        setOnTouchListener(touchListener)
    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs){
        if(attrs != null) {
            readAttrs(attrs)
        }
        setOnTouchListener(touchListener)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyle : Int): super(context, attrs, defStyle){
        if(attrs != null) {
            readAttrs(attrs)
        }
        setOnTouchListener(touchListener)
    }

    private var lastDown: Long = 0L

    private var belong: Int = -1

    private var touchListener = OnTouchListener {
        view, motionEvent ->
        when(motionEvent.action){
            MotionEvent.ACTION_DOWN -> {
                belong = getBelong(motionEvent.x)
                if(belong != -1) {
                    lastDown = System.currentTimeMillis()
                    true
                }
                else {
                    false
                }
            }
            MotionEvent.ACTION_UP -> {
                if(lastDown != 0L && System.currentTimeMillis() - lastDown < MAX_DURATION && getBelong(motionEvent.x) == belong){
                    nowPage = belong
                    belong = -1
                    lastDown = 0L
                    startAnimator()
                    true
                }
                else{
                    false
                }
            }
            else->false
        }
    }

    private fun startAnimator(){
        end = nowPage
        var animator = ObjectAnimator.ofFloat(this, progressProperty, 0f, 1f)
        animator.duration = 500L
        animator.interpolator = DecelerateInterpolator()
        println("" + end + " : " + start)
        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {
            }
            override fun onAnimationEnd(p0: Animator?) {
                start = nowPage
                progress = 0f
                playingAnimator = false
                invalidate()
            }
            override fun onAnimationCancel(p0: Animator?) {
            }
            override fun onAnimationStart(p0: Animator?) {
                viewPager?.setCurrentItem(nowPage, true)
                playingAnimator = true
            }
        })
        animator.start()
    }

    private fun getBelong(x: Float): Int{
        var result = -1
        for(i in 0..xArray.size - 1){
            if(x < xArray[i]){
                if(xArray[i] - x < responseRadius){
                    result = i
                }
                break
            }
            else{
                if(x - xArray[i]< responseRadius){
                    result = i
                    break
                }
            }
        }
        return result
    }

    private fun readAttrs(attrs: AttributeSet){
        val a = context.obtainStyledAttributes(attrs, R.styleable.BubblePagerIndicator)
        maxRadius = a.getDimensionPixelSize(R.styleable.BubblePagerIndicator_maxRadius, 0)
        minRadius = a.getDimensionPixelSize(R.styleable.BubblePagerIndicator_minRadius, 0)
        offset = a.getDimensionPixelOffset(R.styleable.BubblePagerIndicator_offset, 0)
        pageNumber = a.getInt(R.styleable.BubblePagerIndicator_num, 4)
        start = a.getInt(R.styleable.BubblePagerIndicator_current, 0)
        end = start
        prepos = start
        color = a.getColor(R.styleable.BubblePagerIndicator_bubbleColor, Color.GREEN)
        a.recycle()
        initValue()
        initPaint()
    }

    fun setBubbleColor(color: Int){
        this.color = color
        initPaint()
        invalidate()
    }

    var viewPager: ViewPager? = null

    private fun initPaint(){
        paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        paint.style = Paint.Style.FILL
    }

    fun initValue(){
        y = maxRadius
        distance = offset - 2 * maxRadius + minRadius
        farest = maxRadius - minRadius / 2f + (maxRadius - 2 * minRadius) / (maxRadius - minRadius).toFloat() * distance
        var x1 = maxRadius
        responseRadius = (maxRadius + minRadius) / 2f
        xArray = Array(pageNumber, {0})
        for(i in 0..pageNumber - 1){
            xArray[i] = x1
            x1 += offset
        }
    }

    fun setPageNum(page: Int){
        pageNumber = page
        start = 0
        end = 0
        nowPage = page
        initValue()
        invalidate()
    }

    fun setPosition(position: Int): Boolean{
        if(position < 0 || position > pageNumber - 1){
            return false
        }
        start = position
        end = position
        prepos = start
        invalidate()
        return true
    }

    override fun onPageScrollStateChanged(state: Int) {
        // TODO implements
    }

    private var prepos = 0

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if(!playingAnimator) {
            if (start == end) {
                if (positionOffset < 0.5f) {
                    end = position + 1
                    start = position
                    progress = positionOffset
                } else {
                    start = position + 1
                    end = position
                    progress = 1 - positionOffset
                }
            } else {
                if (positionOffset == 0f) {
                    start = position
                    end = position
                    progress = 0f
                    nowPage = position
                } else {
                    if (position != prepos) {
                        if (positionOffset < 0.5f) {
                            end = position + 1
                            start = position
                            progress = positionOffset
                            nowPage = position
                        } else {
                            start = position + 1
                            end = position
                            progress = 1 - positionOffset
                        }
                    } else {
                        if (start < end) {
                            progress = positionOffset
                        } else if (start > end) {
                            progress = 1 - positionOffset
                        }
                    }
                }
            }
            prepos = position
            invalidate()
        }
    }

    override fun onPageSelected(position: Int) {
        // TODO implements
    }


    override fun onMeasure(eSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(maxRadius * 2 + offset * (pageNumber - 1), maxRadius * 2);
    }

    override fun onDraw(canvas: Canvas){
        this.measure(0, 0)
        println("start = [$start] end = [$end]")
        if (Math.abs(start - end) > 1){
            println("inner start = [$start] end = [$end]")
            var startR = minRadius + (1 - progress) * (maxRadius - minRadius)
            var endR = minRadius + progress * (maxRadius - minRadius)
            for (i in 0..pageNumber - 1) {
                if (i != start && i != end) {
                    canvas.drawCircle(xArray[i].toFloat(), y.toFloat(), minRadius.toFloat(), paint)
                } else if (i == start) {
                    canvas.drawCircle(xArray[i].toFloat(), y.toFloat(), startR, paint)
                } else {
                    canvas.drawCircle(xArray[i].toFloat(), y.toFloat(), endR, paint)
                }
            }
        }
        else {
            var startR = maxRadius * (1 - progress)
            var endR = maxRadius * progress
            for (i in 0..pageNumber - 1) {
                if (i != start && i != end) {
                    canvas.drawCircle(xArray[i].toFloat(), y.toFloat(), minRadius.toFloat(), paint)
                } else if (i == start) {
                    canvas.drawCircle(xArray[i].toFloat(), y.toFloat(), if (startR > minRadius) startR else minRadius.toFloat(), paint)
                } else {
                    canvas.drawCircle(xArray[i].toFloat(), y.toFloat(), if (endR > minRadius) endR else minRadius.toFloat(), paint)
                }
            }
            if (start != end) {
                var path = Path()
                if (start < end) {
                    calculateBezier(path, xArray[start], xArray[end], startR, endR)
                } else {
                    calculateBezier(path, xArray[end], xArray[start], endR, startR)
                }
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun calculateBezier(path: Path, xLeft: Int, xRight: Int, radiusLeft: Float, radiusRight: Float){
        if(xRight == 0 || xRight == maxRadius){
            return
        }
        if(radiusLeft >= minRadius && radiusRight >= minRadius) {
            var percent = (radiusLeft - minRadius) / (maxRadius - minRadius)
            var anchrox = xLeft + maxRadius + distance * percent - minRadius / 2f
            var cos_v = (radiusLeft - radiusRight) / offset
            var sin_v = Math.sqrt(1.0 - cos_v * cos_v).toFloat()
            var x1 = xLeft + radiusLeft * cos_v
            var y1 = y - radiusLeft * sin_v
            var x2 = xRight + radiusRight * cos_v
            var y2 = y - radiusRight * sin_v
            var x3 = x2
            var y3 = y + radiusRight * sin_v
            var x4 = x1
            var y4 = y + radiusLeft * sin_v
            path.reset();
            path.moveTo(x1, y1)
            path.quadTo(anchrox, y.toFloat(), x2, y2)
            path.lineTo(x3, y3)
            path.quadTo(anchrox, y.toFloat(), x4, y4)
            path.lineTo(x1, y1)
            path.close()
        }
        else if(radiusRight < minRadius){
            var percent = radiusRight / minRadius
            var end = xLeft + (farest - maxRadius + minRadius / 2f) * percent + maxRadius - minRadius / 2f
            var endRadius = minRadius / 2f
            var cos_v = (radiusLeft - endRadius) / (end - xLeft)
            var sin_v = Math.sqrt(1.0 - cos_v * cos_v).toFloat()
            var x1 = xLeft + radiusLeft * cos_v
            var y1 = y - radiusLeft * sin_v
            var x2 = end + endRadius * cos_v
            var y2 = y - endRadius * sin_v
            var x3 = x2
            var y3 = y + endRadius * sin_v
            var x4 = x1
            var y4 = y + radiusLeft * sin_v
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)
            path.lineTo(x1, y1)
            path.addCircle(end, y.toFloat(), endRadius, Path.Direction.CW)
        }
        else{
            var percent = radiusLeft / minRadius
            var end = xRight - maxRadius + minRadius / 2f - (farest - maxRadius + minRadius / 2f) * percent
            var endRadius = minRadius / 2f
            var cos_v = (endRadius - radiusRight) / (xRight - end)
            var sin_v = Math.sqrt(1.0 - cos_v * cos_v).toFloat()
            var x1 = end + endRadius * cos_v
            var y1 = y - endRadius * sin_v
            var x2 = xRight + radiusRight * cos_v
            var y2 = y - radiusRight * sin_v
            var x3 = x2
            var y3 = y + radiusRight * sin_v
            var x4 = x1
            var y4 = y + endRadius * sin_v
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)
            path.lineTo(x1, y1)
            path.addCircle(end, y.toFloat(), endRadius, Path.Direction.CW)
        }
    }
}