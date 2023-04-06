package com.example.inclass08_09.model;

import java.io.Serializable;

public class Friend implements Serializable {
    private String name;
    private String email;

    private String imageURL;

    public Friend(String name, String email, String imageURL) {
        this.name = name;
        this.email = email;
        this.imageURL = imageURL;
    }

    public Friend() {
    }

    @Override
    public String toString() {
        return "Friend{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", imageURL='" + imageURL + '\'' +
                '}';
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Friend(String name, String email) {
        this.name = name;
        this.email = email;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}