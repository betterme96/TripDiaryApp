package com.wang.tripdiaryapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sendtion.xrichtext.RichTextView;
import com.wang.tripdiaryapp.R;

import com.wang.tripdiaryapp.bean.Note;

import com.wang.tripdiaryapp.util.CommonUtil;
import com.wang.tripdiaryapp.util.StringUtils;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class OtherActivity extends  BaseActivity {
    private static final String TAG = "OtherActivity";
    private TextView tv_note_title;//笔记标题
    private RichTextView tv_note_content;//笔记内容
    private TextView tv_note_time;//笔记创建时间
    private TextView tv_note_author;//笔记作者
    //private ScrollView scroll_view;
    private Note note;//笔记对象
    private String myTitle;
    private String myContent;
    private int myId;


    private ProgressDialog loadingDialog;
    private Subscription subsLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);

        initView();

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setNavigationIcon(R.drawable.ic_dialog_info);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_note);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("数据加载中...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        tv_note_title = (TextView) findViewById(R.id.tv_note_title);//标题
        tv_note_title.setTextIsSelectable(true);
        tv_note_content = (RichTextView) findViewById(R.id.tv_note_content);//内容
        tv_note_time = (TextView) findViewById(R.id.tv_note_time);
        tv_note_author=(TextView)findViewById(R.id.tv_note_author);


        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        note = (Note) bundle.getSerializable("note");

        myId = note.getId();
        myTitle = note.getTitle();
        myContent = note.getContent();

        tv_note_title.setText(myTitle);
        tv_note_content.post(new Runnable() {
            @Override
            public void run() {
                //showEditData(myContent);
                tv_note_content.clearAllLayout();
                showDataSync(myContent);
            }
        });
        tv_note_time.setText(note.getCreateTime());
        tv_note_author.setText("作者："+note.getAuthor());
        setTitle("日记详情");

    }

    /**
     * 异步方式显示数据
     * @param html
     */
    private void showDataSync(final String html){

        subsLoading = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                showEditData(subscriber, html);
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        showToast("解析错误：图片不存在或已损坏");
                        Log.e(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String text) {
                        if (text.contains("<img") && text.contains("src=")) {
                            //imagePath可能是本地路径，也可能是网络地址
                            String imagePath = StringUtils.getImgSrc(text);
                            tv_note_content.addImageViewAtIndex(tv_note_content.getLastIndex(), imagePath);
                        } else {
                            tv_note_content.addTextViewAtIndex(tv_note_content.getLastIndex(), text);
                        }
                    }
                });

    }

    /**
     * 显示数据
     * @param html
     */
    private void showEditData(Subscriber<? super String> subscriber, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                subscriber.onNext(text);
            }
            subscriber.onCompleted();
        } catch (Exception e){
            e.printStackTrace();
            subscriber.onError(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_note_credit://评论笔记
                Intent intent = new Intent(OtherActivity.this, CreditActivity.class);
                intent.putExtra("note_id",myId);
                startActivity(intent);
                break;
            case R.id.action_note_share://分享笔记
                CommonUtil.shareTextAndImage(this, note.getTitle(), note.getContent(), null);//分享图文
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}


