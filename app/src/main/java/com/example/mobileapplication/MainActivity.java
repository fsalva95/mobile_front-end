package com.example.mobileapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.graphics.Color.rgb;

//Icons made by <a href="https://www.flaticon.com/authors/cursor-creative" title="Cursor Creative">Cursor Creative</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>


//startinf layout in which is possible to sign in, sign up or log with google account

public class MainActivity extends AppCompatActivity {
    SignInButton b1;
    Button b2,b3;
    GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    String token;
    ActionBar toolbar;


    private Object lock=new Object();
    private String name;
    private String email;
    private OkHttpHandler okHttpHandler;
    public ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = getSupportActionBar();
        toolbar.setTitle(" ");

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestProfile()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // google sign in
        b1 = findViewById(R.id.sign_in_button);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                    // ...
                }


            }
        });


        //sign up
        b2 = findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                switch (v.getId()) {
                    case R.id.button2:

                        Intent intent = new Intent(MainActivity.this, Signup.class);
                        startActivity(intent);
                        break;
                }


            }
        });


        //log in

        b3 = findViewById(R.id.button3);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.button3:



                        Intent intent2 = new Intent(MainActivity.this, Login.class);
                        startActivity(intent2);
                        break;
                }


            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            progressDialog = new ProgressDialog(MainActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();


            GoogleSignInAccount account = completedTask.getResult(ApiException.class);


            name = account.getDisplayName().toString(); //problema qui appena starto con emulatore
            email = account.getEmail().toString();

            okHttpHandler= new OkHttpHandler();
            okHttpHandler.execute();




            Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent3 = new Intent(MainActivity.this, StartActivity.class);
                                intent3.putExtra("token",token);
                                intent3.putExtra("email",email);
                                startActivity(intent3);

                                Context context = getApplicationContext();
                                CharSequence text = "Sign in Successfull! Welcome "+name;
                                int duration = Toast.LENGTH_LONG;

                                Toast toast = Toast.makeText(context, text, duration);
                                View view = toast.getView();

                                view.getBackground().setColorFilter(rgb(100,255,91), PorterDuff.Mode.SRC_IN);
                                progressDialog.dismiss();


                                toast.show();
                            }
                        });



                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                };
            };
            thread.start();


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            progressDialog.dismiss();

            CharSequence text = "signInResult:failed code=" + e.getStatusCode();
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            View view1 = toast.getView();

            view1.getBackground().setColorFilter(rgb(255,82,82), PorterDuff.Mode.SRC_IN);


            toast.show();

        }

    }


    public class OkHttpHandler extends AsyncTask {

        OkHttpClient client = new OkHttpClient();


        @Override
        protected Object doInBackground(Object[] objects) {

            MediaType MEDIA_TYPE = MediaType.parse("application/json");
            String url = "https://immense-sierra-91111.herokuapp.com/googlelog";

            OkHttpClient client = new OkHttpClient();

            JSONObject postdata = new JSONObject();
            try {
                postdata.put("name", name);
                postdata.put("email", email);

            } catch(JSONException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String mMessage = e.getMessage().toString();
                    Log.w("failure Response", mMessage);
                    //call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String mMessage = response.body().string();
                    try {
                        JSONObject json = new JSONObject(mMessage);
                        token = json.getString("auth_token");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, mMessage);
                    Log.e(TAG, token+"ciao");


                    synchronized (lock){
                        lock.notify();
                    }

                }
            });

            return null;
        }
    }


    public void postRequest(String name,String email) throws IOException {

        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "https://immense-sierra-91111.herokuapp.com/googlelog";

        OkHttpClient client = new OkHttpClient();

        JSONObject postdata = new JSONObject();
        try {
            postdata.put("name", name);
            postdata.put("email", email);

        } catch(JSONException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                progressDialog.dismiss();

                CharSequence text = "Failure response from backend server: "+mMessage;
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                View view1 = toast.getView();

                view1.getBackground().setColorFilter(rgb(255,82,82), PorterDuff.Mode.SRC_IN);


                toast.show();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String mMessage = response.body().string();
                try {
                    JSONObject json = new JSONObject(mMessage);
                    token = json.getString("auth_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
