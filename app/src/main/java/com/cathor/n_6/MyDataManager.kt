package com.cathor.n_6

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.AsyncTask
import android.provider.MediaStore
import org.jetbrains.anko.async
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileNotFoundException
import java.util.*

/**
* Created by Cathor on 2016/2/17 21:30.
*/

class MyDataManager : DataManager {

    companion object{
        val init_color : Long = 0xff7744ff
        val init_color_darker : Long = 0xff4f2DAA
    }

    val context: Context
    var db : SQLiteDatabase? = null
    var album_list:HashMap<String, ArrayList<Music>>
    var author_list:HashMap<String, ArrayList<Music>>
    var folder_list:HashMap<String, ArrayList<Music>>
    var play_list:HashMap<String, ArrayList<Music>>
    var music_list : ArrayList<Music>
    var has_loaded: Boolean = false
    var scan_system : Boolean = false
    val skip :Int
    private val DB_DIR: String
    private val DB_FILE : String
    constructor(context: Context,skip : Int){
        this.context = context
        this.skip = skip
        album_list = hashMapOf<String, ArrayList<Music>>()
        author_list = hashMapOf<String, ArrayList<Music>>()
        folder_list = hashMapOf<String, ArrayList<Music>>()
        play_list = hashMapOf<String, ArrayList<Music>>()
        music_list = ArrayList<Music>()
        DB_FILE = context.filesDir.toString() + "/../databases/data.db3"
        DB_DIR = context.filesDir.toString() + "/../databases/"
    }

    /**
     * 根据name来在程序数据库中查询数组
     * @param db 程序数据库
     * *
     * @param name 查询的名称
     * *
     * @param array 分类的依据及输出Map
     * *
     */
    private fun loadItemFormDatabase(db: SQLiteDatabase, name: String, array: Map<String, ArrayList<Music>>) {
        for (item in array.keys) {
            println(item)
            val cursor2 = db.query("music_info", arrayOf("title", "author", "path", "album", "album_id", "folder", "play_list", "music_id", "img_path", "color", "color_darker"), name + "=\"" + item + "\"", null, null, null, null)
            cursor2.moveToFirst()
            var music = Music(cursor2.getString(cursor2.getColumnIndex("title")),
                    cursor2.getString(cursor2.getColumnIndex("author")),
                    cursor2.getString(cursor2.getColumnIndex("path")),
                    cursor2.getString(cursor2.getColumnIndex("album")),
                    cursor2.getLong(cursor2.getColumnIndex("album_id")),
                    cursor2.getString(cursor2.getColumnIndex("folder")),
                    cursor2.getString(cursor2.getColumnIndex("play_list")),
                    cursor2.getLong(cursor2.getColumnIndex("music_id")),
                    cursor2.getString(cursor2.getColumnIndex("img_path")),
                    cursor2.getLong(cursor2.getColumnIndex("color")),
                    cursor2.getLong(cursor2.getColumnIndex("color_darker"))
                    )
            array[item]!!.add(music)
            while (cursor2.moveToNext()) {
                music = Music(cursor2.getString(cursor2.getColumnIndex("title")),
                        cursor2.getString(cursor2.getColumnIndex("author")),
                        cursor2.getString(cursor2.getColumnIndex("path")),
                        cursor2.getString(cursor2.getColumnIndex("album")),
                        cursor2.getLong(cursor2.getColumnIndex("album_id")),
                        cursor2.getString(cursor2.getColumnIndex("folder")),
                        cursor2.getString(cursor2.getColumnIndex("play_list")),
                        cursor2.getLong(cursor2.getColumnIndex("music_id")),
                        cursor2.getString(cursor2.getColumnIndex("img_path")),
                        cursor2.getLong(cursor2.getColumnIndex("color")),
                        cursor2.getLong(cursor2.getColumnIndex("color_darker")))
                array[item]!!.add(music)
            }
            cursor2.close()
        }
    }

