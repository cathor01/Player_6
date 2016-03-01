package com.cathor.n_6

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import org.jetbrains.anko.imageBitmap

/**
 * Created by Cathor on 2016/2/15.
 */

open public class Knob : RelativeLayout, GestureDetector.OnGestureListener, View.OnTouchListener {
    val inner_radius: Int
    val outer_radius: Int
    val origin_img_off : Bitmap
    val origin_img_on : Bitmap
    var rotator : ImageView
    var percent : Int
    var state : Boolean
    val inner_outer : Float
    val max_degree : Float
    val max_percent: Int
    var pre_degree : Float
    var start_degree : Float
    var stateable : Boolean = true
    val detector : GestureDetector
    var now_img : Bitmap
    var listener : OnScollListener? = null

    fun SetOnScollListener(listener: OnScollListener){
        this.listener = listener
    }

    interface OnScollListener{
        fun OnStateChange(item: Knob, state: Boolean)
        fun OnRotate(item: Knob, percent : Int);
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        when(detector.onTouchEvent(p1)) {
            true -> return true
            false -> return super.onTouchEvent(p1)
        }
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        if(p0 == null){
            return false
        }
        var x = p0.x
        var y = p0.y
        var makeup = CalculateMakeup(x, y)
        var degree = CalculateDegree(p0.x, p0.y)
        if(degree < max_degree && degree > - max_degree && state){
            if(makeup < inner_outer + 0.1 && makeup > inner_outer - 0.1){
                ChangeDegree(degree)
                return true
            }
        }
        if(makeup < inner_outer && stateable) {
            if (state) {
                state = false
                now_img = origin_img_off
            } else {
                state = true
                now_img = origin_img_on
            }
            listener?.OnStateChange(this, state)
            rotator.setImageBitmap(rotateBitmap(now_img, percent * pre_degree))
            return true
        }
        return false
    }

    override fun onShowPress(p0: MotionEvent?) {
        if(p0 == null || !state){
            return
        }
        var x = p0.x
        var y = p0.y
        var makeup = CalculateMakeup(x, y)
        println("show press")
        var degree = CalculateDegree(p0.x, p0.y)
        if(degree < max_degree && degree > - max_degree){
            if(makeup < inner_outer + 0.25 && makeup > inner_outer - 0.2){
                ChangeDegree(degree)
            }
        }
    }

    fun SetPercentage(value: Int){
        if(value == percent){
            return
        }
        percent = if(value > 0 && value < max_percent) value else 0
        rotator.imageBitmap = rotateBitmap(now_img, percent * pre_degree)
        listener?.OnRotate(this, percent)
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        if(p1 == null || !state) return false
        var x = p1.x
        var y = p1.y
        var distance = CalculateMakeup(x, y)
        var degree = CalculateDegree(x, y)
        if(distance > 0.1 && degree < max_degree && degree > - max_degree){
            ChangeDegree(degree)
            return true
        }
        return false
    }

    private fun ChangeDegree(degree: Float){
        if (degree > max_degree || degree < - max_degree){
            return
        }
        var percent_ = Math.rint((degree + max_degree) / pre_degree.toDouble()).toInt()
        if(percent_ == percent){
            return
        }
        percent = percent_
        var img = rotateBitmap(now_img, percent * pre_degree)
        rotator.setImageBitmap(img)
        listener?.OnRotate(this, percent)
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return false
    }

    override fun onLongPress(p0: MotionEvent?) {
        Toast.makeText(context, "你想要啥效果？", Toast.LENGTH_SHORT).show()
    }


    public constructor(context: Context, back: Bitmap, top: Bitmap, inner_radius: Int, outer_radius: Int, max_degree : Float, max_percent : Int) : this(context, back, top, top, inner_radius, outer_radius, max_degree, max_percent, true) {
        stateable = false
    }

    /**
     * @param max_degree 最大旋转角度
     * @param max_percent 分格数
     * @param default_on 默认为打开？
     *
     * **/
    public constructor(context: Context, back: Bitmap, top_off: Bitmap, top_on: Bitmap, inner_radius: Int, outer_radius: Int, max_degree : Float, max_percent : Int, default_on : Boolean) : super(context) {
        state = default_on
        this.max_percent = max_percent
        this.inner_radius = inner_radius
        this.outer_radius = outer_radius
        percent = 0
        this.max_degree = max_degree
        origin_img_off = top_off
        origin_img_on = top_on
        pre_degree = 2 * max_degree / max_percent
        start_degree = 0f
        now_img = if (!state) origin_img_off else origin_img_on
        inner_outer = inner_radius.toFloat() / outer_radius
        println("inner/outer=" + inner_outer)
        var back_view = ImageView(context)
        back_view.setImageBitmap(back)
        var back_params = LayoutParams(outer_radius, outer_radius)
        back_params.addRule(RelativeLayout.CENTER_IN_PARENT)
        addView(back_view, back_params)
        rotator = ImageView(context)
        rotator.scaleType = ImageView.ScaleType.CENTER
        rotator.setImageBitmap(rotateBitmap(now_img, 0f))
        var top_params = LayoutParams(inner_radius, inner_radius)
        top_params.addRule(RelativeLayout.CENTER_IN_PARENT)
        addView(rotator, top_params)
        detector = GestureDetector(context, this)
        setOnTouchListener(this)
    }
    private  fun rotateBitmap(bmp : Bitmap, degree : Float) : Bitmap {
        var matrix = Matrix();
        matrix.postRotate(degree - max_degree);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    private fun CalculateMakeup(_x : Float, _y : Float) : Float{
        var x = _x / width - 0.5
        var y = _y / height - 0.5
        var distance = Math.sqrt(x * x + y * y).toFloat() * 2
        return distance
    }

    private fun CalculateDegree(_x : Float, _y : Float) : Float{
        var x = _x / width - 0.5
        var y = _y / height - 0.5
        if(y == 0.0){
            if(x < 0){
                return -90.0f
            }
            else{
                return 90.0f
            }
        }
        if(x < 0){
            var degree = Math.toDegrees(Math.atan(x / y))
            if(degree < 0){
                degree = 180 + degree
            }
            return - degree.toFloat()
        }
        var degree = Math.toDegrees(Math.atan(-x / y))
        if(degree <  0){
            degree = 180 + degree
        }
        return degree.toFloat()
    }
}