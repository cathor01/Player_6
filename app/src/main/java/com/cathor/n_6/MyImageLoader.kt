package com.cathor.n_6

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import java.io.File
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.util.*

/**
 * Created by Cathor on 2016/2/29 15:27.
 */

class MyImageLoader {
    companion object{
        private var _instance : MyImageLoader? = null

        fun getInstance(): MyImageLoader {
            if(_instance == null){
                _instance = MyImageLoader()
            }
            return _instance!!
        }
        private val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        private val mUriAlbums = "content://media/external/audio/albums"
        private val options = DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build()
    }



    private constructor(){
    }

    class ValueType(val type: Boolean, val value: Any? )

    private val map: HashMap<Int, ValueType> = HashMap()

    private val urimap: HashMap<Int, Uri?> = HashMap()

    private val imageLoader = ImageLoader.getInstance()

    fun updateValue(music: Music){
        if(music.hashCode() in map.keys){
            map.set(music.hashCode(), ValueType(true, music.img_path))
        }
        else{
            map.put(music.hashCode(), ValueType(true, music.img_path))
        }
    }

    fun loadSystemImg(music: Music): ValueType?{
        val projection = arrayOf("album_art")
        var cur = MainActivity.getInstance().contentResolver.query(Uri.parse(mUriAlbums + "/" + music.album_id), projection, null, null, null)
        var album_art: String? = null
        if (cur!!.count > 0 && cur.columnCount > 0) {
            cur.moveToNext()
            album_art = cur.getString(0)
        }
        cur.close()
        cur = null
        println("Image path------->" + album_art)
        val music_id = music.music_id
        if (album_art == null || album_art == "") {
            if (music_id < 0) {
                if(!map.containsKey(music.hashCode())){
                    map.put(music.hashCode(), ValueType(true, null))
                }
                return null
            } else {
                val ds = getFileDescriptorFromSongID(MainActivity.getInstance(), music_id)
                if (ds != null) {
                    if(!map.containsKey(music.hashCode())) {
                        map.put(music.hashCode(), ValueType(false, ds))
                    }
                    return ValueType(false, ds)
                } else {
                    if(!map.containsKey(music.hashCode())) {
                        map.put(music.hashCode(), ValueType(true, null))
                    }
                    return null
                }
            }
        } else {
            if (!File(album_art).exists()) {
                val ds = getFileDescriptorFromSongID(MainActivity.getInstance(), music_id)
                if (ds != null) {
                    if(!map.containsKey(music.hashCode())) {
                        map.put(music.hashCode(), ValueType(false, ds))
                    }
                    return ValueType(false, ds)
                } else {
                    if(!map.containsKey(music.hashCode())) {
                        map.put(music.hashCode(), ValueType(true, null))
                    }
                    return null
                }
            }
            Log.v("path", album_art)
            if(!map.containsKey(music.hashCode())) {
                map.put(music.hashCode(), ValueType(true, album_art))
            }
            return ValueType(true, album_art)
        }
    }

    fun getImageValue(music: Music): ValueType?{
        if (music.img_path != "") {
            return ValueType(true, music.img_path)
        } else {
            if(music.hashCode() in map.keys){
                var value = map[music.hashCode()]!!
                if(value.value == null){
                    return null
                }
                if(value.type){
                    return ValueType(true, value.value as String)
                }
                return ValueType(false, value.value as FileDescriptor)
            }
            else {
                return loadSystemImg(music)
            }
        }
    }

    private fun getFileDescriptorFromSongID(context: Context, songid: Long): FileDescriptor? {
        var ds: FileDescriptor? = null
        try {
            val uri = getUriFormSongID(songid)
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                ds = pfd.fileDescriptor
            }
        } catch (ex: FileNotFoundException) {
            ex.printStackTrace()
        }

