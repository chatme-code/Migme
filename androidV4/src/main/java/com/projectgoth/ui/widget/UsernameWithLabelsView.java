package com.projectgoth.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Labels;
import com.projectgoth.b.enums.UserLabelAdminEnum;
import com.projectgoth.b.enums.UserLabelMerchantEnum;
import com.projectgoth.imagefetcher.UIUtils;

/**
 *
 * UsernameWithLabelsView will show a username followed by (V) (A) (M) icons as appropriate
 *
 * XML Tags (xmlns:migme="http://schemas.android.com/apk/res/com.projectgoth")
 *
 * @param migme:labelSize          - Required
 * @param migme:textSize           - Defaults to dimen.text_size_small
 *
 */
public class UsernameWithLabelsView extends View {

    private final int       mLabelSize;
    private final int       mTextSize;

    private String          mUsername="";
    private CharSequence    mEllipsizedUsername="";

    private Drawable        mVerifiedDrawable;
    private Drawable        mAdminDrawable;
    private Drawable        mMerchantDrawable;

    private final Paint     mTextPaint;

    private int             mTextX;
    private int             mTextY;
    private int             mTotalLabelWidth;
    private int             mLabelX;
    private float           mTextWidth;
    private float           mTextHeight;

    private static final Drawable VERIFIED;
    private static final Drawable GROUP_ADMIN;
    private static final Drawable CHATROOM_ADMIN;
    private static final Drawable GLOBAL_ADMIN;
    private static final Drawable MERCHANT_MENTOR;
    private static final Drawable MERCHANT;
    private static final int      NAME_TO_LABEL_SPACING;
    private static final int      LABEL_SPACING;

    static {
        Resources resources = ApplicationEx.getInstance().getResources();
        VERIFIED = resources.getDrawable(R.drawable.ad_verified_green);
        GROUP_ADMIN = resources.getDrawable(R.drawable.ad_grpadmin_orange);
        CHATROOM_ADMIN = resources.getDrawable(R.drawable.ad_chatadmin_orange);
        GLOBAL_ADMIN = resources.getDrawable(R.drawable.ad_admin_orange);
        MERCHANT_MENTOR = resources.getDrawable(R.drawable.ad_merchmentor_purple);
        MERCHANT = resources.getDrawable(R.drawable.ad_merch_purple);

        NAME_TO_LABEL_SPACING = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics());
        LABEL_SPACING = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, resources.getDisplayMetrics());
    }

    public UsernameWithLabelsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UsernameWithLabelsView,
                0, 0);

        try {
            mTextSize = a.getDimensionPixelSize(R.styleable.UsernameWithLabelsView_textSize, 0);
            mLabelSize = a.getDimensionPixelSize(R.styleable.UsernameWithLabelsView_labelSize, 0);
        } finally {
            a.recycle();
        }

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(ApplicationEx.getColor(R.color.default_green));
        if(mTextSize != 0) mTextPaint.setTextSize(mTextSize);
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setUsername(String username) {
        mUsername = username;
        requestLayout();
    }

    public void setLabels(Labels labels) {
        setLabels(labels, true);
    }
    
    public void setLabels(Labels labels, boolean shouldSetUsernameColorFromLabels) {
        if (labels == null) {
            return;
        }
        mVerifiedDrawable = labels.isVerified() ? VERIFIED : null;

        UserLabelMerchantEnum merchant = labels.getMerchant();
        if(merchant == UserLabelMerchantEnum.MERCHANT_MENTOR) {
            mMerchantDrawable = MERCHANT_MENTOR;
        } else if(merchant == UserLabelMerchantEnum.MERCHANT_SUBMENTOR) {
            mMerchantDrawable = MERCHANT;
        } else {
            mMerchantDrawable = null;
        }

        UserLabelAdminEnum admin = labels.getAdmin();
        if(admin == UserLabelAdminEnum.GLOBAL_ADMIN) {
            mAdminDrawable = GLOBAL_ADMIN;
        } else if(admin == UserLabelAdminEnum.CHATROOM_ADMIN) {
            mAdminDrawable = CHATROOM_ADMIN;
        } else if(admin == UserLabelAdminEnum.GROUP_ADMIN) {
            mAdminDrawable = GROUP_ADMIN;
        } else {
            mAdminDrawable = null;
        }
        
        if (shouldSetUsernameColorFromLabels) {
            setTextColor(UIUtils.getUsernameColorFromLabels(labels, false));
        }

        requestLayout();
    }

    protected int getNonTextWidth() {
        int padding = getPaddingLeft() + getPaddingRight();
        int numberOfLabels = 0;
        if(mVerifiedDrawable != null) numberOfLabels++;
        if(mMerchantDrawable != null) numberOfLabels++;
        if(mAdminDrawable != null) numberOfLabels++;
        mTotalLabelWidth = numberOfLabels == 0 ? 0 : NAME_TO_LABEL_SPACING + numberOfLabels * (mLabelSize + LABEL_SPACING);

        return padding + mTotalLabelWidth;
    }

    private void measureString() {
        mTextWidth = mTextPaint.measureText(mUsername);

        // Just to get the height.
        Rect r = new Rect();
        mTextPaint.getTextBounds(mUsername, 0, mUsername.length(), r);
        mTextHeight = mTextSize + r.bottom;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureString();

        int minw = getNonTextWidth() + (int) mTextWidth;
        int w = resolveSize(minw, widthMeasureSpec);

        int minh = getPaddingBottom() + getPaddingTop() + Math.max((int) mTextHeight+1, mLabelSize);
        int h = resolveSize(minh, heightMeasureSpec);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mTextX = 0;
        mTextY = (int) ((bottom-top - mTextSize) * 0.5 - mTextPaint.ascent());

        if(TextUtils.isEmpty(mUsername))
        {
            mEllipsizedUsername = "";
            mTextWidth = 0;
            mLabelX = 0;
        }
        else
        {
            mEllipsizedUsername = TextUtils.ellipsize(mUsername, new TextPaint(mTextPaint), right-left-mTotalLabelWidth, TextUtils.TruncateAt.END);
            mTextWidth = mTextPaint.measureText(mEllipsizedUsername, 0, mEllipsizedUsername.length());
            mLabelX = (int) mTextWidth + NAME_TO_LABEL_SPACING;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawText(mEllipsizedUsername, 0, mEllipsizedUsername.length(), mTextX, mTextY, mTextPaint);

        int x = mLabelX;
        final int labelTop = (getBottom()-getTop()-mLabelSize)/2;

        if(mVerifiedDrawable != null) {
            mVerifiedDrawable.setBounds(x, labelTop, x+mLabelSize, labelTop+mLabelSize);
            mVerifiedDrawable.draw(canvas);
            x += mLabelSize + LABEL_SPACING;
        }
        if(mMerchantDrawable != null) {
            mMerchantDrawable.setBounds(x, labelTop, x+mLabelSize, labelTop+mLabelSize);
            mMerchantDrawable.draw(canvas);
            x += mLabelSize + LABEL_SPACING;
        }
        if(mAdminDrawable != null) {
            mAdminDrawable.setBounds(x, labelTop, x+mLabelSize, labelTop+mLabelSize);
            mAdminDrawable.draw(canvas);
        }
    }
}
