package thesandwichguys.sandwichstory;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import java.util.ArrayList;
import android.widget.SearchView;
import android.widget.ImageView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONObject;

/* library displays the sandwiches saved in the database in a gridView */
public class library extends Fragment {

    GridView gridView; //gridView for library sandwiches
    gridAdapter adapter; //adapter for library sandwiches
    ArrayList<Sandwich> listOfSandwiches; //list of sandwiches displayed
    ArrayList<Sandwich> searchSandwiches; //list of sandwiches returned from search
    GridView searchGridView; //gridView for search result sandwiches
    gridAdapter searchAdapter; //adapter for search sandwiches
    View rootView; //library is the rootView, needed for fragment
    SearchView searchView; //search view for searching phrases
    String logTag = "Debug";
    AppInfo appInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.library, container, false); //library.xml corresponds to this class
        listOfSandwiches = new ArrayList<>();
        searchView = (SearchView) rootView.findViewById(R.id.searchView);
        int searchCloseButtonId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = (ImageView) this.searchView.findViewById(searchCloseButtonId);
        gridView = (GridView) rootView.findViewById(R.id.gridView);
        appInfo = AppInfo.getInstance(getActivity());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { //when user presses enter to search a phrase
                search(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText){
                return false;
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //when user presses 'x' to close the search bar
                searchView.onActionViewCollapsed();
                searchView.setQuery("", false);
                searchView.clearFocus();

                if (searchGridView != null){ //make gridView visible after search view is closed
                    searchSandwiches.clear();
                    searchAdapter.notifyDataSetChanged();
                    searchGridView.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);
                }
            }
        });

        adapter = new gridAdapter(getActivity(), listOfSandwiches);
        gridView.setAdapter(adapter); //set gridView adapter
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //when user selects an item, opens up sandwichInfo page for that specific recipe
                appInfo.sandwichFromLibrary = listOfSandwiches.get(position); //store sandwich to be displayed in appInfo
                Intent nextScreen = new Intent(getActivity(), sandwichInfo.class);
                nextScreen.putExtra("index", position); //pass index so sandwichInfo knows which info to display
                nextScreen.putExtra("fromLib", true); //So sandwichInfo knows to display a sandwich from library (hide edit and delete buttons)
                startActivity(nextScreen);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecipes("get_user_recipes", listOfSandwiches, adapter); //load sandwiches from backend into gridView
    }

    //Call database to perform query
    public void search(String q) {
        JSONObject obj = new JSONObject();

        try{
            obj.put("q", q);
        } catch (Exception e) {
            Log.d(logTag, "Problem putting query in POST");
        }

        //initialize gridView if it is null
        if (searchGridView == null) {
            searchSandwiches = new ArrayList<>();
            searchGridView = (GridView) getActivity().findViewById(R.id.searchGridView);
            searchAdapter = new gridAdapter(getActivity(), searchSandwiches);

            searchGridView.setAdapter(searchAdapter);
            searchGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //when user selects an item, opens up the info page for that specific recipe
                    appInfo.sandwichFromLibrary = searchSandwiches.get(position);
                    Intent nextScreen = new Intent(getActivity(), sandwichInfo.class);
                    nextScreen.putExtra("index", position); //pass index so sandwichInfo knows which info to display
                    nextScreen.putExtra("fromLib", true); //So sandwichInfo knows to display a sandwich from library (hide edit and delete buttons)
                    startActivity(nextScreen);
                }
            });
        }

        JsonObjectRequest jsobj = new JsonObjectRequest(
                "https://sandwichstory-172520.appspot.com/api/search", obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(logTag, "Received: " + response.toString());
                        try{
                            gridView.setVisibility(View.GONE); //hide regular gridView
                            searchGridView.setVisibility(View.VISIBLE); //show search gridView
                            JSONArray receivedList = response.getJSONArray("results"); //load search results into the arrayList

                            decodeJSON(receivedList, searchSandwiches, searchAdapter); //decode the search results
                        }catch (Exception e){
                            Log.d(logTag, "Issue with search response" + e.getStackTrace());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(logTag, "Error");
            }
        });
        appInfo.queue.add(jsobj); //volley request queue
    }

    //loadRecipes from backend to gridView
    public void loadRecipes(String apiMethod, final ArrayList<Sandwich> sandwichArrayList, final gridAdapter adapt){

        String url = "https://sandwichstory-172520.appspot.com/api/" + apiMethod;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(logTag, "Received: " + response.toString());

                        try{
                            JSONArray receivedList = response.getJSONArray("results"); //load into arrayList

                            decodeJSON(receivedList, sandwichArrayList, adapt); //update
                        }catch(Exception e){
                            Log.d(logTag, ""+ e.getStackTrace());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Dismiss progress bar and let user know there was an issue connecting to server
                        Log.d(logTag, error.toString());
                    }
                });

        appInfo.queue.add(jsObjRequest);
    }

    //parses JSON info received from server
    public void decodeJSON(JSONArray jArray, ArrayList<Sandwich> sandwichArrayList, gridAdapter adapt){
        try{
            sandwichArrayList.clear(); //clear current list to copy new sandwiches into it
            for(int i = 0; i < jArray.length(); i++){
                JSONObject jsonSandwichRecipe = jArray.getJSONObject(i);
                String name = jsonSandwichRecipe.getString("name"); //get name
                JSONArray jsonIngredients = jsonSandwichRecipe.getJSONArray("ingredients"); //get ingredient list
                ArrayList<Ingredients> ingredients = new ArrayList<>();
                String msg = jsonSandwichRecipe.getString("msg"); //get instructions
                String imgId = ""; //assign blank image for now since we weren't able to implement this

                //add each component of json ingredient list to sandwich ingredient list
                for(int j = 0; j < jsonIngredients.length(); j++){
                    JSONObject jsonIngredientComponents = jsonIngredients.getJSONObject(j);
                    String ingName = jsonIngredientComponents.getString("name");
                    String qty = jsonIngredientComponents.getString("qty");
                    String unit = jsonIngredientComponents.getString("measure");
                    ingredients.add(new Ingredients(ingName, qty, unit ));
                }
                sandwichArrayList.add(new Sandwich(name, ingredients, msg, imgId)); //add this to list
            }
        }catch(Exception e) {
            Log.d(logTag, "JSON loading failed");
        }
        adapt.notifyDataSetChanged(); //notify adapter to update data
    }
}