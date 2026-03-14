/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.projectgoth.imagefetcher;

import java.io.InputStream;
import java.util.Random;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Labels;
import com.projectgoth.b.data.Photo;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.ui.widget.NoUnderlineClickableSpan;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.ProfileUtils;

/**
 * An assortment of UI helpers.
 */
@SuppressLint("NewApi")
public class UIUtils {
    
    public static final String LOG_TAG = AndroidLogger.makeLogTag(UIUtils.class);
    
    public static final int DEFAULT_CORNER_RADIUS          = 4;
    public static final int DEFAULT_BUTTONEX_CORNER_RADIUS = 2;

    /**
     * Size of {@link Photo} to be displayed
     * 
     * @author warrenbalcos Mar 5, 2012
     */
    public enum PhotoSize {
        NORMAL(10000), _48X(48), _96X(96), _120X(120), _200X(200), _240X(240), _320X(
                320), _480X(480), _800X(800);

        private int size;

        private PhotoSize(int size) {
            this.size = size;
        }

        /**
         * @return the size
         */
        public int getSize() {
            return size;
        }
    }
    
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS   = 60 * MINUTE_MILLIS;

    @SuppressWarnings("unused")
    private static final int DAY_MILLIS    = 24 * HOUR_MILLIS;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setActivatedCompat(View view, boolean activated) {
        if (hasHoneycomb()) {
            view.setActivated(activated);
        }
    }

    public static View getActionBarView(Activity activity) {
        Window window = activity.getWindow();
        View v = window.getDecorView();

        int actionViewResId = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            actionViewResId = activity.getResources().getIdentifier(
                    "action_bar_container", "id", activity.getPackageName());
        } else {
            actionViewResId = activity.getResources().getIdentifier(
                    "action_bar_container", "id", "android");
        }
        if (actionViewResId > 0) {
            return v.findViewById(actionViewResId);
        }

