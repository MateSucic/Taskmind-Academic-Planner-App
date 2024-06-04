package com.matesucic.taskmind;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class ItemSpacingDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public ItemSpacingDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        int itemPosition = parent.getChildAdapterPosition(view);
        int itemCount = Objects.requireNonNull(parent.getAdapter()).getItemCount();

        if (itemPosition < itemCount - 1) {
            outRect.bottom = spacing;
        }    }
}
