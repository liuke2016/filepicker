package com.lynn.filepicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.RxBus;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.activity.BasePickerActivity;
import com.lynn.filepicker.activity.VideoPickerActivity;
import com.lynn.filepicker.entity.VideoFile;
import com.lynn.filepicker.entity.event.FileClickEvent;
import com.lynn.filepicker.mvp.PickerContract;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DCIM;

/**
 * Created by liuke on 2017/5/12.
 */

public class VideoPickerAdapter extends BasePickerAdapter<VideoFile> {
    private boolean mIsNeedCamera;
    public String mVideoPath;

    public VideoPickerAdapter(Context context, ArrayList<VideoFile> files, boolean isNeedCamera) {
        super(context, files);
        mIsNeedCamera = isNeedCamera;
    }

    public void setNeedCamera(boolean needCamera) {
        mIsNeedCamera = needCamera;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            RelativeLayout itemView = new RelativeLayout(mContext);
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            itemView.setBackgroundColor(mContext.getResources().getColor(R.color.BgItem));
            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, width / BasePickerActivity.COLUMN_NUMBER));

            ImageView ivThumbnail = new ImageView(mContext);
            ivThumbnail.setId(R.id.iv_thumbnail);
            ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            itemView.addView(ivThumbnail,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));

            ImageView ivCbx = new ImageView(mContext);
            ivCbx.setId(R.id.cbx);
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_selected},mContext.getResources().getDrawable(R.mipmap.ic_checked_audio));
            drawable.addState(new int[]{},mContext.getResources().getDrawable(R.mipmap.ic_uncheck_audio));
            ivCbx.setImageDrawable(drawable);
            ivCbx.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(Util.dip2px(40), Util.dip2px(40));
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            itemView.addView(ivCbx,layoutParams2);

            View shadow = new View(mContext);
            shadow.setId(R.id.shadow);
            shadow.setBackgroundColor(mContext.getResources().getColor(R.color.ShadowItem));
            shadow.setVisibility(View.INVISIBLE);

            TextView tvDuration = new TextView(mContext);
            tvDuration.setId(R.id.tv_duration);
            tvDuration.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            tvDuration.setTextColor(Color.WHITE);
            tvDuration.setPadding(0,0,Util.dip2px(5),0);
            tvDuration.setBackgroundColor(mContext.getResources().getColor(R.color.ShadowItem));
            tvDuration.setGravity(Gravity.RIGHT);
            tvDuration.setPadding(0,Util.dip2px(2),0,Util.dip2px(2));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            itemView.addView(tvDuration,layoutParams);

            itemView.addView(shadow,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new VideoPickerHolder(itemView);
        } else {
            View itemView = initIvCamera();
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            if (params != null) {
                WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                int width = wm.getDefaultDisplay().getWidth();
                params.height = width / BasePickerActivity.COLUMN_NUMBER;
            }
            return new CameraHolder(itemView);
        }
    }

    private View initIvCamera() {
        ImageView ivCamera = new ImageView(mContext);
        ivCamera.setImageResource(R.mipmap.ic_camera);
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.launchCamera(new Util.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {
                        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
                        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getAbsolutePath()
                                + "/VID_" + timeStamp + ".mp4");
                        mVideoPath = file.getAbsolutePath();
                        Uri uri = null;
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            uri = FileProvider.getUriForFile(mContext,mContext.getPackageName()+".provider",file);
                        }else{
                            uri = Uri.fromFile(file);
                        }
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        ((Activity) mContext).startActivityForResult(intent, VideoPickerActivity.REQUEST_CODE_TAKE_VIDEO);
                    }
                }, (PickerContract.IPickerView) mContext);
            }
        });
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ivCamera.setLayoutParams(layoutParams);
        ivCamera.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return ivCamera;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(mIsNeedCamera && position == 0)) {
            final VideoPickerHolder videoPickerHolder = (VideoPickerHolder) holder;
            final VideoFile file;
            if (mIsNeedCamera) {
                file = mFiles.get(position - 1);
            } else {
                file = mFiles.get(position);
            }

            Glide.with(mContext)
                    .load(file.getThumbnail())
                    .centerCrop()
                    .crossFade()
                    .placeholder(R.mipmap.ic_place_holder)
                    .error(R.mipmap.ic_place_holder)
                    .into(videoPickerHolder.mIvThumbnail);

            if (file.isSelected()) {
                videoPickerHolder.mCbx.setSelected(true);
                videoPickerHolder.mShadow.setVisibility(View.VISIBLE);
            } else {
                videoPickerHolder.mCbx.setSelected(false);
                videoPickerHolder.mShadow.setVisibility(View.INVISIBLE);
            }

            videoPickerHolder.mCbx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected() && isUpToMax()) {
                        ((PickerContract.IPickerView)mContext).showMessage(mContext.getString(R.string.most_pick)+ FilePicker.getPickerConfig().getMaxNumber()+mContext.getString(R.string.files));
                        return;
                    }

                    if (v.isSelected()) {
                        videoPickerHolder.mShadow.setVisibility(View.INVISIBLE);
                        videoPickerHolder.mCbx.setSelected(false);
                        file.setSelected(false);
                        mSelectedList.remove(file);
                    } else {
                        videoPickerHolder.mShadow.setVisibility(View.VISIBLE);
                        videoPickerHolder.mCbx.setSelected(true);
                        file.setSelected(true);
                        mSelectedList.add(file);
                    }
                    RxBus.getDefault().post(new FileClickEvent(videoPickerHolder.mCbx.isSelected(), file));

                }
            });

            videoPickerHolder.itemView.setOnClickListener(new View.OnClickListener() {
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
                    intent.setDataAndType(uri, "video/mp4");
                    if (Util.detectIntent(mContext, intent)) {
                        mContext.startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "No Application exists for this file!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            videoPickerHolder.mDuration.setText(Util.getDurationString(file.getDuration()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mIsNeedCamera && position == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mIsNeedCamera ? mFiles.size() + 1 : mFiles.size();
    }

    class VideoPickerHolder extends RecyclerView.ViewHolder {
        private ImageView mIvThumbnail;
        private View mShadow;
        private ImageView mCbx;
        private TextView mDuration;

        VideoPickerHolder(View itemView) {
            super(itemView);
            mIvThumbnail = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            mShadow = itemView.findViewById(R.id.shadow);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
            mDuration = (TextView) itemView.findViewById(R.id.tv_duration);
        }
    }

    class CameraHolder extends RecyclerView.ViewHolder {

        CameraHolder(View itemView) {
            super(itemView);
        }
    }
}
