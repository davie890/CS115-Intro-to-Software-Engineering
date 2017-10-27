package thesandwichguys.sandwichstory;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import java.util.ArrayList;

/* home is the first tab of app. Displays user created sandwiches
* and sandwich recipes saved from the library in a GridView */
public class home extends Fragment {

    GridView gridView; //GridView to display the sandwiches
    gridAdapter adapter; //adapter for GridView
    View rootView; //home is the rootView, needed for fragment
    ArrayList<Sandwich> listOfSandwiches;  //list to store the sandwich objects that need to be parsed
    AppInfo appInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.home, container, false); //home.xml corresponds to this class
        listOfSandwiches = new ArrayList<>(); //create new ArrayList
        appInfo = AppInfo.getInstance(getActivity());
        gridView = (GridView) rootView.findViewById(R.id.gridview);
        adapter = new gridAdapter(getActivity(), listOfSandwiches);

        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //when user selects an item, opens up sandwichInfo for that specific recipe
                Intent nextScreen = new Intent(getActivity(), sandwichInfo.class);
                nextScreen.putExtra("index", position); //sandwichInfo uses index to determine which sandwich to display
                startActivity(nextScreen);
            }
        });

        return rootView;
    }

    //when the user opens or reopens the page, gets recipes from appInfo to display in GridView
    @Override
    public void onResume() {
        super.onResume();
        listOfSandwiches.clear();
        for (Sandwich recipe : appInfo.savedSandwich) {
            listOfSandwiches.add(recipe);
        }
        adapter.notifyDataSetChanged();
    }
}
