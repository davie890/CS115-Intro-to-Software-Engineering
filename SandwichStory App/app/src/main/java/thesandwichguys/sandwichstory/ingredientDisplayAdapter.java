package thesandwichguys.sandwichstory;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

/* Adapter for displaying ingredients on the sandwichInfo page */
public class ingredientDisplayAdapter extends ArrayAdapter<Ingredients> {
    private Context mContext;
    int mResource;
    ArrayList<Ingredients> ingredients;

    public ingredientDisplayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Ingredients> objects) {
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
        String measurement = getItem(position).getUnit();
        TextView name_tv = (TextView) view.findViewById(R.id.name_tv);
        TextView qty_tv = (TextView) view.findViewById(R.id.qty_tv);
        TextView measurement_tv = (TextView) view.findViewById(R.id.measurement_tv);

        name_tv.setText(name); //set name to textView
        qty_tv.setText(qty); //set quantity to textView
        measurement_tv.setText(measurement); //set measurement to textView

        return view;
    }
}