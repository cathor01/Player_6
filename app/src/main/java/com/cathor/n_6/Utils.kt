package com.cathor.n_6

import android.os.Message
import android.util.Log

/**
 * Created by Cathor on 2016/3/1 15:21.
 */

inline fun <reified T> T.debug(word: Any?) : Unit{
    Log.d(T::class.qualifiedName, "debug " + word)
}

inline fun <reified T> T.error(word: Any?){
    Log.e(T::class.qualifiedName, "error " + word)}

inline fun <reified T> T.verbose(word: Any?){Log.v(T::class.qualifiedName, "verbose " + word)}

object Logger{
    fun d(message: Any?){
        var temp = Thread.currentThread().stackTrace;
        var a = temp[2]
        Log.d(a.className, "FROM " + a.className + ": " + a.lineNumber + " LOG DEBUG " + message.toString())
    }
    fun e(message: Any?){
        var temp = Thread.currentThread().stackTrace;
        var a = temp[2]
        Log.e(a.className, "FROM " + a.className+ ": " + a.lineNumber + " LOG ERROR " + message.toString())
    }
    fun v(message: Any?){
        var temp = Thread.currentThread().stackTrace;
        var a = temp[2]
        Log.v(a.className, "FROM " + a.className+ ": " + a.lineNumber + " LOG VERBOSE " + message.toString())
    }
}
