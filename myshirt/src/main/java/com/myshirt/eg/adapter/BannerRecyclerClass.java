package com.myshirt.eg.adapter;

public class BannerRecyclerClass {

    private boolean isResource;
    private int featured_image_resource;
    private String featured_image_string;
    private String slideTitle;
    private String description;
    private String on_click_to;
    private String category;
    private String url;

    public BannerRecyclerClass(boolean isResource, int imageResource, String imageString , String slideTitle, String description, String on_click_to, String category, String url) {
        this.isResource = isResource;
        this.featured_image_resource = imageResource;
        this.featured_image_string = imageString;
        this.slideTitle = slideTitle;
        this.description = description;
        this.on_click_to = on_click_to;
        this.category = category;
        this.url = url;
    }

    public int getFeatured_image_resource() {
        return featured_image_resource;
    }
    public String getFeatured_image_string() {
        return featured_image_string;
    }
    public String getTitle() {
        return slideTitle;
    }
    public String getDescription() {
        return description;
    }
    public String getOn_click_to() {
        return on_click_to;
    }
    public String getCategory() {
        return category;
    }
    public String getUrl() {
        return url;
    }
    public boolean isResource() {
        return  isResource;
    }

//    public String getThe_caption_Title() {
//        return name;
//    }
//
//    public void setFeatured_image(int featured_image) {
//        this.featured_image = featured_image;
//    }
//
//    public void setThe_caption_Title(String the_caption_Title) {
//        this.name = the_caption_Title;
//    }
}