package com.example.textrecognitionapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class PopOutWindow extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        DBHelper db = new DBHelper(this);

        // choose the layout to display
        setContentView(R.layout.image_overlay);

        // get each view from layout
        ConstraintLayout constrainLayout = findViewById(R.id.constrainLayout);
        ImageView imageView = findViewById(R.id.imageView);

        // get data pass from previous activity
        String image_datetime = getIntent().getStringExtra("DISPLAY_IMAGE");

        //convert byte array to bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(db.getImageFromDatabase(image_datetime), 0, db.getImageFromDatabase(image_datetime).length);

        // set bitmap to imageView
        imageView.setImageBitmap(bitmap);

        //go back to previous activity
        constrainLayout.setOnClickListener(v -> finish());
    }
}
