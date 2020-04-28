package com.example.mobileapplication.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.mobileapplication.Booking;
import com.example.mobileapplication.BookingActivity;
import com.example.mobileapplication.BookingAdapter;
import com.example.mobileapplication.Car;
import com.example.mobileapplication.R;
import com.example.mobileapplication.ui.selectcars.SelectCarsFragment;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ActivitiesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String email;
    private String token;

    private ImageView imageView;


    Handler handlers;
    ArrayList<Booking> activitylist = new ArrayList<Booking>();
    ArrayList<String> list = new ArrayList<String>();


    private Object lock = new Object();


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Fragment used in order to rappresent a list of all activities completed and the corresponding track


    public ActivitiesFragment() {
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

    public static ActivitiesFragment newInstance(String param1, String param2) {
        ActivitiesFragment fragment = new ActivitiesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private ActivitiesViewModel activitiesViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {  //assignment of parameters
            email = getArguments().getString(ARG_PARAM1);
            token = getArguments().getString(ARG_PARAM2);
        }


    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        activitiesViewModel =
                ViewModelProviders.of(this).get(ActivitiesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_activities, container, false);



        final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("obtaining activities...");
        progressDialog.show();


        handlers= new Handler();
        handlers.execute();


        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    synchronized (lock) {



                        lock.wait();  //wait until the list is not received by firestore


                        lock.wait(1000);//to see the animation



                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                //setting the attributes for each element of the list on the listview


                                BookingAdapter myListAdapter = new BookingAdapter(getContext(), activitylist); //adapting listview for our layout



                                ListView lv = (ListView) root.findViewById(R.id.listview_booking);
                                lv.setAdapter(myListAdapter);


                                progressDialog.dismiss();

                                myListAdapter.notifyDataSetChanged();


                                //click event to show the picture of the activity completed
                                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                                            long id) {
                                        Booking entry= (Booking) parent.getAdapter().getItem(position);



                                        onButtonShowPopupWindowClick(view,position);

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




        return root;
    }




    public class Handler extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] objects) {   //retrieving booking list

            db.collection("Bookings").whereEqualTo("email",email).orderBy("car.id")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Booking c = document.toObject(Booking.class);
                                    activitylist.add(c);
                                    list.add(c.getStart_date().toString());


                                }


                                synchronized (lock) {

                                    lock.notify();

                                }

                            } else {
                                Log.w("SelectCarFragment", "Error getting documents.", task.getException());
                            }
                        }
                    });


            return null;
        }
    }


    public void onButtonShowPopupWindowClick(View view,int position) { //popup image for onclick event

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);


        imageView= popupView.findViewById(R.id.activityImageView);


        StorageReference mImageRef = //getting the image from firebase storage
                FirebaseStorage.getInstance().getReferenceFromUrl("gs://mobileapplicatio-1581695012089.appspot.com/map-"+email+"-"+activitylist.get(position).getEnd_date()+".png");



        Glide.with(getContext())  //setting the image on the corresponding view
                .load(mImageRef)
                .into(imageView);


        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
}