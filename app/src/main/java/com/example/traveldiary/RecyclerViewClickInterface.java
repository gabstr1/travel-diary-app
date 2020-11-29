package com.example.traveldiary;

import android.view.View;
import android.widget.ImageView;

public interface RecyclerViewClickInterface {

    void onItemClick(int position, ImageView photo);

    void onLongItemClick(int position);
}
