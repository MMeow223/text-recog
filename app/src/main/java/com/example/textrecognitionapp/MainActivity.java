package com.example.textrecognitionapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    // Variable declaration
    private Button captureImageBtn,submitBtn;
    private ImageButton historyRecordBtn;
    private ImageView capturedImage;
    private Bitmap imageBitmap;
    private Bitmap imageBitmapForProcess;
    private ViewGroup progressView;
    private String bashNumber;
    private int rotation = 0;
    private ArrayList<String> words = new ArrayList<>();
    private final String[] unwantedWords = {"Time", "Time:", "Date", "Date:", "Result", "Result:", "DCCT", "IFCC", "Lot", "Lot:", "Inst ID", "Inst ID ","Inst ID:", "Test ID", "Test ID:", "Operator", "Operator:"};

    private final ArrayList<TextInputLayout> textInputLayouts = new ArrayList<>();
    private TextInputLayout dateTimeView, dateView, resultView1, resultView2, lotView, instIdView, testIdView, operatorView;

    private String mCurrentPhotoPath;
    private boolean isProgressShown = false;
    private boolean submitVerifyPass = false;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.custom_dark_blue));
        }
        // Hides progress bar by default
        hideProgressView();

        // Linking variables to XML
        capturedImage = findViewById(R.id.capturedImage);
        captureImageBtn = findViewById(R.id.captureImageBtn);
        imageBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.placeholder);
        capturedImage.setImageBitmap(imageBitmap);
        dateTimeView = findViewById(R.id.dateTimeView);
//        dateView = findViewById(R.id.dateView);
        resultView1 = findViewById(R.id.resultView1);
//        resultView2 = findViewById(R.id.resultView2);
        lotView = findViewById(R.id.lotView);
        instIdView = findViewById(R.id.instIdView);
        testIdView = findViewById(R.id.testIdView);
        operatorView = findViewById(R.id.operatorView);
        submitBtn = findViewById(R.id.submitBtn);
        historyRecordBtn = findViewById(R.id.historyRecordButton);

        // Adding text input layout views into array
        textInputLayouts.add(dateTimeView);
//        textInputLayouts.add(dateView);
        textInputLayouts.add(resultView1);
