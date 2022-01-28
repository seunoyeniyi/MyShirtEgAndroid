package com.fatima.fabric.adapter;

public class CategoriesList {
    String id;
    String name;
    String link;
    String count;
    String sub_cats;

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    String slug;

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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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
}
