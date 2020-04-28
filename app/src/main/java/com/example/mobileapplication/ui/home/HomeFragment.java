package com.example.mobileapplication.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobileapplication.BookingActivity;
import com.example.mobileapplication.Car;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mobileapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


//fragment in which is posssible to show the user position and the avaiable(blue) and not available(red) car in the areas

public class HomeFragment extends Fragment implements OnMapReadyCallback {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String email;
    private String token;
    private Boolean init=false;


    private List<Car> list=new ArrayList<Car>();
    private List<LatLng> lastloclist = new ArrayList<LatLng>();
    private List<Marker> curlocmarklist = new ArrayList<Marker>();

    private Object lock = new Object();



    FirebaseFirestore db = FirebaseFirestore.getInstance();

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;



    private FusedLocationProviderClient fusedLocationClient;



    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private HomeViewModel homeViewModel;

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            email = getArguments().getString(ARG_PARAM1);
            token = getArguments().getString(ARG_PARAM2);
        }


        Thread thread = new Thread() { //HO DOVUTO FARE COSI PERCHE CAPITAVA ALCUNE VOLTE CHE MAPS NON VENIVA CARICATO E DAVA ERRORE
            @Override
            public void run() {

                synchronized (lock) {

                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //retrieving the car list setting for each one a marker
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            db.collection("Cars").orderBy("id")//DOVREI FARE A INTERI PER FARE LE COSE BENE(10UNPROBLEMA)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Car c = document.toObject(Car.class);
                                                    list.add(c);


                                                    LatLng pos = new LatLng(Double.parseDouble(c.getLatitude()), Double.parseDouble(c.getLongitude()));

                                                    lastloclist.add(pos);

                                                    if (c.getBusy().equals("false")) {

                                                        curlocmarklist.add(mGoogleMap.addMarker(new MarkerOptions().position(pos).icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_drive_eta_blue_24dp)).
                                                                title("Marker for car: " + c.getId())));
                                                    }else{
                                                        curlocmarklist.add(mGoogleMap.addMarker(new MarkerOptions().position(pos).icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_drive_eta_red_24dp)).
                                                                title("Marker for car: " + c.getId())));
                                                    }



                                                }

                                                //event in order to update in real time the car position

                                                db.collection("Cars").orderBy("id")
                                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onEvent(@Nullable QuerySnapshot value,
                                                                                @Nullable FirebaseFirestoreException e) {
                                                                if (e != null) {
                                                                    Log.w("HomeFragment", "Listen failed.", e);
                                                                    return;
                                                                }

                                                                for (QueryDocumentSnapshot doc : value) { //o value o come quello di sopra
                                                                    if (doc.get("id") != null) {
                                                                        list.set(Integer.parseInt(doc.get("id").toString()), doc.toObject(Car.class));

                                                                    }
                                                                }
                                                            }
                                                        });

                                            } else {
                                                Log.w("HomeFragment", "Error getting documents.", task.getException());
                                            }
                                        }
                                    });
                        }
                    });


                }
            }
        };

        thread.start();

    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();


        //stop location updates when Activity is no longer active
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


        //starting location request (gps position of each client)

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000); //120000 two minute interval
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //checking for foreground gps request

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);

            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }

        synchronized (lock) {
            lock.notify();
        }
    }



    //loop function in order to update position markers

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);


                for (Car c: list) {

                    if (curlocmarklist.get(c.getId()) != null){
                        curlocmarklist.get(c.getId()).remove();
                    }


                    LatLng pos = new LatLng(Double.parseDouble(c.getLatitude()),Double.parseDouble(c.getLongitude()));


                    lastloclist.set(c.getId(),pos);

                    if (c.getBusy().equals("false")) {

                        curlocmarklist.set(c.getId(),mGoogleMap.addMarker(new MarkerOptions().position(pos).icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_drive_eta_blue_24dp)).
                                title("Marker for car: " + c.getId())));
                    }else{
                        curlocmarklist.set(c.getId(),mGoogleMap.addMarker(new MarkerOptions().position(pos).icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_drive_eta_red_24dp)).
                                title("Marker for car: " + c.getId())));
                    }

                }

                //setting first position of the camera
                if (!init){
                    init=true;
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));


                }

            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );

                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());


        mapFrag = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);




        return root;
    }

}