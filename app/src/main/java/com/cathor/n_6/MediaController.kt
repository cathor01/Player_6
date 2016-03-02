package com.cathor.n_6

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.dip
import org.jetbrains.anko.margin
import org.jetbrains.anko.textColor

/**
 * Created by Cathor on 2016/2/15.
 */

open class MediaController : AppCompatActivity(), Knob.OnScollListener, View.OnClickListener, InMediaController {

    override var listener: MediaControllerListener? = null
    override fun onClick(p0: View?) {
        this.finish()
    }

    override fun finish(){
        listener?.OnControllerFinished()
        super.finish()
    }

    override fun OnStateChange(item: Knob, state: Boolean) {
        when(item.tag){
            bass_knob_tag -> {
                listener?.OnBassStatusChange(state, item.percent)
            }
            virtual_knob_tag -> {
                listener?.OnVirtualizationStatusChange(state, item.percent)
            }
        }
    }

    private fun myRegisterReceiver(){
        var mVolumeReceiver = MyVolumeReceiver() ;
        var filter = IntentFilter() ;
        filter.addAction("android.media.VOLUME_CHANGED_ACTION") ;
        registerReceiver(mVolumeReceiver, filter) ;
    }

    /**
     * 处理音量变化时的界面显示
     * @author long
     */
    private class MyVolumeReceiver : BroadcastReceiver() {
        override fun onReceive(context:Context, intent : Intent) {
            //如果音量发生变化则更改seekbar的位置
            if(intent.action.equals("android.media.VOLUME_CHANGED_ACTION")){
                var currVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ;// 当前的媒体音量
                if(currVolume != null){
                    MediaController.getInstance()?.volumn_knob?.SetPercentage(currVolume)
                    MediaController.getInstance()?.volumn_per?.text = currVolume.toString()
                }
            }
        }
    }

