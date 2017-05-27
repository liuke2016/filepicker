package com.lynn.filepicker.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.RxBus;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.entity.AudioFile;
import com.lynn.filepicker.entity.event.FileClickEvent;
import com.lynn.filepicker.mvp.PickerContract;

import java.util.ArrayList;

/**
 * Created by liuke on 2017/5/12.
 */

public class AudioPickerAdapter extends BasePickerAdapter<AudioFile> {
    public AudioPickerAdapter(Context context, ArrayList<AudioFile> files) {
        super(context, files);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout itemView = new RelativeLayout(mContext);
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        int[] attribute = new int[]{android.R.attr.selectableItemBackground};
        TypedArray typedArray = mContext.obtainStyledAttributes(typedValue.resourceId, attribute);
        Drawable selectableItemBackground = typedArray.getDrawable(0);
        itemView.setBackgroundDrawable(selectableItemBackground);

        ImageView icAudio = new ImageView(mContext);
        icAudio.setId(R.id.iv_file);
        icAudio.setImageResource(R.mipmap.ic_audio);
        icAudio.setPadding(Util.dip2px(mContext,10),0,0,0);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams1.addRule(RelativeLayout.CENTER_VERTICAL);
        itemView.addView(icAudio,layoutParams1);

        ImageView ivCbx = new ImageView(mContext);
        ivCbx.setId(R.id.cbx);
        ivCbx.setImageResource(R.drawable.selector_cbx_audio);
        ivCbx.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ivCbx.setPadding(0,0,Util.dip2px(mContext,10),0);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(Util.dip2px(mContext,40), Util.dip2px(mContext,40));
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams2.addRule(RelativeLayout.CENTER_VERTICAL);
        itemView.addView(ivCbx,layoutParams2);

        LinearLayout llContainer = new LinearLayout(mContext);
        llContainer.setOrientation(LinearLayout.VERTICAL);
        llContainer.setPadding(Util.dip2px(mContext,10),Util.dip2px(mContext,10),Util.dip2px(mContext,10),Util.dip2px(mContext,10));
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams3.addRule(RelativeLayout.RIGHT_OF,R.id.iv_file);
        layoutParams3.addRule(RelativeLayout.LEFT_OF,R.id.cbx);
        layoutParams3.addRule(RelativeLayout.CENTER_VERTICAL);

        TextView tvFileName = new TextView(mContext);
        tvFileName.setId(R.id.tv_title);
        tvFileName.setLines(2);
        tvFileName.setEllipsize(TextUtils.TruncateAt.END);
        tvFileName.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        tvFileName.setTextColor(mContext.getResources().getColor(R.color.BgToolBar));
        ViewGroup.LayoutParams layoutParams4 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llContainer.addView(tvFileName,layoutParams4);


        TextView tvDuration = new TextView(mContext);
        tvDuration.setId(R.id.tv_duration);
        tvDuration.setPadding(0,Util.dip2px(mContext,5),0,0);
        tvDuration.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        tvDuration.setTextColor(mContext.getResources().getColor(R.color.BgToolBar));
        ViewGroup.LayoutParams layoutParams5 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llContainer.addView(tvDuration,layoutParams5);

        itemView.addView(llContainer,layoutParams3);
        return new AudioPickerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final AudioPickerHolder holder = (AudioPickerHolder) viewHolder;
        final AudioFile file = mFiles.get(position);

        holder.mTvTitle.setText(file.getName());
        holder.mTvTitle.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        if (holder.mTvTitle.getMeasuredWidth() >
                Util.getScreenWidth(mContext) - Util.dip2px(mContext, 10 + 32 + 10 + 48 + 10 * 2)) {
            holder.mTvTitle.setLines(2);
        } else {
            holder.mTvTitle.setLines(1);
        }

        holder.mTvDuration.setText(Util.getDurationString(file.getDuration()));

        if (file.isSelected()) {
            holder.mCbx.setSelected(true);
        } else {
            holder.mCbx.setSelected(false);
        }

        holder.mCbx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isSelected() && isUpToMax()) {
                    ((PickerContract.IPickerView)mContext).showMessage(mContext.getString(R.string.most_pick)+ FilePicker.getPickerConfig().getMaxNumber()+mContext.getString(R.string.files));
                    return;
                }

                if (v.isSelected()) {
                    holder.mCbx.setSelected(false);
                    file.setSelected(false);
                    mSelectedList.remove(file);
                } else {
                    holder.mCbx.setSelected(true);
                    file.setSelected(true);
                    mSelectedList.add(file);
                }
                RxBus.getDefault().post(new FileClickEvent(holder.mCbx.isSelected(), file));
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("file://" + file.getPath());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "audio/mp3");
                if (Util.detectIntent(mContext, intent)) {
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "No Application exists for this file!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    class AudioPickerHolder extends RecyclerView.ViewHolder {
        private TextView mTvTitle;
        private TextView mTvDuration;
        private ImageView mCbx;

        AudioPickerHolder(View itemView) {
            super(itemView);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mTvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
        }
    }
}
