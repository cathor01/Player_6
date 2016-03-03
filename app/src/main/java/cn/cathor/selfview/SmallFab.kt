package cn.cathor.selfview

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton

/**
 * Created by Cathor on 2016/3/3 21:20.
 */

class SmallFab
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): ImageButton(context, attrs, defStyleAttr, defStyleRes){
    constructor(context: Context) :this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)
}