package com.example.textrecognitionapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper( Context context) {
        super(context,"Userdata.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE SlipRecord(id INTEGER primary key autoincrement,datetime DATETIME,result TEXT,lot INTEGER,inst TEXT, test INTEGER,operator TEXT,image BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE if exists SlipRecord");
    }

    public Boolean insertSlipResultToDatabase(String datetime, String result, String lot, String inst, String test, String operator, byte[] image){

        SQLiteDatabase db = this.getWritableDatabase();

        if(checkDataExist(datetime)){
            return false;
        }
        else{
            ContentValues contentValues = new ContentValues();

            contentValues.put("datetime",datetime);
            contentValues.put("result",result);
            contentValues.put("lot",lot);
            contentValues.put("inst",inst);
            contentValues.put("test",test);
            contentValues.put("operator",operator);
            contentValues.put("image", image);

            long insertResult = db.insert("SlipRecord",null,contentValues);

            return insertResult != -1;
        }
    }

    public Boolean checkDataExist(String datetime){
        SQLiteDatabase db = this.getWritableDatabase();

        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT * FROM SlipRecord WHERE datetime= ?",new String[]{datetime});

        return cursor.getCount() > 0;
    }
    public Cursor getDataFromDatabase(){

        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM SlipRecord",null);
    }

    public List<String> getAllYearForTitle(){
        SQLiteDatabase db = this.getWritableDatabase();

        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT datetime FROM SlipRecord",null);

        List<String> titleArray = new ArrayList<>();

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            String textlist = cursor.getString(0);

            String[] date = textlist.split(" ");
            String[] year = date[0].split("/");
            textlist = year[2];

            if(!titleArray.contains(textlist)){
                titleArray.add(textlist);
            }
        }
        titleArray.sort(Collections.reverseOrder());

        return titleArray;
    }

    //find image from database based on datetime
    public byte[] getImageFromDatabase(String datetime){
        SQLiteDatabase db = this.getWritableDatabase();

        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT image FROM SlipRecord WHERE datetime=?",new String[]{datetime});

        if(cursor.getCount()>0){
            cursor.moveToFirst();
            return cursor.getBlob(0);
        }
        else{
            return null;
        }
    }
}
