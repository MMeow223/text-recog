package com.example.textrecognitionapp;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.textrecognitionapp.expandablelist.CustomExpandableListAdapter;
import com.example.textrecognitionapp.expandablelist.ExpandableListDetailWrapper;
import com.example.textrecognitionapp.expandablelist.ExpandableListGroupWrapper;

import java.util.ArrayList;
import java.util.List;

public class HistoryRecordActivity  extends AppCompatActivity {

    private DBHelper db;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    List<ExpandableListGroupWrapper> expandableListDetail;


    /**
     * Initialise the activity
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBHelper(this);

        // set the layout
        setContentView(R.layout.activity_record_history);
        
        // get the view from layout
        this.expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        ImageButton backBtn = (ImageButton) findViewById(R.id.backButton);
        
        //get the data from database
        this.expandableListDetail = getDataFromDatabase();
        this.expandableListTitle = db.getAllYearForTitle();
        this.expandableListAdapter = new CustomExpandableListAdapter(this, this.expandableListTitle, this.expandableListDetail);
        this.expandableListView.setAdapter(this.expandableListAdapter);

        // set the back button
        backBtn.setOnClickListener(v -> finish());
    }

    /**
     * Get data from database
     *
     * @return List<ExpandableListGroupWrapper>
     */
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
    
