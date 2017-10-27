package thesandwichguys.sandwichstory;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

/* Adapter for displaying ingredients on the editRecipe page */
public class ingredientEditAdapter extends ArrayAdapter<Ingredients>{
    private Context mContext;
    int mResource;
    ArrayList<Ingredients> ingredients;

    public ingredientEditAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Ingredients> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.ingredients = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LinearLayout view;

        if (convertView == null) {
            view = new LinearLayout(getContext());
            LayoutInflater vi = (LayoutInflater)
                    getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi.inflate(mResource,  view, true);
        } else {
            view = (LinearLayout) convertView;
        }

        String name = getItem(position).getIngredientName();
        String qty = getItem(position).getQty();
        String unit = getItem(position).getUnit();
        Button deleteButton = (Button) view.findViewById(R.id.delete_button);
        TextView nameTextView = (TextView) view.findViewById(R.id.name_tv);
        TextView qtyTextView = (TextView) view.findViewById(R.id.qty_tv);
        TextView measurementTextView = (TextView) view.findViewById(R.id.measurement_tv);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //when delete button is clicked, removes that ingredient from the list view
                String s = v.getTag().toString();
                int i = Integer.parseInt(s);
                i = i-1;
                ingredients.remove(i);
                notifyDataSetChanged();
            }
        });

        deleteButton.setTag(new Integer(position+1));
        nameTextView.setText(name); //set name to textView
        qtyTextView.setText(qty); //set quantity to textView
        measurementTextView.setText(unit); //set measurement to textView

        return view;
    }
}