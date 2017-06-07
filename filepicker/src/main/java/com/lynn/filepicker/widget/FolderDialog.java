package com.lynn.filepicker.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lynn.filepicker.R;
import com.lynn.filepicker.RxBus;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.entity.Folder;
import com.lynn.filepicker.entity.event.FolderClickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuke on 2017/4/11.
 */

public class FolderDialog extends Dialog {

    private RecyclerView mRecyclerView;
    private DirectoryAdapter mAdapter;

    public FolderDialog(@NonNull Context context) {
        this(context, R.style.quick_option_dialog);
    }

    private FolderDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mRecyclerView = new RecyclerView(context);
        setContentView(mRecyclerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setWindowAnimations(R.style.dialog_anim);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.height = (int) (d.getHeight() * 0.7);
        p.width = d.getWidth();
        p.y = Util.dip2px(50);
        getWindow().setAttributes(p);

    }

    public void setData(List<Folder> directories) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new DirectoryAdapter((ArrayList<Folder>) directories);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    private class DirectoryAdapter extends RecyclerView.Adapter {
        private ArrayList<Folder> mList;

        DirectoryAdapter(ArrayList<Folder> list) {
            mList = list;
        }

        @Override
        public DirectoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_item_dialog_directory,parent,false);
            RelativeLayout itemView = new RelativeLayout(getContext());
            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.dip2px(80)));
            TypedValue typedValue = new TypedValue();
            Util.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            int[] attribute = new int[]{android.R.attr.selectableItemBackground};
            TypedArray typedArray = Util.getContext().obtainStyledAttributes(typedValue.resourceId, attribute);
            Drawable selectableItemBackground = typedArray.getDrawable(0);
            itemView.setBackgroundDrawable(selectableItemBackground);

            LinearLayout llContainer = new LinearLayout(getContext());
            llContainer.setOrientation(LinearLayout.VERTICAL);
            llContainer.setPadding(Util.dip2px(10), 0, 0, 0);
            RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams4.addRule(RelativeLayout.RIGHT_OF, R.id.iv_folder);

            TextView tvDirectoryName = new TextView(getContext());
            tvDirectoryName.setId(R.id.tv_title);
            tvDirectoryName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
            tvDirectoryName.setTextColor(Color.BLACK);
            tvDirectoryName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tvDirectoryName.setGravity(Gravity.BOTTOM);
            llContainer.addView(tvDirectoryName, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1));

            Space space = new Space(getContext());
            llContainer.addView(space, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.dip2px(10)));

            TextView tvFileCount = new TextView(getContext());
            tvFileCount.setId(R.id.tv_file_count);
            tvFileCount.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
            tvFileCount.setTextColor(Color.parseColor("#aaaaaa"));
            tvFileCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvFileCount.setGravity(Gravity.TOP);
            llContainer.addView(tvFileCount, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1));

            itemView.addView(llContainer, layoutParams4);

            ImageView ivFolder = new ImageView(getContext());
            ivFolder.setId(R.id.iv_folder);
            ivFolder.setImageResource(R.mipmap.ic_folder);
            ivFolder.setScaleType(ImageView.ScaleType.CENTER_CROP);
            RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(Util.dip2px(70), Util.dip2px(70));
            layoutParams1.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams1.addRule(RelativeLayout.ALIGN_LEFT, R.id.view);
            itemView.addView(ivFolder, layoutParams1);


            ImageView ivIsSelected = new ImageView(getContext());
            ivIsSelected.setId(R.id.iv_is_selected);
            ivIsSelected.setImageResource(R.mipmap.ic_is_checked);
            ivIsSelected.setPadding(Util.dip2px(10), 0, 0, 0);
            RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams3.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams3.rightMargin = Util.dip2px(10);
            itemView.addView(ivIsSelected, layoutParams3);

            View cut = new View(getContext());
            cut.setId(R.id.view);
            RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            layoutParams2.leftMargin = Util.dip2px(10);
            layoutParams2.rightMargin = Util.dip2px(10);
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            cut.setBackgroundColor(Color.parseColor("#cccccc"));
            itemView.addView(cut, layoutParams2);

            return new DirectoryHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            DirectoryHolder holder = (DirectoryHolder) viewHolder;
            Folder folder = mList.get(position);
            holder.tvDirectoryName.setText(folder.getName());
            holder.tvFileCount.setText(folder.getFiles().size() + "");
            holder.ivIsSelected.setVisibility(folder.isSelected() ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(folder.getCoverPath())) {
                Glide.with(getContext()).load(folder.getCoverPath()).centerCrop().crossFade().into(holder.ivFolder);
            } else {
                holder.ivFolder.setImageResource(R.mipmap.ic_folder);
            }
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        class DirectoryHolder extends RecyclerView.ViewHolder {
            TextView tvDirectoryName;
            TextView tvFileCount;
            ImageView ivIsSelected;
            ImageView ivFolder;

            DirectoryHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        RxBus.getDefault().post(new FolderClickEvent(mList, mList.get(getPosition()), getPosition()));
                    }
                });
                tvDirectoryName = (TextView) itemView.findViewById(R.id.tv_title);
                ivIsSelected = (ImageView) itemView.findViewById(R.id.iv_is_selected);
                ivFolder = (ImageView) itemView.findViewById(R.id.iv_folder);
                tvFileCount = (TextView) itemView.findViewById(R.id.tv_file_count);
            }
        }
    }
}
