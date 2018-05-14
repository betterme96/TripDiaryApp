package com.wang.tripdiaryapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText username;
    private EditText password;
    private Button register;
    private Button login;

    public static String usernameStr,passwordStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        login = findViewById(R.id.login);

        register.setOnClickListener(this);
        login.setOnClickListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.register:
                register();
                break;
            case R.id.login:
                if(!empty()){
                    login();
                }
                else{
                    Toast.makeText(getApplicationContext(), "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
                }
                    break;
            default:
                break;
        }
    }

    //判断用户名或密码是否为空
    private boolean empty(){
        String usernameStr = username.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        if((usernameStr.equals(""))&&(passwordStr.equals(""))){
            return true;
        }
        else
            return false;
    }

    //跳转
    private void register() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void login() {
        usernameStr = username.getText().toString().trim();
        passwordStr = password.getText().toString().trim();
        String url = "http://xixixi.pythonanywhere.com/tripdiary/login";

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
                        //判断登陆状态
                        int status = response.optInt("status");
                        if (status == 200) {
                            Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else if (status == 400) {
                            Toast.makeText(getApplicationContext(), "用户名或密码错误", Toast.LENGTH_SHORT).show();
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
