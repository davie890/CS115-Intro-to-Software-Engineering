package thesandwichguys.sandwichstory;

import android.util.Log;

import org.json.JSONObject;

/* object used to hold individual ingredients. Composed of a name, quantity and measurement unit */
public class Ingredients{
    public String ingredientName;
    public String qty;
    public String unit;
    public JSONObject jsonIngredientComponents;
    String logTag = "Debug";

    public Ingredients(String name, String qty, String unit) {
        this.ingredientName = name;
        this.qty = qty;
        this.unit = unit;

        try{ //Create a JSON representation of this class for exchanges with database
            jsonIngredientComponents = new JSONObject();
            jsonIngredientComponents.put("qty", qty);
            jsonIngredientComponents.put("measure", unit);
            jsonIngredientComponents.put("name", name);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getQty() {
        return qty;
    }

    public String getUnit() {
        return unit;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setQty(String qty){
        this.qty = qty;
        try{
            jsonIngredientComponents.put("qty", qty);
        }catch(Exception e){
            Log.d(logTag, "Error");
        }
    }

    public void setUnit(String unit){
        this.unit = unit;
        try{
            jsonIngredientComponents.put("measure", unit);
        }catch(Exception e){
            Log.d(logTag, "Error");
        }
    }

    public void setIngredientName(String ingredientName){
        this.ingredientName = ingredientName;
        try{
            jsonIngredientComponents.put("name", ingredientName);
        }catch(Exception e){
            Log.d(logTag, "Error");
        }
    }

    public JSONObject getJsonIngredient() {
        return jsonIngredientComponents;
    }
}
