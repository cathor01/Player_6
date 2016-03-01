package com.cathor.n_6

/**
 * Created by Cathor on 2016/2/17.
 */

interface InMediaController{
    var listener: MediaControllerListener?
    fun SetMediaControllerListener(listener: MediaControllerListener){
        this.listener = listener
    }
}

interface MediaControllerListener{
    fun OnBassStatusChange(status: Boolean, percent: Int)
    fun OnBassSpain(percent: Int)
    fun OnVirtualizationStatusChange(status: Boolean, percent: Int)
    fun OnVIturalizationSpain(percent: Int)
    fun OnVolumnSpain(percent: Int)
    fun OnControllerFinished()
}