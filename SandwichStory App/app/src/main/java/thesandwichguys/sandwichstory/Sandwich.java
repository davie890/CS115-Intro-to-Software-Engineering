package thesandwichguys.sandwichstory;

import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;
import java.util.ArrayList;

public class Sandwich {
    public ArrayList<Ingredients> ingredientList;
    private String name;
    private String msg;
    private String imageId;
    private boolean vegan = false;
    private boolean vegetarian = false;
    private boolean glutenFree = false;
    String logTag = "";
    public JSONObject sandwichAsJSON;

    public Sandwich(String name, ArrayList<Ingredients> ingredients, String msg, boolean vegan, boolean vege, boolean glutenFree){
        this.name = name;
        if(ingredients == null)
            ingredients = new ArrayList<>();
        this.ingredientList = ingredients;
        this.msg = msg;
        this.vegan = vegan;
        this.vegetarian = vege;
        this.glutenFree = glutenFree;
        sandwichAsJSON = new JSONObject();
        setSandwichesAsJSON();
    }

    //Constructor for if dietary restrictions are not implemented
    public Sandwich(String name, ArrayList<Ingredients> ingredients, String msg, String imageId){
        this.name = name;
        if(ingredients == null)
            ingredients = new ArrayList<>();
        this.ingredientList = ingredients;
        this.msg = msg;
        this.imageId = imageId;
        sandwichAsJSON = new JSONObject();
        setSandwichesAsJSON();
    }

    public Sandwich(String name, ArrayList<Ingredients> ingredients, String msg) {
        this.name = name;
        if(ingredients == null)
            ingredients = new ArrayList<>();
        this.ingredientList = ingredients;
        this.msg = msg;
        this.vegan = true;
        this.vegetarian = true;
        this.glutenFree = true;
        sandwichAsJSON = new JSONObject(); //intiailzes JSON OBJECT added 7/11/17
        //this.imageid = R.drawable.emptysmall;

        setSandwichesAsJSON2();
    }

    public Sandwich(String name, String msg) {
        this.name = name;
        this.msg = msg;
        this.vegan = true;
        this.vegetarian = true;
        this.glutenFree = true;
        sandwichAsJSON = new JSONObject(); //intiailzes JSON OBJECT added 7/11/17
        //this.imageid = R.drawable.emptysmall;

        setSandwichesAsJSON1();
    }

    public ArrayList<Ingredients> getIngredientList()
    {
        return ingredientList;
    }
    public String getSandwichName()
    {
        return name;
    }
    public String getInstructions()
    {
        return msg;
    }
    public String getImageId(){
        return imageId;
    }
    public Boolean getVegan()
    {
        return vegan;
    }
    public Boolean getVegetarian()
    {
        return vegetarian;
    }
    public Boolean getGlutenFree()
    {
        return glutenFree;
    }

    public void setSandwichesAsJSON(){
        try{
            JSONArray ingredientArray = new JSONArray();
            for (Ingredients ingredients : ingredientList){
                ingredientArray.put(ingredients.getJsonIngredient());
            }
            sandwichAsJSON.put("name", name);
            sandwichAsJSON.put("msg", msg);
            sandwichAsJSON.put("ingredients", ingredientArray);
            sandwichAsJSON.put("imgId", imageId);
        }catch (Exception e){
            Log.d(logTag, "Didn't set JSON properly in sandwich class");
        }
    }

    public void setSandwichesAsJSON1(){
        try{
            JSONArray ingredientArray = new JSONArray();
            sandwichAsJSON.put("name", name);
            sandwichAsJSON.put("msg", msg);
        }catch (Exception e){
            Log.d(logTag, "Didn't set JSON properly in sandwich class");
        }
    }

    public void setSandwichesAsJSON2(){
        try{
            JSONArray ingredientArray = new JSONArray();
            for (Ingredients ingredients : ingredientList){
                ingredientArray.put(ingredients.getJsonIngredient());
            }
            sandwichAsJSON.put("name", name);
            sandwichAsJSON.put("msg", msg);
            sandwichAsJSON.put("ingredients", ingredientArray);
            //sandwichAsJSON.put("imgId", imageId);
        }catch (Exception e){
            Log.d(logTag, "Didn't set JSON properly in sandwich class");
        }
    }

    public void setIngredientList(ArrayList<Ingredients> ingredientList){
        this.ingredientList.clear();
        for(Ingredients ingredients : ingredientList){
            this.ingredientList.add(ingredients);
        }
        setSandwichesAsJSON();
    }

    public void setSandwichName(String name){
        this.name = name;
        setSandwichesAsJSON();
    }

    public void setInstructions(String msg){
        this.msg = msg;
        setSandwichesAsJSON();
    }

    public void setImageId(String imageId){
        this.imageId = imageId;
        setSandwichesAsJSON();
    }
    public void setVegan(Boolean vegan)
    {
        this.vegan = vegan;
        setSandwichesAsJSON();
    }

    public void setVegetarian(Boolean vegetarian)
    {
        this.vegetarian = vegetarian;
        setSandwichesAsJSON();
    }
    public void setGlutenFree(Boolean glutenFree)
    {
        this.glutenFree = glutenFree;
        setSandwichesAsJSON();
    }
}
