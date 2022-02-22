package com.example.textrecognitionapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final ArrayList<TextInputLayout> textInputLayouts = new ArrayList<>();
    private final String[] unwantedWords = {
            "Quo-Lab A1C",
            "Time", "Time:",
            "Date", "Date:",
            "Result", "Result:",
            "DCCT", "IFCC",
            "Lot", "Lot:",
            "Inst ID", "Inst ID ", "Inst ID:",
            "Test ID", "Test ID:",
            "Operator", "Operator:"};

    private Bitmap imageBitmap;
    private Bitmap imageBitmapForProcess;
    private int rotation = 0;
    private String mCurrentPhotoPath;

    private ViewGroup progressView;
    private ImageView capturedImage;
    private DBHelper db;
    private ArrayList<String> words = new ArrayList<>();
    private boolean isProgressShown = false;
    private boolean isQuoLabMachine = true;
//    private boolean isQuoLabMachine = false;

    /**
     * Initialise the activity
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.db = new DBHelper(this);

        hideProgressView();

        this.imageBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.placeholder);
        this.capturedImage = findViewById(R.id.capturedImage);
        this.capturedImage.setImageBitmap(this.imageBitmap);
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
        this.textInputLayouts.add(dateTimeView);
        this.textInputLayouts.add(resultView1);
        this.textInputLayouts.add(lotView);
        this.textInputLayouts.add(instIdView);
        this.textInputLayouts.add(testIdView);
        this.textInputLayouts.add(operatorView);

        // Calls camera activity function when 'Take Picture' button is pressed
        captureImageBtn.setOnClickListener(v -> {
            this.isQuoLabMachine = false;
            this.rotation = 0;
            dispatchTakePictureIntent();
        });

        // Submit data to database
        submitBtn.setOnClickListener(v -> handleValidationAndSubmission());

        // Calls history activity function when 'History' button is pressed
        historyRecordBtn.setOnClickListener(v -> startActivity(
                new Intent(MainActivity.this, HistoryRecordActivity.class))
        );
    }

    /**
     * On activity result
     *
     * @param requestCode int
     * @param resultCode  int
     * @param data        Intent
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When previous activity was from 'dispatchTakePictureIntent' function
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                galleryAddPic();
                setPic();
            } catch (NullPointerException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.rotate_phone_crash), Toast.LENGTH_LONG).show();
            }
            // Calls function to utilise Firebase Vision ML Kit text recognition to extract text from image
            this.imageBitmapForProcess = this.imageBitmap.copy(this.imageBitmap.getConfig(), true);
            extractTextFromImage();
        }
    }

    /**
     * Launching camera activity to capture image
     * <p>
     * Below functions are taken from https://developer.android.com/training/camera/photobasics:<br>
     * |-'dispatchTakePictureIntent()'<br>
     * |-'createImageFile()'<br>
     * |-'galleryAddPic()'<br>
     * |-'setPic()'<br>
     * |-'onActivityResult()'
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_message_prefix) + ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(
                    this,
                    "com.example.android.fileprovider",
                    photoFile
            );

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * Create a unique file for image
     *
     * @return image file
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
        String imageFileName = "HbA1c_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        this.mCurrentPhotoPath = image.getAbsolutePath(); // Save a file: path for use with ACTION_VIEW intents
        return image;
    }


    /**
     * Add picture to gallery
     */
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(this.mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);

        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /**
     * Set the ImageView with the photo taken
     */
    private void setPic() {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(this.mCurrentPhotoPath, bmOptions);

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(((bmOptions.outWidth) / (this.capturedImage.getWidth())), ((bmOptions.outHeight) / (this.capturedImage.getHeight()))));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        this.capturedImage.setImageBitmap(adjustImageOrientation(bmOptions));

    }

    /**
     * Decode the image and identify the image orientation
     *
     * @param bmOptions BitmapFactory.Options
     * @return int
     */
    private int decodeImageAndIdentifyOrientation(BitmapFactory.Options bmOptions) throws IOException {
        // decode image
        this.imageBitmap = BitmapFactory.decodeFile(this.mCurrentPhotoPath, bmOptions);

        // identify orientation and return
        ExifInterface ei = new ExifInterface(new File(this.mCurrentPhotoPath).getAbsolutePath());
        return ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
    }

    /**
     * Rotate image to make it portrait
     *
     * @param bmOptions BitmapFactory.Options
     * @return Bitmap
     */
    private Bitmap adjustImageOrientation(BitmapFactory.Options bmOptions) {
        try {

            switch (decodeImageAndIdentifyOrientation(bmOptions)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(this.imageBitmap, 90);

                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(this.imageBitmap, 180);

                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(this.imageBitmap, 270);

                default:
                    return this.imageBitmap; // return the original bitmap if no changes
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_message_prefix) + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return this.imageBitmap; // return the original bitmap if no changes
    }

    /**
     * Extract text from the image taken<br><br>
     * Below function are referenced from https://www.youtube.com/watch?v=fmTlgwgKJmE:<br>
     * |- 'extractTextFromImage()'<br>
     * |- 'getImageText ()'
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void extractTextFromImage() {
        preparationBeforeExtractTextFromImage();

        // Uses FirebaseVision library to extract text from image
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(this.imageBitmapForProcess);

        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();

        firebaseVisionTextDetector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(
                        this::getProcessedTextFromImage
                )
                .addOnFailureListener(e -> { // Failure listener
                    Toast.makeText(getApplicationContext(), getString(R.string.error_message_prefix) + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handle remove progress view and remove all text in "words" list
     */
    private void preparationBeforeExtractTextFromImage() {
        showProgressView();
        this.words.removeAll(this.words);
    }

    /**
     * Rotate the given image based on given angle
     *
     * @param source Bitmap
     * @param angle  float
     * @return Bitmap
     */
    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Process the words list based by removing:<br>
     * |-all element before a specific word<br>
     * |-all element after a specific word<br>
     * |-all pre-defined unwanted words<br><br>
     * <p>
     * and filter required words
     */
    private void processWordList() {
        removeElementBefore();
        removeElementAfter();
        removeUnwantedWord();
        filterRequiredText();
    }

    /**
     * Remove all elements before the word "Quo-Lab"
     */
    private void removeElementBefore() {
        // Remove everything before "Quo-Lab"
        for (int i = 0; i < this.words.size(); i++) {
            if (this.words.get(i).contains("Quo-Lab")) {
                this.isQuoLabMachine = true; // set verification pass to true when "Quo-Lab" is found
                this.words.subList(0, i).clear();
                break;
            }
        }
    }

    /**
     * Remove all elements after the word "Operator"
     */
    private void removeElementAfter() {
        for (int i = this.words.size() - 1; i > 0; i--) {
            if (this.words.get(i).contains("Operator")) {
                this.words.subList(i, this.words.size()).clear();
                break;
            }
        }
    }

    /**
     * Remove all unwanted words from the extracted text
     */
    private void removeUnwantedWord() {
        for (String unwanted : this.unwantedWords) {
            this.words.remove(unwanted);
        }
    }

    /**
     * Get raw string from the text
     *
     * @param firebaseVisionText FirebaseVisionText
     * @return String
     */
    private String getRawString(FirebaseVisionText firebaseVisionText) {
        StringBuilder lines = new StringBuilder();
        for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
            lines.append(block.getText());
            if (!(lines.toString().toCharArray()[lines.length() - 1] == 'n' && lines.toString().toCharArray()[lines.length() - 2] == '\\')) {
                // "\n" is chosen for the originally "next line" detected in the image
                lines.append("\n");
            }
        }
        return lines.toString();
    }

    /**
     * Get and split raw string
     *
     * @param firebaseVisionText FirebaseVisionText
     */
    private void getAndSplitRawStringIntoList(FirebaseVisionText firebaseVisionText) {
        Collections.addAll(this.words, getRawString(firebaseVisionText).split("\\n"));
    }

    /**
     * Check if the words list have any empty value
     *
     * @return boolean
     */
    private boolean checkIfContainAnyEmpty() {

        for (String word : this.words) {
            if (word.equals("")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get processed text from image
     *
     * @param firebaseVisionText FirebaseVisionText
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getProcessedTextFromImage(FirebaseVisionText firebaseVisionText) {

        getAndSplitRawStringIntoList(firebaseVisionText);
        processWordList();

        // rotate and extract text from image again if no text is found
        if (checkIfContainAnyEmpty() && this.rotation <= 360) {
            rotation += 90;
            this.imageBitmapForProcess = rotateImage(this.imageBitmapForProcess, 90);
            extractTextFromImage();
        } else {
            hideProgressView();
            if (this.words.size() == 7) {
                removeUnitFromValue();
                mergeField();
                handleFailExtractMsg();
                fillValueIntoInputField();
            }
        }
    }

    /**
     * Merge the field
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void mergeField() {

        // merge first element of words array with the second element of words array with space if the first element is not empty
        if (!this.words.get(0).equals("")) {
            //remove "pmm","pmn","amm","amn" from the first element of words array
            this.words.set(0, this.words.get(0).replace("pmm", "").replace("pmn", "").replace("amm", "").replace("amn", ""));
            this.words.set(0, this.words.get(1) + " " + this.words.get(0));
        }
        // merge third element of words array with the fourth element of words array with slash if the second element is not empty
        if (!this.words.get(1).equals("")) {
            this.words.set(1, this.words.get(2) + " / " + this.words.get(3));
        }
        this.words.set(2, this.words.get(4));
        this.words.set(3, this.words.get(5));
        this.words.set(4, this.words.get(6));
        this.words.remove(this.words.size() - 1);
        this.words.remove(this.words.size() - 1);

    }

    /**
     * Remove unit from the words in "words" list
     */
    private void removeUnitFromValue() {
        // Remove "%","mmol/mol","A1C" from the value
        this.words.set(2, this.words.get(2).split("%")[0]);
        this.words.set(3, this.words.get(3).split("m")[0]);
    }

    /**
     * Loop through and fill in value to each input field
     */
    private void fillValueIntoInputField() {
        for (int i = 0; i < this.words.size(); i++) {
            Objects.requireNonNull(this.textInputLayouts.get(i).getEditText()).setText(this.words.get(i));
        }
    }

    /**
     * Handle error message if the text extraction is failed
     */
    private void handleFailExtractMsg() {
        int requiredInputFieldCount = 5;
        int emptyIputFieldCount = countEmptyInputField();

        if (emptyIputFieldCount == requiredInputFieldCount) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_text_found), Toast.LENGTH_LONG).show();

        } else if (emptyIputFieldCount > 0 && emptyIputFieldCount < requiredInputFieldCount) {
            Toast.makeText(getApplicationContext(), "Extract Fail: There is only " + (requiredInputFieldCount - emptyIputFieldCount) + " field found.\n Please fill in manually for the remaining " + emptyIputFieldCount + " field(s) or retake the picture.", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.extract_success), Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * Count the total number of input field with empty value
     *
     * @return int
     */
    private int countEmptyInputField() {
        int emptyIputFieldCount = 0;

        for (String word : this.words) {
            emptyIputFieldCount += (word.equals("")) ? 1 : 0;

        }

        return emptyIputFieldCount;
    }

    /**
     * Filter out the required text
     */
    private void filterRequiredText() {

        ArrayList<String> tempList = new ArrayList<String>(Arrays.asList("", "", "", "", "", "", ""));

        for (int i = 0; i < tempList.size(); i++) {

            for (int j = 0; j < this.words.size(); j++) {

                if (tempList.get(i).equals("")) {

                    tempList.set(i, filteringProcess(i, tempList, this.words.get(j)));

                } else {
                    break;
                }
            }
        }
        this.words = tempList;

    }

    /**
     * Choose the method used for filtering
     *
     * @param filterMethod int
     * @param tempList     ArrayList<String>
     * @param text         String
     * @return String
     */
    private String filteringProcess(int filterMethod, ArrayList<String> tempList, String text) {
        switch (filterMethod) {
            case 0:
                return filterTime(text);
            case 1:
                return filterDate(text);
            case 2:
                return filterA1CPercentage(text);
            case 3:
                return filterA1CMol(text);
            case 4:
                return filterLot(text);
            case 5:
                return filterInstID(text, tempList);
            case 6:
                return filterTestID(text);
            default:
                return text;
        }
    }

    /**
     * Return empty if the word do not match the time format
     *
     * @param word String
     * @return String
     */
    private String filterTime(String word) {
        return ((matchTimeFormat(new ArrayList<>(Arrays.asList(word.split(""))))) ? word : "");
    }

    /**
     * Return empty if the word do not match the date format
     *
     * @param word String
     * @return String
     */
    private String filterDate(String word) {
        return ((matchDateFormat(new ArrayList<>(Arrays.asList(word.split(""))))) ? word : "");
    }

    /**
     * Return empty if the word do not match the a1c percentage format
     *
     * @param word String
     * @return String
     */
    private String filterA1CPercentage(String word) {
        return ((matchA1CPercentageFormat(new ArrayList<>(Arrays.asList(word.split("%"))))) ? word : "");
    }

    /**
     * Return empty if the word do not match the a1c mol format
     *
     * @param word String
     * @return String
     */
    private String filterA1CMol(String word) {
        return ((matchA1CMolFormat(new ArrayList<>(Arrays.asList(word.split("mmol/mol"))))) ? word : "");
    }

    /**
     * Return empty if the word do not match the lot format
     *
     * @param word String
     * @return String
     */
    private String filterLot(String word) {
        try {
            Integer.parseInt(word);
            return ((word.toCharArray().length == 6) ? word : "");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Return empty if the word do not match the inst id format
     *
     * @param word String
     * @return String
     */
    private String filterInstID(String word, ArrayList<String> tempList) {

        if (!(Arrays.asList(word.split("")).size() == 7)) {
            return "";
        }
        if (tempList.contains(word)) {
            return "";
        }
        return word;
    }

    /**
     * Return empty if the word do not match the test id format
     *
     * @param word String
     * @return String
     */
    private String filterTestID(String word) {
        try {
            Integer.parseInt(word);
            return ((word.toCharArray().length == 5) ? word : "");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Format of date
     *
     * @param tempList
     * @return
     */
    private boolean matchDateFormat(ArrayList<String> tempList) {
        return ((tempList.size() == 11) && (tempList.get(3) + tempList.get(6)).equals("//"));
    }

    /**
     * Format of time
     *
     * @param tempList
     * @return
     */
    private boolean matchTimeFormat(ArrayList<String> tempList) {
        return ((tempList.size() == 8 || tempList.size() == 9) && (tempList.get(3).equals(":")) && ((tempList.contains("p") && tempList.contains("m")) || (tempList.contains("a") && tempList.contains("m"))));
    }

    /**
     * Format of a1c mol
     *
     * @param tempList
     * @return
     */
    private boolean matchA1CMolFormat(ArrayList<String> tempList) {
        return (tempList.contains("A1C") || tempList.contains("A1CC") || tempList.contains(" A1C") || tempList.contains(" A1CC"));
    }

    /**
     * Format of a1c %
     *
     * @param tempList
     * @return
     */
    private boolean matchA1CPercentageFormat(ArrayList<String> tempList) {
        return (tempList.contains("A1C") || tempList.contains("A1CC"));
    }


    /**
     * Confirm submit dialog box
     */
    private void confirmSubmitDialogBox(boolean haveEmpty) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String dialogText = (haveEmpty)?"There are empty field(s) found. Proceed submit?":"Confirm submit? Please make sure all field are filled correctly.";
        builder.setMessage(dialogText);
        builder.setCancelable(true);

        builder.setPositiveButton("Proceed", (dialog, which) -> {
            dialog.cancel();

            boolean submitSuccess = submitRecordToDatabase(); // submit to database
            if (submitSuccess) {
                capturedImage.setImageResource(R.drawable.placeholder);
                for (int i = 0; i < textInputLayouts.size(); i++) {
                    Objects.requireNonNull(textInputLayouts.get(i).getEditText()).setText("");
                }
                Toast.makeText(getApplicationContext(), getString(R.string.success_upload), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.fail_upload_repeat), Toast.LENGTH_SHORT).show();
            }
        });

        // Closes alert dialog
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Validate input field data before submit to database
     */
    private void handleValidationAndSubmission() {
        if (!this.isQuoLabMachine) {
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_record_found), Toast.LENGTH_LONG).show();
        } else {
            if(checkIfTextInputLayoutFieldValueAreValid()){
                confirmSubmitDialogBox(checkIfAnyEmptyTextInputLayoutField());
            }
            else{
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_format_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Return true if there is empty field found
     *
     * @return boolean
     */
    private boolean checkIfAnyEmptyTextInputLayoutField(){

        for(TextInputLayout til : this.textInputLayouts){
            if(til.getEditText().getText().toString().equals("")){
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the filled text are fulfil the required format
     *
     * @return
     */
    private boolean checkIfTextInputLayoutFieldValueAreValid(){

        String datetime = textInputLayouts.get(0).getEditText().getText().toString();
        String a1cResult = textInputLayouts.get(1).getEditText().getText().toString();
        String lotView = textInputLayouts.get(2).getEditText().getText().toString();
        String instId = textInputLayouts.get(3).getEditText().getText().toString();
        String testId = textInputLayouts.get(4).getEditText().getText().toString();

        if(!(datetime.equals(""))){
            if(!Regex.DatetimePattern(datetime)){
                return false;
            }
        }
        if(!(a1cResult.equals(""))){
            if(!Regex.A1CResultPattern(a1cResult)){
                return false;
            }
        }
        if(!(lotView.equals(""))){
            if(!Regex.LotViewPattern(lotView)){
                return false;
            }
        }
        if(!(instId.equals(""))){
            if(!Regex.InstIdPattern(instId)){
                return false;
            }
        }
        if(!(testId.equals(""))){
            if(!Regex.TestIdPattern(testId)){
                return false;
            }
        }
        return true;
    }

    /**
     * Submit record to database
     *
     * @return
     */
    private boolean submitRecordToDatabase() {

        ArrayList<String> tempArray = new ArrayList<>();
        for (TextInputLayout til : textInputLayouts) {
            tempArray.add(Objects.requireNonNull(til.getEditText()).getText().toString());
        }

        byte[] imageByteArray = convertBitmapToByteArray();

        return db.insertSlipResultToDatabase(tempArray.get(0), tempArray.get(1), tempArray.get(2), tempArray.get(3), tempArray.get(4), tempArray.get(5), imageByteArray);
    }

    /**
     * Convert bitmap to byte array
     *
     * @return byte array
     */
    private byte[] convertBitmapToByteArray() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }


    /**
     * Show the progress spinner view
     */
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

    /**
     * Hide the progress spinner view
     */
    public void hideProgressView() {
        View v = this.findViewById(android.R.id.content).getRootView();
        ViewGroup viewGroup = (ViewGroup) v;
        viewGroup.removeView(progressView);
        isProgressShown = false;
    }

    /**
     * Un-focuses on the text input layouts when any whitespace is clicked on
     *
     * @param ev MotionEvent
     * @return dispatchTouchEvent
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if ((ev.getAction() != MotionEvent.ACTION_DOWN) || !(v instanceof EditText)) {
            return super.dispatchTouchEvent(ev);
        }

        Rect outRect = new Rect();
        v.getGlobalVisibleRect(outRect);

        if (outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
            return super.dispatchTouchEvent(ev);
        }

        v.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        return super.dispatchTouchEvent(ev);
    }
}