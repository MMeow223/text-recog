package com.example.textrecognitionapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class PopOutWindow extends Activity {

        private ConstraintLayout constrainLayout;
        private ImageView imageView;
        private DBHelper db;

        @Override
        protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        db = new DBHelper(this);
        setContentView(R.layout.image_overlay);

        constrainLayout = findViewById(R.id.constrainLayout);
        imageView = findViewById(R.id.imageView);

        String data = getIntent().getStringExtra("DISPLAY_IMAGE");


        //convert byte array to bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(db.getImageFromDatabase(data), 0, db.getImageFromDatabase(data).length);

        imageView.setImageBitmap(bitmap);





        System.out.println(data);
//        Bundle extras = getIntent().getExtras();
//        byte[] byteArray = extras.getByteArray("DISPLAY_IMAGE");
//
//        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//        imageView.setImageBitmap(bmp);

        constrainLayout.setOnClickListener(v -> {
            finish();
        });
    }
}
