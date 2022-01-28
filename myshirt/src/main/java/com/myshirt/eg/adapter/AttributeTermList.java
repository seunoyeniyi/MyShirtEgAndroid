package com.myshirt.eg.adapter;

public class AttributeTermList {
    String name, taxonomy, slug;

    public AttributeTermList(String name, String taxonomy, String slug) {
        this.name = name;
        this.taxonomy = taxonomy;
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxonomy() {
        return taxonomy;
    }

    public void setTaxonomy(String taxonomy) {
        this.taxonomy = taxonomy;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
