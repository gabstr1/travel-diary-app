package com.example.traveldiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddImageActivity extends AppCompatActivity implements LocationListener {

    private Context context = this;
    //widgets
    ImageView imageView, clockIcon;
    TextView currentTimeText, monthDay, dateText, locationText, latitudeText, longitudeText;
    Bitmap bitmap;
    FloatingActionButton addButton;
    TextInputEditText descriptionText;
    Toolbar toolbar;

    //vars
    RoomDB database;
    Intent intent, listIntent;
    String currentTime, dayOfMonth, currentMonth, details, currentPhotoPath, date, country, district, street, streetNumber, city;
    Date d;
    LocationManager locationManager;
    ProgressDialog nDialog;
    Coordinates coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_image);

        nDialog = new ProgressDialog(this);

        nDialog.dismiss();

        Animation animRotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        clockIcon = findViewById(R.id.clockIcon48);
        clockIcon.startAnimation(animRotate);

        latitudeText = findViewById(R.id.latitudeText);
        longitudeText = findViewById(R.id.longitudeText);


        toolbar = findViewById(R.id.toolbar);
        imageView = findViewById(R.id.imageViewEdit);
        currentTimeText = findViewById(R.id.currentDateTime);
        monthDay = findViewById(R.id.monthDay);
        dateText = findViewById(R.id.date);
        descriptionText = findViewById(R.id.descriptionText);

        descriptionText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        locationText = findViewById(R.id.locationText);

        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(addButtonClick);

        database = RoomDB.getInstance(this);

        intent = getIntent();

        currentPhotoPath = intent.getStringExtra("path");
        currentTime = intent.getStringExtra("currentTime");
        dayOfMonth = intent.getStringExtra("dayOfMonth");
        currentMonth = intent.getStringExtra("currentMonth");
        date = intent.getStringExtra("date");

        try {
            d = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        imageView.setImageBitmap(bitmap);

        currentTimeText.setText(currentTime);
        monthDay.setText(currentMonth + " " + dayOfMonth);

        getLocation();
        setSupportActionBar(toolbar);

    }

    public void runListActivity(boolean flag) {

        double lat = Double.parseDouble(String.valueOf(latitudeText.getText()));
        double lng = Double.parseDouble(String.valueOf(longitudeText.getText()));

        coordinates = new Coordinates(lat, lng);

        descriptionText = findViewById(R.id.descriptionText);
        details = currentTimeText.getText().toString().trim() + " • " + locationText.getText().toString().trim();
        database.myDao().addItem(new Item(currentPhotoPath, descriptionText.getText().toString().trim(), details, currentMonth, dayOfMonth, date, coordinates));

        listIntent = new Intent(context, ListActivity.class);

        overridePendingTransition(0, R.anim.anim_scale_down);
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(true);
        nDialog.setTitle("Creating new post");
        nDialog.setMessage("Sending your data");
        nDialog.show();
        startActivity(listIntent);
    }

    View.OnClickListener addButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            descriptionText = findViewById(R.id.descriptionText);
            if (descriptionText.getText().toString().matches("")) {
                Toast.makeText(getApplicationContext(), "You need to add a description", Toast.LENGTH_LONG).show();
            } else {
                runListActivity(true);
            }
        }
    };

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, AddImageActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    @Override
    public void onLocationChanged(@NonNull final Location location) {
        Log.i("LOCATION", "onLocationChanged() : " + location.getLatitude() + "," + location.getLongitude());
        try {
            //Initialize geoCoder
            Geocoder geocoder = new Geocoder(AddImageActivity.this, Locale.US);
            //Initialize address list
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            Address address = addresses.get(0);

            country = address.getCountryName();
            district = address.getAdminArea();
            street = address.getThoroughfare();
            streetNumber = address.getFeatureName();
            city = address.getLocality();

            Log.i("ADDRESS", "Country: " + country);
            Log.i("ADDRESS", "district: " + address.getAdminArea());
            Log.i("ADDRESS", "locale: " + address.getThoroughfare());
            Log.i("ADDRESS", "getFeatureName : " + address.getFeatureName());
            Log.i("ADDRESS", "getLocality  : " + address.getLocality());
            Log.i("ADDRESS", "latitude  : " + location.getLatitude());
            Log.i("ADDRESS", "longitude  : " + location.getLongitude());

            String allLocationInfo = street + " " + streetNumber + ", " + city + ", " + district + ", " + country;
            locationText.setText(allLocationInfo);

            String lat = Double.toString(location.getLatitude());
            String lng = Double.toString(location.getLongitude());

            longitudeText.setText(lng);
            latitudeText.setText(lat);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
}