    override fun OnRotate(item: Knob, percent: Int) {
        when(item.tag){
            bass_knob_tag -> {
                listener?.OnBassSpain(percent)
                bass_per?.text = percent.toString()
            }
            virtual_knob_tag -> {
                listener?.OnVIturalizationSpain(percent)
                virtual_per?.text = percent.toString()
            }
            volumn_knob_tag -> {
                listener?.OnVolumnSpain(percent)
                volumn_per?.text = percent.toString()
            }
        }
    }
    val bass_knob_tag = "111"
    val virtual_knob_tag = "112"
    val volumn_knob_tag = "113"
    var bass_per : TextView? = null
    var virtual_per : TextView? = null
    var volumn_per : TextView? = null
    var maxVolume : Int = 0
    var currentVolume : Int = 0
    val back_resource : Int = R.drawable.stator
    val on_resource : Int = R.drawable.rotoron
    val off_resource : Int = R.drawable.rotoroff
    var volumn_knob : Knob? = null
    companion object {
        var audioManager: AudioManager? = null
        private var _instance : MediaController? = null
        fun getInstance(): MediaController?{
            return MediaController._instance;
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MediaController._instance = this
        SetMediaControllerListener((application as MyApplication).controller!!)
        var back = BitmapFactory.decodeResource(resources, back_resource)
        var on = BitmapFactory.decodeResource(resources, on_resource)
        var off = BitmapFactory.decodeResource(resources, off_resource)
        back = Bitmap.createScaledBitmap(back, (back.width * 0.6).toInt(), (back.height * 0.6).toInt(), true)
        on = Bitmap.createScaledBitmap(on, (on.width * 0.6).toInt(), (on.height * 0.6).toInt(), true)
        off = Bitmap.createScaledBitmap(off, (off.width * 0.6).toInt(), (off.height * 0.6).toInt(), true)
        var mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        //最大音量
        MediaController.audioManager = mAudioManager
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //当前音量
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        var bass_state = MyService.getInstance().bassBoostState
        var bass_value = if(bass_state) MyService.getInstance().bassBoost.roundedStrength.toInt() else 0
        var bass_knob = Knob(this, back, off, on, on.width, back.width, 155.0f, 1000, bass_state)
        bass_knob.tag = bass_knob_tag
        bass_knob.id = 991110
        bass_knob.SetPercentage(bass_value)
        var virtual_state = MyService.getInstance().virtualizerState
        var virtual_value = if(bass_state) MyService.getInstance().virtualizer.roundedStrength.toInt() else 0
        var virtual_knob = Knob(this, back, off, on, on.width, back.width, 155.0f, 1000, virtual_state)
        virtual_knob.tag = virtual_knob_tag
        virtual_knob.id = 991120
        if(virtual_state){
            virtual_knob.SetPercentage(virtual_value)
        }
        var volumn_knob = Knob(this, back, on, on.width, back.width, 155.0f, maxVolume)
        volumn_knob.tag = volumn_knob_tag
        volumn_knob.SetPercentage(currentVolume)
        volumn_knob.id = 991130
        this.volumn_knob = volumn_knob
        var relativeLayout = RelativeLayout(this)
        relativeLayout.fitsSystemWindows = true
        var toolb = Toolbar(this)
        toolb.elevation = dip(5).toFloat()
        toolb.id = 991101
        toolb.backgroundColor = this.intent.extras.getLong("Color").toInt()
        toolb.minimumHeight = dip(25)
        toolb.title = "音效设置"
        toolb.setTitleTextColor(Color.WHITE)
        toolb.setNavigationIcon(R.mipmap.abc_ic_ab_back_mtrl_am_alpha)
        var params_tool = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params_tool.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        params_tool.margin = 0
        relativeLayout.addView(toolb, params_tool)
        var one = RelativeLayout(this)
        one.tag = "100"
        one.id = 991200
        var params_bass = RelativeLayout.LayoutParams(back.width, back.height)
        params_bass.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        params_bass.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        params_bass.margin = dip(10)
        one.addView(bass_knob, params_bass)
        var bass_title = TextView(this)
        bass_title.tag = "112"
        bass_title.textSize = 18f
        bass_title.textColor = 0x66222222
        bass_title.id = 991111
        bass_title.text = "低音"
        var params_bass_title = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params_bass_title.addRule(RelativeLayout.ALIGN_LEFT, 991110)
        params_bass_title.addRule(RelativeLayout.BELOW, 991110)
        params_bass_title.margin = dip(5)
        one.addView(bass_title, params_bass_title)
        bass_per = TextView(this)
        bass_per?.tag = "113"
        bass_per?.textSize = 15f
        bass_per?.textColor = 0x66333333
        bass_per?.text = bass_value.toString()
        var params_bass_percent = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params_bass_percent.addRule(RelativeLayout.ALIGN_LEFT, 991110)
        params_bass_percent.addRule(RelativeLayout.BELOW, 991111)
        one.addView(bass_per, params_bass_percent)
        var params_vir = RelativeLayout.LayoutParams(back.width, back.height)
        params_vir.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        params_vir.margin = dip(10)
        params_vir.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        one.addView(virtual_knob, params_vir)
        var vir_title = TextView(this)
        vir_title.tag = "122"
        vir_title.id = 991121
        vir_title.textSize = 18f
        vir_title.textColor = 0x66222222
        vir_title.text = "虚拟化"
        var params_vir_title = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params_vir_title.addRule(RelativeLayout.ALIGN_RIGHT, 991120)
        params_vir_title.addRule(RelativeLayout.BELOW, 991120)
        params_vir_title.margin = dip(10)
        one.addView(vir_title, params_vir_title)
        virtual_per = TextView(this)
        virtual_per?.tag = "123"
        virtual_per?.textSize = 23f
        virtual_per?.textColor = 0x66333333
        virtual_per?.text = virtual_value.toString()
        var params_vir_percent = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params_vir_percent.addRule(RelativeLayout.ALIGN_RIGHT, 991120)
        params_vir_percent.addRule(RelativeLayout.BELOW, 991121)
        one.addView(virtual_per, params_vir_percent)
        var params_one = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params_one.addRule(RelativeLayout.BELOW, 991101)
        relativeLayout.addView(one, params_one)
        var two = RelativeLayout(this)
        two.tag = "130"
        var params_volumn = RelativeLayout.LayoutParams(back.width, back.height)
        params_volumn.margin = dip(10)
        params_volumn.addRule(RelativeLayout.CENTER_HORIZONTAL)
        params_volumn.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        two.addView(volumn_knob, params_volumn)
        var volumn_title = TextView(this)
        volumn_title.tag = "132"
        volumn_title.id = 991131
        volumn_title.textSize = 18f
        volumn_title.textColor = 0x66222222
        volumn_title.text = "音量"
        var params_volumn_title = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params_volumn_title.addRule(RelativeLayout.CENTER_HORIZONTAL)
        params_volumn_title.addRule(RelativeLayout.BELOW, 991130)
        params_volumn_title.margin = dip(5)
        two.addView(volumn_title, params_volumn_title)
        volumn_per = TextView(this)
        volumn_per?.tag = "133"
        volumn_per?.textSize = 15f
        volumn_per?.textColor = 0x66333333
        volumn_per?.text = currentVolume.toString()
        var params_volumn_percent = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params_volumn_percent.addRule(RelativeLayout.CENTER_HORIZONTAL)
        params_volumn_percent.addRule(RelativeLayout.BELOW, 991131)
        two.addView(volumn_per, params_volumn_percent)

        var params_two = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params_two.topMargin = dip(20)
        params_two.addRule(RelativeLayout.BELOW, 991200)
        relativeLayout.addView(two, params_two)
        setContentView(relativeLayout)
        setSupportActionBar(toolb)
//        supportActionBar!!.setHomeButtonEnabled(true)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolb.setNavigationOnClickListener(this)
        bass_knob.SetOnScollListener(this)
        virtual_knob.SetOnScollListener(this)
        volumn_knob.SetOnScollListener(this)
        //myRegisterReceiver()
    }
}