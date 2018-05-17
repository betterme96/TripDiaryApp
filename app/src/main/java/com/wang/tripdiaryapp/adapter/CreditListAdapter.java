package com.wang.tripdiaryapp.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wang.tripdiaryapp.R;
import com.wang.tripdiaryapp.bean.Credit;
import com.wang.tripdiaryapp.bean.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WANG on 2018/5/16.
 */

public class CreditListAdapter extends RecyclerView.Adapter<CreditListAdapter.ViewHolder>
        implements View.OnClickListener, View.OnLongClickListener {
    private Context mContext;
    private List<Credit> mCredits;
    private CreditListAdapter.OnRecyclerViewItemClickListener mOnItemClickListener ;
    private CreditListAdapter.OnRecyclerViewItemLongClickListener mOnItemLongClickListener ;

    public CreditListAdapter() {
        mCredits = new ArrayList<>();
    }

    public void setmNotes(List<Credit> credits) {
        this.mCredits = credits;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v,(Note)v.getTag());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnItemLongClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemLongClickListener.onItemLongClick(v,(Note)v.getTag());
        }
        return true;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Note note);
    }

    public void setOnItemClickListener(CreditListAdapter.OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view, Note note);
    }

    public void setOnItemLongClickListener(CreditListAdapter.OnRecyclerViewItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.i(TAG, "###onCreateViewHolder: ");
        //inflate(R.layout.list_item_record,parent,false) 如果不这么写，cardview不能适应宽度
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_note,parent,false);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Log.i(TAG, "###onBindViewHolder: ");
        final Credit credit = mCredits.get(position);
        //将数据保存在itemView的Tag中，以便点击时进行获取
        holder.itemView.setTag(credit);
        //Log.e("adapter", "###record="+record);
        holder.tv_list_content.setMaxLines(50);
        holder.tv_list_author.setTextSize(20);
        holder.tv_list_author.setText(credit.getAuthor()+":");
        holder.tv_list_content.setText(credit.getContent());
        holder.tv_list_time.setText(credit.getCreateTime());
        holder.tv_list_text.setText("评论时间");
    }

    @Override
    public int getItemCount() {
        //Log.i(TAG, "###getItemCount: ");
        if (mCredits != null && mCredits.size()>0){
            return mCredits.size();
        }
        return 0;
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_list_author;//评论作者
        public TextView tv_list_content;//评论内容
        public TextView tv_list_time;//创建时间
        public TextView tv_list_text;
        public CardView card_view_note;

        public ViewHolder(View view){
            super(view);
            card_view_note = (CardView) view.findViewById(R.id.card_view_note);
            tv_list_author = (TextView) view.findViewById(R.id.tv_list_title);
            tv_list_content = (TextView) view.findViewById(R.id.tv_list_summary);
            tv_list_time = (TextView) view.findViewById(R.id.tv_list_author);
            tv_list_text = (TextView)view.findViewById(R.id.tv_list_time);

        }
    }
}
