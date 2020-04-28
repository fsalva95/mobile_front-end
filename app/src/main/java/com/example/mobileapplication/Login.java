package com.example.mobileapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.graphics.Color.rgb;


public class Login extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;

    private String token;
    private String email;
    private String password;
    OkHttpHandler okHttpHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), Signup.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(Login.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();



        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();



        okHttpHandler= new OkHttpHandler();
        okHttpHandler.execute();



        new android.os.Handler().postDelayed(   //DOVRESTI FARLO SINCHRONIZED COME HAI FATTO IN PROFILE PER FARLO FUNZIONARE BENE
                new Runnable() {
                    public void run() {
                        try {
                            synchronized (okHttpHandler) {
                                okHttpHandler.wait();

                                if (token==null) onLoginFailed();
                                else onLoginSuccess();
                                progressDialog.dismiss();

                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, 1000);
    }

    public class OkHttpHandler extends AsyncTask {

        OkHttpClient client = new OkHttpClient();


        @Override
        protected Object doInBackground(Object[] objects) {

            MediaType MEDIA_TYPE = MediaType.parse("application/json");
            String url = "https://immense-sierra-91111.herokuapp.com/authenticate";



            JSONObject postdata = new JSONObject();
            try {
                postdata.put("email", email);
                postdata.put("password", password);

            } catch(JSONException e){
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());


            Request request = new Request.Builder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .url(url)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();

                String mMessage = response.body().string();

                JSONObject json = new JSONObject(mMessage);


                if (!mMessage.toLowerCase().contains("error")){
                    token = json.getString("auth_token");
                }


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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {


                this.finish();
            }
        }
    }



    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        Intent intent3 = new Intent(Login.this, StartActivity.class);
        intent3.putExtra("token",token);
        intent3.putExtra("email",email);
        startActivity(intent3);

        Context context = getApplicationContext();
        CharSequence text = "Welcome back!";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        View view = toast.getView();

        view.getBackground().setColorFilter(rgb(100,255,91), PorterDuff.Mode.SRC_IN);


        toast.show();



        finish();
    }

    public void onLoginFailed() {
        CharSequence text = "Login failed: email or password not correct";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(getBaseContext(), text, duration);
        View view1 = toast.getView();

        view1.getBackground().setColorFilter(rgb(255,82,82), PorterDuff.Mode.SRC_IN);


        toast.show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
