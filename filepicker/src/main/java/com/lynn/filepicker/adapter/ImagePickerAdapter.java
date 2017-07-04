package com.lynn.filepicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.signature.StringSignature;
import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.RxBus;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.activity.BasePickerActivity;
import com.lynn.filepicker.activity.ImageBrowserActivity;
import com.lynn.filepicker.activity.ImagePickerActivity;
import com.lynn.filepicker.entity.ImageFile;
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

public class ImagePickerAdapter extends BasePickerAdapter<ImageFile> {
    private boolean mIsNeedCamera;
    public String mImagePath;

    public ImagePickerAdapter(Context context, ArrayList<ImageFile> files, boolean isNeedCamera) {
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
            itemView.addView(ivThumbnail, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            ImageView ivCbx = new ImageView(mContext);
            ivCbx.setId(R.id.cbx);
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_selected},mContext.getResources().getDrawable(R.mipmap.ic_checked_audio));
            drawable.addState(new int[]{},mContext.getResources().getDrawable(R.mipmap.ic_uncheck_audio));
            ivCbx.setImageDrawable(drawable);
            ivCbx.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(Util.dip2px(40), Util.dip2px(40));
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            itemView.addView(ivCbx, layoutParams2);

            ImageView ivEdited = new ImageView(mContext);
            ivEdited.setId(R.id.iv_edited);
            ivEdited.setImageResource(R.mipmap.ic_edited_pic);
            ivEdited.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(Util.dip2px(40), Util.dip2px(40));
            layoutParams3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            itemView.addView(ivEdited, layoutParams3);

            View shadow = new View(mContext);
            shadow.setId(R.id.shadow);
            shadow.setBackgroundColor(mContext.getResources().getColor(R.color.ShadowItem));
            shadow.setVisibility(View.INVISIBLE);

            itemView.addView(shadow, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new ImagePickViewHolder(itemView);
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
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
                        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getAbsolutePath()
                                + "/IMG_" + timeStamp + ".jpg");
                        mImagePath = file.getAbsolutePath();
                        Uri uri = FileProvider.getUriForFile(mContext,mContext.getPackageName()+".provider",file);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        ((Activity) mContext).startActivityForResult(intent, ImagePickerActivity.REQUEST_CODE_TAKE_IMAGE);
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
            final ImagePickViewHolder imagePickViewHolder = (ImagePickViewHolder) holder;
            final ImageFile file;
            if (mIsNeedCamera) {
                file = mFiles.get(position - 1);
            } else {
                file = mFiles.get(position);
            }
            RequestManager requestManager = Glide.with(mContext);
            DrawableRequestBuilder requestBuilder = null;
            if (file.getEditCount() > 0) {
                requestBuilder = requestManager.load(file.getEditedPath())
                        .signature(new StringSignature(file.getEditCount() + ""));
                imagePickViewHolder.mIvEdited.setVisibility(View.VISIBLE);
            } else {
                requestBuilder = requestManager.load(file.getPath());
                imagePickViewHolder.mIvEdited.setVisibility(View.GONE);
            }
            requestBuilder
                    .centerCrop()
                    .crossFade()
                    .placeholder(R.mipmap.ic_place_holder).into(imagePickViewHolder.mIvThumbnail);

            if (file.isSelected()) {
                imagePickViewHolder.mCbx.setSelected(true);
                imagePickViewHolder.mShadow.setVisibility(View.VISIBLE);
            } else {
                imagePickViewHolder.mCbx.setSelected(false);
                imagePickViewHolder.mShadow.setVisibility(View.INVISIBLE);
            }

            imagePickViewHolder.mCbx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected() && isUpToMax()) {
                        ((PickerContract.IPickerView) mContext).showMessage(mContext.getString(R.string.most_pick) + FilePicker.getPickerConfig().getMaxNumber() + mContext.getString(R.string.files));
                        return;
                    }

                    if (v.isSelected()) {
                        imagePickViewHolder.mShadow.setVisibility(View.INVISIBLE);
                        imagePickViewHolder.mCbx.setSelected(false);
                        file.setSelected(false);
                        mSelectedList.remove(file);
                    } else {
                        imagePickViewHolder.mShadow.setVisibility(View.VISIBLE);
                        imagePickViewHolder.mCbx.setSelected(true);
                        file.setSelected(true);
                        mSelectedList.add(file);
                    }
                    RxBus.getDefault().post(new FileClickEvent(imagePickViewHolder.mCbx.isSelected(), file));

                }
            });

            imagePickViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ImageBrowserActivity.class);
                    intent.putExtra(ImageBrowserActivity.IMAGE_BROWSER_INIT_INDEX,
                            mIsNeedCamera ? imagePickViewHolder.getAdapterPosition() - 1 : imagePickViewHolder.getAdapterPosition());
                    intent.putParcelableArrayListExtra(ImageBrowserActivity.IMAGE_BROWSER_LIST, mFiles);
                    intent.putExtra(ImageBrowserActivity.IMAGE_BROWSER_SELECTED_NUMBER, mSelectedList.size());
                    ((Activity) mContext).startActivityForResult(intent, ImagePickerActivity.REQUEST_CODE_BROWSER_IMAGE);
                    ((Activity) mContext).overridePendingTransition(R.anim.zoom_in, 0);
                }
            });
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

    class ImagePickViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvThumbnail;
        private View mShadow;
        private ImageView mCbx;
        private ImageView mIvEdited;

        ImagePickViewHolder(View itemView) {
            super(itemView);
            mIvThumbnail = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            mShadow = itemView.findViewById(R.id.shadow);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
            mIvEdited = (ImageView) itemView.findViewById(R.id.iv_edited);
        }
    }

    class CameraHolder extends RecyclerView.ViewHolder {

        CameraHolder(View itemView) {
            super(itemView);
        }
    }
}
