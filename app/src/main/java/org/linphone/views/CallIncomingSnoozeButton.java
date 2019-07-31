package org.linphone.views;

/*
CallIncomingDeclineButton.java
Copyright (C) 2018  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import org.linphone.R;

public class CallIncomingSnoozeButton extends LinearLayout
        implements View.OnClickListener, View.OnTouchListener {
    private LinearLayout mRoot;
    private CallIncomingButtonListener mListener;
    private View mSpacer;
    private int scrollHeight;
    private float mDeclineY, mOldSize;
    private boolean mBegin;

    public CallIncomingSnoozeButton(Context context) {
        super(context);
        init();
    }

    public CallIncomingSnoozeButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CallIncomingSnoozeButton(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(CallIncomingButtonListener listener) {
        mListener = listener;
    }

    private void init() {
        inflate(getContext(), R.layout.call_incoming_snooze_button, this);
        mRoot = findViewById(R.id.root);
        mRoot.setOnClickListener(this);
        mRoot.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        performClick();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float curY;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mSpacer.setVisibility(View.GONE);
                mDeclineY = motionEvent.getY() - mRoot.getHeight();
                mBegin = true;
                mOldSize = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                curY = motionEvent.getY() - mRoot.getHeight();
                view.scrollBy(view.getScrollX(), (int) (mDeclineY - curY));
                mOldSize -= mDeclineY - curY;
                mDeclineY = curY;
                if (mOldSize < -25) mBegin = false;
                if (curY < (scrollHeight / 4) - mRoot.getHeight() && !mBegin) {
                    performClick();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                mSpacer.setVisibility(View.VISIBLE);
                view.scrollTo(view.getScrollX(), 0);
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        if (mListener != null) {
            mListener.onAction();
        }
        return true;
    }

    public void setSpacer(View mSpacer) {
        this.mSpacer = mSpacer;
    }
    public void setScrollHeight(int width){
        this.scrollHeight = width;
    }
}
