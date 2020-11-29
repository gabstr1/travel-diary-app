package com.example.traveldiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Collections;
import java.util.List;

import static android.app.ActivityOptions.makeSceneTransitionAnimation;

public class ListActivity extends AppCompatActivity implements RecyclerViewClickInterface{
    private RecyclerView myRecyclerView;
    private ListAdapter adapter;
    List<Item> dataList;
    RoomDB database;
    LinearLayoutManager linearLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        myRecyclerView = findViewById(R.id.listView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = RoomDB.getInstance(this);

        dataList = database.myDao().getAllItems();

        linearLayoutManager = new LinearLayoutManager(this);
        myRecyclerView.setLayoutManager(linearLayoutManager);

        Collections.sort(dataList, Item.byDate);

        adapter = new ListAdapter(ListActivity.this, dataList, this);
        myRecyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.list);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.list:
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext()
                                , HomeActivity.class));
                        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                        return true;
                    case R.id.map:
                        startActivity(new Intent(getApplicationContext()
                                ,MapsActivity.class));
                        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search_view);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemClick(int position, ImageView sharedImage) {
        Intent intent = new Intent(this, DisplayListItem.class);

        intent.putExtra("selected_item", dataList.get(position));
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(ListActivity.this, sharedImage, "journey");
        startActivity(intent, options.toBundle());

    }

    @Override
    public void onLongItemClick(final int position) {

        TextView textView = new TextView(this);
        textView.setText("WARNING!");
        textView.setPadding(70, 30, 20, 30);
        textView.setTextSize(20F);
        textView.setTextColor(Color.RED);

        AlertDialog.Builder alert = new AlertDialog.Builder(
                ListActivity.this);
        alert.setCustomTitle(textView);
        alert.setMessage("Are you sure you want to delete record?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Item item = dataList.get(position);
                database.myDao().delete(item);
                dataList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemChanged(position);
                Toast.makeText(getApplicationContext(), "Item deleted",
                        Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}