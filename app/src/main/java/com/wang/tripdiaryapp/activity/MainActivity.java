package com.wang.tripdiaryapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hdl.myhttputils.MyHttpUtils;
import com.hdl.myhttputils.bean.CommCallback;
import com.wang.tripdiaryapp.fragment.FoundFragment;
import com.wang.tripdiaryapp.fragment.MyFragment;
import com.wang.tripdiaryapp.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //两次返回键退出程序
    boolean isExit;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isExit) {
                isExit = true;
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mHandler.sendEmptyMessageDelayed(0, 2000);
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                System.exit(0);
            }
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            isExit = false;
        }

    };
    //加载fragment
    //定义fragment的对象
    private MyFragment myFragment;
    private FoundFragment foundFragment;
    //帧布局对象，用来存放Fragment的容器
    private FrameLayout frameLayout;
    //定义底部导航栏的三个布局
    private RelativeLayout found_layout;
    private RelativeLayout my_layout;
    //定义底部导航栏中的ImageView与TextView
    private ImageView found_image;
    private ImageView my_image;
    private TextView found_text;
    private TextView my_text;
    //定义要用的颜色值
    private int white =  0xFFFFFFFF;
    private int gray = 0xFF7597B3;
    private int blue =0xFF0AB2FB;
    //定义FragmentManager对象
    FragmentManager fManager;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fManager = getSupportFragmentManager();
        initViews();
    }
    //完成组件初始化
    public void initViews(){
        found_image = findViewById(R.id.found_image);
        my_image = findViewById(R.id.my_image);
        found_text = findViewById(R.id.found_text);
        my_text = findViewById(R.id.my_text);
        found_layout = findViewById(R.id.found_layout);
        my_layout = findViewById(R.id.my_layout);
        found_layout.setOnClickListener(this);
        my_layout.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_new_note:
                intent = new Intent(MainActivity.this, NewActivity.class);
                startActivity(intent);
                break;
            case R.id.action_account:
                intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.found_layout:
                setChioceItem(1);
                break;
            case R.id.my_layout:
                setChioceItem(2);
                break;
            default:
                break;
        }
    }


    //定义一个选中一个item后的处理
    public void setChioceItem(int index){
        //重置选项+隐藏所有Fragment
        FragmentTransaction transaction = fManager.beginTransaction();
        clearChioce();
        hideFragments(transaction);
        switch (index) {
            case 1:
                found_image.setImageResource(R.drawable.ic_tabbar_found_normal);
                found_text.setTextColor(blue);
                found_layout.setBackgroundResource(R.drawable.ic_tabbar_bg_click);
                if (foundFragment == null) {
                    // 如果findFragment为空，则创建一个并添加到界面上
                    foundFragment = new FoundFragment();
                    transaction.add(R.id.content, foundFragment);
                } else {
                    // 如果MessageFragment不为空，则直接将它显示出来
                    transaction.show(foundFragment);
                }
                break;

            case 2:
                my_image.setImageResource(R.drawable.ic_tabbar_settings_pressed);
                my_text.setTextColor(blue);
                my_layout.setBackgroundResource(R.drawable.ic_tabbar_bg_click);
                if (myFragment == null) {
                    // 如果fg1为空，则创建一个并添加到界面上
                    myFragment = new MyFragment();
                    transaction.add(R.id.content, myFragment);
                } else {
                    // 如果MessageFragment不为空，则直接将它显示出来
                    transaction.show(myFragment);
                }
                break;
        }
        transaction.commit();
    }

    //隐藏所有的Fragment,避免fragment混乱
    private void hideFragments(FragmentTransaction transaction) {
        if (foundFragment != null) {
            transaction.hide(foundFragment);
        }
        if (myFragment != null) {
            transaction.hide(myFragment);
        }
    }
    //定义一个重置所有选项的方法
    public void clearChioce()
    {
        found_image.setImageResource(R.drawable.ic_tabbar_found_normal);
        found_layout.setBackgroundColor(white);
        found_text.setTextColor(gray);
        my_image.setImageResource(R.drawable.ic_tabbar_settings_normal);
        my_layout.setBackgroundColor(white);
        my_text.setTextColor(gray);
    }

}

