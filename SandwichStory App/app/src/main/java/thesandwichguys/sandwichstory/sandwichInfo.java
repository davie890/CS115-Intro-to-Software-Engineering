package thesandwichguys.sandwichstory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import java.util.ArrayList;

/* sandwichInfo is the page that is shown when the user clicks on a
* recipe to see more information. Shows all the details and allows
* the user to edit, delete or save a recipe, depending on where the
* user is coming from */
public class sandwichInfo extends Activity {
    AppInfo appInfo;
    String name; //name of the sandwich
    String instructions; //instructions for the sandwich
    ArrayList<Ingredients> ingredients_list = new ArrayList<>(); //list of ingredients for the sandwich
    Sandwich displayedSandwich;
    int index;
    int devIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //no title bar at the top
        setContentView(R.layout.sandwich_info); //sandwich_info.xml corresponds to this class
        appInfo = AppInfo.getInstance(this);
    }

    //sets up everything on the page. onResume is called when onCreate is, so sets up the page initially
    protected void onResume() {
        super.onResume();
        TextView sandwichName = (TextView) findViewById(R.id.sandwich_name); //textView that displays sandwich name at top
        ImageView sandwichPicture = (ImageView) findViewById(R.id.sandwich_picture); //imageView that displays the image saved
        TextView sandwichMessage = (TextView) findViewById(R.id.instructions); //textView that displays the instructions
        ListView ingredients = (ListView) findViewById(R.id.ingredients_list); //listView that displays the list of ingredients
        Button editButton = (Button) findViewById(R.id.edit_button); //button that allows user to edit recipe
        Button deleteButton = (Button) findViewById(R.id.delete_button); //button that allows user to delete recipe
        Button saveButton = (Button) findViewById(R.id.save_button); //button that allows user to save recipe to their list
        Bundle extras = getIntent().getExtras();
        index = extras.getInt("index");

        ingredients_list.clear();
        saveButton.setVisibility(View.GONE); //initially, save button should not be visible

        if(index < 0){ //if this is a "classic/developer" recipe
            devIndex = extras.getInt("devIndex");
            displayedSandwich = appInfo.developerSandwich.get(devIndex); //determine which sandwich item we are actually displaying
            ingredients_list = displayedSandwich.getIngredientList(); //get the ingredient list

            switch(devIndex){ //based on the order in SandwichList.txt, hardcoded images to be displayed for each (default is basic image)
                case 0: sandwichPicture.setImageResource(R.drawable.the_celine);
                    break;
                case 1: sandwichPicture.setImageResource(R.drawable.the_davie);
                    break;
                case 2: sandwichPicture.setImageResource(R.drawable.the_alex);
                    break;
                case 3: sandwichPicture.setImageResource(R.drawable.the_matthew);
                    break;
                case 4: sandwichPicture.setImageResource(R.drawable.the_david);
                    break;
                case 5: sandwichPicture.setImageResource(R.drawable.the_chris);
                    break;
                case 6: sandwichPicture.setImageResource(R.drawable.turkey_panini);
                    break;
                case 7: sandwichPicture.setImageResource(R.drawable.double_double);
                    break;
                default: sandwichPicture.setImageResource(R.drawable.basic_sandwich);
                    break;
            }
            editButton.setVisibility(View.GONE); //for these recipes, all 3 buttons are hidden
            deleteButton.setVisibility(View.GONE);
        }else if(extras.containsKey("fromLib") && extras.getBoolean("fromLib")){ //if this is a recipe displayed in library
            displayedSandwich = appInfo.sandwichFromLibrary; //sandwichFromLibrary gets set in library.java
            ingredients_list = displayedSandwich.getIngredientList(); //get ingredients
            editButton.setVisibility(View.GONE); //hide edit and delete
            deleteButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE); //but give option to save
            sandwichPicture.setImageResource(R.drawable.basic_sandwich); //display basic sandwich image
        }else{ //otherwise, must be a recipe on "my list"
            displayedSandwich = appInfo.savedSandwich.get(index); //determine which sandwich item we are actually displaying
            if(displayedSandwich.getImageId().equals("library")){ //this is a sandwich saved from library to "my list"
                sandwichPicture.setImageResource(R.drawable.basic_sandwich); //display basic sandwich image
                appInfo.sandwichToEdit = displayedSandwich; //set "sandwichToEdit" so we can edit this recipe
                ingredients_list = displayedSandwich.getIngredientList();
            }else { //this is a recipe the user has created and saved on their own
                appInfo.sandwichToEdit = displayedSandwich;
                ingredients_list = displayedSandwich.getIngredientList();
                sandwichPicture.setImageBitmap(turnEncodedStringToBitmap(displayedSandwich.getImageId())); //display the image they uploaded
            }
        }
            ingredients.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) { //allows us to scroll through the ingredient list
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });

            sandwichMessage.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside EditText (instructions)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true); //Allows us to scroll through the instructions
                    return false;
                }
            });

            ingredientDisplayAdapter adapter = new ingredientDisplayAdapter(this, R.layout.layout_display_ingredient, ingredients_list);
            ingredients.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            name = displayedSandwich.getSandwichName();
            instructions = displayedSandwich.getInstructions();
            sandwichName.setText(name);
            sandwichMessage.setText(instructions);
    }

    //When user clicks "save" on a recipe in the library, saves the recipe to their list
    public void onClickSave(View v){
        Sandwich recipe = new Sandwich(name, ingredients_list,instructions, "library");
        appInfo.addSandwich(recipe);
        Toast.makeText(this,"Sandwich Saved!", Toast.LENGTH_LONG).show();
        finish();
    }

    //When user clicks "edit" on a recipe in their list, opens up page to edit
    public void onClickEdit(View v) {
        Intent intent = new Intent(this, editRecipe.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("index", index); //pass index of recipe they want to edit
        startActivity(intent);
    }

    //When the user clicks "delete" on a recipe in their list, removes from "my list"
    public void onClickDelete(View v) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(sandwichInfo.this);
        alertDialog.setTitle("Delete recipe?");

        alertDialog.setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { //user says yes, delete recipe
                        appInfo.savedSandwich.remove(index);
                        Intent intent = new Intent(sandwichInfo.this, mainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        saveAsJSON();
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { //user says no, dismiss dialog
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    //converts from string to bitmap so that we can display the image
    public Bitmap turnEncodedStringToBitmap(String encodedImage){
        byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap photo = BitmapFactory.decodeByteArray(b, 0, b.length);
        return photo;
    }

    //saves recipe in JSON format to retrieve later
    public void saveAsJSON() {
        JSONArray jArray = new JSONArray();
        for (Sandwich savedRecipe : appInfo.savedSandwich) {
            try{
                jArray.put(savedRecipe.sandwichAsJSON);
            } catch (Exception e) {
                Log.e("test", "Error saving as json: " + e.getStackTrace());
            }
        }

        SharedPreferences settings = getSharedPreferences(mainActivity.MYPREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("sandwichesAsJSON", jArray.toString());
        editor.commit();
    }
}

