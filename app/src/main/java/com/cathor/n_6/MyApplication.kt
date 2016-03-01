package com.cathor.n_6

import android.app.Activity
import android.app.Application

/**
 * Created by Cathor on 2016/2/17.
 */

class MyApplication : Application {
    companion object{
        val PREFERENCE_NAME = "Perferences"
        val PREFERENCE_BASS_STATUS = "bass_status"
        val PREFERENCE_BASS_VALUE = "bass_value"
        val PREFERENCE_VIRTUAL_STATUS = "virtual_status"
        val PREFERENCE_VIRTUAL_VALUE = "virtual_value"
        val PREFERENCE_SKIP_VALUE = "skip_value"
    }
    constructor(): super(){

    }
    var datamanager : DataManager? = null
    var controller: CenterController? = null;
    //  在程序onCreate时就可以调用

    override fun onCreate() {
        super.onCreate()
        controller = CenterController(this, getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE))
        datamanager = MyDataManager(this, getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE).getInt(PREFERENCE_SKIP_VALUE, 500))
    }

    fun GetDataManager(): DataManager {
        if (datamanager == null){
            datamanager = MyDataManager(this, getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE).getInt(PREFERENCE_SKIP_VALUE, 500))
        }
        return datamanager!!
    }

}