package com.myshirt.eg.adapter;

import org.json.JSONArray;

public class AttributesList {

    String name;
    JSONArray options;
    String label;

//    public void setId(String id) {
//        this.id = id;
//    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONArray getOptions() {
        return options;
    }

    public void setOptions(JSONArray options) {
        this.options = options;
    }

//    public void setPosition(String position) {
//        this.position = position;
//    }

//    public void setVisible(String visible) {
//        this.visible = visible;
//    }

//    public void setVariation(String variation) {
//        this.variation = variation;
//    }

    public void setIs_variation() {
    }

    public void setIs_taxonomy() {
    }

    public void setValue() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