    /**
     * 从程序数据库中获取音乐列表
     */
    private fun loadFromDatabase(db: SQLiteDatabase) {
        var cursor = db.query("album_list", arrayOf("title"), null, null, null, null, null)
        cursor.moveToFirst()
        if(cursor.getColumnIndex("title") != -1){
            album_list.put(cursor.getString(cursor.getColumnIndex("title")),
                    ArrayList<Music>())
            while (cursor.moveToNext()) {
                album_list.put(cursor.getString(cursor.getColumnIndex("title")),
                        ArrayList<Music>())
            }
            loadItemFormDatabase(db, "album", album_list)
        }
        cursor.close()
        cursor = db.query("author_list", arrayOf("author"), null, null, null, null, null)
        cursor.moveToFirst()
        if(cursor.getColumnIndex("author") != -1) {
            author_list.put(cursor.getString(cursor.getColumnIndex("author")),
                    ArrayList<Music>())
            while (cursor.moveToNext()) {
                author_list.put(cursor.getString(cursor.getColumnIndex("author")),
                        ArrayList<Music>())
            }
            loadItemFormDatabase(db, "author", author_list)
        }
        cursor.close()
        cursor = db.query("folder_list", arrayOf("folder"), null, null, null, null, null)
        cursor.moveToFirst()
        if(cursor.getColumnIndex("folder") != -1) {
            folder_list.put(cursor.getString(cursor.getColumnIndex("folder")),
                    ArrayList<Music>())
            while (cursor.moveToNext()) {
                folder_list.put(cursor.getString(cursor.getColumnIndex("folder")),
                        ArrayList<Music>())
            }
            loadItemFormDatabase(db, "folder", folder_list)
        }
        cursor.close()
        cursor = db.query("play_list", arrayOf("play_list"), null, null, null, null, null)
        cursor.moveToFirst()
        if(cursor.getColumnIndex("play_list") != -1) {
            play_list.put(cursor.getString(cursor.getColumnIndex("play_list")),
                    ArrayList<Music>())
            while (cursor.moveToNext()) {
                play_list.put(cursor.getString(cursor.getColumnIndex("play_list")),
                        ArrayList<Music>())
            }
            loadItemFormDatabase(db, "play_list", play_list)
        }
        cursor.close()

    }


    /**

     * 在系统数据库查询中插入第一个元素，并在程序中记录

     */
    private fun insertFirst(db: SQLiteDatabase, music: Music) {
        val album_array = ArrayList<Music>()
        album_array.add(music)
        album_list.put(music.album, album_array)
        val author_array = ArrayList<Music>()
        author_array.add(music)
        author_list.put(music.author, author_array)
        val folder_array = ArrayList<Music>()
        folder_array.add(music)
        folder_list.put(music.folder, folder_array)
        val play_list_array = ArrayList<Music>()
        play_list_array.add(music)
        play_list.put(music.play_list, author_array)
        val content = ContentValues()
        content.put("title", music.title)
        content.put("author", music.author)
        content.put("path", music.path)
        content.put("album", music.album)
        content.put("album_id", music.album_id)
        content.put("folder", music.folder)
        content.put("play_list", music.play_list)
        content.put("music_id", music.music_id)
        content.put("img_path", music.img_path)
        db.insert("music_info", null, content)
    }


    /**

     * 在系统数据库查询中，插入非第一个元素，并在程序中记录

     */
    private fun insertInWhile(db: SQLiteDatabase, music: Music) {
        if (!album_list.containsKey(music.album)) {
            val barray = ArrayList<Music>()
            barray.add(music)
            album_list.put(music.album, barray)
        } else {
            album_list[music.album]!!.add(music)
        }
        if (!author_list.containsKey(music.author)) {
            val author_array = ArrayList<Music>()
            author_array.add(music)
            author_list.put(music.author, author_array)
        } else {
            author_list[music.author]!!.add(music)
        }
        if (!folder_list.containsKey(music.folder)) {
            val folder_array = ArrayList<Music>()
            folder_array.add(music)
            folder_list.put(music.folder, folder_array)
        } else {
            folder_list[music.folder]!!.add(music)
        }
        if (!play_list.containsKey(music.play_list)) {
            val list_array = ArrayList<Music>()
            list_array.add(music)
            play_list.put(music.play_list, list_array)
        } else {
            play_list[music.play_list]!!.add(music)
        }
        val content = ContentValues()
        content.put("title", music.title)
        content.put("author", music.author)
        content.put("path", music.path)
        content.put("album", music.album)
        content.put("album_id", music.album_id)
        content.put("folder", music.folder)
        content.put("play_list", music.play_list)
        content.put("music_id", music.music_id)
        content.put("img_path", music.img_path)
        content.put("color", music.color)
        content.put("color_darker", music.color_darker)
        db.insert("music_info", null, content)
    }


