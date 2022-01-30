package com.example.textrecognitionapp;

import java.util.List;

public class ExpandableListGroupWrapper {
     //create a variable
    private final String year;
    private final List<ExpandableListDetailWrapper> list;
    //create a constructor
    public ExpandableListGroupWrapper(String year, List<ExpandableListDetailWrapper> list){
        this.year = year;
        this.list = list;
    }
    //getter
    public String getYear(){
        return this.year;
    }
    public List<ExpandableListDetailWrapper> getList(){
        return this.list;
    }

    //insert expandablelistdetailwrapper to list
    public void addExpandableListDetailWrapper(ExpandableListDetailWrapper expandableListDetailWrapper){
        this.list.add(expandableListDetailWrapper);
    }
}
