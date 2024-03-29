package com.example.traveldiary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.List;

public class DisplayListItem extends AppCompatActivity {
    //widgets
    TextView description, details, monthNameDay;
    ImageView photo, clockIcon;
    FloatingActionButton editButton, doneButton;
    TextInputLayout textInputLayout;
    TextInputEditText editText;
    //vars
    RoomDB database;
    Bitmap bitmap;
    String number, month, mainText, detailsText, photoPath;
    int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_list_item);

        Animation animRotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);

        database = RoomDB.getInstance(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clockIcon = findViewById(R.id.clockIcon);
        clockIcon.startAnimation(animRotate);

        textInputLayout = findViewById(R.id.textInput);
        editText = findViewById(R.id.editText);

        editButton = findViewById(R.id.editButton);
        doneButton = findViewById(R.id.doneButton);

        photo = findViewById(R.id.imageView);
        description = findViewById((R.id.itemDescription));
        details = findViewById((R.id.itemDetails));
        monthNameDay = findViewById((R.id.itemMonthName));

        if(getIntent().hasExtra("selected_item")) {
            Item item = getIntent().getParcelableExtra("selected_item");

            photoPath = item.getImage();
            number = item.getMonthDayNumber();
            month = item.getMonthName();
            mainText = item.getDescription();
            detailsText = item.getDetails();
            itemId = item.getItemId();

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(photoPath, bmOptions);

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.max(1, Math.min(photoW/700, photoH/700));

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
            photo.setImageBitmap(bitmap);
            description.setText(mainText);
            details.setText(detailsText);
            monthNameDay.setText(month + " " + number);
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);
                editText.setText(mainText);
                textInputLayout.setVisibility(View.VISIBLE);
                doneButton.setVisibility(View.VISIBLE);
                doneButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dddddd")));

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        doneButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8EA4D2")));
                        doneButton.setClickable(true);
                    }
                    @Override
                    public void afterTextChanged(Editable s) { }
                });

                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        database.myDao().update(itemId, editText.getText().toString());
                        Intent intent = new Intent(DisplayListItem.this, ListActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}