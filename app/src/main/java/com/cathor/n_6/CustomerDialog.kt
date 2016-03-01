package com.cathor.n_6

/**
 * Created by Cathor on 2016/2/14.
 */
import android.app.Dialog
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import me.zhanghai.android.materialprogressbar.*

open class CustomeProgressDialog : ProgressDialog{

    val mContext: Context
    var message : String = " "

    constructor(context: Context): super(context, R.style.loading_dialog) {
        this.mContext = context
    }

    constructor(context : Context, theme : Int) : super(context, theme){
        this.mContext = context
    };


    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState);
        this.setIndeterminateDrawable(IndeterminateProgressDrawable(mContext)) ;
        this.setCanceledOnTouchOutside(false) ;
        this.setContentView(R.layout.dialog_material)
        var text = this.findViewById(R.id.tipTextView) as TextView
        text.setText(message)
    }

    override fun setMessage(message: CharSequence?) {
        super.setMessage(message)
        this.message = message.toString()
    }


    companion object {
        fun  show(context : Context, message : String):CustomeProgressDialog
        {
            var dialog = CustomeProgressDialog(context, R.style.loading_dialog)
            dialog.setMessage(message)
            dialog.setCancelable(false)
            dialog.show() ;
            return dialog ;
        }
    }
}