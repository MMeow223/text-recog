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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private Bitmap imageBitmap;
    private Bitmap imageBitmapForProcess;
    private ViewGroup progressView;
    private ImageView capturedImage;
    private DBHelper db;
    private ArrayList<String> words = new ArrayList<>();
    private final ArrayList<TextInputLayout> textInputLayouts = new ArrayList<>();
    private final String[] unwantedWords = {"Time", "Time:", "Date", "Date:", "Result", "Result:", "DCCT", "IFCC", "Lot", "Lot:", "Inst ID", "Inst ID ","Inst ID:", "Test ID", "Test ID:", "Operator", "Operator:"};

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private int rotation = 0;
    private String mCurrentPhotoPath;
    private boolean isProgressShown = false;
    private boolean isVerificationPass = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DBHelper(this);

        hideProgressView();

        imageBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.placeholder);
        capturedImage = findViewById(R.id.capturedImage);
        capturedImage.setImageBitmap(imageBitmap);
        TextInputLayout dateTimeView = findViewById(R.id.dateTimeView);
        TextInputLayout resultView1 = findViewById(R.id.resultView1);
        TextInputLayout lotView = findViewById(R.id.lotView);
        TextInputLayout instIdView = findViewById(R.id.instIdView);
        TextInputLayout testIdView = findViewById(R.id.testIdView);
        TextInputLayout operatorView = findViewById(R.id.operatorView);
        Button submitBtn = findViewById(R.id.submitBtn);
        Button captureImageBtn = findViewById(R.id.captureImageBtn);
        ImageButton historyRecordBtn = findViewById(R.id.historyRecordButton);


        // Add text input layouts to array list
        textInputLayouts.add(dateTimeView);
        textInputLayouts.add(resultView1);
        textInputLayouts.add(lotView);
        textInputLayouts.add(instIdView);
        textInputLayouts.add(testIdView);
        textInputLayouts.add(operatorView);

        // Calls camera activity function when 'Take Picture' button is pressed
        captureImageBtn.setOnClickListener(v -> {
            isVerificationPass = false;
            rotation = 0;
            dispatchTakePictureIntent();
        });

        // Submit data to database
        submitBtn.setOnClickListener(v -> submitValidation());

        // Calls history activity function when 'History' button is pressed
        historyRecordBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HistoryRecordActivity.class)));
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
                Toast.makeText(getApplicationContext(),  getString(R.string.rotate_phone_crash) , Toast.LENGTH_LONG).show();
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

    private Bitmap rotateImage(Bitmap img) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        rotation += 90;
        return rotatedImg;
    }

    private void removeElementBefore(){
        // Remove everything before "Quo-Lab"
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).contains("Quo-Lab")) {
                isVerificationPass = true; // set verification pass to true when "Quo-Lab" is found
                words.subList(0, i).clear();
                break;
            }
        }
    }

    private void removeElementAfter(){
        // Remove everything after "Operator:"
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).contains("Operator:")) {
                words.subList(i+1, words.size()).clear();
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
        StringBuilder lines = new StringBuilder();
        for (FirebaseVisionText.Block block: firebaseVisionText.getBlocks()) {
            lines.append(block.getText());
            if(!(lines.toString().toCharArray()[lines.length()-1] == 'n' && lines.toString().toCharArray()[lines.length()-2] == '\\')){
                // "\n" is chosen for the originally "next line" detected in the image
                lines.append("\n");
            }
        }
        return lines.toString();
    }

    private void splitRawString(String lines){
        // Split the lines and added into words array
        Collections.addAll(words, lines.split("\\n"));
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

        // rotate and extract text from image again if no text is found
        if(words.stream().map(Object::toString).collect(Collectors.joining("")).equals("") && rotation<=360){
            imageBitmapForProcess = rotateImage(imageBitmapForProcess);
            extractTextFromImage();
        }
        else{
            hideProgressView();
            if(words.size() == 7){
                removeUnitFromValue();
                mergeField();
                failExtractMsg();
                fillValueIntoInputField();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void mergeField(){

        // merge first element of words array with the second element of words array with space if the first element is not empty
        if(!words.get(0).equals("")){
            //remove "pmm","pmn","amm","amn" from the first element of words array
            words.set(0, words.get(0).replace("pmm","").replace("pmn","").replace("amm","").replace("amn",""));
            words.set(0, words.get(1) + " " + words.get(0));
        }
        // merge third element of words array with the fourth element of words array with slash if the second element is not empty
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
        // Remove "%","mmol/mol","A1C" from the value
        words.set(2, words.get(2).split("%")[0]);
        words.set(3, words.get(3).split("m")[0]);
    }

    private void fillValueIntoInputField(){
        // Loop through word list and fills the text input layouts
        for (int i = 0; i < words.size(); i++) {
            Objects.requireNonNull(textInputLayouts.get(i).getEditText()).setText(words.get(i));
        }
    }

    private void failExtractMsg(){
        int count = 0;
        int allCount = 5;
        for(String word : words){
            count += (word.equals(""))?1:0;
        }
        if(count==allCount){
            Toast.makeText(getApplicationContext(), getString(R.string.no_text_found), Toast.LENGTH_LONG).show();
        }
        else if(count>0 && count<allCount){
            Toast.makeText(getApplicationContext(), "Extract Fail: There is only "+(allCount-count)+" field found.\n Please fill in manually for the remaining "+count+" field(s) or retake the picture.", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.extract_success), Toast.LENGTH_SHORT).show();
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
            else if(!tempList.contains("")){
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
            String text = Objects.requireNonNull(til.getEditText()).getText().toString();

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

            boolean submitSuccess = submitRecordToDatabase();
            if(submitSuccess){
                capturedImage.setImageResource(R.drawable.placeholder);
                for (int i = 0; i < textInputLayouts.size(); i++) {
                    Objects.requireNonNull(textInputLayouts.get(i).getEditText()).setText("");
                }
                Toast.makeText(getApplicationContext(), "Upload record to database successfully!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Fail to upload record to database. Are you submitting a repeated slip?", Toast.LENGTH_SHORT).show();
            }
        });
        // Closes alert dialog
        builder.setNegativeButton("Go Back", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void confirmSubmitEmptyField(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("There are empty field(s) found. Proceed submit?");
        builder.setCancelable(true);

        // Uploads data to database (To be implemented) & resets image and text input layout values to default values
        builder.setPositiveButton("Proceed", (dialog, which) -> {
            dialog.cancel();

            boolean submitSuccess = submitRecordToDatabase();
            if(submitSuccess){
                capturedImage.setImageResource(R.drawable.placeholder);
                for (int i = 0; i < textInputLayouts.size(); i++) {
                    Objects.requireNonNull(textInputLayouts.get(i).getEditText()).setText("");
                }
                Toast.makeText(getApplicationContext(), "Upload record to database successfully!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Fail to upload record to database. Are you submitting a repeated slip?", Toast.LENGTH_SHORT).show();
            }
        });
        // Closes alert dialog
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean submitRecordToDatabase(){
        
        ArrayList<String> tempArray = new ArrayList<>();
        for(TextInputLayout til:textInputLayouts){
            tempArray.add(Objects.requireNonNull(til.getEditText()).getText().toString());
        }

        byte[] imageByteArray = convertBitmapToByteArray();

        return db.insertSlipResultToDatabase(tempArray.get(0),tempArray.get(1),tempArray.get(2),tempArray.get(3),tempArray.get(4),tempArray.get(5),imageByteArray);
    }

    private byte[] convertBitmapToByteArray(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        return stream.toByteArray();
    }

    private void submitValidation() {
        // Validate form values before submitting it to the database
        if(!isVerificationPass){
            Toast.makeText(getApplicationContext(), "Submission Fail: Invalid record found, please make sure you slip is from 'Quo-Lab' machine", Toast.LENGTH_LONG).show();
        }
        else{
            confirmSubmitEmptyField();
        }
    }

    // Function to show the progress spinner view
    @SuppressLint("InflateParams")
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