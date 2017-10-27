package thesandwichguys.sandwichstory;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

/* presetRecipes is the second tab of the application. Parses the file "sandwichInfo.txt"
 * to load preset recipes into the app. */
public class presetRecipes extends Fragment {
    GridView gridView; //gridView that displays the recipes
    ArrayList<Sandwich> ListOfDevelopersSandwich; //list of sandwiches displayed
    AppInfo appInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.preset_recipes, container, false); //preset_recipes.xml corresponds to this class
        ListOfDevelopersSandwich = new ArrayList<>();
        ListOfDevelopersSandwich = readFile();
        appInfo = AppInfo.getInstance(getActivity());
        gridView = (GridView) rootView.findViewById(R.id.gridview);

        for(int i = 0 ; i<ListOfDevelopersSandwich.size() ; i++){ //add recipe to appInfo to save on device
            appInfo.addDevSandwich(ListOfDevelopersSandwich.get(i));
        }
        gridAdapter adapter = new gridAdapter(getActivity(), ListOfDevelopersSandwich);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //when the user clicks an item on the page, opens sandwichInfo page
                Intent nextScreen = new Intent(getActivity(), sandwichInfo.class);
                nextScreen.putExtra("index", -1); //pass index of -1 so we know we came from developer
                nextScreen.putExtra("devIndex", position); //pass index so we know which info to display
                startActivity(nextScreen);
            }
        });
        return rootView;
    }

    //reads in from "SandwichList.txt"
    public ArrayList<Sandwich> readFile(){
        ArrayList<Sandwich> ListOfSandwiches = new ArrayList<>(); //Create a new sandwich list to store the sandwich objects that need to be parsed
        try{
            InputStream fileInput = getActivity().getAssets().open("SandwichList.txt");
            Scanner fileScanner = new Scanner(fileInput);
            String lineText = "";
            String name = "";
            String qty = "";
            String unit = "";
            while(!lineText.equals("END")) {
                String sandwichName = fileScanner.nextLine(); //Get the first like of the file ie. the sandwich name
                //name, qty, unit
                String ingredientLine = "";
                ArrayList<Ingredients> ingredientList = new ArrayList<>();
                ingredientLine = fileScanner.nextLine(); //Gets the next line of the file
                while (!ingredientLine.equals("-")) { //Get the ingredient line from the file until delimeter is hit
                    String[] ingredientComponents = ingredientLine.split(","); // Split the ingredient line into qty,ratio,unit,name and store in array
                    qty = ingredientComponents[0]; //Breaks the ingredient components and stores it appropriately
                    unit = ingredientComponents[1];
                    name = ingredientComponents[2];
                    Ingredients currentIngredient = new Ingredients(name, qty, unit); //Creates the Ingredient object using the parsed data
                    ingredientList.add(currentIngredient); //Adds the ingredient to the ingredient list
                    ingredientLine = fileScanner.nextLine(); //Get the next ingredient line from the file
                }
                String cookingInstruction = fileScanner.nextLine(); //Gets the cooking instruction
                String picture = fileScanner.nextLine();
                ListOfSandwiches.add(new Sandwich(sandwichName, ingredientList, cookingInstruction, picture));
                lineText = fileScanner.nextLine();  //Should be # or END delimiter
            }
            fileInput.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return ListOfSandwiches;
    }
}