//        textInputLayouts.add(resultView2);
        textInputLayouts.add(lotView);
        textInputLayouts.add(instIdView);
        textInputLayouts.add(testIdView);
        textInputLayouts.add(operatorView);

        // Calls camera activity function when 'Take Picture' button is pressed
        captureImageBtn.setOnClickListener(v -> {
            submitVerifyPass = false;
            rotation = 0;
            dispatchTakePictureIntent();
        });

        submitBtn.setOnClickListener(v -> {
            submitValidation();
        });

        historyRecordBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HistoryRecordActivity.class));
        });
    }

    // 'dispatchTakePictureIntent()', 'createImageFile()', 'galleryAddPic()', 'setPic()', and 'onActivityResult()' taken from https://developer.android.com/training/camera/photobasics
    // Function for launching camera activity to capture image
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(getApplicationContext(), "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();

        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
        String imageFileName = "HbA1c_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        System.out.print("Pa");
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = capturedImage.getWidth();
        int targetH = capturedImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        capturedImage.setImageBitmap(imageBitmap);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // When previous activity was from 'dispatchTakePictureIntent' function
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // This try catch is to prevent the error happen when "rotate screen after taking picture"
            try{
                galleryAddPic();
                setPic();
            }catch(NullPointerException e){
                Toast.makeText(getApplicationContext(), "Error: Try not to rotate your phone and take a picture again.", Toast.LENGTH_LONG).show();
            }
            // Calls function to utilise Firebase Vision ML Kit text recognition to extract text from image
            imageBitmapForProcess = imageBitmap.copy(imageBitmap.getConfig(), true);
            extractTextFromImage();
        }
    }


    // 'extractTextFromImage()', & 'getImageText()' taken from https://www.youtube.com/watch?v=fmTlgwgKJmE
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void extractTextFromImage() {
        // Show progress bar
        showProgressView();

        // clear the words content for the new extract
        words.removeAll(words);

        // Uses FirebaseVision library to extract text from image
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmapForProcess);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(
                    this::getImageText
                ).addOnFailureListener(e -> { // Failure listener
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        rotation += 90;
        return rotatedImg;
    }

    private void removeElementBefore(){
        // Remove everything before "Quo-Lab"
        for(String word : words){
            if(word.contains("Quo-Lab")){
                submitVerifyPass = true;
                words.subList(0, words.indexOf(word)).clear();
                if(words.indexOf(word) == 0){
                    bashNumber = word.replace("Quo-Lab ","");
                    words.remove(word);
                }
                break;
            }
        }
    }

    private void removeElementAfter(){
        // Remove everything after "Operator:"
        for(String word : words){
            if(word.contains("Operator") && (words.indexOf(word) != words.size())){
                words.subList(words.indexOf(word),words.size() ).clear();
                if(words.indexOf(word) == words.size()-1){
                    words.remove(word);
                }
                break;
            }
        }
    }

    private void removeUnwantedWord(){
        // Remove all unwanted words from the word list (Based on unwanted String array)
        for (String unwanted: unwantedWords) {
            words.remove(unwanted);
        }
    }

    private String getRawString(FirebaseVisionText firebaseVisionText){
        String lines = "";
        for (FirebaseVisionText.Block block: firebaseVisionText.getBlocks()) {
            lines += block.getText();
            if(!(lines.toCharArray()[lines.length()-1] == 'n' && lines.toCharArray()[lines.length()-2] == '\\')){
                // "\n" is chosen for the originally "next line" detected in the image
                lines += "\n";
            }
        }
        return lines;
    }

    private void splitRawString(String lines){
        // Convert String to Array and add into List
        for(String word : lines.split("\\n")){
            words.add(word);
        }
    }
    // Function to validate data from Firebase Vision ML Kit and set values to text input layouts
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getImageText(FirebaseVisionText firebaseVisionText) {
        String lines = getRawString(firebaseVisionText);

        splitRawString(lines);
        removeElementBefore();
        removeElementAfter();
        removeUnwantedWord();
        words = filterRequiredText();

        // First condition here means [if (joins of values in "words" ArrayList) equal to "" ]
        if(words.stream().map(Object::toString).collect(Collectors.joining("")).equals("") && rotation<=360){
            imageBitmapForProcess = rotateImage(imageBitmapForProcess,90);
            extractTextFromImage();
        }
        else{
            // This line is for debugging can be deleted
            if(words.size() == 7){
                hideProgressView();
                removeUnitFromValue();
                mergeField();
                failExtractMsg();
                fillValueIntoInputField();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void mergeField(){
        if(!words.get(0).equals("")){
            ArrayList<String> tempList =new ArrayList<>(Arrays.asList(words.get(0).split("")));

            if(words.get(0).contains("pmm")||words.get(0).contains("pmn")||words.get(0).contains("amm")||words.get(0).contains("amn")){
                tempList.remove(tempList.size()-1);
            }

            words.set(0,String.join("", tempList));
            words.set(0, words.get(1)+ " "+ words.get(0));
        }
        if(!words.get(1).equals("")){
            words.set(1, words.get(2)+ " / "+words.get(3));
        }
        words.set(2, words.get(4));
        words.set(3, words.get(5));
        words.set(4, words.get(6));
        words.remove(words.size()-1);
        words.remove(words.size()-1);
    }

    private void removeUnitFromValue(){
        // Remove the "A1C" substring from the A1C data values
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).contains("A1C")){
                words.set(i, words.get(i).replace("A1C", ""));
            }
        }
        // Removes the '%' and 'mmol/mol' section of the result data
        words.set(2, words.get(2).replace("%", ""));
        words.set(3, words.get(3).split("m")[0]);
    }

    private void fillValueIntoInputField(){
        // Loop through word list and fills the text input layouts
        for (int i = 0; i < words.size(); i++) {
            textInputLayouts.get(i).getEditText().setText(words.get(i));
        }
    }

    private void failExtractMsg(){
        int count = 0;
        int allCount = 5;
        for(String word : words){
            count += (word.equals(""))?1:0;
        }
        if(count==allCount){
            Toast.makeText(getApplicationContext(), "Extract Fail: There is no text found.\n Please retake the picture.", Toast.LENGTH_LONG).show();
        }
        else if(count>0 && count<allCount){
            Toast.makeText(getApplicationContext(), "Extract Fail: There is only "+(allCount-count)+" field found.\n Please fill in manually for the remaining "+count+" field(s) or retake the picture.", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Text extracted successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> filterRequiredText() {

        ArrayList<String> tempList = new ArrayList<>(Arrays.asList("","","","","","",""));

        for(String word: words){

            if(tempList.get(0).equals("")){
                tempList.set(0,filterTime(word));

            }
            else if(tempList.get(1).equals("")){
                tempList.set(1,filterDate(word));
            }
            else if(tempList.get(2).equals("")){
                tempList.set(2,filterA1CPercentage(word));
            }
            else if(tempList.get(3).equals("")){
                tempList.set(3,filterA1CMol(word));
            }
            else if(tempList.get(4).equals("")){
                tempList.set(4,filterLot(word));
            }
            else if(tempList.get(5).equals("")){
                tempList.set(5,filterInstID(word));
            }
            else if(tempList.get(6).equals("")){
                tempList.set(6,filterTestID(word));
            }
            else if(tempList.contains("") == false){
                break;
            }
        }
        return tempList;
    }

    private String filterTime(String word) {
        return ((matchTimeFormat(new ArrayList<>(Arrays.asList(word.split("")))))?word:"");
    }

    private String filterDate(String word) {
        return ((matchDateFormat(new ArrayList<>(Arrays.asList(word.split("")))))?word:"");
    }

    private String filterA1CPercentage(String word) {
        return ((matchA1CPercentageFormat(new ArrayList<>(Arrays.asList(word.split("%")))))?word:"");
    }

    private String filterA1CMol(String word) {
        return ((matchA1CMolFormat(new ArrayList<>(Arrays.asList(word.split("mmol/mol")))))?word:"");
    }

    private String filterLot(String word) {
        try{
            Integer.parseInt(word);
            return ((word.toCharArray().length == 6)?word:"");
        }
        catch (Exception e){
            return "";
        }
    }

    private String filterInstID(String word) {
        // wait for further confirmation about the format of the Inst ID
        return word;
    }
    private String filterTestID(String word) {
        try{
            Integer.parseInt(word);
            return ((word.toCharArray().length == 5)?word:"");
        }
        catch (Exception e){
            return "";
        }
    }

    private boolean matchA1CMolFormat(ArrayList<String> tempList){
        return (tempList.contains("A1C")||tempList.contains("A1CC")||tempList.contains(" A1C")||tempList.contains(" A1CC"));
    }
    private boolean matchA1CPercentageFormat(ArrayList<String> tempList){
        return (tempList.contains("A1C")||tempList.contains("A1CC"));
    }
    private boolean matchDateFormat(ArrayList<String> tempList){
        return ((tempList.size()==11)&&(tempList.get(3) + tempList.get(6)).equals("//"));
    }
    private boolean matchTimeFormat(ArrayList<String> tempList){
        return ((tempList.size() == 8 || tempList.size() == 9) && (tempList.get(3).equals(":")) && ((tempList.contains("p")&&tempList.contains("m"))||(tempList.contains("a")&&tempList.contains("m"))));
    }

    private boolean validateEachField(){
        // Check for empty values in the text input layouts
        for(TextInputLayout til: textInputLayouts){
            String text = til.getEditText().getText().toString();

            if(text.equals("")){
                return false;
            }
        }
        return true;
    }
    private void submitValidationDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please confirm that all values are correct before submitting.");
        builder.setCancelable(true);

        // Uploads data to database (To be implemented) & resets image and text input layout values to default values
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            dialog.cancel();
            Toast.makeText(getApplicationContext(), "Uploading data to database.", Toast.LENGTH_SHORT).show();
            capturedImage.setImageResource(R.drawable.placeholder);
            for (int i = 0; i < textInputLayouts.size(); i++) {
                textInputLayouts.get(i).getEditText().setText("");
            }
            submitRecordToDatabase();
        });
        // Closes alert dialog
        builder.setNegativeButton("Go Back", (dialog, which) -> { dialog.cancel(); });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void submitRecordToDatabase(){
        // TODO to be implement (submit to database)
    }
    private void submitValidation() {
        // Validate form values before submitting it to the database

        if (validateEachField()) {
            if(!submitVerifyPass){
                Toast.makeText(getApplicationContext(), "Submission Fail: Invalid record found, please make sure you slip is from 'Quo-Lab' machine", Toast.LENGTH_LONG).show();
            }
            else{
                submitValidationDialogBox();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Submission Fail: There are empty values found, please make sure that they are filled in.", Toast.LENGTH_LONG).show();
        }
    }

    // Function to show the progress spinner view
    public void showProgressView() {
        if (!isProgressShown) {
            isProgressShown = true;
            progressView = (ViewGroup) getLayoutInflater().inflate(R.layout.progressbar_layout, null);
            View v = this.findViewById(android.R.id.content).getRootView();
            ViewGroup viewGroup = (ViewGroup) v;
            viewGroup.addView(progressView);
        }
    }

    // Function to hide the progress spinner view
    public void hideProgressView() {
        View v = this.findViewById(android.R.id.content).getRootView();
        ViewGroup viewGroup = (ViewGroup) v;
        viewGroup.removeView(progressView);
        isProgressShown = false;
    }

    // Un-focuses on the text input layouts when any whitespace is clicked on
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}