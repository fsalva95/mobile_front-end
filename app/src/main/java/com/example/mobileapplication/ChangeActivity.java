package com.example.mobileapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobileapplication.ui.home.HomeFragment;

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



//editing account activity

public class ChangeActivity extends AppCompatActivity {


    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_address) EditText _addressText;
    @BindView(R.id.input_mobile) EditText _mobileText;
    @BindView(R.id.btn_edit) Button _editButton;

    private String email;
    private String id;
    private String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);


        Intent myIntent = getIntent(); // gets the previously created intent
        email = myIntent.getStringExtra("email");
        token = myIntent.getStringExtra("token");
        id = myIntent.getStringExtra("id");


        ButterKnife.bind(this);

        _editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit();
            }
        });

    }

    private void edit() {

        if (!validate()) {
            onEditFailed();
            return;
        }


        _editButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(ChangeActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Editing Account...");
        progressDialog.show();

        String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        String mobile = _mobileText.getText().toString();


        try {
            putRequest(name,address,mobile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        onEditSuccess();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }



    public void onEditSuccess() {
        _editButton.setEnabled(true);
        setResult(RESULT_OK, null);

        finish();
    }

    public void onEditFailed() {
        Toast.makeText(getBaseContext(), "Edit failed", Toast.LENGTH_LONG).show();

        _editButton.setEnabled(true);
    }


    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        String mobile = _mobileText.getText().toString();


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


        if (mobile.isEmpty() || mobile.length()!=10) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }



        return valid;
    }

    //edit request to backend server
    public void putRequest(String name,String address,String mobile) throws IOException {

        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "https://immense-sierra-91111.herokuapp.com/users/"+id;

        OkHttpClient client = new OkHttpClient();

        JSONObject postdata = new JSONObject();
        try {
            postdata.put("name", name);
            postdata.put("address", address);
            postdata.put("mobile", mobile);
        } catch(JSONException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String mMessage = response.body().string();
                Log.e("ChangeActivity", mMessage);


            }
        });
    }
}
