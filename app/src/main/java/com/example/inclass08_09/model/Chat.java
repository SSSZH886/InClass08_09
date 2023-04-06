package com.example.inclass08_09.model;

public class Chat {
    private String sender;
    private String receiver;
    private String message;

    @Override
    public String toString() {
        return "Chat{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", message='" + message + '\'' +
                ", imageURL='" + imageURL + '\'' +
                '}';
    }

    public Chat(String sender, String receiver, String message, String imageURL) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.imageURL = imageURL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    private String imageURL;

    public Chat() {
    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Chat(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }
}