        return ds
    }

    private fun getUriFormSongID(songid: Long): Uri{
        return Uri.parse("content://media/external/audio/media/$songid/albumart")
    }

    private fun getUriFromSystem(music: Music): Uri?{
        if(urimap.containsKey(music.hashCode())){
            return urimap.get(music.hashCode())
        }
        val projection = arrayOf("album_art")
        var cur = MainActivity.getInstance().contentResolver.query(Uri.parse(mUriAlbums + "/" + music.album_id), projection, null, null, null)
        var album_art: String? = null
        if (cur != null && cur.count > 0 && cur.columnCount > 0) {
            cur.moveToNext()
            album_art = cur.getString(0)
            cur.close()
        }
        cur = null
        debug("Image path------->" + album_art)
        val music_id = music.music_id
        if (album_art == null || album_art == "") {
            if (music_id < 0) {
                if(!urimap.containsKey(music.hashCode())){
                    urimap.put(music.hashCode(), null)
                }
                return null
            } else {
                var uri = getUriFormSongID(music_id)
                urimap.put(music.hashCode(), uri)
                return uri
            }
        } else {
            if (!File(album_art).exists()) {
                var uri = getUriFormSongID(music_id)
                urimap.put(music.hashCode(), uri)
                return uri
            }
            Log.v("path", album_art)
            var uri = Uri.fromFile(File(album_art))
            urimap.put(music.hashCode(), uri)
            return uri
        }
    }


   /* Didn't work......
   private var lock = Object()

    private var mAllowLoad = true

    private var firstLoad = true

    private var mStartLoadLimit = 0

    private var mStopLoadLimit = 0

    var executeorService = Executors.newFixedThreadPool(1)

    private val imageCache = Collections.synchronizedList(ArrayList<DrawableCache>())

    private val CACHE_LIMIT = 100

    private var last_index = 0

    private var lock1 = Object()
    private var lock2 = Object()

    interface OnImageLoadListener {
        fun onImageLoad(t: Int, drawable: Drawable)

        fun onError(t: Int)
    }

    fun setLoadLimit(startLoadLimit: Int, stopLoadLimit: Int) {
        if (startLoadLimit > stopLoadLimit) {
            return
        }
        mStartLoadLimit = startLoadLimit
        mStopLoadLimit = stopLoadLimit
    }

    fun restore() {
        mAllowLoad = true
        firstLoad = true
    }

    fun lock() {
        mAllowLoad = false
        firstLoad = false
    }

    fun unlock() {
        mAllowLoad = true
        synchronized (lock) {
            lock.notifyAll()
        }
    }*/

    private val imageLoaderListener = MyImageLoaderListener()

    fun loadImage(musics: ArrayList<Music>, position: Int, imageView: ImageView) {
        imageView.tag = position
        if (imageLoaderListener.LoadList(position, musics)) {
            imageLoader.displayImage(getUriFromSystem(musics[0]).toString(), imageView, options, imageLoaderListener)
        }
    }

    fun setOnImageLoadListener(listener: OnImageLoadListener){
        imageLoaderListener.setOnImageLoadListener(listener)
    }

    interface OnImageLoadListener{
        fun OnImageLoaded(position: Int)
        fun OnImageLoadFailed(position: Int)
    }



    private class MyImageLoaderListener : ImageLoadingListener{
        private data class IndicatorIndex(val indivator: Int,var index: Int)
        private var listener :OnImageLoadListener? = null
        constructor(){}
        fun setOnImageLoadListener(listener: OnImageLoadListener){
            this.listener = listener
        }
        private val musics_map = Collections.synchronizedMap(HashMap<IndicatorIndex, ArrayList<Music>>())

        fun LoadList(indicator: Int, musics: ArrayList<Music>):Boolean{
            musics_map.put(IndicatorIndex(indicator, 0), musics)
            return true
        }

        fun DropList(){
            musics_map.clear()
        }

        override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
            if(view != null) {
                var img = view as ImageView
                var indicator = view.tag as Int
                if (loadedImage != null) {
                    //img.setImageBitmap(loadedImage)\
                    listener?.OnImageLoaded(indicator)
                } else {
                    var pair = musics_map.keys.find { it.indivator == indicator }
                    if(pair != null){
                        var list = musics_map.get(pair)
                        var index = pair.index
                        if(list != null && list.size - 1 > index){
                            pair.index++
                            var uri = MyImageLoader.getInstance().getUriFromSystem(list[0])
                            ImageLoader.getInstance().displayImage(uri.toString(), img, options, this)
                            return
                        }
                    }
                    img.setImageResource(R.drawable.choose)
                    listener?.OnImageLoadFailed(indicator)
                }
            }
        }

        override fun onLoadingCancelled(p0: String?, p1: View?) {
            debug("canceled")
        }

        override fun onLoadingStarted(p0: String?, p1: View?) {
            debug("started")
        }

        override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
            if(view != null && failReason != null && failReason.type.equals(FailReason.FailType.IO_ERROR)){
                var indicator = view.tag as Int
                var pair = musics_map.keys.find { it.indivator == indicator }
                if(pair != null){
                    var list = musics_map.get(pair)
                    var index = pair.index
                    if(list != null && list.size - 1 > index){
                        pair.index++
                        var uri = MyImageLoader.getInstance().getUriFromSystem(list[0])
                        ImageLoader.getInstance().displayImage(uri.toString(), view as ImageView, options, this)
                        return
                    }
                }
                (view as ImageView).setImageResource(R.drawable.choose)
                listener?.OnImageLoadFailed(indicator)
            }
        }
    }

    /*private fun loadImage(musics: ArrayList<Music>, mt: Int, mListener: OnImageLoadListener ) {
        for(music in musics) {
            var d = getDrawableFromCache(music)
            if (d != null) {
                mListener.onImageLoad(mt, d)
                return
            }
            try {
                val bitmap = loadBitmap(music)
                if (bitmap != null) {
                    var width = bitmap.width
                    var height = bitmap.height
                    var matrix = Matrix()
                    matrix.postScale(100.0f / width, 100.0f / height)
                    var newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
                    var d = BitmapDrawable(newbmp)
                    addItem(music, d)
                    mListener.onImageLoad(mt, d)
                    bitmap.recycle()
                    return
                }
                else{
                    continue
                }
            } catch (e: IOException) {
                e.printStackTrace()
                continue
            }
        }
        mListener.onError(mt)
    }

    private fun getRandom(count: Int): Int {
        return Math.round(Math.random() * count).toInt()
    }

    private val random_words = "abcdefghijklmnopqrstuvwxyz1234567890"

    private fun getRandomString(length: Int): String {
        val sb = StringBuffer()
        val len = random_words.length
        for (i in 0..length - 1) {
            sb.append(random_words[getRandom(len - 1)])
        }
        return sb.toString()
    }

    fun getDrawableFromCache(music: Music):Drawable?{
        var item : DrawableCache? = null
        var index = 0
        while(index < imageCache.size){
            if(imageCache.get(index).id == music.hashCode()){
                item = imageCache.get(index)
            }
            index ++
        }
        if(item != null){
            if(item.drawable != null){
                if(item.drawable!!.get() != null) {
                    return item.drawable!!.get()
                }
                else{
                    imageCache.removeAt(index)
                    return null
                }
            }
            else{
                return Drawable.createFromStream(FileInputStream(item.path), null)
            }
        }
        return null
    }*/

    /*fun addItem(music:Music, drawable: Drawable){
        if(imageCache.size >= CACHE_LIMIT){
            var old_draw = imageCache[last_index].drawable!!.get()
            if(old_draw == null){
                imageCache.removeAt(last_index)
                imageCache.add(DrawableCache(System.currentTimeMillis(), music.hashCode(), drawable = SoftReference(drawable)))
            }
            else {
                var bmp = (old_draw as BitmapDrawable).getBitmap();
                //先把Drawable转成Bitmap，如果是Bitmap，就不用这一步了
                val root = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path + "/../SimplePlayer/thumbs/")
                if (!root.exists()) {
                    root.mkdir()
                }
                var file = File(root, getRandomString(8))
                var fop = FileOutputStream(file);
                try {
                    //实例化FileOutputStream，参数是生成路径
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fop);
                    //压缩bitmap写进outputStream 参数：输出格式  输出质量  目标OutputStream
                    //格式可以为jpg,png,jpg不能存储透明
                    //关闭流
                    //bmp.recycle()
                    imageCache[last_index].drawable = null
                    imageCache[last_index].path = file.path
                    last_index++
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    fop.close();
                    imageCache.add(DrawableCache(System.currentTimeMillis(), music.hashCode(), drawable = SoftReference(drawable)))
                }
            }
        }
        else{
            imageCache.add(DrawableCache(System.currentTimeMillis(), music.hashCode(), drawable = SoftReference(drawable)))
            println(imageCache.size)
        }
    }

    private fun loadStream(music: Music): ValueType?{
        if(music.hashCode() in map.keys){
            var value = map[music.hashCode()]!!
            if(value.value == null){
                return null
            }
            return value
        }
        else {
            return loadSystemImg(music)
        }

    }

    fun loadBitmap(music: Music) : Bitmap?{
        var valuetype = loadStream(music)
        if(valuetype == null){
            return null
        }
        else{
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            options.inSampleSize = 2   // width，hight设为原来的十分一
            try {
                if(valuetype.type) {
                    BitmapFactory.decodeFile(valuetype.value as String, options)
                }
                else{
                    BitmapFactory.decodeFileDescriptor(valuetype.value as FileDescriptor, null, options)
                }
                val REQUIRED_SIZE = 100
                var width_tmp = options.outWidth
                var height_tmp = options.outHeight
                var scale = 1
                while (true) {
                    if (width_tmp / 2 < REQUIRED_SIZE
                            || height_tmp / 2 < REQUIRED_SIZE)
                        break;
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }

                // decode with inSampleSize
                var o2 = BitmapFactory.Options()
                o2.inSampleSize = scale;
                var bitmap = if(valuetype.type) {
                    BitmapFactory.decodeFile(valuetype.value as String, o2)
                }
                else{
                    BitmapFactory.decodeFileDescriptor(valuetype.value as FileDescriptor, null, o2)
                }
                return bitmap
            } catch(e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }

    private class DrawableCache(val time: Long, val id: Int, var drawable: SoftReference<Drawable>? = null, var path: String? = null, var failed: Boolean = false) : Comparable<DrawableCache>{
        override fun compareTo(other: DrawableCache): Int {
            return this.time.compareTo(other.time)
        }

        override fun hashCode(): Int{
            return "drawablecache".hashCode() + id.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other is DrawableCache){
                return other.id.equals(this.id)
            }
            return false
        }
    }

    protected fun finalize(){
        val root = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path + "/../SimplePlayer/thumbs/")
        if(root.exists()) {
            deleteDir(root)
        }
        executeorService.shutdownNow()
    }

    private fun deleteDir(dir: File) :Boolean{
        if (dir.isDirectory()) {
            var children = dir.list();
            for (i in 0..children.size - 1) {
                var success = deleteDir(File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }*/
}