    /**
     * 从系统数据库中获取Music元素
     */
    private fun loadMusicFromSystem(cursor: Cursor): Music {
        var album: String? = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
        if (album == null || album.length == 0) {
            album = "NULL"
        }
        var author: String? = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
        if (author == null || author.length == 0) {
            author = "UNKNOWN"
        }
        var folder = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
        folder = folder.substring(0, folder.lastIndexOf("/"))
        return Music(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                author,
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                album,
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                folder,
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                "", init_color, init_color_darker)
    }

    /**
     * 调用系统数据库获取Audio(Slow)，并将获取到的数据存储到软件的数据库中
     * @param kb 过滤多少kb以下的内容(用户控制过滤尚未设计好，接下来会做)
     * *
     * @param db 写入的SQLiteDatabase
     * *
     * @return 写入数据后的array
     * *
     */

    @Throws(FileNotFoundException::class)
    fun loadFromSystem(kb: Int, db: SQLiteDatabase) {
        try {
            db.execSQL("create table album_list(_id integer primary key autoincrement, title)")
            db.execSQL("create table author_list(_id integer primary key autoincrement, author)")
            db.execSQL("create table folder_list(_id integer primary key autoincrement, folder)")
            db.execSQL("create table play_list(_id integer primary key autoincrement, play_list)")
            db.execSQL("create table music_info(_id integer primary key autoincrement, title," +
                    " author, path, album, album_id integer, folder, play_list, music_id integer," +
                    " img_path, color integer, color_darker integer)")
        } catch (e: SQLiteException) {
            db.execSQL("drop table album_list")
            db.execSQL("drop table folder_list")
            db.execSQL("drop table author_list")
            db.execSQL("drop table play_list")
            db.execSQL("drop table music_info")
            db.execSQL("create table album_list(_id integer primary key autoincrement, title)")
            db.execSQL("create table author_list(_id integer primary key autoincrement, author)")
            db.execSQL("create table folder_list(_id integer primary key autoincrement, folder)")
            db.execSQL("create table play_list(_id integer primary key autoincrement, play_list)")
            db.execSQL("create table music_info(_id integer primary key autoincrement, title, " +
                    "author, path, album, album_id integer, folder, play_list, music_id integer, " +
                    "img_path, color integer, color_darker integer)")
        }

        val cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media._ID),
                null, null, null)
        var music: Music
        if (cursor.moveToFirst()) {
            music = loadMusicFromSystem(cursor)
            if (kb != 0) {
                var `in` = File(music.path)
                if (`in`.length() >= kb.toLong() * 1024L * 8) {
                    music_list.add(music)
                    insertFirst(db, music)
                }
                while (cursor.moveToNext()) {
                    music = loadMusicFromSystem(cursor)
                    `in` = File(music.path)
                    if (`in`.length() >= kb.toLong() * 1024L * 8) {
                        music_list.add(music)
                        insertInWhile(db, music)
                    }
                }
            } else {
                music_list.add(music)
                insertFirst(db, music)
                while (cursor.moveToNext()) {
                    music = loadMusicFromSystem(cursor)
                    music_list.add(music)
                    insertInWhile(db, music)
                }
            }
        }
        cursor.close()
        val cursor2 = context.contentResolver.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media._ID),
                null, null, null)
        if (cursor2.moveToFirst()) {
            music = loadMusicFromSystem(cursor2)
            if (kb != 0) {
                var `in` = File(music.path)
                if (`in`.length() >= kb.toLong() * 1024L * 8) {
                    music_list.add(music)
                    insertInWhile(db, music)
                }
                while (cursor2.moveToNext()) {
                    music = loadMusicFromSystem(cursor2)
                    `in` = File(music.path)
                    if (`in`.length() >= kb.toLong() * 1024L * 8) {
                        music_list.add(music)
                        insertInWhile(db, music)
                    }
                }
            } else {
                music_list.add(music)
                insertInWhile(db, music)
                while (cursor2.moveToNext()) {
                    music = loadMusicFromSystem(cursor2)
                    music_list.add(music)
                    insertInWhile(db, music)
                }
            }
        }
        cursor2.close()
        for (talbum in album_list.keys) {
            val content1 = ContentValues()
            content1.put("title", talbum)
            db.insert("album_list", null, content1)
        }
        for (tauthor in author_list.keys) {
            val content1 = ContentValues()
            content1.put("author", tauthor)
            db.insert("author_list", null, content1)
        }
        for (tfolder in folder_list.keys) {
            val content1 = ContentValues()
            content1.put("folder", tfolder)
            db.insert("folder_list", null, content1)
        }
        for (tlist in play_list.keys) {
            val content1 = ContentValues()
            content1.put("play_list", tlist)
            db.insert("play_list", null, content1)
        }
    }

    /**
     * 将数据库文件内容清空
     */

    fun dropData() {
        val f = File(DB_FILE)
        if (f.exists()) {
            f.delete()
        }
        db = null
        println("File--------->" + f.exists())
    }

    fun loadList(){
        try {
            if (db == null) {
                db = SQLiteDatabase.openDatabase(DB_FILE, null, SQLiteDatabase.OPEN_READWRITE)
            }
            loadFromDatabase(db!!)
            for(item in album_list.values){
                music_list.addAll(item)
            }
        } catch (e: SQLiteCantOpenDatabaseException) {

            try {
                val f = File(DB_DIR)
                if (!f.exists()) {
                    f.mkdirs()
                    println("directory--------->" + f.isDirectory)
                    println("result--------->" + f.exists())
                }
                println("Before Load")
                db = SQLiteDatabase.openOrCreateDatabase(DB_FILE, null)
                println("Load Succeed")
                loadFromSystem(skip, db!!)
            } catch (e1: FileNotFoundException) {
                // TODO Auto-generated catch block
                e1.printStackTrace()
            }

        }
        db?.close()
        for(list in album_list.values){
            SortArrayList(list)
        }
        for(list in author_list.values){
            SortArrayList(list)
        }
        for(list in folder_list.values){
            SortArrayList(list)
        }
        for(list in play_list.values){
            SortArrayList(list)
        }
        has_loaded = true
    }

    fun findNowMusic(kb: Int):ArrayList<Music>{
        var music_list = ArrayList<Music>()
        val cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media._ID),
                null, null, null)
        var music: Music
        if (cursor.moveToFirst()) {
            music = loadMusicFromSystem(cursor)
            if (kb != 0) {
                var `in` = File(music.path)
                if (`in`.length() >= kb.toLong() * 1024L * 8) {
                    music_list.add(music)
                }
                while (cursor.moveToNext()) {
                    music = loadMusicFromSystem(cursor)
                    `in` = File(music.path)
                    if (`in`.length() >= kb.toLong() * 1024L * 8) {
                        music_list.add(music)
                    }
                }
            } else {
                music_list.add(music)
                while (cursor.moveToNext()) {
                    music = loadMusicFromSystem(cursor)
                    music_list.add(music)
                }
            }
        }
        cursor.close()
        val cursor2 = context.contentResolver.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media._ID),
                null, null, null)
        if (cursor2.moveToFirst()) {
            music = loadMusicFromSystem(cursor2)
            if (kb != 0) {
                var `in` = File(music.path)
                if (`in`.length() >= kb.toLong() * 1024L * 8) {
                    music_list.add(music)
                }
                while (cursor2.moveToNext()) {
                    music = loadMusicFromSystem(cursor2)
                    `in` = File(music.path)
                    if (`in`.length() >= kb.toLong() * 1024L * 8) {
                        music_list.add(music)
                    }
                }
            } else {
                music_list.add(music)
                while (cursor2.moveToNext()) {
                    music = loadMusicFromSystem(cursor2)
                    music_list.add(music)
                }
            }
        }
        cursor2.close()
        return music_list
    }

    override fun GetAlbumData(): HashMap<String, ArrayList<Music>> {
        if(!has_loaded) loadList()
        return album_list
    }

    override fun GetAuthorData(): HashMap<String, ArrayList<Music>> {
        if(!has_loaded) loadList()
        return author_list
    }

    override fun GetFolderData(): HashMap<String, ArrayList<Music>> {
        if(!has_loaded) loadList()
        return folder_list
    }

    override fun GetListData(): HashMap<String, ArrayList<Music>> {
        if(!has_loaded) loadList()
        return play_list
    }

    override fun ScanForChange(): HashMap<Boolean, Music> {
        val hashmap = HashMap<Boolean, Music>()
        if(!scan_system){
            async() {
                var to = findNowMusic(skip)
                uiThread {
                    var task = DifferAsyncTask(music_list, to)
                    task.execute()
                }
            }
        }
        return hashmap
    }

    fun SortArrayList(list: ArrayList<Music>){
        list.sortBy{
            it.music_id
        }
    }

    override fun DropData() {
        has_loaded = false
        album_list.clear()
        author_list.clear()
        folder_list.clear()
        play_list.clear()
        if(db != null){
            var db_ = db!!
            if(db_.isOpen){
                db_.close()
            }
        }
        db = null
        var file = File(DB_FILE)
        if(file.exists()){
            file.delete()
        }
    }

    override fun AddToPlayList(music: Music): Boolean {
        throw UnsupportedOperationException()
    }

    override fun RemoveFromPlayList(music: Music, list_name: String): Boolean {
        throw UnsupportedOperationException()
    }

    class DifferAsyncTask(val from: ArrayList<Music>, val to: ArrayList<Music>) : AsyncTask<Unit, Unit, HashMap<Boolean, Music>>(){
        var differ = HashMap<Boolean, Music>()
        override fun onPostExecute(result: HashMap<Boolean, Music>?) {
            super.onPostExecute(result)
            MainActivity.getInstance().toast("扫描数据库结束╭(′▽`)╭(′▽`)╯")
        }

        override fun onPreExecute() {
            super.onPreExecute()
            MainActivity.getInstance().toast("扫描数据库开始o(一︿一+)o")
        }

        override fun doInBackground(vararg p0: Unit?): HashMap<Boolean, Music>? {
            var list = Array(from.size, {x -> false})
            forout@for(item in to) {
                for(i in 0..from.size - 1){
                    if(item.equals(from[i])){
                        list[i] = true
                        continue@forout
                    }
                }
                differ.put(true, item)
            }
            for(i in 0..from.size - 1){
                if(!list[i]){
                    differ.put(false, from[i])
                }
            }
            return differ
        }
    }

    override fun ResolveDataChange(musicList: HashMap<Boolean, Music>) {
        if (musicList.size == 0){
            return
        }
        var db = SQLiteDatabase.openDatabase(DB_FILE, null, SQLiteDatabase.OPEN_READWRITE)
        val deleteState = db.compileStatement("delete from music_info where music_id = ?")
        val insertState = db.compileStatement("insert into music_info(title, author, path, album, " +
                "album_id, folder, play_list, music_id)  values(?,?,?,?,?,?,?,?)")
        musicList.map{
            pair -> {
            var music = pair.value
            if(pair.key){
                insertState.bindString(1, music.title)
                insertState.bindString(2, music.author)
                insertState.bindString(3, music.path)
                insertState.bindString(4, music.album)
                insertState.bindLong(5, music.album_id)
                insertState.bindString(6, music.folder)
                insertState.bindString(7, music.play_list)
                insertState.bindLong(8, music.music_id)
                insertState.executeInsert()
                1
            }
            else{
                deleteState.bindLong(1, music.music_id)
                deleteState.executeUpdateDelete()
                2
            }
        }
        }
    }

    override fun UpdateAlbumPath(music: Music): Boolean{
        var flag = false
        try{
            if (db == null || ! db!!.isOpen) {
                db = SQLiteDatabase.openDatabase(DB_FILE, null, SQLiteDatabase.OPEN_READWRITE)
            }
            db!!.execSQL("update music_info set img_path = ?, color = ?, color_darker = ? " +
                    "where music_id = ?",
                    arrayOf(music.img_path, music.color, music.color_darker, music.music_id))
            flag = true
        }
        catch(e : Exception){
        }
        finally{
            if (db != null) {
                if(db!!.isOpen){
                    db!!.close()
                }
                db = null
            }
            return flag
        }
    }
}
