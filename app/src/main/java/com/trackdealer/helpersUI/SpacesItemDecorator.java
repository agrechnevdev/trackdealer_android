package com.trackdealer.helpersUI;

/**
 * Created by grechnev-av on 08.09.2017.
 */

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Декоратор для recyclerView (Расстановка пробелов)
 */
public class SpacesItemDecorator extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecorator(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;

        if (parent.getChildAdapterPosition(view) == 0)
            outRect.top = space;
    }
}