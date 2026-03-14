// Adapted from http://stackoverflow.com/questions/8471075/android-listview-find-the-amount-of-pixels-scrolled

package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class ObservableListView extends ListView {

    public static interface ListViewObserver {
        public void onListViewScrolled(int deltaY);
    }

    private ListViewObserver mObserver;
    private View mTrackedChild;
    private int mTrackedChildPrevTop;
    private int mTrackedChildPrevPosition;

    public ObservableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        updateTrackingData();
    }

    private void updateTrackingData() {

        // See how our tracking has updated
        if(mTrackedChild != null && mTrackedChild.getParent() == this) {
            if(getPositionForView(mTrackedChild) == mTrackedChildPrevPosition) {
                int top = mTrackedChild.getTop();
                if(top != mTrackedChildPrevTop) {
                    if (mObserver != null) {
                        int deltaY = top - mTrackedChildPrevTop;
                        mObserver.onListViewScrolled(deltaY);
                    }

                    mTrackedChildPrevTop = top;
                }
            }
        }

        // Update our tracking reference
        if (getChildCount() > 0) {
            mTrackedChild = getChildInTheMiddle();
            mTrackedChildPrevTop = mTrackedChild.getTop();
            mTrackedChildPrevPosition = getPositionForView(mTrackedChild);
        } else {
            mTrackedChild = null;
        }
    }

    private View getChildInTheMiddle() {
        return getChildAt(getChildCount() / 2);
    }

    public void setListViewObserver(ListViewObserver observer) {
        mObserver = observer;
    }
}
