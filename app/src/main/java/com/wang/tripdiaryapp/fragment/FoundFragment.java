package com.wang.tripdiaryapp.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wang.tripdiaryapp.R;
import com.wang.tripdiaryapp.activity.LoginActivity;
import com.wang.tripdiaryapp.activity.MainActivity;
import com.wang.tripdiaryapp.activity.MyNoteActivity;
import com.wang.tripdiaryapp.activity.NewActivity;
import com.wang.tripdiaryapp.activity.OtherActivity;
import com.wang.tripdiaryapp.adapter.MyNoteListAdapter;
import com.wang.tripdiaryapp.bean.Note;
import com.wang.tripdiaryapp.db.NoteDao;
import com.wang.tripdiaryapp.view.SpacesItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class FoundFragment extends Fragment {
    private static final String TAG="FoundFragment";
    private RecyclerView rv_list_main;
    private MyNoteListAdapter mNoteListAdapter;
    private List<Note> noteList;
    private NoteDao noteDao;
    private Note note;
    private int groupId =0;//分类ID
    private String groupName ="默认笔记";
    private  View view;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_my,container,false);

        initView();
        return view;
    }

    private void initView() {
        noteDao = new NoteDao(getActivity());
        rv_list_main = view.findViewById(R.id.rv_list_main);

        rv_list_main.addItemDecoration(new SpacesItemDecoration(0));//设置item间距

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        rv_list_main.setLayoutManager(layoutManager);

        //设置RecyclerView的适配器
        mNoteListAdapter = new MyNoteListAdapter();
        rv_list_main.setAdapter(mNoteListAdapter);
        mNoteListAdapter.setmNotes(noteList);


        mNoteListAdapter.setOnItemClickListener(new MyNoteListAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Note note) {
                Toast.makeText(getActivity().getApplicationContext(), note.getTitle(),
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), OtherActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }
        });
    }
    //刷新笔记列表
    private void refreshNoteList(){
       // noteList = noteDao.queryNotesAll(groupId);
        noteList = new ArrayList<>();
        String url = "http://xixixi.pythonanywhere.com/tripdiary/alldiary";
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        Map<String,String> map = new HashMap<>();
        map.put("Content-type","application/json;charset=utf-8");
        JSONObject paramJsonObject = new JSONObject(map);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, paramJsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response!=null&&response.length()>0){
                            JSONArray diary = response.optJSONArray("diary");
                            String dataString = diary.toString();

                            for(int i=0;i<diary.length();i++){
                                JSONObject jsonData = diary.optJSONObject(i);
                                Note data = new Note();
                                data.setId(jsonData.optInt("id"));
                                //Log.i("id", "###id="+data.getId());
                                data.setTitle(jsonData.optString("title"));
                                data.setContent(jsonData.optString("content"));
                                data.setGroupId(groupId);
                                data.setGroupName(groupName);
                                data.setType(2);
                                data.setBgColor("#FFFFFF");
                                data.setIsEncrypt(0);
                                data.setCreateTime(jsonData.optString("date"));
                                data.setUpdateTime(jsonData.optString("date"));
                                data.setAuthor(jsonData.optString("author"));
                                noteList.add(data);
                            }
                            mNoteListAdapter.setmNotes(noteList);
                            mNoteListAdapter.notifyDataSetChanged();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        //Add the request to the queue
        queue.add(request);

    }
    @Override
    public void onResume() {
        super.onResume();
        refreshNoteList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
