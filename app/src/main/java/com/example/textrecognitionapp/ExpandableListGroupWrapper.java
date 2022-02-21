package com.example.textrecognitionapp;

import java.util.List;

public class ExpandableListGroupWrapper {
    private final String year;
    private final List<ExpandableListDetailWrapper> list;

    /**
     * Constructor of ExpandableListGroupWrapper
     *
     * @param year String
     * @param list List<ExpandableListDetailWrapper>
     */
    public ExpandableListGroupWrapper(String year, List<ExpandableListDetailWrapper> list){
        this.year = year;
        this.list = list;
    }

    /**
     * Get year
     *
     * @return String
     */
    public String getYear(){
        return this.year;
    }

    /**
     * Get the list of "Expandable list detail wrapper"
     *
     * @return List<ExpandableListDetailWrapper>
     */
    public List<ExpandableListDetailWrapper> getList(){
        return this.list;
    }

    /**
     * Insert "Expandable list detail wrapper" into the list
     * @param expandableListDetailWrapper
     */
    public void addExpandableListDetailWrapper(ExpandableListDetailWrapper expandableListDetailWrapper){
        this.list.add(expandableListDetailWrapper);
    }
}
