package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;

/**
 * Created by lopenny on 1/28/15.
 */
public class TimelineView extends View {

    public enum Type {
        UPPER, CENTRAL, BOTTOM, SINGLE;
    }
    private Paint mPaint;
    private Type mDrawType = Type.CENTRAL;

    private int mHeight;
    private int mCenterX;
    private int mCenterY;
    private int mGap;

    public TimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(ApplicationEx.getColor(R.color.time_line));

        mGap = getSize(R.dimen.timeline_dot_gap);
    }

    public void setLineType(Type type) {
        mDrawType = type;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mHeight = getHeight();
        mCenterX = getWidth() / 2;
        mCenterY = mHeight / 2;
            switch (mDrawType) {
                case SINGLE:
                    setDotPaint(canvas, true);
                    break;
                case UPPER:
                    setDotPaint(canvas, true);
                    setLinePaint(canvas, true);
                    break;
                case BOTTOM:
                    setDotPaint(canvas, true);
                    setLinePaint(canvas, false);
                    break;
                case CENTRAL:
                default:
                    setDotPaint(canvas, false);
                    setLinePaint(canvas, true);
                    setLinePaint(canvas, false);
                    break;
            }
    }

    private int getSize(int dimenId) {
        return ApplicationEx.getDimension(dimenId);
    }

    private void setLinePaint(Canvas canvas, boolean upper) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(getSize(R.dimen.timeline_width));
        if (upper) {
            canvas.drawLine(mCenterX, mCenterY + mGap, mCenterX, mHeight, mPaint);
        } else {
            canvas.drawLine(mCenterX, 0, mCenterX, mCenterY - mGap, mPaint);
        }
    }

    private void setDotPaint(Canvas canvas, boolean large) {
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (large) {
            mPaint.setStrokeWidth(getSize(R.dimen.timeline_dot_large));
        } else {
            mPaint.setStrokeWidth(getSize(R.dimen.timeline_dot));
        }
        canvas.drawCircle(mCenterX, mCenterY, getSize(R.dimen.timeline_dot), mPaint);
    }
}
