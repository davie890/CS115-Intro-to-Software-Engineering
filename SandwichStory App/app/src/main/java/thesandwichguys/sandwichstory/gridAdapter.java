package thesandwichguys.sandwichstory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/* translator between xml and the data to be displayed in the GridView on all pages displaying recipes */
public class gridAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<Sandwich> data; //data contained in the GridView (sandwiches)
    AppInfo appInfo;
    private LayoutInflater inflater;

    public gridAdapter(Context context, ArrayList<Sandwich> data){
        this.context = context;
        this.data = data;
        appInfo = AppInfo.getInstance(this.context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.indexOf(getItem(position));
    }

    //Add another grid item
    public void add(Sandwich item){
        data.add(item);
        notifyDataSetChanged();
    }

    class ViewHolder{
        ImageView icon;
        TextView name;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        View gridView = convertView;
        ViewHolder holder;

        if(gridView == null){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.gridview_layout,null);

            //Convert code data to items to View types that display on screen
            holder = new ViewHolder();
            holder.icon = (ImageView) gridView.findViewById(R.id.sandwich_picture);
            holder.name = (TextView) gridView.findViewById(R.id.sandwich_name);
            gridView.setTag(holder);
        }else{
            holder = (ViewHolder) gridView.getTag(); //Initialize holder to the values in the gridview's ViewHolder tag
        }

        Sandwich gridItem = data.get(position);
        String encodedImage = gridItem.getImageId();

        switch(encodedImage){ //set specific images for "classic" recipes, a basic one for default and the uploaded photo for created recipes
            case "": holder.icon.setImageResource(R.drawable.basic_sandwich);
                break;
            case "library": holder.icon.setImageResource(R.drawable.basic_sandwich);
                break;
            case "0": holder.icon.setImageResource(R.drawable.the_celine);
                break;
            case "1": holder.icon.setImageResource(R.drawable.the_davie);
                break;
            case "2": holder.icon.setImageResource(R.drawable.the_alex);
                break;
            case "3": holder.icon.setImageResource(R.drawable.the_matthew);
                break;
            case "4": holder.icon.setImageResource(R.drawable.the_david);
                break;
            case "5": holder.icon.setImageResource(R.drawable.the_chris);
                break;
            case "6": holder.icon.setImageResource(R.drawable.turkey_panini);
                break;
            case "7": holder.icon.setImageResource(R.drawable.double_double);
                break;
            default: holder.icon.setImageBitmap(turnEncodedStringToBitmap(encodedImage)); //otherwise, set image to the one saved
                break;
        }

        holder.name.setText(gridItem.getSandwichName()); //set sandwich name
        return gridView;
    }

    //Converts image string to bitmap when loading image to an imageView
    public Bitmap turnEncodedStringToBitmap(String encodedImage){
        byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap photo = BitmapFactory.decodeByteArray(b, 0, b.length);
        return photo;
    }
}