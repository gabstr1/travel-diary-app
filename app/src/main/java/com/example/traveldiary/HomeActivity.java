package com.example.traveldiary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQ_CODE = 1;
    public static final int GALLERY_REQ_CODE = 1000;
    public static final int GALLERY_PERM_CODE = 1001;

    private ImageButton cameraButton, galleryButton;
    BottomNavigationView bottomNavigationView;
    String currentTime, month, monthDay, currentPhotoPath, dateString, country, district, street;
    TextView textView;
    Date d;
    private Context context = this;

    FusedLocationProviderClient fusedLocationProviderClient;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        zoomIn.reset();

        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);

        textView = findViewById(R.id.textView2);
        textView.startAnimation(zoomIn);

        cameraButton = findViewById(R.id.cameraButton);
        cameraButton.startAnimation(zoomIn);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                askCameraPermission();
            }
        });

        galleryButton = findViewById(R.id.galleryButton);
        galleryButton.startAnimation(zoomIn);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.list:
                        startActivity(new Intent(getApplicationContext()
                                , ListActivity.class));
                        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                        return true;
                    case R.id.map:
                        startActivity(new Intent(getApplicationContext()
                                , MapsActivity.class));
                        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                        return true;
                    case R.id.home:
                        return true;
                }
                return false;
            }
        });
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

    /**
     * Write Location information to image.
     *
     * @param : image absolute path
     * @return : location information
     */
    public void MarkGeoTagImage(String imagePath, Location location) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, String.valueOf(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, String.valueOf(location.getLongitude()));

            Log.i("GEOTAG", "geotags latitude: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));

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

    private void getLocation() {

        //Check for permissions
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //If permission is granted
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    //Initialize location
                    location = task.getResult();
                    Log.i("LOCATION", " location: " + location);
                    if (location != null) {
                        try {
                            //Initialize geoCoder
                            Geocoder geocoder = new Geocoder(HomeActivity.this, Locale.US);
                            //Initialize address list
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1);

                            Address address = addresses.get(0);

                            country = address.getCountryName();
                            district = address.getAdminArea();
                            street = address.getThoroughfare();

                            Log.i("ADDRESS", "Country: " + country);
                            Log.i("ADDRESS", "district: " + address.getAdminArea());
                            Log.i("ADDRESS", "street: " + address.getThoroughfare());

                            //MarkGeoTagImage(currentPhotoPath, location);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            //Ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = new Intent(this, AddImage.class);

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