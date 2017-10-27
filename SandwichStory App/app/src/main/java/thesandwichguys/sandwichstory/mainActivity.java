package thesandwichguys.sandwichstory;

import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/* MainActivity contains the four fragments displayed as a tab page controller */
public class mainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    AppInfo appInfo;
    static final public String MYPREFS = "myprefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //activity_main.xml corresponds to this class
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager()); //Create adapter that will return a fragment for each section

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter); //Set up the ViewPager with the sections adapter

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //Set icons to the tabs
        tabLayout.getTabAt(0).setIcon(R.drawable.my_list);
        tabLayout.getTabAt(1).setIcon(R.drawable.preset);
        tabLayout.getTabAt(2).setIcon(R.drawable.create);
        tabLayout.getTabAt(3).setIcon(R.drawable.library);
        appInfo = AppInfo.getInstance(this);
        loadRecipes(); //load recipes into the app
    }

    //called whenever the activity is moved to the background
    public void onPause() {
        saveAsJSON();
        super.onPause();
    }

    //saves recipe in JSON format to retrieve later (if the user pauses the app)
    public void saveAsJSON(){
        JSONArray jArray = new JSONArray();
        SharedPreferences settings = getSharedPreferences(MYPREFS, 0);

        for(Sandwich savedRecipe : appInfo.savedSandwich){
            try{
                jArray.put(savedRecipe.sandwichAsJSON);
            }catch(Exception e){
                Log.e("test", "Error saving as JSON: " + e.getStackTrace());
            }
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("sandwichesAsJSON", jArray.toString());
        editor.commit();
    }

    //load the recipes to displayed in each activity
    public void loadRecipes() {
        SharedPreferences settings = getSharedPreferences(MYPREFS, 0);
        String sandwichesAsJSON = settings.getString("sandwichesAsJSON", null);

        if (sandwichesAsJSON == null) { //if there are no recipes saved
            Log.d("debug", "No sandwiches!");
            return;
        }

        try {
            JSONArray allSandwiches = new JSONArray(sandwichesAsJSON); //Creates a JSONArray for the sandwich Objects
            appInfo.savedSandwich.clear(); //Clear the array list before adding elements

            for (int i = 0; i < allSandwiches.length(); i++) { //Iterate through sandwiches in the JSON array
                JSONObject recipe = allSandwiches.getJSONObject(i); //get sandwich from the list
                String name = recipe.getString("name"); //get name of sandwich
                String instructions = recipe.getString("msg"); //get instructions
                String img = recipe.getString("imgId"); //get image

                JSONArray ingredientJSONList = recipe.getJSONArray("ingredients"); //Get the JSON list of ingredients
                ArrayList<Ingredients> ingredientArrayList = new ArrayList<>();

                //Convert ingredient from JSON to Ingredient Object
                for (int j = 0; j < ingredientJSONList.length(); j++) { //Iterate through ingredients in the JSON array
                    JSONObject ingredientComponents = ingredientJSONList.getJSONObject(j); //get ingredient from the list
                    String qty = ingredientComponents.getString("qty"); //get quantity
                    String unit = ingredientComponents.getString("measure"); //get unit
                    String ingredientName = ingredientComponents.getString("name"); //get name
                    ingredientArrayList.add(new Ingredients(ingredientName, qty, unit)); //adds to the arrayList of ingredients
                }
                appInfo.savedSandwich.add(new Sandwich(name, ingredientArrayList, instructions, img)); //finally, add this sandwich to appInfo
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //Assign an activity to each tab
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    home tab1 = new home();
                    return tab1;
                case 1:
                    presetRecipes tab2 = new presetRecipes();
                    return tab2;
                case 2:
                    newSandwich tab3 = new newSandwich();
                    return tab3;
                case 3:
                    library tab4 = new library();
                    return tab4;
                default:
                    return null;
            }
        }

        //Returns number of tabs in the tab page controller
        @Override
        public int getCount() {
            return 4;
        }

        //Displays labels under each icon in the tab page controller
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "My List";
                case 1:
                    return "Classic";
                case 2:
                    return "Create";
                case 3:
                    return "Global";
            }
            return null;
        }
    }
}
