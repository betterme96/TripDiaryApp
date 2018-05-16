package com.wang.tripdiaryapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wang.tripdiaryapp.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText username;
    private EditText password;
    private EditText repeatpassword;
    private Button re_register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username = findViewById(R.id.re_username);
        password = findViewById(R.id.re_password);
        repeatpassword = findViewById(R.id.re_repassword);
        re_register = findViewById(R.id.re_register);

        re_register.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.re_register:
                register();
                break;
            default:
                break;
    }
}

private void register(){
    final String usernameStr = username.getText().toString().trim();
    final String passwordStr = password.getText().toString().trim();
    final String re_passwordStr = repeatpassword.getText().toString().trim();
    String url = "http://xixixi.pythonanywhere.com/tripdiary/register";//username="+usernameStr+"&password="+passwordStr;
    if(TextUtils.isEmpty(usernameStr)){
        Toast.makeText(this,"用户名不能为空",Toast.LENGTH_SHORT).show();
        return;
    }
    if(TextUtils.isEmpty(passwordStr)){
        Toast.makeText(this,"密码不能为空",Toast.LENGTH_SHORT).show();
        return;
    }
    if(!passwordStr.equals(re_passwordStr)){
        Toast.makeText(this,"密码不一致",Toast.LENGTH_SHORT).show();
        return;
    }
    RequestQueue queue = Volley.newRequestQueue(this);
    Map<String,String> map = new HashMap<>();
    map.put("username",usernameStr);
    map.put("password",passwordStr);
    map.put("Content-type","application/json;charset=utf-8");
    JSONObject paramJsonObject = new JSONObject(map);
    JsonObjectRequest jsonrequest = new JsonObjectRequest(Request.Method.POST, url, paramJsonObject,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //判断注册状态
                    int status = response.optInt("status");
                    if (status == 200) {
                        Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                    } else if (status == 400) {
                        Toast.makeText(getApplicationContext(), "注册失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
        }
    });

    //Add the request to the queue
    queue.add(jsonrequest);
}
}

