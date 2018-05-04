package com.wang.tripdiaryapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hdl.myhttputils.MyHttpUtils;
import com.hdl.myhttputils.bean.CommCallback;
import com.sendtion.xrichtext.RichTextEditor;
import com.wang.tripdiaryapp.bean.Note;
import com.wang.tripdiaryapp.db.NoteDao;
import com.wang.tripdiaryapp.util.CommonUtil;
import com.wang.tripdiaryapp.util.ImageUtils;
import com.wang.tripdiaryapp.util.StringUtils;
import com.wang.tripdiaryapp.util.UploadUtil;
import com.wang.tripdiaryapp.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NewActivity extends BaseActivity implements RichTextEditor.OnDeleteImageListener{
    private static final String TAG = "NewActivity";

    private EditText et_new_title;
    private RichTextEditor et_new_content;
    private TextView tv_new_time;

    private NoteDao noteDao;
    private Note note;//笔记对象
    private String myTitle;
    private String myContent;
    private String myNoteTime;
    private int flag;//区分是新建笔记还是编辑笔记

    private static final int cutTitleLength = 20;//截取的标题长度

    private ProgressDialog loadingDialog;
    private ProgressDialog insertDialog;
    private int screenWidth;
    private int screenHeight;
    private Subscription subsLoading;
    private Subscription subsInsert;

    private static final String BASE_URL = "http://xixixi.pythonanywhere.com/tripdiary/upload/";//文件上传的接口
    private static final String IMG_URL = "https://www.pythonanywhere.com/user/xixixi/files/home/xixixi/Trip/upload";//文件存放的路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        initView();

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setNavigationIcon(R.drawable.ic_dialog_info);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealwithExit();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        noteDao = new NoteDao(this);
        note = new Note();

        screenWidth = CommonUtil.getScreenWidth(this);
        screenHeight = CommonUtil.getScreenHeight(this);

        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage("正在插入图片...");
        et_new_title = (EditText) findViewById(R.id.et_new_title);
        et_new_content = (RichTextEditor) findViewById(R.id.et_new_content);
        tv_new_time = (TextView) findViewById(R.id.tv_new_time);

        Intent intent = getIntent();
        flag = intent.getIntExtra("flag", 0);//0新建，1编辑
        if (flag == 1){//编辑
            Bundle bundle = intent.getBundleExtra("data");
            note = (Note) bundle.getSerializable("note");

            myTitle = note.getTitle();
            myContent = note.getContent();
            myNoteTime = note.getCreateTime();

            loadingDialog = new ProgressDialog(this);
            loadingDialog.setMessage("数据加载中...");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();

            setTitle("编辑笔记");
            tv_new_time.setText(note.getCreateTime());
            et_new_title.setText(note.getTitle());
            et_new_content.post(new Runnable() {
                @Override
                public void run() {
                    //showEditData(note.getContent());
                    et_new_content.clearAllLayout();
                    showDataSync(note.getContent());
                }
            });
        } else {
            setTitle("新建笔记");
            myNoteTime = CommonUtil.date2string(new Date());
            tv_new_time.setText(myNoteTime);
        }

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
                        //在图片全部插入完毕后，再插入一个EditText，防止最后一张图片后无法插入文字
                        et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        showToast("解析错误：图片不存在或已损坏");
                    }

                    @Override
                    public void onNext(String text) {
                        if (text.contains("<img") && text.contains("src=")) {
                            //imagePath可能是本地路径，也可能是网络地址
                            String imagePath = StringUtils.getImgSrc(text);
                            //插入空的EditText，以便在图片前后插入文字
                            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                            et_new_content.addImageViewAtIndex(et_new_content.getLastIndex(), imagePath);
                        } else {
                            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
                        }
                    }
                });
    }

    /**
     * 显示数据
     */
    protected void showEditData(Subscriber<? super String> subscriber, String html) {
        try{
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                subscriber.onNext(text);
            }
            subscriber.onCompleted();
        }catch (Exception e){
            e.printStackTrace();
            subscriber.onError(e);
        }
    }

    /**
     * 负责处理编辑数据提交等事宜，请自行实现
     */
    private String getEditData() {
        List<RichTextEditor.EditData> editList = et_new_content.buildEditData();
        StringBuffer content = new StringBuffer();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
                content.append(itemData.inputStr);
            } else if (itemData.imagePath != null) {
                content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
            }
        }
        return content.toString();
    }

    /**
     * 保存数据,=0销毁当前界面，=1不销毁界面，为了防止在后台时保存笔记并销毁，应该只保存笔记
     */
    private void saveNoteData(boolean isBackground) {
        String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        String noteTime = tv_new_time.getText().toString();
        if (noteTitle.length() == 0 ){//如果标题为空，则截取内容为标题
             if (noteContent.length() > cutTitleLength){
                 noteTitle = noteContent.substring(0,cutTitleLength);
             } else if (noteContent.length() > 0 && noteContent.length() <= cutTitleLength){
                 noteTitle = noteContent;
             }
        }
        //这里换成volley请求，传到后台
        note.setTitle(noteTitle);
        note.setContent(noteContent);
        note.setType(2);
        note.setBgColor("#FFFFFF");
        note.setIsEncrypt(0);
        note.setCreateTime(CommonUtil.date2string(new Date()));
        if (flag == 0 ) {//新建笔记
            if (noteTitle.length() == 0 && noteContent.length() == 0) {
                if (!isBackground){
                    Toast.makeText(NewActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                }
            } else {
                long noteId = noteDao.insertNote(note);
                //Log.i("", "noteId: "+noteId);
                //查询新建笔记id，防止重复插入
                note.setId((int) noteId);
                flag = 1;//插入以后只能是编辑
                if (!isBackground){
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }else if (flag == 1) {//编辑笔记
            if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                    || !noteTime.equals(myNoteTime)) {
                    noteDao.updateNote(note);
                }
                if (!isBackground){
                    finish();
                }
            }
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_insert_image:
                callGallery();
                break;
            case R.id.action_new_save:
                showToast("this is diary save");
                saveNoteData(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 调用图库选择
     */
    private void callGallery(){
   //调用第三方图库选择
        PhotoPicker.builder()
                .setPhotoCount(9)//可选择图片数量
                .setShowCamera(true)//是否显示拍照按钮
                .setShowGif(true)//是否显示动态图
                .setPreviewEnabled(true)//是否可以预览
               .start(this, PhotoPicker.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (requestCode == 1){
                    //处理调用系统图库
                } else if (requestCode == PhotoPicker.REQUEST_CODE){
                    //异步方式插入图片
                    insertImagesSync(data);
                }
            }
        }
    }

    /**
     * 异步方式插入图片
     * @param data
     */
    private void insertImagesSync(final Intent data){
        insertDialog.show();

        subsInsert = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try{
                    et_new_content.measure(0, 0);
                    int width = CommonUtil.getScreenWidth(NewActivity.this);
                    int height = CommonUtil.getScreenHeight(NewActivity.this);
                    ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    //可以同时插入多张图片
                    for (String imagePath : photos) {
                        Log.i("NewActivity", "###imagePath="+imagePath);
                        Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, width, height);//压缩图片
                        imagePath = UploadUtil.saveToSdCard(bitmap);//获得压缩之后的图片存储路径
                        //上传图片
                        uploadImage(imagePath);
                        //上传到服务器之后的图片路径
                        imagePath=IMG_URL+"/"+ UploadUtil.getFileName(imagePath)+".png";
                        Log.i("NewActivity", "###path=" + imagePath);
                        subscriber.onNext(imagePath);
                    }
                    subscriber.onCompleted();
                }catch (Exception e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        insertDialog.dismiss();
                        et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), " ");
                        showToast("图片插入成功");
                    }

                    @Override
                    public void onError(Throwable e) {
                        insertDialog.dismiss();
                        showToast("图片插入失败:"+e.getMessage());
                    }

                    @Override
                    public void onNext(String imagePath) {
                        et_new_content.insertImage(imagePath, et_new_content.getMeasuredWidth());
                    }
                });
    }

    /**
     * 图片上传
     *
     */
    public void uploadImage(String imagePath) {
        MyHttpUtils.build()
                .uploadUrl("http://xixixi.pythonanywhere.com/tripdiary/upload/")
                .addFile(imagePath)
                .onExecuteUpLoad(new CommCallback() {
                    @Override
                    public void onSucceed(Object o) {
                        Toast.makeText(getApplicationContext(), "上传成功", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailed(Throwable throwable) {
                        Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();

                    }
                });

    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //如果APP处于后台，或者手机锁屏，则保存数据
        if (CommonUtil.isAppOnBackground(getApplicationContext()) ||
                CommonUtil.isLockScreeen(getApplicationContext())){
            saveNoteData(true);//处于后台时保存数据
        }
    }

    /**
     * 退出处理
     */
    private void dealwithExit(){
        String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        String noteTime = tv_new_time.getText().toString();
        if (flag == 0) {//新建笔记
            if (noteTitle.length() > 0 || noteContent.length() > 0) {
                saveNoteData(false);
            }
        }else if (flag == 1) {//编辑笔记
            if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                    || !noteTime.equals(myNoteTime)) {
                saveNoteData(false);
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        dealwithExit();
    }

    @Override
    public void onDeleteImage(String imagePath) {
        boolean isOK = UploadUtil.deleteFile(imagePath);
        if (isOK){
            showToast("删除成功："+imagePath);
        }
    }


}
