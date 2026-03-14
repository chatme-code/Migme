// Code based on answer here:
// http://stackoverflow.com/questions/10613552/pinned-groups-in-expandablelistview

package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

/**
 * A ListView that maintains a header pinned at the top of the list. The
 * pinned header can be pushed up and dissolved as needed.
 */
public class PinnedHeaderExpandableListView extends ExpandableListView {

    enum PinnedHeaderState {
        GONE,
        VISIBLE,
        PUSHED_UP,
        PUSHED_UP2;
    }

    private static final int PINNED_BACKGROUND_ALPHA = 240;

    private View mHeaderView;
    private boolean mForceUpdate = false;
    private boolean mHeaderViewVisible;

    private int mHeaderViewWidth;
    private int mHeaderViewHeight;

    private int mPositionY = 0;
    private int mLastGroup = -1;

    public PinnedHeaderExpandableListView(Context context) {
        super(context);
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPinnedHeaderView(View view) {
        mHeaderView = view;

        // Disable vertical fading when the pinned header is present
        // in this particular case we would like to disable the top, but not the bottom edge.
        if (mHeaderView != null) {
            setFadingEdgeLength(0);
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);

            // Since our cells are match_parent for width, capture the widthMeasureSpec
            mHeaderViewWidth = widthMeasureSpec;
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderView != null) {
            mHeaderView.layout(getPaddingLeft(),
                    mPositionY+getPaddingTop(),
                    mHeaderViewWidth-getPaddingRight(),
                    mPositionY+mHeaderViewHeight-getPaddingBottom());
            configureHeaderView(getFirstVisiblePosition());
        }
    }

    /**
     * animating header pushing
     * @param position
     */
    public void configureHeaderView(int position) {
        if (mHeaderView == null) {
            return;
        }

        final int group = getPackedPositionGroup(getExpandableListPosition(position));
        int nextSectionPosition = getFlatListPosition(getPackedPositionForGroup(group+1));
        ExpandableListAdapter adapter = getExpandableListAdapter();

        PinnedHeaderState state;
        if(position < 0) {
            state = PinnedHeaderState.GONE;
        } else if (adapter.getGroupCount() == 0) {
            state = PinnedHeaderState.GONE;
        } else if (isGroupExpanded(group) == false) {
            state = PinnedHeaderState.GONE;
        } else if(adapter.getChildrenCount(group) == 0) {
            state = PinnedHeaderState.GONE;
        } else if (position == nextSectionPosition - 1) {
            state = PinnedHeaderState.PUSHED_UP;
        } else if(position == nextSectionPosition - 2) {
            state = PinnedHeaderState.PUSHED_UP2;
        } else state = PinnedHeaderState.VISIBLE;

        switch (state) {
        case GONE:
            mHeaderViewVisible = false;
            break;

        case VISIBLE:
            int groupView = getFlatListPosition(getPackedPositionForGroup(group));
            if(position <= groupView) {
                View firstView = getChildAt(0);
                if(firstView != null && firstView.getBottom() >= mHeaderViewHeight) {
                    mHeaderViewVisible = false;
                    break;
                }
            }
            updateHeaderView(group, 0);
            break;

        case PUSHED_UP:
            {
                View firstView = getChildAt(0);
                int y = 0;
                if (firstView != null) {
                    int bottom = firstView.getBottom();
                    if (bottom < mHeaderViewHeight) {
                        y = (bottom - mHeaderViewHeight);
                    }
                }

                updateHeaderView(group, y);
            }
            break;

        case PUSHED_UP2:
            {
                View secondView = getChildAt(1);
                int y = 0;
                if (secondView != null) {
                    int bottom = secondView.getBottom();
                    if (bottom < mHeaderViewHeight) {
                        y = (bottom - mHeaderViewHeight);
                    }
                }

                updateHeaderView(group, y);
            }
            break;
        }
    }

    @Override
    protected boolean drawChild (Canvas canvas, View child, long drawingTime) {
        boolean result = false;
        if(child != null) {
            result = super.drawChild(canvas, child, drawingTime);
            int childCount = getChildCount();
            if(mHeaderViewVisible && childCount >= 2 && getChildAt(childCount-1) == child) {
                if(drawChild(canvas, mHeaderView, drawingTime)) return true;
            }
        }
        return result;
    }

    private void updateHeaderView(int groupIndex, int newY) {
        if(groupIndex != mLastGroup) {
            mLastGroup = groupIndex;
            getExpandableListAdapter().getGroupView(groupIndex, true, mHeaderView, null);
        }
        if (mForceUpdate || mHeaderView.getTop() != newY) {
            mForceUpdate = false;
            mPositionY = newY;
            measureChild(mHeaderView, mHeaderViewWidth, mHeaderViewHeight);

            mHeaderView.layout(getPaddingLeft(),
                    mPositionY + getPaddingTop(),
                    mHeaderViewWidth - getPaddingRight(),
                    mHeaderViewHeight + mPositionY - getPaddingBottom());
        }
        if(mHeaderView.getBackground() != null) {
            mHeaderView.getBackground().setAlpha(PINNED_BACKGROUND_ALPHA);
        }
        mHeaderViewVisible = true;
    }

    @Override protected void handleDataChanged() {
        super.handleDataChanged();
        mForceUpdate = true;
        mLastGroup = -1;
        configureHeaderView(getFirstVisiblePosition());
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent event) {
        if(mHeaderViewVisible && mHeaderView != null
            && event.getAction() == MotionEvent.ACTION_DOWN
            && event.getY() < mHeaderView.getBottom()) {

            long groupPackedPos = ExpandableListView.getPackedPositionForGroup(mLastGroup);
            final int groupFlatPos = getFlatListPosition(groupPackedPos);
            post(new Runnable() {
                @Override public void run() {
//                    smoothScrollToPosition(groupFlatPos);
                    setSelection(groupFlatPos);
                }
            });
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }
}
