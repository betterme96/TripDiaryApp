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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.wang.tripdiaryapp.view.SpacesItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreditActivity extends BaseActivity {
    private static final String TAG = "CreditActivity";
    private RecyclerView rv_list_credit;
    private CreditListAdapter mCreditListAdapter;
    private List<Credit> creditsList;
    private Credit data;//Credit对象
    private int id;


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
    }

    //刷新笔记列表
    private void refreshNoteList(){
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
        refreshNoteList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
