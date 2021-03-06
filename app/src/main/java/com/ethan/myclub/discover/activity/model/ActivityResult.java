package com.ethan.myclub.discover.activity.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ActivityResult implements MultiItemEntity, Serializable {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("is_special")
    @Expose
    public Boolean isSpecial;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("brief_introduction")
    @Expose
    public String briefIntroduction;
    @SerializedName("home_page_img")
    @Expose
    public String homePageImg;
    @SerializedName("comment_num")
    @Expose
    public String commentNum;
    @SerializedName("like_num")
    @Expose
    public Integer likeNum;
    @SerializedName("publish_time")
    @Expose
    public String publishTime;
    @SerializedName("special_index_image")
    @Expose
    public String specialIndexImage;

    @Override
    public int getItemType() {
        return isSpecial ? 1 : 0;
    }
}