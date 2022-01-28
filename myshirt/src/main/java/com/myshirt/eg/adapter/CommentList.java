package com.myshirt.eg.adapter;

public class CommentList {
    String username, comment, rating, user_image;

    public CommentList(String username, String comment, String rating, String user_image) {
        this.username = username;
        this.comment = comment;
        this.rating = rating;
        this.user_image = user_image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }
    public String getUser_image() {
        return user_image;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
    public void setUser_image(String image) {
        this.user_image = image;
    }
}
