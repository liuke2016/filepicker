package com.lynn.filepicker.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
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
import com.lynn.filepicker.entity.OtherFile;
import com.lynn.filepicker.entity.event.FileClickEvent;
import com.lynn.filepicker.mvp.PickerContract;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by liuke on 2017/5/12.
 */

public class OtherFilesPickerAdapter extends BasePickerAdapter<OtherFile> {
    public OtherFilesPickerAdapter(Context context, ArrayList<OtherFile> files) {
        super(context, files);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout itemView = new RelativeLayout(mContext);
        itemView.setPadding(0,Util.dip2px(10),0,Util.dip2px(10));
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        int[] attribute = new int[]{android.R.attr.selectableItemBackground};
        TypedArray typedArray = mContext.obtainStyledAttributes(typedValue.resourceId, attribute);
        Drawable selectableItemBackground = typedArray.getDrawable(0);
        itemView.setBackgroundDrawable(selectableItemBackground);

        ImageView icFile = new ImageView(mContext);
        icFile.setId(R.id.iv_file);
        icFile.setImageResource(R.mipmap.ic_audio);
        icFile.setPadding(Util.dip2px(10),0,0,0);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams1.addRule(RelativeLayout.CENTER_VERTICAL);
        itemView.addView(icFile,layoutParams1);

        ImageView ivCbx = new ImageView(mContext);
        ivCbx.setId(R.id.cbx);
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_selected},mContext.getResources().getDrawable(R.mipmap.ic_checked_audio));
        drawable.addState(new int[]{},mContext.getResources().getDrawable(R.mipmap.ic_uncheck_audio));
        ivCbx.setImageDrawable(drawable);
        ivCbx.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ivCbx.setPadding(0,0,Util.dip2px(10),0);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(Util.dip2px(40), Util.dip2px(40));
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams2.addRule(RelativeLayout.CENTER_VERTICAL);
        itemView.addView(ivCbx,layoutParams2);

        LinearLayout llContainer = new LinearLayout(mContext);
        llContainer.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams3.setMargins(Util.dip2px(10),Util.dip2px(10),Util.dip2px(10),Util.dip2px(10));
        layoutParams3.addRule(RelativeLayout.RIGHT_OF,R.id.iv_file);
        layoutParams3.addRule(RelativeLayout.LEFT_OF,R.id.cbx);
        layoutParams3.addRule(RelativeLayout.CENTER_VERTICAL);

        TextView tvFileName = new TextView(mContext);
        tvFileName.setId(R.id.tv_title);
        tvFileName.setLines(2);
        tvFileName.setEllipsize(TextUtils.TruncateAt.END);
        tvFileName.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        tvFileName.setTextColor(mContext.getResources().getColor(R.color.BgToolBar));
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams4.gravity = Gravity.CENTER_VERTICAL;
        llContainer.addView(tvFileName,layoutParams4);

        itemView.addView(llContainer,layoutParams3);
        return new NormalFilePickerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final NormalFilePickerHolder holder = (NormalFilePickerHolder) viewHolder;
        final OtherFile file = mFiles.get(position);

        holder.mTvTitle.setText(file.getName());
        holder.mTvTitle.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        if (holder.mTvTitle.getMeasuredWidth() >
                Util.getScreenWidth() - Util.dip2px( 10 + 32 + 10 + 48 + 10 * 2)) {
            holder.mTvTitle.setLines(2);
        } else {
            holder.mTvTitle.setLines(1);
        }

        if (file.isSelected()) {
            holder.mCbx.setSelected(true);
        } else {
            holder.mCbx.setSelected(false);
        }

        if (file.getPath().endsWith("xls") || file.getPath().endsWith("xlsx")) {
            holder.mIvIcon.setImageResource(R.mipmap.ic_excel);
        } else if (file.getPath().endsWith("doc") || file.getPath().endsWith("docx")){
            holder.mIvIcon.setImageResource(R.mipmap.ic_word);
        } else if (file.getPath().endsWith("ppt") || file.getPath().endsWith("pptx")){
            holder.mIvIcon.setImageResource(R.mipmap.ic_ppt);
        } else if (file.getPath().endsWith("pdf")){
            holder.mIvIcon.setImageResource(R.mipmap.ic_pdf);
        } else if (file.getPath().endsWith("txt")){
            holder.mIvIcon.setImageResource(R.mipmap.ic_txt);
        } else {
            holder.mIvIcon.setImageResource(R.mipmap.ic_file);
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
                Uri uri = null;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    String authorities = mContext.getPackageName()+".provider";
                    uri = FileProvider.getUriForFile(mContext, authorities, new File(file.getPath()));
                } else {
                    uri = Uri.parse("file://" + file.getPath());
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, file.getMimeType());
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

    class NormalFilePickerHolder extends RecyclerView.ViewHolder {
        private ImageView mIvIcon;
        private TextView mTvTitle;
        private ImageView mCbx;

        NormalFilePickerHolder(View itemView) {
            super(itemView);
            mIvIcon = (ImageView) itemView.findViewById(R.id.iv_file);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
        }
    }
}
