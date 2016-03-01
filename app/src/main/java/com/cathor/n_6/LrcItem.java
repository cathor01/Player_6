package com.cathor.n_6;

import java.io.Serializable;

/**
 * Created by Cathor on 2015/9/18.
 */
public class LrcItem implements Serializable {
    private String title; //标题
    private String author; //作者
    private String link; //歌词链接
    private Resource resource;


    public String getTitle(){
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getLink() {
        return link;
    }

    public Resource getResource(){ return resource;}

    LrcItem(String title, String author, String link, Resource resource){
        this.author = author;
        this.title = title;
        this.link = link;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "[" + author + "-" + title + ":" + link + "]";
    }


}

enum Resource{
    Net,
    XIAMI
}