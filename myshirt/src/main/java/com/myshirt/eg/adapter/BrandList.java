package com.myshirt.eg.adapter;

public class BrandList {
    String name;
    String slug;
    String count;

    public BrandList(String name, String slug, String count) {
        this.name = name;
        this.slug = slug;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }
    public String getCount() {
        return count;
    }
}
