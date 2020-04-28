package com.example.mobileapplication.ui.profiles;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.mobileapplication.Booking;
import com.example.mobileapplication.ChangeActivity;
import com.example.mobileapplication.MainActivity;
import com.example.mobileapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.graphics.Color.rgb;


//profile area in which is possible to edit personal attributes and logout from the application

public class ProfilesFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String email;
    private String token;
    private String name;
    private String mobile;
    private String address;
    private String id;
    private View root;
    OkHttpHandler okHttpHandler;

    ImageView _editLink;
    Button _logoutButton;


    FirebaseStorage storage = FirebaseStorage.getInstance();

    FirebaseFirestore db = FirebaseFirestore.getInstance();



    ArrayList<Booking> activitylist = new ArrayList<Booking>();


    private static final int PICK_FROM_GALLERY = 1;

    private ImageView choosePhoto;

    private ImageView imageView;



    public String url= "https://immense-sierra-91111.herokuapp.com/info";


    public ProfilesFragment() {
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
    public static ProfilesFragment newInstance(String param1, String param2) {
        ProfilesFragment fragment = new ProfilesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


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

        okHttpHandler= new OkHttpHandler();
        okHttpHandler.execute(url);




        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    synchronized (okHttpHandler) {
                        okHttpHandler.wait();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView e= root.findViewById(R.id.email);
                                e.setText(email);
                                TextView n= root.findViewById(R.id.name);
                                n.setText(name);
                                TextView a= root.findViewById(R.id.address);
                                a.setText(address);
                                TextView m= root.findViewById(R.id.mobileNumber);
                                m.setText(mobile);
                            }
                        });



                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            };
        };
        thread.start();



        ImageView imageView = root.findViewById(R.id.profile);


        StorageReference storageReference = storage.getReferenceFromUrl("gs://mobileapplicatio-1581695012089.appspot.com");
        StorageReference photoReference= storageReference.child("img-"+email+"-img.png");

        final long ONE_MEGABYTE = 1024 * 1024*20;////////////
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bmp);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(root.getContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
            }
        });


    }

    public class OkHttpHandler extends AsyncTask {

        OkHttpClient client = new OkHttpClient();

        //requiring user information to backend server (implemented on heroku through rails)
        @Override
        protected Object doInBackground(Object[] objects) {

            MediaType MEDIA_TYPE = MediaType.parse("application/json");


            JSONObject postdata = new JSONObject();
            try {
                postdata.put("email", email);

            } catch(JSONException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());


            Request request = new Request.Builder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", token) //token used as logger for our backend server
                    .url(url)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();

                String mMessage = response.body().string();

                JSONObject json = new JSONObject(mMessage);
                name = json.getString("name");
                mobile = json.getString("mobile");
                address = json.getString("address");
                id = json.getString("id");

                synchronized (this) {
                    this.notify();
                }


                return mMessage;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_profiles, container, false);

        //retrieve list of activities done and setting the number of completed activities on profile
        new Thread(new Runnable() {
            public void run() {
                db.collection("Bookings").whereEqualTo("email", email)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Booking c = document.toObject(Booking.class);
                                        activitylist.add(c);
                                    }
                                    TextView num= root.findViewById(R.id.number_activity);
                                    num.setText(Integer.toString(activitylist.size()));



                                } else {
                                    Log.w("SelectCarFragment", "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        }).start();


        _editLink = root.findViewById(R.id.edit);
        _logoutButton = root.findViewById(R.id.btn_logout);
        choosePhoto=root.findViewById(R.id.profile);



        //logout request (implemented with a blacklist on backend)
        _logoutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    postRequest(token);
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }
        });
        //edit parameters
        _editLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChangeActivity.class);
                intent.putExtra("token",token);
                intent.putExtra("email",email);
                intent.putExtra("id",id);
                startActivity(intent);
            }
        });


        //set phono on profile
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){

                try {
                    if (ActivityCompat.checkSelfPermission(root.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                    } else {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });




        return root;
    }

    public void postRequest(String token) throws IOException {

        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "https://immense-sierra-91111.herokuapp.com/blacklists";


        final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Logout");
        progressDialog.show();

        OkHttpClient client = new OkHttpClient();

        JSONObject postdata = new JSONObject();
        try {
            postdata.put("token", token);

        } catch(JSONException e){
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                progressDialog.dismiss();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String mMessage = response.body().string();
                progressDialog.dismiss();



                Intent intent = new Intent(getActivity(), MainActivity.class);

                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    //request permission for access on gallery
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case PICK_FROM_GALLERY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                } else {
                    Context context = root.getContext();
                    CharSequence text = "Access gallery not allowed. Please allow permissions";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    View view1 = toast.getView();

                    view1.getBackground().setColorFilter(rgb(255,82,82), PorterDuff.Mode.SRC_IN);


                    toast.show();

                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            try {
                InputStream inputStream = root.getContext().getContentResolver().openInputStream(data.getData());
                Bitmap bitmap1 = BitmapFactory.decodeStream(inputStream);


                imageView= root.findViewById(R.id.profile);

                Glide.with(root.getContext())
                            .load(data.getData())
                            .into(imageView);

                new Thread(new Runnable() {
                    public void run() {
                        Bitmap b= null;
                        try {
                            b = Glide.with(root.getContext())
                                    .asBitmap()
                                    .load(data.getData())
                                    .submit()
                                    .get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                        save_photo(b);
                    }
                }).start();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    //saving photo on firebase storage
    private void save_photo(Bitmap bitmap) {




        StorageReference storageRef = storage.getReferenceFromUrl("gs://mobileapplicatio-1581695012089.appspot.com");


        String name = "img-" +email+"-img.png";


        StorageReference mountainsRef = storageRef.child(name);

        StorageReference mountainImagesRef = storageRef.child("images/" + name);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 10, baos);
        byte[] data = baos.toByteArray();



        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("ProfileActivity", "Image upload error");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Log.e("ProfileActivity", "Image successfully uploaded!");


            }
        });

    }


}