package com.example.mobileapplication.ui.selectcars;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.mobileapplication.BookingActivity;
import com.example.mobileapplication.Car;
import com.example.mobileapplication.CarAdapter;
import com.example.mobileapplication.MainActivity;
import com.example.mobileapplication.R;
import com.example.mobileapplication.Signup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.graphics.Color.rgb;
import static android.provider.AlarmClock.EXTRA_MESSAGE;


//fragment in which are defined all the cars available or not and their current position

public class SelectCarsFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String email;
    private String token;
    Handler handlers;
    private Object lock = new Object();
    View root;

    ArrayList<Integer> carlist = new ArrayList<Integer>();
    ArrayList<Car> list = new ArrayList<Car>();



    FirebaseFirestore db = FirebaseFirestore.getInstance();



    public SelectCarsFragment() {
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
    public static SelectCarsFragment newInstance(String param1, String param2) {
        SelectCarsFragment fragment = new SelectCarsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private SelectCarsViewModel selectCarsViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            email = getArguments().getString(ARG_PARAM1);
            token = getArguments().getString(ARG_PARAM2);
        }



    }



    @Override
    public void onResume(){
        super.onResume();


        carlist = new ArrayList<Integer>();
        list = new ArrayList<Car>();



        final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("obtaining cars...");
        progressDialog.show();



        //retrieving car list and adapting listview with respect to our list element view

        handlers= new Handler();
        handlers.execute();


        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    synchronized (lock) {


                        lock.wait();


                        lock.wait(1000);///to show the loading



                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                CarAdapter myListAdapter = new CarAdapter(getContext(), list);

                                ListView lv = (ListView) root.findViewById(R.id.listview_car);
                                lv.setAdapter(myListAdapter);

                                progressDialog.dismiss();

                                myListAdapter.notifyDataSetChanged();



                                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                                            long id) {
                                        Car c= (Car) parent.getAdapter().getItem(position);


                                        if (c.getBusy().equals("false")) {


                                            Intent intent = new Intent(getActivity(), BookingActivity.class);
                                            intent.putExtra("token", token);
                                            intent.putExtra("email", email);
                                            intent.putExtra("id", "" + c.getId());
                                            intent.putExtra("longitude", c.getLongitude());
                                            intent.putExtra("latitude", c.getLatitude());


                                            startActivity(intent);
                                        }else{
                                            Context context = root.getContext();
                                            CharSequence text = "Car already used. Please wait or select another car";
                                            int duration = Toast.LENGTH_LONG;

                                            Toast toast = Toast.makeText(context, text, duration);
                                            View view1 = toast.getView();

                                            view1.getBackground().setColorFilter(rgb(255,82,82), PorterDuff.Mode.SRC_IN);


                                            toast.show();
                                        }
                                    }
                                });

                            }
                        });



                    }
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }

            };
        };
        thread.start();

    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        selectCarsViewModel =
                ViewModelProviders.of(this).get(SelectCarsViewModel.class);
        root = inflater.inflate(R.layout.fragment_selectcars, container, false);



        return root;
    }

    //car retrieving request on firestore
    public class Handler extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] objects) {

            db.collection("Cars")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Car c = document.toObject(Car.class);
                                    carlist.add(c.getId());
                                    list.add(c);


                                }


                                synchronized (lock) {

                                    lock.notify();
                                }

                            } else {
                                Log.e("SelectCarFragment", "Error getting documents.", task.getException());
                            }
                        }
                    });


            return null;
        }
    }











}


