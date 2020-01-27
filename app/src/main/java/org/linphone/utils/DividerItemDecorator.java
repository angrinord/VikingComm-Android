package org.linphone.utils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import org.linphone.R;

public class DividerItemDecorator extends DividerItemDecoration{
    private Drawable mDivider;
    private Drawable whiteDivider;

    public DividerItemDecorator (Context context, int orientation) {
        super(context, orientation);
        mDivider = ContextCompat.getDrawable(context, R.drawable.divider);
        whiteDivider = ContextCompat.getDrawable(context, R.drawable.white_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getAdapter().getItemCount();
        for (int i = 0; i < childCount; i++) {

            if (i == (parent.getAdapter().getItemCount() - 1)) {
                View child = parent.getChildAt(i);

                try{
                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int top = child.getBottom() + params.bottomMargin;
                    int bottom = top + whiteDivider.getIntrinsicHeight();

                    whiteDivider.setBounds(left, top, right, bottom);
                    whiteDivider.draw(c);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                continue;
            }

            View child = parent.getChildAt(i);

            try{
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
