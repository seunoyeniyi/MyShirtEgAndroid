package com.myshirt.eg.adapter;

public class TagsList {

    String name;
    String slug;

    public TagsList(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
    public TagsList() {
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }
    //
    public void setName(String name) {
        this.name = name;
    }
}
