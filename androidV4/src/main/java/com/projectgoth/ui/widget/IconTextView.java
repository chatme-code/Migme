package com.projectgoth.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.imagefetcher.ImageHandler;

/**
 * Created by houdangui on 28/4/15.
 */
public class IconTextView extends LinearLayout  {

    private Context mContext;
    private ImageView imageView;
    private TextView textView;
    private boolean mUseShortCount;
    private int mIconSize;
    private int mSpacing;
    private int mTextSize;
    private int mCounter;

    public IconTextView(Context context) {
        super(context);
        this.mContext = context;

        init();
    }

    public IconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.IconTextView,
                0, 0);

        try {
            mUseShortCount = a.getBoolean(R.styleable.IconTextView_useShortCount, true);
            mSpacing = a.getDimensionPixelSize(R.styleable.IconTextView_iconToTextSpace, 0);
            mTextSize = a.getDimensionPixelSize(R.styleable.IconTextView_textSize, 0);
            mIconSize = a.getDimensionPixelSize(R.styleable.IconTextView_iconSize, 0);
        } finally {
            a.recycle();
        }

        init();
    }


    private void init() {
        imageView = new ImageView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayoutEx.LayoutParams(mIconSize, mIconSize);
        addView(imageView, layoutParams);

        textView = new TextView(mContext);
        layoutParams = new LinearLayoutEx.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(mSpacing, 0, 0, 0);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        textView.setTextColor(ApplicationEx.getColor(R.color.default_timestamp));
        mCounter = 0;
        textView.setText("0");
        addView(textView, layoutParams);
    }

    public void setCounter(int newValue) {
        if (newValue != mCounter) {
            mCounter = newValue;

            String strCount = Tools.formatCounters(mCounter, mUseShortCount ? 999 : Integer.MAX_VALUE);
            textView.setText(strCount);
        }
    }

    public void setIconResource(int drawableResourceId) {
        imageView.setImageResource(drawableResourceId);
    }

    /**
     * the view of displaying Emotional Footprint can be changed when the image fetched from server
     * because the list will recycle the view of the list item. so we send a event to refresh list
     * when image loaded
     *
     * @param url
     * @param defaultResourceId
     */
    public void loadEmotionalFootprintOnPostList(String url, int defaultResourceId) {
        if (!TextUtils.isEmpty(url)) {
            BitmapLoadListener loadListener = new BitmapLoadListener();
            Bitmap bitmap = ImageHandler.getInstance().loadImage(url, null, defaultResourceId, -1, -1, loadListener);

            if (bitmap == null) {
                setIconResource(defaultResourceId);
                loadListener.setBmpLoadedFromCache(false);
            } else {
                imageView.setImageBitmap(bitmap);
                loadListener.setBmpLoadedFromCache(true);
            }
        } else {
            setIconResource(defaultResourceId);
        }
    }

    private static class BitmapLoadListener implements ImageHandler.ImageLoadListener {

        /**
         * by default it is true otherwise there's no chance to change to true since the
         * loading from cache is synchronised option
         */
        private boolean mIsBmpLoadedFromCache = true;

        public void setBmpLoadedFromCache(boolean isBmpLoadedCache) {
            this.mIsBmpLoadedFromCache = isBmpLoadedCache;
        }

        @Override
        public void onImageLoaded(Bitmap bitmap) {
            if (!mIsBmpLoadedFromCache) {
                //cannot create image span with the bmp loaded of emotions in TextViewEx directly, so send a event
                //to refresh the ui
                Logger.debug.log("BitmapLoadListener",  "sendBitmapFetched");
                BroadcastHandler.Post.sendEmotionalFootprintBmpFetch();
            }
        }

        @Override
        public void onImageFailed(ImageView imageView) {

        }
    }

}
