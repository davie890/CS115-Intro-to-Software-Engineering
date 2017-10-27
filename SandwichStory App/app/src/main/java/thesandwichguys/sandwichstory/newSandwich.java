package thesandwichguys.sandwichstory;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import static android.support.v4.content.PermissionChecker.checkCallingOrSelfPermission;

/* newSandwich is the activity that allows the user to create a new recipe */
public class newSandwich extends Fragment {

    EditText name; //name of the sandwich displayed (editText)
    ListView ingredients; //list of ingredients (qty, unit, name)
    ImageView sandwichPicture; //sandwich picture from gallery or camera
    Spinner qtySpinner; //spinner to pick quantity of an ingredient to add
    Spinner fractionSpinner; //spinner to pick fractional quantity of an ingredient to add
    Spinner measurementSpinner; //spinner to pick measurement of an ingredient to add
    EditText ingredientName; //editText to enter ingredient name to add to list
    EditText addInstructions; //editText to enter instructions
    View rootView; //newSandwich is the rootView in this case
    ArrayList<Ingredients> ingredient_list; //list of ingredients
    String encodedImage;
    AppInfo appInfo;
    String logTag = "Debug";
    private static final int GALLERY = 1;
    private static final int CAMERA = 2;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.new_sandwich, container, false); //new_sandwich.xml corresponds to this class
        Button saveSandwich = (Button) rootView.findViewById(R.id.save_sandwich_button);
        appInfo = AppInfo.getInstance(getActivity());
        ingredient_list = new ArrayList<>(); //initialize ingredient_list as new arrayList

        init();
        saveSandwich.setOnClickListener( //listener for when "save sandwich" button is pressed, calls saveSandwich()
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveSandwich();
                    }
                }
        );
        return rootView;
    }

    //Initialize everything in the view
    private void init(){
        name = (EditText) rootView.findViewById(R.id.sandwich_name); //editText for adding sandwich name
        ingredients = (ListView) rootView.findViewById(R.id.ingredients_list); //listView for displaying ingredient list
        sandwichPicture = (ImageView) rootView.findViewById(R.id.sandwich_picture); //imageView for displaying the uploaded image
        ingredientName = (EditText) rootView.findViewById(R.id.ingredient_name); //editText for adding ingredient name
        addInstructions = (EditText) rootView.findViewById(R.id.instructions); //editText for adding instructions
        Button pickImage = (Button) rootView.findViewById(R.id.add_photo_button); //button for choosing an image
        final ingredientEditAdapter adapter = new ingredientEditAdapter(getActivity(), R.layout.layout_edit_ingredient, ingredient_list);
        Button addIngredientButton = (Button) rootView.findViewById(R.id.add_ingredient_button); // add ingredients to the list view


        ingredients.setAdapter(adapter);
        ingredients.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
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
                            Toast toast= Toast.makeText(getActivity(),toastMsg,Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }else{ //otherwise, we can add this ingredient to our list
                            String qty = (String) qtySpinner.getSelectedItem();
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
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getActivity());
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
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null){
            startActivityForResult(cameraIntent, CAMERA);
        }
    }

    //after the user has taken or chosen a picture from gallery. Sets the chosen image in the ImageView
    //on the screen
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) { //chosen from gallery
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), contentURI); //sets the file path of the image
                    sandwichPicture.setImageBitmap(photo); //set image in ImageView
                    encodedImage = turnBitMapToEncodedString(photo); //turns from bitMap to string
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_LONG).show();
                }
            }
        }else if (requestCode == CAMERA) { //taken from camera
            Bitmap imageTakenFromCamera = (Bitmap) data.getExtras().get("data");
            sandwichPicture.setImageBitmap(imageTakenFromCamera); //set image in ImageView from the bitmap data
            saveImage(imageTakenFromCamera); //calls the saveImage function to store the image taken from camera to phone
            encodedImage = turnBitMapToEncodedString(imageTakenFromCamera); //turns from bitMap to string so that it can be saved/called
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

    //Saves the image to the phone
    public String saveImage(Bitmap imageToSave){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageToSave.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File imageDirectory = new File(Environment.getExternalStorageDirectory() + "/DirName");
        if (!imageDirectory.exists()) {
            imageDirectory.mkdir();
        }
        try {
            File file = new File(imageDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
            file.createNewFile();
            FileOutputStream fileOutput = new FileOutputStream(file);
            fileOutput.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(getActivity(), new String[] {file.getPath()}, new String[]{"image/jpeg"}, null);
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
            res = checkCallingOrSelfPermission(getActivity(), perms);
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
                    Toast.makeText(getActivity(), "Storage Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //sets up the spinners for ingredient options
    public void setup_spinners(){
        qtySpinner = (Spinner) rootView.findViewById(R.id.qty_spinner);
        fractionSpinner = (Spinner) rootView.findViewById(R.id.frac_spinner);
        measurementSpinner = (Spinner) rootView.findViewById(R.id.measurement_spinner);

        //Spinner 1: Whole number quantity spinner
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity(), //values shown in spinner are "qty_array" in strings.xml
                R.array.qty_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //Specify the layout to use when the list of choices appears
        qtySpinner.setAdapter(adapter1); // Apply the adapter to the spinner

        //Spinner 2: Fractional quantity spinner
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(), //values shown in spinner are "frac_array" in strings.xml
                R.array.frac_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //Specify the layout to use when the list of choices appears
        fractionSpinner.setAdapter(adapter2); // Apply the adapter to the spinner

        //Spinner 3: Measurement spinner
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getActivity(), //values shown in spinner are "measurements_array" in strings.xml
                R.array.measurements_array, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //Specify the layout to use when the list of choices appears
        measurementSpinner.setAdapter(adapter3); // Apply the adapter to the spinner
    }

    //called when the user clicks the "save sandwich" button
    public void saveSandwich() {
        String sandwichName = name.getText().toString(); //get the name, store as string
        String instructions = addInstructions.getText().toString(); //get the instructions, store as string
        String imageId = encodedImage;
        ArrayList<Ingredients> ingredients = new ArrayList<>();

        for (Ingredients ingredient : ingredient_list) { //add from listView to array list
            ingredients.add(ingredient);
        }

        //if all values aren't filled out, prompt again
        if (sandwichName.isEmpty() || instructions.isEmpty() || ingredients.size() == 0 || sandwichPicture.getDrawable().getConstantState().equals
                (getResources().getDrawable(R.drawable.chooseimage).getConstantState())) {
            Toast.makeText(getActivity(), "Invalid recipe! Please fill out all fields.", Toast.LENGTH_LONG).show();
            return;
        }else { //otherwise, save the recipe
            Sandwich recipe = new Sandwich(sandwichName, ingredients, instructions, imageId); //create new sandwich with this name, ingredients, instructions and image
            appInfo.addSandwich(recipe);
            showSaveDialogue(recipe); //show dialog to ask if user wants to save to library
        }
    }

    //Displays dialog to ask if the user wants to save to library
    public void showSaveDialogue(final Sandwich recipe) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Recipe Added to List!");

        //ask user if they'd like to add to database
        alertDialog.setMessage(("Recipe saved to your list! Would you also like to add this recipe to the global database for all users to see?"))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { //user selects yes, add to database
                        saveToLibrary(recipe); //saves to database
                        Intent intent = new Intent(getActivity(), mainActivity.class); //close after saving
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        saveAsJSON();
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { //user selects no, only save locally & return home
                        Intent intent = new Intent(getActivity(), mainActivity.class);
                        saveAsJSON();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            } catch(Exception e){
                Log.e(logTag, "Error " + e.getStackTrace());
            }
        }

        SharedPreferences settings = getActivity().getSharedPreferences(mainActivity.MYPREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("sandwichesAsJSON", jArray.toString()); //saves JSON as string in sandwichesAsJSON
        editor.commit();
    }

    //save recipe to database
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
            Log.d(logTag, "Something went wrong");
        }

        //backend method to add to database
        JsonObjectRequest jsobj = new JsonObjectRequest(
                "https://sandwichstory-172520.appspot.com/api/add_sandwich", obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(logTag, "Received: " + response.toString());
                        try {
                            Log.d(logTag, "Saved properly to database");
                        } catch (Exception e) {
                            Log.d(logTag, "Not saved properly to database");
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