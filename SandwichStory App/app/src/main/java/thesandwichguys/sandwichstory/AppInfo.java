package thesandwichguys.sandwichstory;

import android.content.Context;
import java.util.ArrayList;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/* AppInfo is used to store data while the app is open on the device.
Returns an instance of AppInfo to be used in all of the activities/classes. */

public class AppInfo{
    private static AppInfo instance = null;
    private Context my_context;

    protected AppInfo() {
    }

    public RequestQueue queue;
    public ArrayList<Sandwich> savedSandwich; //list of saved sandwiches
    public ArrayList<Sandwich> developerSandwich; //list of developer sandwiches
    public Sandwich sandwichFromLibrary; //Used when sandwich is received from library
    public Sandwich sandwichToEdit; //Used when sandwich is opened from library to edit
    public ArrayList<Ingredients> ingredientsToEdit; //list of ingredients in the sandwich that's being edited

    //creates instance when the app is opened
    public static AppInfo getInstance(Context context){
        if(instance == null){
            instance = new AppInfo();
            instance.my_context = context;
            instance.savedSandwich = new ArrayList<>();
            instance.developerSandwich = new ArrayList<>();
            instance.ingredientsToEdit = new ArrayList<>();
            instance.sandwichToEdit = null;
            instance.queue = Volley.newRequestQueue(context);
        }
        return instance;
    }

    //used to save a recipe to the device
    public void addSandwich(Sandwich sandwich){
        instance.savedSandwich.add(sandwich);
    }
    //used to save a recipe from .txt file to device
    public void addDevSandwich(Sandwich sandwich){
        instance.developerSandwich.add(sandwich);
    }
}
