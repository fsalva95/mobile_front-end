package com.example.mobileapplication;

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


public class Signup extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_address) EditText _addressText;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_mobile) EditText _mobileText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;


    Signup.OkHttpHandler okHttpHandler;

    String name;
    String address;
    String email;
    String mobile;
    String password;
    String reEnterPassword;
    String mMessage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }
    public void signup() {

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(Signup.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        name = _nameText.getText().toString();
        address = _addressText.getText().toString();
        email = _emailText.getText().toString();
        mobile = _mobileText.getText().toString();
        password = _passwordText.getText().toString();
        reEnterPassword = _reEnterPasswordText.getText().toString();


        okHttpHandler= new Signup.OkHttpHandler();
        okHttpHandler.execute();

        new android.os.Handler().postDelayed(   //DOVRESTI FARLO SINCHRONIZED COME HAI FATTO IN PROFILE PER FARLO FUNZIONARE BENE
                new Runnable() {
                    public void run() {
                        try {
                            synchronized (okHttpHandler) {
                                okHttpHandler.wait();
                            }


                            Log.e(TAG, mMessage);



                            if (mMessage.contains("has already been taken")){
                                progressDialog.dismiss();
                                doubleEmail();
                            }else {

                                onSignupSuccess();
                                progressDialog.dismiss();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, 1000);




    }

    public void doubleEmail(){

        CharSequence text = "Signup failed: Email already in use";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(getBaseContext(), text, duration);
        View view1 = toast.getView();

        view1.getBackground().setColorFilter(rgb(255,82,82), PorterDuff.Mode.SRC_IN);


        toast.show();

        _signupButton.setEnabled(true);

    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);


        Context context = getApplicationContext();
        CharSequence text = "Sign up completed! Please activate your account in order to access";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        View view = toast.getView();

        view.getBackground().setColorFilter(rgb(100,255,91), PorterDuff.Mode.SRC_IN);


        toast.show();


        finish();
    }

    public void onSignupFailed() {
        CharSequence text = "Signup failed";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(getBaseContext(), text, duration);
        View view1 = toast.getView();

        view1.getBackground().setColorFilter(rgb(255,82,82), PorterDuff.Mode.SRC_IN);


        toast.show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (address.isEmpty()) {
            _addressText.setError("Enter Valid Address");
            valid = false;
        } else {
            _addressText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=10) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }

    public void postRequest(String name,String address,String email,String mobile,String password,String rpassword) throws IOException {

        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "https://immense-sierra-91111.herokuapp.com/users";

        OkHttpClient client = new OkHttpClient();

        JSONObject postdata = new JSONObject();
        try {
            postdata.put("name", name);
            postdata.put("email", email);
            postdata.put("address", address);
            postdata.put("mobile", mobile);
            postdata.put("password", password);
            postdata.put("password_confirmation", rpassword);
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

                synchronized (okHttpHandler) {
                    okHttpHandler.notify();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                mMessage = response.body().string();

                Log.e(TAG, mMessage);

                synchronized (okHttpHandler) {
                    okHttpHandler.notify();
                }
            }
        });
    }


    public class OkHttpHandler extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {

            //post request in order to trigger the activation email on the backend server and for saving the user
            try {
                postRequest(name,address,email,mobile,password,reEnterPassword);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
