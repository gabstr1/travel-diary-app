package com.example.traveldiary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int GALLERY_REQ_CODE = 1000;
    public static final int GALLERY_PERM_CODE = 1001;

    //Widgets
    ImageButton cameraButton, galleryButton;
    BottomNavigationView bottomNavigationView;
    DrawerLayout drawerLayout;
    NavigationView drawerNavigationView;
    Toolbar toolbar;
    TextView textView;

    //Vars
    String currentTime, month, monthDay, currentPhotoPath, dateString;
    Date d;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*-------------Widgets-------------------------*/
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        textView = findViewById(R.id.textView2);
        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);
        toolbar = findViewById(R.id.toolbar);

        /*-------------Hooks-------------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerNavigationView = findViewById(R.id.nav_view);

        /*-------------Toolbar-------------------------*/
        setSupportActionBar(toolbar);

        /*-------------Navigation Drawer Meniu-------------------------*/
        drawerNavigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        drawerNavigationView.setNavigationItemSelectedListener(this);

        /*-------------Animations-------------------------*/
        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        zoomIn.reset();
        galleryButton.startAnimation(zoomIn);
        textView.startAnimation(zoomIn);
        cameraButton.startAnimation(zoomIn);

        /*-------------Click Listeners-------------------------*/
        cameraButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.list:
            case R.id.nav_all_posts:
                startActivity(new Intent(getApplicationContext()
                        , ListActivity.class));
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                return true;
            case R.id.map:
            case R.id.nav_places:
                startActivity(new Intent(getApplicationContext()
                        , MapsActivity.class));
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                return true;
            case R.id.home:
                return true;
            case R.id.nav_google_drive:
                startActivity(new Intent(getApplicationContext()
                        , GoogleLoginActivity.class));
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
        switch (v.getId()) {
            case R.id.cameraButton:
                v.startAnimation(animAlpha);
                askCameraPermission();
                break;
            case R.id.galleryButton:
                v.startAnimation(animAlpha);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        //request perm
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        //show popup
                        requestPermissions(permissions, GALLERY_PERM_CODE);
                    } else {
                        //permission already granted
                        pickImageFromGallery();
                    }
                } else {
                    //system is less than marshmallow
                    pickImageFromGallery();
                }
        }

    }

    private void pickImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQ_CODE);
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GALLERY_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Permission is required to open gallery", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        try {
            File imageFile = createImageFile();
            Uri imageUri = FileProvider.getUriForFile(HomeActivity.this, "com.example.traveldiary.android.fileprovider", imageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 1);
            } else {
                Log.e("PACKAGE_MANAGER", "PackageManager is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = new Intent(this, AddImageActivity.class);

        if (resultCode == RESULT_OK) {

            if (requestCode == GALLERY_REQ_CODE) {
                Uri imageUri = data.getData();
                currentPhotoPath = getRealPathFromURI(imageUri);
            }
            ExifInterface intf = null;
            try {
                intf = new ExifInterface(currentPhotoPath);
                dateString = intf.getAttribute(ExifInterface.TAG_DATETIME);
                Log.i("date", "ExIfInterface gives us date: " + dateString);
                String lat = intf.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                Log.i("LOCATION", "latitude: " + lat);
                //String city = intf.getAttribute(ExifInterface.TAG_GPS_LATITUDE);

                d = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(dateString);
                month = new SimpleDateFormat("MMMM", Locale.US).format(d);
                currentTime = new SimpleDateFormat("h:mm a EEEE", Locale.US).format(d);
                monthDay = new SimpleDateFormat("dd", Locale.US).format(d);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

            intent.putExtra("path", currentPhotoPath);
            intent.putExtra("currentTime", currentTime);
            intent.putExtra("dayOfMonth", monthDay);
            intent.putExtra("currentMonth", month);
            intent.putExtra("date", dateString);

            startActivity(intent);
        }
    }
}