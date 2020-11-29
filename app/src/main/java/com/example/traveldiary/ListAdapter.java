package com.example.traveldiary;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> implements Filterable {

    //Initialize variable
    private List<Item> items;
    private List<Item> itemsAll;
    private Activity context;
    private RoomDB database;
    private RecyclerViewClickInterface recyclerViewClickInterface;

    //create constructor
    public ListAdapter(Activity context, List<Item> items, RecyclerViewClickInterface recyclerViewClickInterface){
        this.context = context;
        this.items = items;
        this.itemsAll = new ArrayList<>(items);
        this.recyclerViewClickInterface = recyclerViewClickInterface;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Initialize view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Initialize list
        Item item = items.get(position);
        //Initialize database
        database = RoomDB.getInstance(context);
        //Set text on text view
        holder.description.setText(item.getDescription());
        holder.details.setText(item.getDetails());
        holder.monthName.setText(item.getMonthName());
        holder.dayNumber.setText(item.getMonthDayNumber());
        holder.date.setText(item.getDate());
        holder.photo.setImageBitmap(setPic(item));
    }

    private Bitmap setPic(Item item){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(item.getImage(), bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/150, photoH/150));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(item.getImage(), bmOptions);

        return bitmap;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        //run on background thread
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Item> filteredList = new ArrayList<>();

            String charString = charSequence.toString();

            if(charString.isEmpty()){
                filteredList.addAll(itemsAll);
            } else {
                for (Item item : itemsAll){
                    if(item.getDetails().toLowerCase().contains(charString.toLowerCase())
                            || item.getMonthName().toLowerCase().contains(charString.toLowerCase())
                            || item.getDescription().toLowerCase().contains(charString.toLowerCase())){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        //run on a ui thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            items.clear();
            items.addAll((Collection<? extends Item>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder{
        //Initialize variable
        TextView description, details, monthName, dayNumber, date;
        ImageView photo;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            //Assign variables
            description = itemView.findViewById(R.id.description);
            details = itemView.findViewById(R.id.details);
            monthName = itemView.findViewById(R.id.monthName);
            photo = itemView.findViewById(R.id.photo);
            dayNumber = itemView.findViewById(R.id.dayNumber);
            date = itemView.findViewById(R.id.date);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerViewClickInterface.onItemClick(getAdapterPosition(), photo);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    recyclerViewClickInterface.onLongItemClick(getAdapterPosition());
                    return true;
                }
            });
        }
    }
}
