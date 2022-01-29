package com.example.textrecognitionapp;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;
import java.util.List;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableListTitle;
    private HashMap<String, List<List<String>>> expandableListDetail;

    public CustomExpandableListAdapter(Context context, List<String> expandableListTitle,
                                       HashMap<String, List<List<String>>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }
//    public void showProgressView() {
//        LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        ViewGroup progressView = (ViewGroup) layoutInflater.inflate(R.layout.progressbar_layout, null);
//        Activity convertView = null;
//        View v = convertView.findViewById(android.R.id.content).getRootView();
//        ViewGroup viewGroup = (ViewGroup) v;
//        viewGroup.addView(progressView);
//    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        List<String> expandedListText = (List<String>) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }
        ImageView expandedListItemImage = (ImageView) convertView
                .findViewById(R.id.expandedListItemImage);
        TextView expandedListItemDate = (TextView) convertView
                .findViewById(R.id.expandedListItemDate);
        TextView expandedListItemTime = (TextView) convertView
                .findViewById(R.id.expandedListItemTime);
        TextView expandedListItemA1C = (TextView) convertView
                .findViewById(R.id.expandedListItemA1C);


        //TODO need to change the design first(create those item with their unique ids)

        expandedListItemDate.setText(expandedListText.get(1));
        expandedListItemTime.setText(expandedListText.get(2));
        expandedListItemA1C.setText(expandedListText.get(3));

        convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // Here suppose to be open an overlay of the image
                Toast.makeText(
                        context.getApplicationContext(),
                        "Clicked : " + expandedListItemDate.getText() + " " + expandedListItemA1C.getText()
                        , Toast.LENGTH_SHORT
                ).show();
//                showProgressView();
            }
        });

        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }

}
