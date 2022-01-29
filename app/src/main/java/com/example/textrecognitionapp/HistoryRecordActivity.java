package com.example.textrecognitionapp;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.sax.SAXResult;

public class HistoryRecordActivity  extends AppCompatActivity {

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<List<String>>> expandableListDetail;
    private ImageButton backBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_history);
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        backBtn = (ImageButton) findViewById(R.id.backButton);
        expandableListDetail = ExpandableListDataPump.getData();
        System.out.println(expandableListDetail);
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);


        backBtn.setOnClickListener(v -> {
            finish();
        });


        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        expandableListTitle.get(groupPosition) + " List Expanded.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        expandableListTitle.get(groupPosition) + " List Collapsed.",
                        Toast.LENGTH_SHORT).show();
            }

        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                String selected = expandableListAdapter.getChild(groupPosition,childPosition).toString();
                Toast.makeText(
                        getApplicationContext(),
                        selected+" is clicked"
                        , Toast.LENGTH_SHORT
                ).show();
                return false;
            }
        });






//        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
//            @Override
//            public boolean onChildClick() {
//
//            }
//            Toast.makeText(
//                    getApplicationContext(),
//                    expandableListTitle.get(groupPosition)
//                            + " -> "
//                            + expandableListDetail.get(
//                            expandableListTitle.get(groupPosition)).get(
//                            childPosition), Toast.LENGTH_SHORT
//            ).show();
//            return false;
//        });
    }
    // Function to show the progress spinner view
}

