package thesandwichguys.sandwichstory;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/* editRecipe is used when the user clicks the edit button from the sandwichInfo page.
Receives the index to determine which recipe the user wants to edit and pre-fills the
ingredients, name, picture and instructions. Then allows the user to save the edited recipe
and updates it on the device (not saving another recipe, just updates) */
public class editRecipe extends Activity {

    ArrayList<Ingredients> ingredient_list = new ArrayList<>(); //ArrayList of ingredients
    EditText editName; //name of the sandwich displayed (editText)
    ListView ingredients; //list of ingredients (qty, unit, name)
    ImageView sandwichPicture; //sandwich picture from gallery or camera
    Spinner qtySpinner; //spinner to pick quantity of an ingredient to add
    Spinner fractionSpinner; //spinner to pick fractional quantity of an ingredient to add
    Spinner measurementSpinner; //spinner to pick measurement of an ingredient to add
    EditText ingredientName; //editText to enter ingredient name to add to list
    EditText addInstructions; //editText to enter instructions
    String encodedImage;
    int index; //index to determine which sandwich to edit
    AppInfo appInfo;
    private static final int GALLERY = 1;
    private static final int CAMERA = 2;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //Hides the top bar
        setContentView(R.layout.edit_recipe); //edit_recipe.xml corresponds to this class
        appInfo = AppInfo.getInstance(this);
        init(); //sets everything else up
    }

    //Initialize everything in the view
    private void init(){
        editName = (EditText) findViewById(R.id.sandwich_name); //EditText that displays sandwich name at top
        sandwichPicture = (ImageView) findViewById(R.id.sandwich_picture); //ImageView that displays the sandwich picture
        ingredientName = (EditText) findViewById(R.id.ingredient_name); //EditText for the ingredient being added
        addInstructions = (EditText) findViewById(R.id.instructions); //EditText for the instructions
        ingredients = (ListView) findViewById(R.id.ingredients_list); //ListView displaying ingredients
        Button pickImage = (Button) findViewById(R.id.add_photo_button); //"add photo" button
        Button addIngredientButton = (Button) findViewById(R.id.add_ingredient_button); //"+" add ingredient button
        encodedImage = appInfo.sandwichToEdit.getImageId();
        Bundle extras = getIntent().getExtras(); //get extras passed from previous screen
        if(extras != null) {
            index = extras.getInt("index"); //index is passed from gridView
        }

        editName.setText(appInfo.sandwichToEdit.getSandwichName()); //pre-fills sandwich name at top
        addInstructions.setText(appInfo.sandwichToEdit.getInstructions()); //pre-fill instructions in editText
        sandwichPicture.setImageBitmap(turnEncodedStringToBitmap(appInfo.sandwichToEdit.getImageId())); //pre-fill sandwich picture
        ingredients.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ListView (ingredients)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true); //Allows us to scroll through the ingredients
                return false;
            }
        });

        addInstructions.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside EditText (instructions)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true); //Allows us to scroll through the instructions
                return false;
            }
        });

        final ingredientEditAdapter adapter = new ingredientEditAdapter(this, //set adapter for ingredient list as ingredientEditAdapter
                R.layout.layout_edit_ingredient, ingredient_list); //layout is "layout_edit_ingredient" filled with ArrayList "ingredient_list"
        ingredients.setAdapter(adapter);

        //add ingredients to the ListView
        if(appInfo.sandwichToEdit.getIngredientList() != null){
            ingredient_list.clear();
            for(Ingredients ingredient : appInfo.sandwichToEdit.getIngredientList()){ //get ingredients from appInfo
                ingredient_list.add(new Ingredients(ingredient.getIngredientName(),
                        ingredient.getQty(), ingredient.getUnit()));
            }
            adapter.notifyDataSetChanged(); //need to notify that the data set has been changed or else won't update
        }

        pickImage.setOnClickListener( //opens dialog when the user presses the "add photo" button
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        if(hasPermissions() == true){
                            showPictureDialog();
                        }else{
                            requestPermissions();
                        }
                    }
                }
        );

        addIngredientButton.setOnClickListener( //adds ingredient to listView when user presses "+" button
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        String name = ingredientName.getText().toString(); //get ingredient name

                        if(name.isEmpty()) { //check if the user has actually entered a value
                            String toastMsg = "Please enter an ingredient name!"; //if not, prompted again
                            Toast toast= Toast.makeText(getBaseContext(),toastMsg,Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }else{ //otherwise, we can add this ingredient to our list
                            String qty = (String)qtySpinner.getSelectedItem();
                            String fraction = (String)fractionSpinner.getSelectedItem();
                            String measure = (String)measurementSpinner.getSelectedItem();

                            qty = qty + " " + fraction; //concat the whole number and fractional quantity to one string

                            ingredient_list.add(new Ingredients(name, qty, measure)); //add this ingredient to the arrayList
                            adapter.notifyDataSetChanged(); //notify change to update list

                            //reset the text and spinners
                            qtySpinner.setSelection(0);
                            fractionSpinner.setSelection(0);
                            measurementSpinner.setSelection(0);
                            ingredientName.setText("");
                        }
                    }
                }
        );
        setup_spinners();
    }

    //opens a dialog when the user clicks the "add photo" button to ask whether the user wants to pick a photo
    //from gallery or from the camera
    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String [] pictureDialogItems = {
                "Select photo from gallery", "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int whichPictureMethod){
                switch (whichPictureMethod){
                    case 0:
                        choosePhotoFromGallery();
                        break;
                    case 1:
                        takePhotoFromCamera();
                        break;
                }
            }
        });
        pictureDialog.show();
    }

    //called when the user picks "photo from gallery" in dialog, opens gallery on device
    public void choosePhotoFromGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    //called when the user picks "photo from camera" in dialog, opens camera
    private void takePhotoFromCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(this.getPackageManager()) != null){
            startActivityForResult(cameraIntent, CAMERA);
        }
    }

    //after the user has taken or chosen a picture from gallery. Sets the chosen image in the ImageView
    //on the screen
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED){
            return;
        }

        if (requestCode == GALLERY){ //chosen from gallery
            if (data != null){
                Uri contentURI = data.getData();
                try{
                    Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI); //sets the file path of the image
                    sandwichPicture.setImageBitmap(photo); //set image in ImageView
                    encodedImage = turnBitMapToEncodedString(photo); //turns from bitMap to string
                }catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Failed!", Toast.LENGTH_LONG).show();
                }
            }
        }else if (requestCode == CAMERA) { //taken from camera
            Bitmap imageTakenFromCamera = (Bitmap) data.getExtras().get("data");
            sandwichPicture.setImageBitmap(imageTakenFromCamera); //set image in ImageView from the bitmap data
            saveImage(imageTakenFromCamera); //calls the saveImage function to store the image taken from camera to phone
            encodedImage = turnBitMapToEncodedString(imageTakenFromCamera); //turns from bitMap to string so that it can be saved/called
            Toast.makeText(this, "Image Saved!", Toast.LENGTH_LONG).show();
        }
    }

    //converts the bitmap into a string so that it can be saved throughout AppInfo and sharedPreferences
    public String turnBitMapToEncodedString(Bitmap image){
        String encodedImageString;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        encodedImageString = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImageString;
    }

    //Converts the string path of the image to bitMap so that it can load and be applied to imageViews
    public Bitmap turnEncodedStringToBitmap(String encodedImage){
        byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap photo = BitmapFactory.decodeByteArray(b, 0, b.length);
        return photo;
    }

    //Saves the image to the phone
    public String saveImage(Bitmap imageToSave){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageToSave.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File imageDirectory = new File(Environment.getExternalStorageDirectory() + "/SandwichStoryImageDir");
        if (!imageDirectory.exists()) {
            imageDirectory.mkdir();
        }
        try {
            File file = new File(imageDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
            file.createNewFile();
            FileOutputStream fileOutput = new FileOutputStream(file);
            fileOutput.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this, new String[] {file.getPath()}, new String[]{"image/jpeg"}, null);
            fileOutput.close();
            Log.d("TAG", "File Saved::--->" + file.getAbsolutePath());
            return file.getAbsolutePath();
        }catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    //Checks to see if the app has the permission to access the phone's memory to store the images
    private boolean hasPermissions(){
        int res = 0;
        String [] permissions = new String []{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        for(String perms : permissions){
            res = checkCallingOrSelfPermission(perms);
            if(!(res == PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }

    //Asks the user for permissions
    private void requestPermissions(){
        String [] permissions = new String []{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    //Checks the result to see if the user game the app permission or not
    //If it does allow the app to open the pictureDialog so that the user can take a picture/use the gallery images
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String [] permissions, @NonNull int [] grantResults){
        boolean allowed = true;
        switch (requestCode){
            case PERMISSION_REQUEST_CODE: //if user granted all permissions
                for(int res : grantResults){
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default: //if user not granted permissions
                allowed = false;
                break;
        }
        if (allowed){ //user granted all permissions we can do work
            showPictureDialog();
        }else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //sets up the spinners for ingredient options
    public void setup_spinners(){
        qtySpinner = (Spinner) findViewById(R.id.qty_spinner);
        fractionSpinner = (Spinner) findViewById(R.id.frac_spinner);
        measurementSpinner = (Spinner) findViewById(R.id.measurement_spinner);

        //Spinner 1: Whole number quantity spinner
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, //values shown in spinner are "qty_array" in strings.xml
                R.array.qty_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Specify the layout to use when the list of choices appears
        qtySpinner.setAdapter(adapter1); // Apply the adapter to the spinner

        //Spinner 2: Fractional quantity spinner
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, //values shown in spinner are "frac_array" in strings.xml
                R.array.frac_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Specify the layout to use when the list of choices appears
        fractionSpinner.setAdapter(adapter2); // Apply the adapter to the spinner

        //Spinner 3: Measurement spinner
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, //values shown in spinner are "measurements_array" in strings.xml
                R.array.measurements_array, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Specify the layout to use when the list of choices appears
        measurementSpinner.setAdapter(adapter3); // Apply the adapter to the spinner
    }

    //called when the user clicks the "save sandwich" button
    public void saveSandwich(View v) {
        String sandwichName = editName.getText().toString(); //get the name, store as string
        String instructions = addInstructions.getText().toString(); //get the instructions, store as string
        String imageId = encodedImage;
        Sandwich recipe;
        ArrayList<Ingredients> ingredients = new ArrayList<>();

        for (Ingredients ingredient : ingredient_list) { //add from listView to array list
            ingredients.add(ingredient);
        }

        //if all values aren't filled out, prompt again
        if (sandwichName.isEmpty() || instructions.isEmpty() || ingredients.size() == 0 || sandwichPicture.getDrawable().getConstantState().equals
                (getResources().getDrawable(R.drawable.chooseimage).getConstantState())) {
            Toast.makeText(this, "Invalid recipe! Please fill out all fields.", Toast.LENGTH_LONG).show();
            return;
        }else{ //otherwise, save the recipe
            recipe = appInfo.savedSandwich.get(index);
            recipe.setSandwichName(sandwichName);
            recipe.setIngredientList(ingredients);
            recipe.setInstructions(instructions);
            recipe.setImageId(imageId);
        }
        showSaveDialogue(recipe); //show dialog to ask if user wants to save to library
    }

    //Displays dialog to ask if the user wants to save to library
    public void showSaveDialogue(final Sandwich recipe) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Recipe added to list!");

        //ask user if they'd like to add to database
        alertDialog.setMessage(("Recipe saved to your list! Would you also like to add this recipe to the global database for all users to see?"))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {  //user selects yes, add to database
                        saveToLibrary(recipe); //saves to database
                        Intent intent = new Intent(editRecipe.this, mainActivity.class); //close after saving
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        saveAsJSON();
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { //user selects no, only save locally & return home
                        Intent intent = new Intent(editRecipe.this, mainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        saveAsJSON();
                        startActivity(intent);
                    }
                });
        alertDialog.show();
    }

    //saves recipe in JSON format to retrieve later
    public void saveAsJSON() {
        JSONArray jArray = new JSONArray();

        for(Sandwich savedRecipe : appInfo.savedSandwich){
            try{
                jArray.put(savedRecipe.sandwichAsJSON);
            }catch(Exception e){
                Log.e("help", "Error" + e.getStackTrace());
            }
        }

        SharedPreferences settings = this.getSharedPreferences(mainActivity.MYPREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("sandwichesAsJSON", jArray.toString()); //saves JSON as string in sandwichesAsJSON
        editor.commit();
    }

    //saves recipe to database
    public void saveToLibrary(Sandwich recipe) {

        JSONObject obj = new JSONObject(); //create JSON representation of recipe being added to database
        try{
            obj.put("name", recipe.getSandwichName());
            obj.put("msg", recipe.getInstructions());
            obj.put("imgId", recipe.getImageId());

            JSONArray jArray = new JSONArray();

            for (Ingredients ingredient : recipe.getIngredientList()) { //add the ingredients
                jArray.put(ingredient.getJsonIngredient());
            }

            final String ing = jArray.toString();
            obj.put("ingredients", ing);
        }catch (Exception e) {
            Log.d("Error", "Something went wrong");
        }

        //backend method to add to database
        JsonObjectRequest jsobj = new JsonObjectRequest(
                "https://sandwichstory-172520.appspot.com/api/add_sandwich", obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Error", "Received: " + response.toString());
                        try {
                            Log.d("Error", "Saved properly to database");
                        } catch (Exception e) {
                            Log.d("Error", "Not saved properly to database");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Debug", "Error");
            }
        });
        appInfo.queue.add(jsobj);
    }
}