        return null;
    }

    public static int getActionBarHeight(Activity activity){
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return 0;
    }


    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static boolean isGoogleTV(Context context) {
        return context.getPackageManager().hasSystemFeature("com.google.android.tv");
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed
        // behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * Check if the device is 2.3.x
     * @return true if the os version is less than 2.3.3
     */
    public static boolean isLegacyDevice() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasHoneycombMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isJellyBeanMr1() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1;
    }
    
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return hasHoneycomb() && isTablet(context);
    }

    @SuppressWarnings("deprecation")
    public static void setBackground(View view, Drawable drawable) {
        if (view == null)
            return;

        if (UIUtils.hasJellyBean()) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    @SuppressWarnings("deprecation")
    public static void setPluginState(WebSettings websettings, boolean state) {
        if (websettings == null)
            return;

        if (UIUtils.hasFroyo()) {
            websettings.setPluginState(state ? PluginState.ON : PluginState.OFF);
        }
    }

    @SuppressWarnings("deprecation")
    public static Point getDisplaySize(Display display) {
        Point outSize = new Point();

        if (UIUtils.hasHoneycombMR2()) {
            display.getSize(outSize);
        } else {
            outSize.x = display.getWidth();
            outSize.y = display.getHeight();
        }

        return outSize;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = Theme.getColor(ThemeValues.ROUNDED_CORNERS_COLOR);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(false);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        if (bitmap != output) {
            bitmap.recycle();
        }
        return output;
    }

    /**
     * Get the photo url based on size. Also checks for the next available size
     * if {@link PhotoSize} if unavailable.
     * 
     * @param photo
     * @param size
     *            - specific {@link PhotoSize} requested
     * @param maxSize
     *            - max size
     * @return
     */

    static public String getPhotoUrl(Photo photo, PhotoSize size, int maxSize) {
        String url = getPhotoUrl(photo, size);

        // Auto fallback if size requested is not available
        if (url == null) {
            int preferredSize = PhotoSize._480X.getSize();
            if (maxSize > 1) {
                preferredSize = maxSize;
            } else if (size != null) {
                preferredSize = size.getSize();
            }
            url = photo.getNearestAvailableUrl(preferredSize);
        }
        return url;
    }
    
    
    /**
     * Get the specific {@link Photo} url from {@link PhotoSize} returns null if
     * size is not available
     * 
     * @param photo
     * @param size
     *            - specific {@link PhotoSize} requested
     * @return
     */
    public static String getPhotoUrl(Photo photo, PhotoSize size) {
        if (null == size) {
            return null;
        }
        switch (size) {
            case NORMAL:
                return photo.getUrl();
            case _120X:
                return photo.getUrl120x();
            case _200X:
                return photo.getUrl200x();
            case _240X:
                return photo.getUrl240x();
            case _320X:
                return photo.getUrl320x();
            case _480X:
                return photo.getUrl480x();
            case _800X:
                return photo.getUrl800x();
            case _48X:
                return photo.getUrl48x();
            case _96X:
                return photo.getUrl96x();
        }
        return null;
    }
    
    public static int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.top) + 2;
    }
    
    public static int getTextWidth(String text, float fontSize, Typeface typeface) {
        Paint paint = new Paint();
        paint.setTypeface(typeface);
        paint.setTextSize(fontSize);
        
        return  (int)paint.measureText(text);
    }
    
    
    public interface LinkClickListener {
        void onClick();
    }
    
    public static void setLinkSpan(SpannableString spannable, String fullText, String linkText, final LinkClickListener clickListener) {
        if (spannable == null || fullText == null || linkText == null) {
            return;
        }
        
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ApplicationEx.getColor(R.color.default_green));
        NoUnderlineClickableSpan clickableSpan = new NoUnderlineClickableSpan(clickListener);
        int start = fullText.indexOf(linkText);
        if (start < 0) {
            //cannot find the linkText in the full Text
            return;
        }
        int end = start + linkText.length();
        spannable.setSpan(clickableSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(colorSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);        
    }

    static private int[] chatroomColorResArray = new int[] {
            R.color.chatroom_icon_green,
            R.color.chatroom_icon_bg_purple,
            R.color.chatroom_icon_bg_red,
            R.color.chatroom_icon_bg_brown,
            R.color.chatroom_icon_bg_orange
    };

    static private int[] chatroomColorArray;

    static public int[] getChatroomColorArray() {
        if (chatroomColorArray == null) {
            chatroomColorArray = new int[chatroomColorResArray.length];
            for (int i = 0; i < chatroomColorResArray.length; i++) {
                chatroomColorArray[i] = ApplicationEx.getColor(chatroomColorResArray[i]) ;
            }
        }
        return chatroomColorArray;
    }

    public static int getRandomChatroomColor() {
        int[] colorArray = getChatroomColorArray();
        int randomIndex = new Random().nextInt(colorArray.length);
        return  colorArray[randomIndex];
    }

    public static int getUsernameColorFromLabels(Labels labels, boolean isInOwnChatroom) {
        if (labels != null) {
            String colorStr = labels.getColor();            
            if (colorStr != null) {
                colorStr = colorStr.trim().toLowerCase();
                if (colorStr.length() > 0) {
                    try {
                        // If the label is merchant or label is global admin or isInOwnChatroom
                        // then return the color provided by the server.
                        if (ProfileUtils.isMerchant(labels) || ProfileUtils.isGlobalAdmin(labels) || isInOwnChatroom) {
                            return Color.parseColor(
                                    colorStr.startsWith(Constants.HASH_TAG) ? colorStr : "#" + colorStr);
                        }
                    }
                    catch (IllegalArgumentException ex) {
                        Logger.error.log(LOG_TAG, "Failed to parse color ", colorStr, " for profile label.");
                    }
                }
            }
        }
        
        return ApplicationEx.getColor(R.color.default_green);
    }
    
    public static String getRRGGBBString(final int color) {
        return String.format("%06X", (0xFFFFFF & color));
    }

    public static Bitmap getBitmapFromDrawableResource(Context context, int imageResId) {
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;

            //recycle
            opt.inPurgeable = true;

            //share a reference to the input data
            opt.inInputShareable = true;

            InputStream is = context.getResources().openRawResource(imageResId);
            return BitmapFactory.decodeStream(is, null, opt);
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, "Failed to decode image resource");
            return null;
        }
    }
}
