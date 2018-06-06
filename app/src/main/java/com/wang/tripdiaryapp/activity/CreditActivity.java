package com.wang.tripdiaryapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wang.tripdiaryapp.R;
import com.wang.tripdiaryapp.adapter.CreditListAdapter;
import com.wang.tripdiaryapp.adapter.MyNoteListAdapter;
import com.wang.tripdiaryapp.bean.Credit;
import com.wang.tripdiaryapp.bean.Note;
import com.wang.tripdiaryapp.util.CommonUtil;
import com.wang.tripdiaryapp.view.SpacesItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreditActivity extends BaseActivity implements View.OnClickListener{
    private static final String TAG = "CreditActivity";
    private RecyclerView rv_list_credit;
    private CreditListAdapter mCreditListAdapter;
    private List<Credit> creditsList;
    private Credit data;//Credit对象
    private int id;

    private EditText tv_new_credit;
    private ImageButton tv_new_send;
    private Button send;

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        initView();

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        rv_list_credit = (RecyclerView)findViewById(R.id.rv_list_credit);
        rv_list_credit.addItemDecoration(new SpacesItemDecoration(0));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_list_credit.setLayoutManager(layoutManager);

        //设置RecyclerView的适配器
        mCreditListAdapter = new CreditListAdapter();
        rv_list_credit.setAdapter(mCreditListAdapter);
        mCreditListAdapter.setmNotes(creditsList);

        tv_new_credit = (EditText)findViewById(R.id.tv_new_credit);
        tv_new_send = (ImageButton)findViewById(R.id.send);

        tv_new_send.setOnClickListener(this);
    }

    //刷新笔记列表
    private void refreshCreditList(){
        Intent intent = getIntent();
        id = intent.getIntExtra("note_id",0);
        String note_id = String.valueOf(id);
        creditsList = new ArrayList<>();
        String url = "http://xixixi.pythonanywhere.com/tripdiary/showcredit";
        RequestQueue queue = Volley.newRequestQueue(this);
        Map<String,String> map = new HashMap<>();
        map.put("note_id",note_id);
        map.put("Content-type","application/json;charset=utf-8");
        JSONObject paramJsonObject = new JSONObject(map);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, paramJsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response!=null&&response.length()>0){
                            JSONArray credit = response.optJSONArray("credit");
                            String dataString = credit.toString();

                            for(int i=0;i<credit.length();i++){
                                JSONObject jsonData = credit.optJSONObject(i);
                                data = new Credit();
                                data.setId(jsonData.optInt("id"));
                               // Log.i("id", "###id="+credit.getId());
                                data.setAuthor(jsonData.optString("author"));
                                data.setContent(jsonData.optString("content"));
                                data.setCreateTime(jsonData.optString("date"));
                                creditsList.add(data);
                            }
                            mCreditListAdapter.setmNotes(creditsList);
                            mCreditListAdapter.notifyDataSetChanged();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        //Add the request to the queue
        queue.add(request);

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCreditList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.send:
                if(tv_new_credit.getText().length()>0){
                    uploadCredit();
                    tv_new_credit.getText().clear();
                }else{
                    toast =Toast.makeText(getApplicationContext(), "评论不能为空", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                break;
    }
}

    private void uploadCredit() {

        Intent intent = getIntent();
        id = intent.getIntExtra("note_id",0);

        String url = "http://xixixi.pythonanywhere.com/tripdiary/savecredit";
        //Toast.makeText(getApplicationContext(), String.valueOf(id), Toast.LENGTH_SHORT).show();
        String c_date = CommonUtil.date2string(new Date());
        String c_diary = String.valueOf(id);
        String c_author = LoginActivity.usernameStr;
        String c_content = tv_new_credit.getText().toString();

        RequestQueue queue = Volley.newRequestQueue(this);
        Map<String,String> map = new HashMap<>();
        map.put("c_date",c_date);
        map.put("c_diary",c_diary);
        map.put("c_author",c_author);
        map.put("c_content",c_content);
        map.put("Content-type","application/json;charset=utf-8");
        JSONObject paramJsonObject = new JSONObject(map);

        JsonObjectRequest jsonrequest = new JsonObjectRequest(Request.Method.POST, url, paramJsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //判断状态
                        int status = response.optInt("status");
                        if (status == 200) {
                            Toast.makeText(getApplicationContext(), "评论成功", Toast.LENGTH_SHORT).show();
                            tv_new_credit.clearComposingText();
                            refreshCreditList();
                        } else if (status == 400) {
                            Toast.makeText(getApplicationContext(), "网络出错了~", Toast.LENGTH_SHORT).show();
                        }else if (status == 500){
                            Toast.makeText(getApplicationContext(), "评论失败", Toast.LENGTH_SHORT).show();
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
