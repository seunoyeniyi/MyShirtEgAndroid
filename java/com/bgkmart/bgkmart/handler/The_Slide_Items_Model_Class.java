package com.fatima.fabric.handler;

public class The_Slide_Items_Model_Class {

    private int featured_image;
    private String name;

    public The_Slide_Items_Model_Class(int hero, String name) {
        this.featured_image = hero;
        this.name = name;
    }

    public int getFeatured_image() {
        return featured_image;
    }

    public String getThe_caption_Title() {
        return name;
    }

    public void setFeatured_image(int featured_image) {
        this.featured_image = featured_image;
    }

    public void setThe_caption_Title(String the_caption_Title) {
        this.name = the_caption_Title;
    }
}