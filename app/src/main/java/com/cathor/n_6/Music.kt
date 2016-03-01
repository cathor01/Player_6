package com.cathor.n_6

import java.io.Serializable

/**
* Created by Cathor on 2016/2/17 21:29.
*/
class Music : Comparable<Music>, Serializable {
    override fun compareTo(other: Music): Int {
        throw UnsupportedOperationException()
    }

    override fun equals(other: Any?): Boolean {
        if(other is Music){
            return path.equals(other.path)
        }
        return false
    }

    var folder: String
    var title: String
    var author: String
    var path: String
    var album: String
    var play_list: String
    var album_id: Long = -1
    var music_id: Long = -1
    var img_path: String = ""
    var color: Long
    var color_darker: Long = 0xff4f2DAA

    constructor(title: String, author: String, path: String, album: String, album_id: Long,
                folder: String, music_id: Long, img_path: String, color: Long, color_darker: Long) {
        this.title = title
        this.author = author
        this.path = path
        this.album = album
        this.album_id = album_id
        this.folder = folder
        this.play_list = "null"
        this.music_id = music_id
        this.img_path = img_path
        this.color = color
        this.color_darker = color_darker
    }

    constructor(title: String, author: String, path: String, album: String, album_id: Long,
                folder: String, play_list: String, music_id: Long, img_path: String,
                color: Long, color_darker: Long) {
        this.title = title
        this.author = author
        this.path = path
        this.album = album
        this.album_id = album_id
        this.folder = folder
        this.play_list = play_list
        this.music_id = music_id
        this.img_path = img_path
        this.color = color
        this.color_darker = color_darker
    }

    override fun hashCode(): Int {
        return this.album.hashCode()+this.path.hashCode()+this.title.hashCode()+this.music_id.hashCode()+10086
    }


}