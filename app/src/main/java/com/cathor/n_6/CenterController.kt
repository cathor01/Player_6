package com.cathor.n_6

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import org.jetbrains.anko.audioManager

/**
 * Created by Cathor on 2016/2/17.
 */

class CenterController(val context: Context, val preferences: SharedPreferences): MediaControllerListener{


    override fun OnBassStatusChange(status: Boolean, percent: Int) {
        MyService.getInstance().bassBoost.enabled = status
        if(status){
            MyService.getInstance().bassBoost.setStrength(percent.toShort())
        }
        var edit = preferences.edit()
        edit.putBoolean(MyApplication.PREFERENCE_BASS_STATUS, status)
        edit.commit()
    }

    override fun OnControllerFinished() {
        var edit = preferences.edit()
        edit.putBoolean(MyApplication.PREFERENCE_BASS_STATUS, MyService.getInstance().bassBoostState)
        edit.putInt(MyApplication.PREFERENCE_BASS_VALUE, MyService.getInstance().bassBoost.roundedStrength.toInt())
        edit.putBoolean(MyApplication.PREFERENCE_VIRTUAL_STATUS, MyService.getInstance().virtualizerState)
        edit.putInt(MyApplication.PREFERENCE_VIRTUAL_VALUE, MyService.getInstance().virtualizer.roundedStrength.toInt())
        edit.commit()
    }

    override fun OnBassSpain(percent: Int) {
        MyService.getInstance().bassBoost.setStrength(percent.toShort())
    }

    override fun OnVirtualizationStatusChange(status: Boolean, percent: Int) {
        MyService.getInstance().virtualizer.enabled = status
        if(status){
            MyService.getInstance().virtualizer.setStrength(percent.toShort())
        }
        var edit = preferences.edit()
        edit.putBoolean(MyApplication.PREFERENCE_VIRTUAL_STATUS, status)
        edit.commit()
    }

    override fun OnVIturalizationSpain(percent: Int) {
        MyService.getInstance().virtualizer.setStrength(percent.toShort())
    }

    override fun OnVolumnSpain(percent: Int) {
        context.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, percent, 0)
    }
}