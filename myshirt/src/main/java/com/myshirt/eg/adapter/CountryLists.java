package com.myshirt.eg.adapter;

public class CountryLists {
    public String getCode() {
        return code;
    }

// --Commented out by Inspection START (6/28/2021 5:38 PM):
//    public CountryLists() {
//    }
// --Commented out by Inspection START (6/28/2021 5:38 PM):
//// --Commented out by Inspection STOP (6/28/2021 5:38 PM)
//
    public CountryLists(String code, String name) {
        this.code = code;
        this.name = name;
    }

// --Commented out by Inspection START (6/28/2021 6:05 PM):
//    public void setCode(String code) {
//        this.code = code;
//    }
// --Commented out by Inspection STOP (6/28/2021 6:05 PM)

    public String getName() {
        return name;
    }

// --Commented out by Inspection START (6/28/2021 6:05 PM):
//    public void setName(String name) {
//        this.name = name;
//    }
// --Commented out by Inspection STOP (6/28/2021 6:05 PM)

    String code;
    String name;
}
