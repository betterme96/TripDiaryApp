package com.wang.tripdiaryapp.bean;

import java.io.Serializable;

/**
 * Created by WANG on 2018/5/16.
 */

public class Credit implements Serializable {

    private int id;//评论ID
    private String createTime;//创建时间
    private int diary_id;//评论的日记id
    private String content;//评论内容
    private String author;//评论作者

    private String bgColor;//背景颜色，存储颜色代码


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDiary_id(int diary_id){
        this.diary_id = diary_id;
    }
    public int getDiary_id(){
        return diary_id;
    }
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }



    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }


    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }


}

