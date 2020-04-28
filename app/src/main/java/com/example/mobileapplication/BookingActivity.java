package com.example.mobileapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.graphics.Color.rgb;



//activity in which is started the tracking computation (both foreground and background) and is saved the track after
//the ending of the computation

public class BookingActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {



    Map<String, Object> book;


    Button b1;
    Button b2;

    private String email;
    private String token;



    private String car_id;
    private String car_longitude;
    private String car_latitude;
    private String car_address;
    private String car_state;

    private Object lock= new Object();
    private Handle handlers;

    private List<LatLng> points;

    private Boolean clicked=false;  //to complete the screenshot
    private Boolean request=false;  //to request  gps permission
    private Boolean service=false;
    private Boolean toast=false;
    private Boolean serviceRunning=false;    //to stop and start service in background
    private Boolean blockBackcommand=false;  //to block the back event when is execute the start event

    private GoogleMap map;
    private Polyline gpsTrack;
    private Boolean approved=false;
    private SupportMapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    private LatLng lastKnownLatLng;


    private Intent intent;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseStorage storage = FirebaseStorage.getInstance();




    Geocoder geocoder;
    List<Address> addresses = new ArrayList<Address>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.track);
        mapFragment.getMapAsync(this);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        Intent myIntent = getIntent(); // gets the previously created intent
        email = myIntent.getStringExtra("email");
        token = myIntent.getStringExtra("token");
        car_id = myIntent.getStringExtra("id");
        car_latitude = myIntent.getStringExtra("latitude");
        car_longitude = myIntent.getStringExtra("longitude");



        b1 = findViewById(R.id.start);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.start:


                        LocationManager lm = (LocationManager)BookingActivity.this.getSystemService(Context.LOCATION_SERVICE);
                        boolean gps_enabled = false;
                        boolean network_enabled = false;

                        try {
                            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        } catch(Exception ex) {}

                        try {
                            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                        } catch(Exception ex) {}

                        if(!gps_enabled && !network_enabled) {
                            // notify user
                            new AlertDialog.Builder(BookingActivity.this)
                                    .setMessage("Gps not enabled")
                                    .setPositiveButton("open location settings", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                            BookingActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                    }).setNegativeButton("cancel",null).show();
                             break;
                        }



                        start();
                        break;
                }


            }
        });

        b2 = findViewById(R.id.end);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.end:
                        end();
                        break;
                }


            }
        });



        b2.setEnabled(false);



        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        Points dene = (Points)intent.getSerializableExtra("points");
                        points=dene.getPoint();
                        gpsTrack.setPoints(points);



                    }
                }, new IntentFilter(LocationMonitoringService.ACTION_LOCATION_BROADCAST)
        );



    }


    public void start(){


        handlers= new Handle();
        handlers.execute();



        geocoder = new Geocoder(this, Locale.getDefault());


        Thread thread = new Thread() {
            @Override
            public void run() {

                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                Car c = new Car(Integer.parseInt(car_id), car_longitude, car_latitude, "true",car_address,car_state);

                Booking b = new Booking(c, email, new Date(System.currentTimeMillis()).toString(), "");

                book = new HashMap<>();
                book.put("email", b.getEmail());
                book.put("car", b.getCar());
                book.put("start_date", b.getStart_date());
                book.put("end_date", "");


                Map<String, Object> car = new HashMap<>();
                car.put("id", Integer.parseInt(car_id));

                car.put("longitude", car_longitude);
                car.put("latitude", car_latitude);
                car.put("busy", "true");


                db.collection("Bookings").document()
                        .set(book)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e("BookingActivity", "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("BookingActivity", "Error writing document", e);
                            }
                        });

                db.collection("Cars").document(car_id)
                        .set(car)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e("BookingActivity", "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("BookingActivity", "Error writing document", e);
                            }
                        });


                service = true;
                blockBackcommand=true;

            }
        };
        thread.start();


        b2.setEnabled(true);
        b1.setEnabled(false);


        new Thread(new Runnable() {
            public void run(){


                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                intent = new Intent(getApplicationContext(), LocationMonitoringService.class);
                startService(intent);
                serviceRunning=true;
            }
        }).start();

    }





    public void end(){

        b2.setEnabled(false);

        clicked=true;
        service=false;
        blockBackcommand=false;

        stopService(intent);

        zoomRoute(map, points);

    }



    public void completeEx(){

        Double l1=lastKnownLatLng.latitude;
        Double l2=lastKnownLatLng.longitude;
        String coordl1 = l1.toString();
        String coordl2 = l2.toString();

        Map<String, Object> car = new HashMap<>();
        car.put("id", Integer.parseInt(car_id));

        car.put("longitude", coordl2);
        car.put("latitude", coordl1);
        car.put("busy", "false");


        try {
            addresses=geocoder.getFromLocation(Double.parseDouble(coordl1), Double.parseDouble(coordl2), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            car.put("address",addresses.get(0).getAddressLine(0));
            car.put("state",addresses.get(0).getAdminArea());

        } catch (IOException e) {
            e.printStackTrace();
        }


        db.collection("Cars").document(car_id)
                .set(car)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("BookingActivity", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("BookingActivity", "Error writing document", e);
                    }
                });


        book.put("end_date",new Date(System.currentTimeMillis()).toString());

        db.collection("Bookings").whereEqualTo("email",email)
                .whereEqualTo("start_date",book.get("start_date"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                db.collection("Bookings").document(document.getId())
                                        .set(book)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.e("BookingActivity", "Booking successfully rewritten!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("BookingActivity", "Error writing document", e);
                                            }
                                        });

                            }

                        } else {
                            Log.w("BookingActivities", "Error getting documents.", task.getException());
                        }
                    }
                });


        captureScreen();


        Context context = getApplicationContext();
        CharSequence text = "Activity completed";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        View view = toast.getView();
        view.getBackground().setColorFilter(rgb(100,255,91), PorterDuff.Mode.SRC_IN);

        toast.show();

        clicked=false;

        points=new ArrayList<LatLng>();

    }









    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng car_pos = new LatLng(Double.parseDouble(car_latitude), Double.parseDouble(car_longitude));
        map.moveCamera(CameraUpdateFactory.newLatLng(car_pos));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(car_pos, 15));

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.CYAN);
        polylineOptions.width(5);
        gpsTrack = map.addPolyline(polylineOptions);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        map.setMyLocationEnabled(true);

    }


    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (!approved) googleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onDestroy()
    {
        // Unregistered or disconnect what you need to
        if (serviceRunning) stopService(intent);

        googleApiClient.disconnect();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!approved) stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }
        request=false;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("BookingActivity", "Connection suspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (!clicked)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0f));
        //updateTrack();
    }



    public static final int MY_PERMISSION1_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSION2_REQUEST_LOCATION = 98;



    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if (!request) {
            request=true;
            boolean permissionAccessCoarseLocationApproved =
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;

            if (permissionAccessCoarseLocationApproved) {
                boolean backgroundLocationPermissionApproved =
                        ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                == PackageManager.PERMISSION_GRANTED;

                if (backgroundLocationPermissionApproved) {
                    // App can access location both in the foreground and in the background.
                    // Start your service that doesn't have a foreground service type
                    // defined.
                    approved = true;

                } else {
                    // App can only access location in the foreground. Display a dialog
                    // warning the user that your app must have all-the-time access to
                    // location in order to function properly. Then, request background
                    // location.
                    approved=false;

                    ActivityCompat.requestPermissions(this, new String[] {
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        MY_PERMISSION2_REQUEST_LOCATION );
                }
            } else {
                approved=false;
                // App doesn't have access to the device's location at all. Make full request
                // for permission.

                CharSequence text = "warning: at least the foreground location permission must be selected in order to work properly";
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();



                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        },
                        MY_PERMISSION1_REQUEST_LOCATION);



                return;
            }
        }


        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION2_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        approved=true;
                        Toast.makeText(this, "permissions background approved", Toast.LENGTH_LONG).show();

                    }else{
                        Toast.makeText(this, "permission background denied", Toast.LENGTH_LONG).show();

                    }
                    if ( ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(this, "permissions foreground approved", Toast.LENGTH_LONG).show();

                    } else {


                        Toast.makeText(this, "permissions foreground denied", Toast.LENGTH_LONG).show();
                    }



                }else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;

            }
        }

    }





    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }





    private void captureScreen() {



        StorageReference storageRef = storage.getReferenceFromUrl("gs://mobileapplicatio-1581695012089.appspot.com");


        String name = "map-" + email + "-" + book.get("end_date") + ".png";


        StorageReference mountainsRef = storageRef.child(name);

        StorageReference mountainImagesRef = storageRef.child("images/" + name);


        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                bitmap = snapshot;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = mountainsRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Log.e("BookingActivity", "Image successfully uploaded!");
                        gpsTrack.remove();//////////////////////////
                        //googleApiClient.disconnect();


                    }
                });

            }
        };


        map.snapshot(callback);






    }

    //zoom in order to capture all the track points on the screen
    public void zoomRoute(GoogleMap googleMap, List<LatLng> lstLatLngRoute) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 100;
        LatLngBounds latLngBounds = boundsBuilder.build();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

                completeEx();
            }

            @Override
            public void onCancel() {

            }
        });
    }



    public class Handle extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] objects) {

            DocumentReference docRef = db.collection("Cars").document(car_id);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("BookingActivity", "DocumentSnapshot data: " + document.getData());

                            Car c= document.toObject(Car.class);
                            car_address=c.getAddress();
                            car_state=c.getState();

                            if (c.getBusy().equals("false")){

                                synchronized (lock) {


                                    lock.notifyAll();

                                    Log.e("SelectCarFragment", "è ANCORA LIBERO");

                                }


                            }else{


                                Log.e("BookingActivity", "car already selectes");



                                finish();

                            }


                        } else {
                            Log.d("BookingActivity", "No such document");
                        }
                    } else {
                        Log.d("BookingActivity", "get failed with ", task.getException());
                    }
                }
            });






            return null;
        }
    }


    @Override
    public void onBackPressed() {
        if (!blockBackcommand) {
            super.onBackPressed();
        }
    }




}
