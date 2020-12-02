package com.ashish.videoprocessingapp.Model;

public class VideoModel {
    String title;
    String url;
    String userid;
    String username;



    public VideoModel() {
    }

    public VideoModel(String title, String url, String userid, String username) {
        this.title = title;
        this.url = url;
        this.userid = userid;
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
