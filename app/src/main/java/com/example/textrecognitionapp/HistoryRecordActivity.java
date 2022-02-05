package com.example.textrecognitionapp;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HistoryRecordActivity  extends AppCompatActivity {

    private DBHelper db;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    List<ExpandableListGroupWrapper> expandableListDetail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBHelper(this);
        // set the layout
        setContentView(R.layout.record_history);
        
        // get the view from layout
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        ImageButton backBtn = (ImageButton) findViewById(R.id.backButton);
        
        //get the data from database
        expandableListDetail = getDataFromDatabase();
        expandableListTitle = db.getAllYearForTitle();
        expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);

        // set the back button
        backBtn.setOnClickListener(v -> finish());
    }

    // get the data from database
    public List<ExpandableListGroupWrapper> getDataFromDatabase() {
        List<ExpandableListGroupWrapper> expandableListDetail = new ArrayList<>();
        Cursor cursor =  db.getDataFromDatabase();

        List<String> everyYears = db.getAllYearForTitle();

        //initially create a expandableListGroupWrapper for each year with empty list 
        for (String year : everyYears) {
            expandableListDetail.add(new ExpandableListGroupWrapper(year, new ArrayList<>()));
        }

        //sort expandableListDetail by year by descending order
        expandableListDetail.sort((o1, o2) -> o2.getYear().compareTo(o1.getYear()));

        //add all courses to the expandableListGroupWrapper of the corresponding year
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String datetime = cursor.getString(cursor.getColumnIndex("datetime"));
            @SuppressLint("Range") String result = cursor.getString(cursor.getColumnIndex("result"));
            @SuppressLint("Range") byte[] image = cursor.getBlob(cursor.getColumnIndex("image"));

            //insert the course to the expandableListGroupWrapper of the corresponding year
            for (ExpandableListGroupWrapper expandableListGroupWrapper : expandableListDetail) {
                if (datetime.contains(expandableListGroupWrapper.getYear())) {
                    expandableListGroupWrapper.addExpandableListDetailWrapper(new ExpandableListDetailWrapper(image,datetime,result));
                }
            }
        }

        //sort expandableListDetail by datetime by descending order
        for (ExpandableListGroupWrapper expandableListGroupWrapper : expandableListDetail) {
            expandableListGroupWrapper.getList().sort((o1, o2) -> o2.getDatetime().compareTo(o1.getDatetime()));
        }
        return expandableListDetail;
    }
}
    
