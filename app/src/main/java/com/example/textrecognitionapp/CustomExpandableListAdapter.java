package com.example.textrecognitionapp;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import java.util.List;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private final Activity context;
    private final List<String> expandableListTitle;
    private final List<ExpandableListGroupWrapper> expandableListDetail;

    /**
     * Initialise CustomExpandableListAdapter
     *
     * @param context Activity
     * @param expandableListTitle List<String>
     * @param expandableListDetail List<ExpandableListGroupWrapper>
     */
    public CustomExpandableListAdapter(Activity context, List<String> expandableListTitle,
                                       List<ExpandableListGroupWrapper> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;

    }

    /**
     * Get the child item of expandableListDetail
     *
     * @param listPosition int
     * @param expandedListPosition int
     * @return Object
     */
    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(listPosition).getList().get(expandedListPosition);
    }

    /**
     * Get child item id
     *
     * @param listPosition int
     * @param expandedListPosition int
     * @return long
     */
    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    /**
     * Create the child item view
     *
     * @param listPosition int
     * @param expandedListPosition int
     * @param isLastChild boolean
     * @param convertView View
     * @param parent ViewGroup
     * @return View
     */
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        ExpandableListDetailWrapper expandedListText = (ExpandableListDetailWrapper) getChild(listPosition, expandedListPosition);


        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }
        ImageView expandedListItemImage = (ImageView) convertView.findViewById(R.id.expandedListItemImage);
        TextView expandedListItemDate = (TextView) convertView.findViewById(R.id.expandedListItemDate);
        TextView expandedListItemTime = (TextView) convertView.findViewById(R.id.expandedListItemTime);
        TextView expandedListItemA1C = (TextView) convertView.findViewById(R.id.expandedListItemA1C);

        expandedListItemImage.setImageBitmap(expandedListText.getBitmap());
        expandedListItemDate.setText(expandedListText.getMonthAndDate());
        expandedListItemTime.setText(expandedListText.getTime());
        expandedListItemA1C.setText(expandedListText.getResult());

        convertView.setOnClickListener(view -> {
            Intent intent = new Intent(context,PopOutWindow.class);
            intent.putExtra("DISPLAY_IMAGE", expandedListText.getDatetime());
            context.startActivity(intent);
        });

        return convertView;
    }

    /**
     * Get the number of children of expandableListDetail
     *
     * @param listPosition int
     * @return int
     */
    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(listPosition).getList().size();
    }

    /**
     * Get all the groups in expandableListTitle
     *
     * @param listPosition int
     * @return Object
     */
    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    /**
     * Get the number of groups in expandableListTitle
     *
     * @return int
     */
    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    /**
     * Get the group id
     *
     * @param listPosition int
     * @return long
     */
    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    /**
     * Create the group view
     *
     * @param listPosition int
     * @param isExpanded boolean
     * @param convertView View
     * @param parent ViewGroup
     * @return View
     */
    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    /**
     * Set hasStableIds to falase
     *
     * @return boolean
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Set isChildSelectable to true
     *
     * @param listPosition int
     * @param expandedListPosition int
     * @return boolean
     */
    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
