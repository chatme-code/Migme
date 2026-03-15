package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;

/**
 * Created by jtlim on 13/8/14.
 */
public class PostViewContainer extends RelativeLayout {

    private boolean mShowSidebar;

    private static final Paint paint;
    private static final int SIDEBAR_WIDTH;

    static {
        paint = new Paint();
        paint.setColor(Theme.getColor(ThemeValues.PROMOTED_POST_MARKER_BG_COLOR));

        SIDEBAR_WIDTH = ApplicationEx.getDimension(R.dimen.side_marker_width);
    }

    public PostViewContainer(Context context) {
        super(context);
    }

    public PostViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostViewContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void hideSideMarker() {
        mShowSidebar = false;
        setPadding(0, 0, 0, 0);
        requestLayout();
    }

    public void showSideMarker() {
        mShowSidebar = true;
        setPadding(SIDEBAR_WIDTH, 0, 0, 0);
        requestLayout();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mShowSidebar) {
            Rect r = canvas.getClipBounds();
            canvas.drawRect(0, r.top, SIDEBAR_WIDTH, r.bottom, paint);
        }
    }
}
