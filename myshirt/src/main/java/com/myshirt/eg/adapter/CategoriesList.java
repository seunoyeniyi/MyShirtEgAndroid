package com.myshirt.eg.adapter;

public class CategoriesList {

    String name;
    String count;
    String sub_cats;
    String image;
    String icon;
    String slug;

    public CategoriesList(String name, String slug, String count, String sub_cats, String image, String icon) {
        this.name = name;
        this.count = count;
        this.sub_cats = sub_cats;
        this.slug = slug;
        this.image = image;
        this.icon = icon;
    }
    public CategoriesList() {
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

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getSub_cats() {
        return sub_cats;
    }

    public void setSub_cats(String sub_cats) {
        this.sub_cats = sub_cats;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String getImage() {
        return image;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
