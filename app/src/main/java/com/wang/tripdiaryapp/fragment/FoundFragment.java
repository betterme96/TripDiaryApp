package com.wang.tripdiaryapp.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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
import com.wang.tripdiaryapp.activity.OtherActivity;
import com.wang.tripdiaryapp.adapter.MyNoteListAdapter;
import com.wang.tripdiaryapp.bean.Note;
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
    private SearchView searchView;
    private SwipeRefreshLayout mSwipeRefreshLayout;//用于下拉刷新
    private MyNoteListAdapter mNoteListAdapter;
    private MyNoteListAdapter findListAdapter;
    private List<Note> noteList;
    private List<Note> findList;
    private List<String> nameList;
    private Note note;
    private int groupId =0;//分类ID
    private String groupName ="默认笔记";
    private  View view;

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 123:
                    mNoteListAdapter = new MyNoteListAdapter();
                    rv_list_main.setAdapter(mNoteListAdapter);
                    mNoteListAdapter.setmNotes(noteList);
                    for(int i = 0; i < noteList.size(); i++)
                    {
                        Note information = noteList.get(i);
                        nameList.add(information.getTitle());
                    }
                    break;
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_found,container,false);

        initView();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListener();
    }

    //下拉刷新
    private void initListener() {
        //下拉刷新
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipelayout);
        //设置 进度条的颜色变化，最多可以设置4种颜色
        mSwipeRefreshLayout.setColorSchemeColors(Color.parseColor("#d3d3d3"),Color.parseColor("#3F51B5"));
        initPullRefresh();
    }

    private void initPullRefresh() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshNoteList();
                        //刷新完成
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), "加载完成~", Toast.LENGTH_SHORT).show();
                    }

                }, 3000);

            }
        });
    }

    private void initView() {
        rv_list_main = view.findViewById(R.id.rv_list_main);
        searchView = view.findViewById(R.id.searchEdit);
        rv_list_main.addItemDecoration(new SpacesItemDecoration(0));//设置item间距

        findList = new ArrayList<Note>();
        nameList = new ArrayList<String>();

        /**
         * 默认情况下是没提交搜索的按钮，所以用户必须在键盘上按下"enter"键来提交搜索.你可以同过setSubmitButtonEnabled(
         * true)来添加一个提交按钮（"submit" button)
         * 设置true后，右边会出现一个箭头按钮。如果用户没有输入，就不会触发提交（submit）事件
         */
        searchView.setSubmitButtonEnabled(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //输入完成后，提交时触发的方法，一般情况是点击输入法中的搜索按钮才会触发，表示现在正式提交了
            public boolean onQueryTextSubmit(String query) {

                if(TextUtils.isEmpty(query))
                {
                    Toast.makeText(getActivity(), "请输入查找内容！", Toast.LENGTH_SHORT).show();
                    rv_list_main.setAdapter(mNoteListAdapter);
                }
                else
                {
                    findList.clear();
                    for(int i = 0; i < noteList.size(); i++)
                    {
                        Note information = noteList.get(i);
                        if(information.getTitle().contains(query)||information.getContent().contains(query))
                        {
                            findList.add(information);
                        }
                    }
                    if(findList.size() == 0)
                    {
                        Toast.makeText(getActivity(), "查找的日记不存在", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Log.i("NewActivity", "###length="+findList.size());
                        Toast.makeText(getActivity(), "查找成功", Toast.LENGTH_SHORT).show();
                        findListAdapter = new MyNoteListAdapter();
                        rv_list_main.setAdapter(findListAdapter);
                        findListAdapter.setmNotes(findList);
                        findListAdapter.notifyDataSetChanged();
                        findListAdapter.setOnItemClickListener(new MyNoteListAdapter.OnRecyclerViewItemClickListener() {
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
                }
                return true;
            }
            //在输入时触发的方法，当字符真正显示到searchView中才触发，像是拼音，在输入法组词的时候不会触发
            public boolean onQueryTextChange(String newText)
            {

                if(TextUtils.isEmpty(newText))
                {
                    rv_list_main.setAdapter(mNoteListAdapter);
                }
                else
                {
                    findList.clear();
                    for(int i = 0; i < noteList.size(); i++)
                    {
                        Note information = noteList.get(i);
                        if(information.getTitle().contains(newText))
                        {
                            findList.add(information);
                        }
                    }
                    findListAdapter = new MyNoteListAdapter();
                    findListAdapter.notifyDataSetChanged();
                    rv_list_main.setAdapter(findListAdapter);
                    findListAdapter.setmNotes(findList);
                }
                return true;
            }
        });

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
        String url = "http://xixixi.pythonanywhere.com/tripdiary/alldiary";

        noteList = new ArrayList<>();
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
                            for(int i=0;i<diary.length();i++){
                                JSONObject jsonData = diary.optJSONObject(i);
                                note = new Note();
                                note.setId(jsonData.optInt("id"));
                                note.setTitle(jsonData.optString("title"));
                                note.setContent(jsonData.optString("content"));
                                note.setType(2);
                                note.setBgColor("#FFFFFF");
                                note.setIsEncrypt(0);
                                note.setCreateTime(jsonData.optString("date"));
                                note.setAuthor(jsonData.optString("author"));
                                noteList.add(note);
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
