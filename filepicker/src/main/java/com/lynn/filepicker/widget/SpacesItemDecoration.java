package com.lynn.filepicker.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by liuke on 2017/6/5.
 */

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildPosition(view) != parent.getAdapter().getItemCount() - 1)
            outRect.right = space;
        if (parent.getChildPosition(view) != 0)
            outRect.left = space;
    }
}