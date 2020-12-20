package com.example.psychic;

public class User {
    private String id;
    private String name;
    private String username;
    private String imgURL;
    public User(){}
    public User(String id, String name, String username, String imgURL) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.imgURL = imgURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }


}
