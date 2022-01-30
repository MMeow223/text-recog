package com.example.textrecognitionapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ExpandableListDetailWrapper {
    private final byte[] image;
    private final String datetime;
    private final String result;
    //
    public ExpandableListDetailWrapper(byte[] image, String datetime, String result){
        this.image = image;
        this.datetime = datetime;
        this.result = result;
    }

    // Getter methods
   public String getDatetime(){
        return this.datetime;
   }
   public String getResult(){
        return this.result;
   }

   //get date from datetime
    public String getDate(){
          String[] date = this.datetime.split(" ");
          return date[0];
    }

    //get time from datetime
    public String getTime(){
        String[] time = this.datetime.split(" ");
        return time[1];
    }

    //convert byte[] to bitmap
    public Bitmap getBitmap(){
        return BitmapFactory.decodeByteArray(this.image, 0, this.image.length);
    }

    //convert month of date into english month
    public String getMonthAndDate(){
        String date = getDate();
        String[] month = date.split("/");
        String month_eng = "";
        switch(month[1]){
            case "01":
                month_eng = "Jan";
                break;
            case "02":
                month_eng = "Feb";
                break;
            case "03":
                month_eng = "Mar";
                break;
            case "04":
                month_eng = "Apr";
                break;
            case "05":
                month_eng = "May";
                break;
            case "06":
                month_eng = "Jun";
                break;
            case "07":
                month_eng = "Jul";
                break;
            case "08":
                month_eng = "Aug";
                break;
            case "09":
                month_eng = "Sep";
                break;
            case "10":
                month_eng = "Oct";
                break;
            case "11":
                month_eng = "Nov";
                break;
            case "12":
                month_eng = "Dec";
                break;
        }
        return month_eng + month[0];
    }
}
