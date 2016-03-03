package cn.cathor.selfview

import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ListView
import android.widget.RelativeLayout

/**
 * Created by Cathor on 2016/3/3 20:51.
 */

class FABListGroup
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): RelativeLayout(context, attrs, defStyleAttr, defStyleRes){

    var mainButton : FloatingActionButton


    init{
        mainButton = FloatingActionButton(context)
        var layoutParam = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        addView(mainButton, layoutParams)
    }

    constructor(context: Context) :this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)


}