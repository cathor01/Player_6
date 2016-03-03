package cn.cathor.selfview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.NinePatchDrawable
import android.util.AttributeSet
import android.widget.ImageView
import com.cathor.n_6.R

/**
 * Created by Cathor on 2016/3/1 21:06.
 */

class EditRoundImageView : ImageView {

    companion object{
        private val defaultRadius = 0
    }

    private var leftTopRadius: Int = 0
    private var leftBottomRadius: Int = 0
    private var rightTopRadius: Int = 0
    private var rightBottomRadius: Int = 0

    constructor(context: Context) :super(context){
        // TODO Auto-generated constructor stub
    }

    constructor(context: Context, attrs: AttributeSet, defStyle : Int): super(context, attrs, defStyle) {
        // TODO Auto-generated constructor stub
        setDefaultAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet) :super(context, attrs) {
        // TODO Auto-generated constructor stub
        setDefaultAttributes(attrs)
    }

    fun setDefaultAttributes(attrs: AttributeSet){
        val a = context.obtainStyledAttributes(attrs, R.styleable.EditRoundImageView)
        leftTopRadius = a.getDimensionPixelSize(
                R.styleable.EditRoundImageView_left_top_radius, defaultRadius)
        leftBottomRadius = a.getDimensionPixelSize(
                R.styleable.EditRoundImageView_left_bottom_radius, defaultRadius)
        rightTopRadius = a.getDimensionPixelSize(
                R.styleable.EditRoundImageView_right_top_radius, defaultRadius)
        rightBottomRadius = a.getDimensionPixelSize(
                R.styleable.EditRoundImageView_right_bottom_radius, defaultRadius)
    }


    override fun onDraw(canvas: Canvas?) {
        val drawable = drawable ?: return

        if (width == 0 || height == 0) {
            return
        }
        this.measure(0, 0)
        if (drawable.javaClass == NinePatchDrawable::class.java)
            return
        val b = (drawable as BitmapDrawable).bitmap
        val bitmap = b.copy(Bitmap.Config.ARGB_8888, true)
        val maxRadius = if (height < width) height / 2  else width / 2
        reCaculate(maxRadius)
        // 保证重新读取图片后不会因为图片大小而改变控件宽、高的大小（针对宽、高为wrap_content布局的imageview，但会导致margin无效）
        // if (defaultWidth != 0 && defaultHeight != 0) {
        // LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        // defaultWidth, defaultHeight);
        // setLayoutParams(params);
        // }
        var output = getCroppedRoundBitmap(bitmap)
        canvas!!.drawBitmap(output, 0f, 0f, null)
    }

    fun getCroppedRoundBitmap(bmp: Bitmap): Bitmap {
        var origin : Bitmap?
        origin = bmp
        if(origin != null) {
            // 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图片
            val bmpWidth = bmp.width
            val bmpHeight = bmp.height
            if (bmpHeight != height || bmpWidth != width) {
                var matrix = Matrix()
                matrix.postScale(width.toFloat() / bmpWidth, height.toFloat() / bmpHeight)
                origin = Bitmap.createBitmap(origin, 0, 0, bmpWidth, bmpHeight, matrix, true)!!
            }
            val output = Bitmap.createBitmap(origin.width,
                    origin.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val rect = Rect(0, 0, width, height)

            val paint = Paint()
            val path = Path()
            path.moveTo(0f, leftTopRadius.toFloat())
            if (leftTopRadius != 0) {
                path.arcTo(RectF(0f, 0f, leftTopRadius * 2f, leftTopRadius * 2f), 180f, 90f)
            }
            path.lineTo(width - rightTopRadius.toFloat(), 0f)
            if (rightTopRadius != 0) {
                path.arcTo(RectF(width - rightTopRadius * 2f, 0f, width.toFloat(), rightTopRadius * 2f), 270f, 90f)
            }
            path.lineTo(width.toFloat(), height - rightBottomRadius.toFloat())
            if (rightBottomRadius != 0) {
                path.arcTo(RectF(width - rightBottomRadius * 2f, height - rightBottomRadius * 2f, width.toFloat(), height.toFloat()), 0f, 90f)
            }
            path.lineTo(leftBottomRadius.toFloat(), height.toFloat())
            if (leftBottomRadius != 0) {
                path.arcTo(RectF(0f, height - leftBottomRadius * 2f, leftBottomRadius * 2f, height.toFloat()), 90f, 90f)
            }
            //path.lineTo(0f, leftTopRadius.toFloat())
            path.close()
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true
            paint.style = Paint.Style.FILL
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawPath(path, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(origin, rect, rect, paint)
            // bitmap回收(recycle导致在布局文件XML看不到效果)
            // bmp.recycle();
            // squareBitmap.recycle();
            // scaledSrcBmp.recycle();
            origin = null
            return output
        }
        return bmp
    }

    private fun reCaculate(maxRadius: Int){
        this.leftBottomRadius = if(leftBottomRadius > maxRadius) maxRadius else leftBottomRadius
        this.leftTopRadius = if(leftTopRadius > maxRadius) maxRadius else leftTopRadius
        this.rightBottomRadius = if(rightBottomRadius > maxRadius) maxRadius else rightBottomRadius
        this.rightTopRadius = if(rightTopRadius > maxRadius) maxRadius else rightTopRadius
    }
}