package com.myshirt.eg.adapter;

import java.util.List;

public class StateLists {
    String countryCode;
    List<CountryStates> states;

//    public StateLists() {}
    public StateLists(String countryCode, List<CountryStates> states) {
        this.countryCode = countryCode;
        this.states = states;
    }

    public String getCountryCode() {
        return countryCode;
    }

//    public void setCountryCode(String countryCode) {
//        this.countryCode = countryCode;
//    }

    public List<CountryStates> getStates() {
        return states;
    }

//    public void setStates(List<CountryStates> states) {
//        this.states = states;
//    }
}
