package com.cathor.n_6

import java.util.*

/**
 * Created by Cathor on 2016/2/17.
 */

interface DataManager {
    fun GetAlbumData() : HashMap<String, ArrayList<Music>>
    fun GetAuthorData() : HashMap<String, ArrayList<Music>>
    fun GetFolderData() : HashMap<String, ArrayList<Music>>
    fun GetListData() : HashMap<String, ArrayList<Music>>
    fun ScanForChange() : HashMap<Boolean, Music>
    fun DropData(): Unit
    fun AddToPlayList(music:Music):Boolean
    fun RemoveFromPlayList(music:Music, list_name:String):Boolean
    fun ResolveDataChange(musicList: HashMap<Boolean, Music>)
    fun UpdateAlbumPath(music: Music): Boolean
}