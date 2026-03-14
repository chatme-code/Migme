/**
 * Copyright (c) 2013 Project Goth
 *
 * Tools.java
 * Created May 30, 2013, 12:55:13 AM
 */

package com.projectgoth.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.migme.commonlib.enums.ImageFileType;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Author;
import com.projectgoth.b.data.BaseProfile;
import com.projectgoth.b.data.DisplayImage;
import com.projectgoth.b.data.Labels;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.enums.ImageSizeEnum;
import com.projectgoth.b.enums.PostApplicationEnum;
import com.projectgoth.b.enums.UserLabelAdminEnum;
import com.projectgoth.b.enums.UserLabelMerchantEnum;
import com.projectgoth.b.enums.UserProfileGenderEnum;
import com.projectgoth.b.enums.UserProfileRelationshipEnum;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.controller.ThirdPartyIMController;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.AppEvents;
import com.projectgoth.fusion.packet.UrlHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.model.InputString;
import com.projectgoth.nemesis.model.ContactGroup;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.BaseEmoticon.EmoticonType;
import com.projectgoth.ui.fragment.BaseDialogFragment;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.CrashlyticsLog;
import com.projectgoth.util.MathUtils;

/**
 * @author cherryv
 * 
 */
public class Tools {

    private static final String                    TAG                      = AndroidLogger.makeLogTag(Tools.class);
    private final static float                     DEFAULT_MEMCACHE_PERCENT = 0.05f;

    //@formatter:off
    private static int                             memCacheSize             = Math.round(DEFAULT_MEMCACHE_PERCENT * Runtime.getRuntime().maxMemory() / 1024);

    public enum TimeFormat {
        MISSING_YEAR,
        MISSING_MONTH,
        MISSING_DAY,
        FULL
    };

    private static final LruCache<Integer, Bitmap> loadingBitmaps           = new LruCache<Integer, Bitmap>(memCacheSize) {
        @Override
        protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
            if (!UIUtils.hasHoneycomb()) {
                if (oldValue != newValue) {
                    oldValue.recycle();
                }
            }
        }
    };
    //@formatter:on

    public static ShapeDrawable getCircleDrawable(int color, int width, int height) {
        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.setIntrinsicWidth(width);
        circle.setIntrinsicHeight(height);
        circle.setBounds(new Rect(0, 0, width, height));
        circle.getPaint().setColor(color);

        return circle;
    }

    /**
     * @param context
     * @param icon
     * @return
     */
    public static int getDrawableResId(Context context, String name) {
        if (name.startsWith(Constants.LINK_DRAWABLE)) {
            int idx = name.indexOf(Constants.LINK_DRAWABLE);
            name = name.substring(idx + Constants.LINK_DRAWABLE.length());
        }

        Resources res = context.getResources();
        int resourceId = res.getIdentifier(name, "drawable", context.getPackageName());
        return resourceId;
    }
    
    public static Bitmap getBitmap(Context context, String name) {
        int resourceId = getDrawableResId(context, name);

        return loadBitmapFromCache(resourceId);
    }

    public static Bitmap getBitmap(int resourceId) {
        return loadBitmapFromCache(resourceId);
    }

    private static Bitmap loadBitmapFromCache(int resId) {
        if (resId > 0) {
            Bitmap loadingBitmap = loadingBitmaps.get(resId);
            if (null == loadingBitmap) {
                loadingBitmap = BitmapFactory.decodeResource(ApplicationEx.getInstance().getResources(), resId);
                if (loadingBitmap != null) {
                    loadingBitmaps.put(resId, loadingBitmap);
                    return loadingBitmap;
                }
            } else {
                return loadingBitmap;
            }
        }

        return null;
    }

    public static void makeBackgroundTiled(View view) {
        if (view != null) {
            Drawable bg = view.getBackground();
            if (bg instanceof BitmapDrawable) {
                ((BitmapDrawable) bg).setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            }
        }
    }

    public static BitmapDrawable getBitmapDrawable(int imageResource) {
        BitmapDrawable bitmap = (BitmapDrawable) ApplicationEx.getInstance().getResources().getDrawable(imageResource);
        return bitmap;
    }

    public static BitmapDrawable createHorizontallyTiledImage(int imageResource) {
        BitmapDrawable bitmap = (BitmapDrawable) ApplicationEx.getInstance().getResources().getDrawable(imageResource);
        bitmap.setTileModeX(TileMode.REPEAT);
        return bitmap;
    }

    public static String getDrawableUri(String filename) {
        return Constants.LINK_DRAWABLE + filename;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        if (bm == null) {
            return null;
        }

        int width = bm.getWidth();
        int height = bm.getHeight();
        if (width == 0 || height == 0 || newWidth == 0 || newHeight == 0) {
            return null;
        }

        float scaleWidth = (float) newWidth / (float) width;
        float scaleHeight = (float) newHeight / (float) height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = bm;
        try {
            resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        } catch (OutOfMemoryError error) {
            Logger.error.log(TAG, error);
        }
        return resizedBitmap;
    }

    /**
     * Read contents of file
     * 
     * @param fileName
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String readFromfile(String fileName, Context context) {
        StringBuilder ReturnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets().open(fileName, Context.MODE_WORLD_READABLE);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                ReturnString.append(line);
            }
        } catch (Exception e) {
            Logger.error.log(TAG, e);
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (Exception e2) {
                    Logger.error.log(TAG, e2);
                }
            }
            if (fIn != null) {
                try {
                    fIn.close();
                } catch (Exception e2) {
                    Logger.error.log(TAG, e2);
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e2) {
                    Logger.error.log(TAG, e2);
                }
            }
        }
        return ReturnString.toString();
    }

    public static float getPixels(DisplayMetrics dm, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    public static int getPixels(float dp) {
        float scale = Config.getInstance().getScreenScale();
        if (scale != 0) {
            int pixels = (int) (dp * scale);
            // low density phone's screen scale is 0.5. if client request for
            // 1dp will reuturn 0
            // min value to return should be 1 if requested dp > 0
            if (pixels <= 0 && dp > 0)
                return 1;
            return pixels;
        }
        return (int) dp;
    }

    public static int getScaledPixels(float sp) {
        float scale = Config.getInstance().getFontScale();
        if (scale != 0) {
            return (int) (sp * scale + 0.5);
        }
        return (int) sp;
    }

    /**
     * Helper method for creating a mig33 URL
     * 
     * @param tag
     *            Base method or tag
     * @param params
     *            Parameters needed
     * @return
     */
    public static String constructMigUrl(String tag, String... params) {
        StringBuilder sb = new StringBuilder(Constants.LINK_MIG33);
        sb.append(tag);
        sb.append("('");
        TextUtils.join(sb, ',', params);
        sb.append("')");
        return sb.toString();
    }

    /**
     * @param imageName
     * @param size
     * @param monotone
     * @return
     */
    public static String constructBadgeUrl(String imageName, int size, boolean monotone) {
        String filename = imageName.toLowerCase();
        if (monotone) {
            filename = "monotone/" + filename;
        }
        String url = UrlHandler.getInstance().getImagesUrl()
                + String.format(Constants.BADGES_PATH, size, size, filename);

        return url;
    }

    /**
     * Helper method to extract the requested image from DisplayImage. Fallback
     * mechanism is implemented
     * 
     * @param image
     * @param size
     * @return
     */
    public static String getUrlFromImage(DisplayImage image, ImageSizeEnum size) {
        ImageSizeEnum sizes[] = new ImageSizeEnum[] {
                ImageSizeEnum.SIZE_120X,
                ImageSizeEnum.SIZE_96X,
                ImageSizeEnum.SIZE_64X,
                ImageSizeEnum.SIZE_48X,
                ImageSizeEnum.SIZE_32X,
                ImageSizeEnum.SIZE_24X,
                ImageSizeEnum.SIZE_16X,
                ImageSizeEnum.SIZE_14X,
                ImageSizeEnum.SIZE_12X, };

        String result = null;
        int count = sizes.length;
        int idx = 0;
        for (idx = 0; idx < count; idx++) {
            if (size == sizes[idx]) {
                for (int n = idx; n < count; n++) {
                    result = image.getUrlFromSize(sizes[n]);
                    if (result != null) {
                        return result;
                    }
                }
                break;
            }
        }
        return null;
    }

    public static int getFusionPresenceResource(final PresenceType presence) {
        switch (presence) {
            case AVAILABLE:
                return R.drawable.ic_presence_online;
            case AWAY:
                return R.drawable.ic_presence_away;
            case BUSY:
                return R.drawable.ic_presence_busy;
            default:
                return R.drawable.ic_presence_offline;
        }
    }

    public static String getTimeAgoDate(String timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        String receivedTimeStamp = timestamp;
        Date receivedDate = null;
        try {
            receivedDate = formatter.parse(receivedTimeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return TimeAgo.format(receivedDate.getTime());
    }
    
    /**
     * This method adjusts the current timestamp based on the server timestamp
     * received in the LoginOk packet. Call this instead of
     * System.currentTimeMillis()
     * 
     * @return Adjusted current timestamp
     */
    public static long getClientTimestampBasedOnServerTime() {
        long currTime = System.currentTimeMillis();

        boolean clientBehind = false;
        long timeDiff = Math.abs(Session.getInstance().getClientTimeOnLogin()
                - Session.getInstance().getServerTimeOnLogin());
        if (Session.getInstance().getServerTimeOnLogin() > Session.getInstance().getClientTimeOnLogin()) {
            clientBehind = true;
        }

        // if time difference is less than 1 second, ignore
        if (timeDiff < 1000) {
            return currTime;
        } else {
            if (clientBehind) {
                return (currTime + timeDiff);
            } else {
                return (currTime - timeDiff);
            }
        }
    }

    /**
     * Takes a timestamp and returns an appropriate date-time format to display.
     * If the timestamp is more than a day old, then a day-month format is
     * returned. If the timestamp is a day old, then "Yesterday" is returned. If
     * the timestamp is less than a day old, then a time format is returned.
     * 
     * @return A String containing the format to display.
     */
    public static String getDisplayDate(long messageTimeStamp) {

        if (messageTimeStamp <= 0) {
            return Constants.BLANKSTR;
        }

        long now = System.currentTimeMillis();

        // First sanity check
        if(now > messageTimeStamp) {
            long minutes = (now - messageTimeStamp) / (60*1000);
            if(minutes == 0) return I18n.tr("now");
            if(minutes < 60) return String.format("%dm", minutes);
            long hours = minutes / 60;
            if(hours < 24) return String.format("%dh", hours);
            long days = hours / 24;
            if(days < 7) return String.format("%dd", days);
            long weeks = days / 7;
            if(weeks < 4) return String.format("%dw", weeks);
        }


        final Date messageDate = new Date(messageTimeStamp);
        final SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(Constants.FORMAT_SHORT_DATE);
        return sdf.format(messageDate);
    }

    /**
     * Returns the date format of the timestamp
     * 
     * @param currTimestamp
     *            Returns the date in format "Month Day, Year"
     * @return
     */
    public static String getMessageDisplayDate(long currTimestamp) {
        Date currDate = new Date(currTimestamp);
        return java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG).format(currDate);
    }

    /**
     * Returns the time format of the timestamp
     * 
     * @param currTimestamp
     *            Returns the time in format HH:mm
     * @return
     */
    public static String getMessageDisplayTime(long currTimestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.FORMAT_TIME_12);
        return sdf.format(new Date(currTimestamp));
    }

    /**
     * Compares timestamp of current message and previous message If dates of
     * the previous and current messages are the same, then this method will
     * return true
     * 
     * @param currTimestamp
     *            Timestamp of the current message to be displayed
     * @param prevTimestamp
     *            Timestamp of the previous message. Pass 0 if there's no
     *            previous message to compare with
     * @return
     */
    @SuppressWarnings("deprecation")
    public static boolean hasSameDayTimestamp(long currTimestamp, long prevTimestamp) {
        if (prevTimestamp == 0) {
            return false;
        }

        try {
            Date currMessageDate = new Date(currTimestamp);
            Date prevMessageDate = new Date(prevTimestamp);

            int YearPrevious = prevMessageDate.getYear();
            int MonthPrevious = prevMessageDate.getMonth();
            int DayPrevious = prevMessageDate.getDate();

            int YearCurrent = currMessageDate.getYear();
            int MonthCurrent = currMessageDate.getMonth();
            int DayCurrent = currMessageDate.getDate();

            if (DayCurrent == DayPrevious && MonthCurrent == MonthPrevious && YearCurrent == YearPrevious) {
                return true;
            }

        } catch (Exception e) {
        }

        return false;
    }

    /**
     * Returns details about the user (country, gender, age)
     *
     * @param profile
     * @return
     */
    public static String formatProfileRemarksForPopup(BaseProfile profile) {
        if (profile == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();

        // country
        if (profile.getCountry() != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(profile.getCountry());
        }

        // gender
        if (profile.getGender() != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            if (profile.getGender() == UserProfileGenderEnum.MALE) {
                sb.append(I18n.tr("Male"));
            } else if (profile.getGender() == UserProfileGenderEnum.FEMALE) {
                sb.append(I18n.tr("Female"));
            }
        }

        // age
        String age = getAgeByBirthDate(profile.getDateOfBirth());
        if (!TextUtils.isEmpty(age)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(age);
        }

        return sb.toString();
    }

    /**
     * Returns details about the user (age, gender and country)
     * 
     * @param profile
     * @return
     */
    public static String formatProfileRemarks(BaseProfile profile) {
        if (profile == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();

        // gender
        if (profile.getGender() != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            if (profile.getGender() == UserProfileGenderEnum.MALE) {
                sb.append(I18n.tr("Male"));
            } else if (profile.getGender() == UserProfileGenderEnum.FEMALE) {
                sb.append(I18n.tr("Female"));
            }
        }

        // country
        if (profile.getCountry() != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(profile.getCountry());
        }
        
        // relationship status
        UserProfileRelationshipEnum userRelationshipEnum = profile.getApplication();
        String strRelationship = getUserRelationshipEnumString(userRelationshipEnum);
        if (!TextUtils.isEmpty(strRelationship)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(strRelationship);
        }
        
        // age
        String age = getAgeByBirthDate(profile.getDateOfBirth());
        if (!TextUtils.isEmpty(age)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(age);
        }

        return sb.toString();
    }

    public static String formatCounters(int counter, int maxFullDisplay) {
        String strCounter = String.valueOf(counter);

        if (counter > maxFullDisplay) {
            double thousands = (double) counter / 1000;
            String suffix = "K";
            if (thousands >= 1000) {// millions
                thousands = thousands / 1000;
                suffix = "M";
            }
            strCounter = String.valueOf(thousands);
            // display only until the first decimal value
            if (strCounter.indexOf('.') > -1) {
                strCounter = strCounter.substring(0, strCounter.indexOf('.') + 2);
            }

            strCounter = strCounter + suffix;
        }

        return strCounter;
    }

    public static void setLabels(Labels labels, ImageView labelAdmin, ImageView labelMerchant, ImageView labelVerified) {
        if (labels == null) {
            Logger.warning.log(TAG, "labels is null!");
            return;
        }
        
        if (labelVerified != null) {
            if (labels.isVerified()) {
                labelVerified.setVisibility(View.VISIBLE);
            } else {
                labelVerified.setVisibility(View.GONE);
            }
        } else {
            Logger.warning.log(TAG, "labelVerified is null!");
        }

        if (labelAdmin != null) {
            UserLabelAdminEnum admin = labels.getAdmin();
            if (admin != null) {
                if (admin.equals(UserLabelAdminEnum.GROUP_ADMIN)) {
                    labelAdmin.setImageResource(R.drawable.ad_grpadmin_orange);
                    labelAdmin.setVisibility(View.VISIBLE);
                } else if (admin.equals(UserLabelAdminEnum.CHATROOM_ADMIN)) {
                    labelAdmin.setImageResource(R.drawable.ad_chatadmin_orange);
                    labelAdmin.setVisibility(View.VISIBLE);
                } else if (admin.equals(UserLabelAdminEnum.GLOBAL_ADMIN)) {
                    labelAdmin.setImageResource(R.drawable.ad_admin_orange);
                    labelAdmin.setVisibility(View.VISIBLE);
                } else {
                    labelAdmin.setVisibility(View.GONE);
                }
            } else {
                labelAdmin.setVisibility(View.GONE);
                Logger.debug.log(TAG, "labels.getAdmin() is null!");
            }
        } else {
            Logger.warning.log(TAG, "labelAdmin is null!");
        }

        if (labelMerchant != null) {
            UserLabelMerchantEnum merchant = labels.getMerchant();
            if (merchant != null) {
                if (merchant.equals(UserLabelMerchantEnum.MERCHANT_MENTOR)) {
                    labelMerchant.setImageResource(R.drawable.ad_merchmentor_purple);
                    labelMerchant.setVisibility(View.VISIBLE);
                } else if (merchant.equals(UserLabelMerchantEnum.MERCHANT_SUBMENTOR)) {
                    labelMerchant.setImageResource(R.drawable.ad_merch_purple);
                    labelMerchant.setVisibility(View.VISIBLE);
                } else {
                    labelMerchant.setVisibility(View.GONE);
                }
            } else {
                labelMerchant.setVisibility(View.GONE);
                Logger.debug.log(TAG, "labels.getMerchant() is null!");
            }
        } else {
            Logger.warning.log(TAG, "labelMerchant is null!");
        }
    }

    /**
     * Loads string data from a config file.
     * 
     * @param configFilename
     *            The name / relative path to the config path. The relative path
     *            is respective to the assets config path.
     * @return A String containing the contents of the config file and null if
     *         the file could not be successfully read.
     */
    public static String readFromConfigFile(final String configFilename) {
        String buff = null;
        try {
            String fileName = Constants.PATH_ASSETS_CONFIG + File.separator + configFilename;
            buff = readFromfile(fileName, ApplicationEx.getContext());
        } catch (Exception e) {
            Logger.error.log(TAG, e);
        }
        return buff;
    }

    public static String formatTimestampVia(Long timestamp, PostApplicationEnum app) {
        long ts = (timestamp == null) ? 0 : timestamp;
        return String.format(I18n.tr("%s via %s"), TimeAgo.format(ts), getPostApplication(app));
    }

    public static String formatPostType(String postType) {
        return String.format("<font color='#%1$06X'><i>%2$s</i></font>",
                ApplicationEx.getColor(R.color.default_timestamp)
                & 0xffffff, postType);
    }

    public static String getPostApplication(PostApplicationEnum app) {
        if (app != null) {
            if (app == PostApplicationEnum.ANDROID) {
                return I18n.tr("Android");
            } else if (app == PostApplicationEnum.J2ME) {
                return I18n.tr("J2ME");
            } else if (app == PostApplicationEnum.WAP) {
                return I18n.tr("WAP");
            } else if (app == PostApplicationEnum.WEB) {
                return I18n.tr("Web");
            } else if (app == PostApplicationEnum.BLACKBERRY) {
                return I18n.tr("Blackberry");
            } else if (app == PostApplicationEnum.IOS) {
                return I18n.tr("iOS");
            }
        }

        return I18n.tr("Mobile");
    }

    /**
     * Creates StateListDrawable with 4 predefined states (normal, pressed,
     * focused and disabled). If you don't want the state to be supported, just
     * pass a null Drawable for that state.
     * 
     * @param normal
     *            Background state for normal state. Must NOT be null.
     * @param pressed
     *            Background state for pressed state. Generally applies for
     *            buttons. Can be null for other widgets.
     * @param focused
     *            Background state for focused state. Can be same as pressed
     *            state. Generally supported for both buttons and text fields.
     *            Can be null for other widgets.
     * @param disabled
     *            Background state for disabled state. Generally applies for
     *            buttons. Can be null for other widgets.
     * @return StateListDrawable
     */
    public static StateListDrawable createBackgroundStates(Drawable normal, Drawable pressed, Drawable focused,
            Drawable disabled) {
        StateListDrawable backgroundStates = new StateListDrawable();
        if (pressed != null) {
            backgroundStates.addState(new int[] { android.R.attr.state_pressed }, pressed);
        }
        if (focused != null) {
            backgroundStates.addState(new int[] { android.R.attr.state_focused }, focused);
        }
        if (disabled != null) {
            backgroundStates.addState(new int[] { -android.R.attr.state_enabled }, disabled);
        }
        backgroundStates.addState(new int[] {}, normal);
        return backgroundStates;
    }

    public static String formatItalicString(String stringToFormat) {
        return String.format("<font><i>%1$s</i></font>", stringToFormat);
    }
    
    public static String formatItalicHintText(String hintText) {
        return String.format("<font color='#%1$06X'><i>%2$s</i></font>",
                ApplicationEx.getColor(R.color.default_text), hintText);
    }
    
    public static String constructProperUrl(String path) {
        return constructProperUrl(UrlHandler.getInstance().getPageletServerUrl(), path);
    }
    
    public static String constructProperImageUrl(String path) {
        return constructProperUrl(UrlHandler.getInstance().getImageServerUrl(), path);
    }
    
    // TODO: REMOVE THESE... when SE-667 is fixed and released
    // =====================================
    // Hardcoded images endpoints, to make the images to load for the the mean
    // time
    private static String IMAGE_ENDPOINT   = "https://img.migxchat.net/";
    private static String STICKER_ENDPOINT = "https://img.migxchat.net/images/emoticons/stickers/";

    public static String constructEmoticonUrl(EmoticonType type, String path) {
        String root = null;
        switch (type) {
            case STICKER:
                root = STICKER_ENDPOINT;
                break;
            default:
                root = IMAGE_ENDPOINT;
                break;
        }
        return constructProperUrl(root, path);
    }
    // =====================================

    /**
     * Ensures URL is correct and properly formatted based on supplied root
     * (usually domain) and the URI path
     * 
     * @param root
     *            Base URL (e.g. domain)
     * @param path
     *            URI path to append to base URL
     * @return The proper URL
     */
    public static String constructProperUrl(String root, String path) {
        if (path == null || path.startsWith(Constants.LINK_MIG33) || path.contains(Constants.PROTOCOL_MARK)) {
            return path;
        } else {
            StringBuilder properURL = new StringBuilder();
            if (!root.contains(Constants.PROTOCOL_MARK)) {
                // assume HTTP protocol if not defined
                properURL.append(Constants.LINK_HTTP).append(root);
            } else {
                properURL.append(root);
            }

            if (root.endsWith(Constants.SLASHSTR)) {
                if (path.startsWith(Constants.SLASHSTR)) {
                    properURL.append(path.substring(1, path.length()));
                } else {
                    properURL.append(path);
                }
            } else {
                if (path.startsWith(Constants.SLASHSTR)) {
                    properURL.append(path);
                } else {
                    properURL.append(Constants.SLASHSTR).append(path);
                }
            }
            return properURL.toString();
        }
    }

    /**
     * Constructs the full miglevel image url given a partial path to the
     * miglevel image.
     * 
     * @param imageName
     *            The name of the image file whose url is to be constructed.
     * @return The fully constructed url image link.
     */
    public static String constructFullMigLevelImageURL(final String imageName, int size) {
        if (TextUtils.isEmpty(imageName)) {
            return null;
        }
        
        StringBuffer migLevelUrl = new StringBuffer();

        if (!imageName.startsWith(Constants.LINK_HTTP)) {
            migLevelUrl.append(UrlHandler.getInstance().getUrlPrefix()).append(Constants.SLASHSTR);
        }

        if (!imageName.contains("images/reputation")) {
            migLevelUrl.append("images/reputation/");
        }

        if (size > -1) {
            int extensionPt = imageName.indexOf(".");
            if (extensionPt > -1) {
                migLevelUrl.append(imageName.substring(0, extensionPt));
                migLevelUrl.append("-").append(size).append("x").append(size);
                migLevelUrl.append(imageName.substring(extensionPt));
            } else {
                migLevelUrl.append(imageName);
                migLevelUrl.append("-").append(size).append("x").append(size);
            }
        } else {
            migLevelUrl.append(imageName);
        }

        // i know this is a semi-hackish way of checking if the filename
        // contains the type extension. this is more optimized though than doing
        // a String.endsWith() compare will all possible image types. if the
        // type is not defined anyway, we will hardcode it to use 'png'
        if (imageName.indexOf('.') == -1) {
            migLevelUrl.append(".").append(ImageFileType.PNG.getExtension());
        }

        Logger.debug.log(TAG, "MigLevel URL: " + migLevelUrl);
        return migLevelUrl.toString();
    }

    public static <T> List<T> makeListFromSet(final Set<T> set) {
        List<T> resultList = null;
        if (set != null) {
            resultList = new ArrayList<T>(set);
        }

        return resultList;
    }

    public static Post getLastReply(Post post) {
        Post latestReply = null;
        if (post.getReplies() != null && post.getReplies().getTotal().intValue() > 0
                && post.getReplies().getReplies() != null) {
            Post[] replies = post.getReplies().getReplies();
            if (replies.length > 0) {
                latestReply = replies[0];
            }
        }
        return latestReply;
    }

    public static String getPostConversationDisplayTitle(Post post) {
        Post latestReply = getLastReply(post);
        Author authorToFilter = null;
        if (latestReply != null) {
            authorToFilter = latestReply.getAuthor();
        } else {
            authorToFilter = post.getAuthor();
        }
        String authorName = null;
        if (authorToFilter != null) {
            authorName = authorToFilter.getUsername();
        }
        return authorName;
    }

    /**
     * Hides the virtual keyboard
     */
    public static boolean hideVirtualKeyboard(Activity activity) {
        return hideVirtualKeyboard(activity, null);
    }

    public static boolean hideVirtualKeyboard( View view, Context context) {
        try {
            InputMethodManager inputManager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (view == null) {
                return false;
            } else {
                return inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            Logger.error.log(TAG, e);
        }
        return false;
    }

    /**
     * Hides the virtual keyboard
     */
    public static boolean hideVirtualKeyboard(Activity activity, View v) {
        if (null == activity) {
            return false;
        }
        if (v == null) {
            v = activity.getCurrentFocus();
        }
        return hideVirtualKeyboard(v, activity);
    }

    public static void showVirtualKeyboard(Activity activity, View view) {
        if (activity != null) {
            InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    public static void showVirtualKeyboard(BaseDialogFragment fragment, View view) {
        if (fragment != null && fragment.getDialog() != null) {
            if (fragment.getShowsDialog()) {
                fragment.getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            } else {
                showVirtualKeyboard(fragment.getActivity(), view);
            }
        }
    }
    
    public static void showContextMenu(String title, final List<ContextMenuItem> menuItemList,
            final ContextMenuItemListener listener) {
        showContextMenu(title, menuItemList, listener, null);
    }

    public static void showContextMenu(String title, final List<ContextMenuItem> menuItemList,
            final ContextMenuItemListener listener, OnCancelListener onDismissListener) {
        Activity currentActivity = ApplicationEx.getInstance().getCurrentActivity();
        if (currentActivity == null) {
            CrashlyticsLog.log(new NullPointerException(), "getCurrentActivity() is null");
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(currentActivity);
        if (!TextUtils.isEmpty(title)) {
            alert.setTitle(title);
        }
        String[] optionTitleArray = new String[menuItemList.size()];
        for (int i = 0; i < optionTitleArray.length; i++) {
            optionTitleArray[i] = menuItemList.get(i).getTitle();
        }
        
        alert.setItems(optionTitleArray, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onContextMenuItemClick(menuItemList.get(which));
                //using cancel instead of dismiss so that the registered onCancelListener can be called,
                //can not use AlertDialog.Builder.setOnDismissListener(listener) here because it requires api level 17
                dialog.cancel();
            }
        });
        
        alert.setOnCancelListener(onDismissListener);
        alert.create().show();
    }

    public static String getPresenceText(final boolean isSelf, final PresenceType presenceType) {
        switch (presenceType) {
            case AVAILABLE:
                return I18n.tr("Online");
            case AWAY:
                return I18n.tr("Away");
            case BUSY:
                return I18n.tr("Busy");
            default:
                return (isSelf) ? I18n.tr("Invisible") : I18n.tr("Offline");
        }
    }

    /**
     * @param webView
     * @return
     */
    public static String getUserAgentForBrowser(WebView webView) {
        return webView.getSettings().getUserAgentString() + Constants.SPACESTR + Version.getUserAgent();
    }

    /**
     * @param dateOfBirth
     *            example: 1985-6-29
     * @return it returns null if the param is not in expected format
     */
    public static String getAgeByBirthDate(String dateOfBirth) {
        String strAge = null;
        if (!TextUtils.isEmpty(dateOfBirth)) {
            String[] birthdayArray = dateOfBirth.split("-");
            if (birthdayArray != null && birthdayArray.length == 3) {
                int birthYear = Integer.parseInt(birthdayArray[0]);
                int birthMonth = Integer.parseInt(birthdayArray[1]);
                int birthdate = Integer.parseInt(birthdayArray[2]);

                // get Current date
                Calendar calendar = Calendar.getInstance();
                int todayYear = calendar.get(Calendar.YEAR);
                int todayMonth = calendar.get(Calendar.MONTH);
                int todaydate = calendar.get(Calendar.DAY_OF_MONTH);

                int age = todayYear - birthYear;
                if (todayMonth < birthMonth || (todayMonth == birthMonth && todaydate < birthdate)) {
                    age--;
                }
                strAge = Integer.toString(age);
            }
        }
        return strAge;
    }

    public static String formatDateProfile(String dateToFormat) {
        TimeFormat formatType = TimeFormat.FULL;
        if(dateToFormat != null){
            boolean match = dateToFormat.matches(Constants.DAY_OF_BIRTH_MISSINGYEAR_REGEX);
            formatType = match ? TimeFormat.MISSING_YEAR : TimeFormat.FULL;
        }
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat form = new SimpleDateFormat(pattern);;
        SimpleDateFormat postFormater = new SimpleDateFormat("dd MMMM yyyy");;

        switch (formatType){
            case MISSING_YEAR:
                pattern = "MM-dd";
                form = new SimpleDateFormat(pattern);
                postFormater = new SimpleDateFormat("dd MMMM");
                break;
            case MISSING_MONTH:
                // currently there is no this logic
                break;
            case MISSING_DAY:
                // currently there is no this logic
                break;
            case FULL:
            default:

        }
        if (dateToFormat == null || dateToFormat.length() < pattern.length()) {
            return Constants.BLANKSTR;
        }
        dateToFormat = dateToFormat.substring(0, pattern.length());
        java.util.Date date = null;
        try {
            date = form.parse(dateToFormat);
        } catch (ParseException e) {
            Logger.error.log(TAG, e);
        }

        String newDateStr = Constants.BLANKSTR;
        if (date != null) {
            newDateStr = postFormater.format(date);
        }
        return newDateStr;
    }

    private static long timestamp;

    /**
     * init the timestamp for investigating slowness issue
     */
    public static void initTimeStamp() {
        timestamp = System.currentTimeMillis();
    }

    /**
     * output the timestamp difference for investigating slowness issue
     * 
     * @param msg
     */
    public static void outputTimeStamp(String msg) {
        long localTimestamp = System.currentTimeMillis();
        Logger.debug.log("Dangui", msg, ":", (localTimestamp - timestamp));
        timestamp = localTimestamp;
    }

    public static InputString checkInputField(String text, boolean checkUrl, final int MAX_MESSAGE_LENGTH,
            final int MAX_LINK_LENGTH, boolean returnCount) {
        int counterLeft = MAX_MESSAGE_LENGTH;

        if (checkUrl) {
            Matcher m = SpannableBuilder.URL_PATTERN.matcher(text);
            String[] texts = text.split(Constants.URL_REGEX);

            int count = 0;

            // Direct use of Pattern
            while (m.find()) { // Find each match in turn; String can't do this.
                count = MAX_LINK_LENGTH;
                counterLeft = counterLeft - count;
            }

            // Count for non-Pattern
            for (int i = 0; i < texts.length; i++) {
                count = texts[i].length() == 1 ? 0 : texts[i].length();
                counterLeft = counterLeft - count;
            }

        } else {
            counterLeft = MAX_MESSAGE_LENGTH - text.length();
        }

        return new InputString(counterLeft, text);
    }

    /**
     * @param userRelationshipEnum
     * @return
     */
    public static String getUserRelationshipEnumString(UserProfileRelationshipEnum userRelationshipEnum) {
        if (userRelationshipEnum == UserProfileRelationshipEnum.SINGLE) {
            return I18n.tr("Single");
        } else if (userRelationshipEnum == UserProfileRelationshipEnum.IN_A_RELATIONSHIP) {
            return I18n.tr("In a relationship");
        } else if (userRelationshipEnum == UserProfileRelationshipEnum.MARRIED) {
            return I18n.tr("Married");
        } else if (userRelationshipEnum == UserProfileRelationshipEnum.COMPLICATED) {
            return I18n.tr("Complicated");
        } else if (userRelationshipEnum == UserProfileRelationshipEnum.DOMESTIC_PARTNER) {
            return I18n.tr("Domestic Partner");
        }

        return null;
    }

    /**
     * this bitmap resizing can be very memory and time consuming, especially
     * when the bitmap is big. Do not use it easily unless it's really necessary
     */
    public static Bitmap resizeBitmapProportional(Bitmap bitmap, int maxWidth, int maxHeight) {
        float scaledSize;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > height) {
            scaledSize = (float) maxWidth / (float) width;
        } else {
            scaledSize = (float) maxHeight / (float) height;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaledSize, scaledSize);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        if (resizedBitmap != bitmap) {
            bitmap.recycle();
        }
        
        return resizedBitmap;
    }

    public static byte[] getBitmapDataForUpload(Bitmap bitmap, boolean shouldResize) {
        byte[] data = null;

        Bitmap resizedBitmap = null;
        // Currently the bitmap already resized when it is loaded from camera or
        // gallery, with a sample size that is a powers of 2,
        // which is very efficient , so the shouldResize here is false before
        // uploading, no need to resize it again, which may bring
        // OutOfMemory issue
        if (shouldResize) {
            resizedBitmap = resizeBitmapProportional(bitmap, Constants.DEFAULT_PHOTO_SIZE, Constants.DEFAULT_PHOTO_SIZE);
        } else {
            resizedBitmap = bitmap;
        }

        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            resizedBitmap.compress(CompressFormat.JPEG, Constants.DEFAULT_PHOTO_QUALITY, bos);
            data = bos.toByteArray();
        } catch (Exception e) {
            Logger.error.log(TAG, e);
        } finally {
            if (bos != null) {
                bos.reset();
                try {
                    bos.close();
                } catch (IOException e) {
                    Logger.error.log(TAG, e);
                }
                bos = null;
            }
            if (resizedBitmap != null) {
                resizedBitmap.recycle();
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
        }

        if (data != null) {
            Logger.debug.log(TAG, "bitmap data size: ", data.length / 1024.f, "KB");
        }

        return data;
    }

    public static Bitmap resizeImage(Context c, Uri uri, int maxWidth, int maxHeight) throws FileNotFoundException {

        BitmapFactory.Options o = getImageMetaData(c.getContentResolver().openInputStream(uri));
        int scale = MathUtils.getPowerOfTwoScale(o.outWidth, o.outHeight, maxWidth, maxHeight);

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        o2.inPurgeable = true;
        o2.inInputShareable = true;
        InputStream inputStream = c.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, o2);
        return bitmap;
    }

    public static Bitmap resizeAndRotateImage(Context c, Uri uri, int maxWidth, int maxHeight)
            throws FileNotFoundException, IOException {

        Bitmap rotatedBitmap = null;
        Bitmap resizedBitmap = resizeImage(c, uri, maxWidth, maxHeight);

        rotatedBitmap = resizedBitmap;

        int orientation = getOrientation(c, uri);
        if (orientation != -1 && orientation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            rotatedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(),
                    resizedBitmap.getHeight(), matrix, true);
        }
        
        if (resizedBitmap != rotatedBitmap) {
            resizedBitmap.recycle();
        }

        return rotatedBitmap;
    }

    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor == null || cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();

        return cursor.getInt(0);
    }

    public static Bitmap getBitmapAndRotateIfNeeded(String filename) throws IOException {
        ExifInterface exif = new ExifInterface(filename);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
        Matrix matrix = null;
        if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
            float degrees = 0f;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                {
                    degrees = 90;
                    break;
                }
                case ExifInterface.ORIENTATION_ROTATE_180:
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                {
                    degrees = 180;
                    break;
                }
                case ExifInterface.ORIENTATION_ROTATE_270:
                {
                    degrees = 270;
                    break;
                }
            }

            if (degrees > 0) {
                matrix = new Matrix();
                matrix.postRotate(degrees);
            }
        }

        File file = new File(filename);

        // Decode image size
        FileInputStream fis = new FileInputStream(file);
        BitmapFactory.Options o = getImageMetaData(fis);
        fis.close();

        fis = new FileInputStream(file);
        Bitmap resizedBitmap = resizeImage(fis, o, Constants.DEFAULT_PHOTO_SIZE, Constants.DEFAULT_PHOTO_SIZE);
        fis.close();
        Bitmap rotatedBitmap = null;
        if (matrix != null) {
            rotatedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(),
                    resizedBitmap.getHeight(), matrix, true);
        } else {
            rotatedBitmap = resizedBitmap;
        }
        
        if (resizedBitmap != rotatedBitmap) {
            resizedBitmap.recycle();
        }

        return rotatedBitmap;
    }

    public static BitmapFactory.Options getImageMetaData(InputStream is) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(is, null, o);

        return o;
    }

    public static Bitmap resizeImage(InputStream is, BitmapFactory.Options o, int maxWidth, int maxHeight) {
        int scale = MathUtils.getPowerOfTwoScale(o.outWidth, o.outHeight, maxWidth, maxHeight);

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        o2.inPurgeable = true;
        o2.inInputShareable = true;
        return BitmapFactory.decodeStream(is, null, o2);
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * Utility method for ensuring Context to be used for navigating is not null
     * 
     * @param context
     *            original context passed to be used for navigation
     * @return Returns original context if not null. If null, will return
     *         Application context
     */
    public static Context ensureContext(Context context) {
        // if context is null, use the application context
        if (context == null) {
            if (ApplicationEx.getInstance().getCurrentActivity() != null) {
                // use current activity's context by default
                context = ApplicationEx.getInstance().getCurrentActivity();
            } else {
                context = ApplicationEx.getContext();
                if (context == null) {
                    CrashlyticsLog.log(new NullPointerException(), "application context is still null");
                }
            }
        }
        return context;
    }

    public static void showToastForIntent(final Context context, final Intent intent) {

        final String toastMessage = intent.getStringExtra(AppEvents.Misc.Extra.FORMATTED_MESSAGE);
        showToast(context, toastMessage);
    }

    /**
     * Display a toast message.
     * 
     * @param context
     *            The context to use. Can be the Application's or activity.
     * @param message
     *            The message to be displayed.
     */
    public static void showToast(final Context context, final String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    /**
     * Display a toast message.
     * 
     * @param context
     *            The context to use. Can be the Application's or activity.
     * @param message
     *            The message to be displayed.
     */
    public static void showToast(Context context, final String message, final int toastLength) {
        if (!TextUtils.isEmpty(message)) {
            context = ensureContext(context);
            if (context != null) {
                Toast.makeText(context, message, toastLength).show();
            }
        }
    }

    /**
     * Checks whether the given string mentions the currently logged in user.
     * 
     * @param str
     *            The string to be checked.
     * @return true if the currently logged in user is mentioned and false
     *         otherwise.
     */
    public static boolean stringContainsOwnMention(final String str) {
        return stringContainsUserMention(str, Session.getInstance().getUsername());
    }

    /**
     * Checks whether the given string mentions any username.
     * 
     * @param str
     *            The string to be checked
     * @return true if any username is mentioned and false otherwise.
     */
    public static boolean stringContainsAnyMention(final String str) {
        if (!TextUtils.isEmpty(str)) {
            Pattern mentionsPattern = Pattern.compile(Constants.MENTIONS_REGEX, Pattern.CASE_INSENSITIVE);
            Matcher m = mentionsPattern.matcher(str);
            return m.find();
        }

        return false;
    }

    /**
     * Checks whether the given string mentions the specified username.
     * 
     * @param str
     *            The string to be checked.
     * @param username
     *            The name of the user to be checked for.
     * @return true if the specified username is mentioned and false otherwise.
     */
    private static boolean stringContainsUserMention(final String str, final String username) {
        if (!TextUtils.isEmpty(str) && !TextUtils.isEmpty(username)) {
            String MENTIONS_REGEX = Constants.MENTIONS_TAG + username;
            Pattern mentionsPattern = Pattern.compile(MENTIONS_REGEX, Pattern.CASE_INSENSITIVE);
            Matcher m = mentionsPattern.matcher(str);
            return m.find();
        }

        return false;
    }

    public static File createTemporaryFile(String part, String ext) throws Exception {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    @SuppressWarnings("deprecation")
    public static String getTempFolderPath(Context context) {
        String path = null;

        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            path = context.getDir("temp", Context.MODE_WORLD_WRITEABLE).getAbsolutePath();
        }

        return path;
    }

    public static String getCapturedPhotoFile(Context context) {
        String path = getTempFolderPath(context);
        return path + File.separatorChar + "migme" + File.separatorChar + "temp" + File.separatorChar
                + "captured_photo.jpg";
    }

    public static String getCroppedImageFile(Context context) {
        String path = getTempFolderPath(context);
        return path + File.separatorChar + "migme" + File.separatorChar + "temp" + File.separatorChar
                + "cropped_photo.png";
    }

    public static void deleteCapturedPhotoFile(Context context) throws IOException {
        File capturedFile = new File(getCapturedPhotoFile(context));
        if (capturedFile.exists() && !capturedFile.delete()) {
            throw new IOException("Unable to the delete existing file " + getCapturedPhotoFile(context));
        }
    }

    public static void deleteCroppedImageFile(Context context) throws IOException {
        File croppedFile = new File(getCroppedImageFile(context));
        if (croppedFile.exists() && !croppedFile.delete()) {
            throw new IOException("Unable to the delete existing file " + getCroppedImageFile(context));
        }
    }

    static public Bitmap loadImageFromCapturedPhotoFile(Context context) {
        return loadImageFromCapturedPhotoFile(context, getCapturedPhotoFile(ApplicationEx.getContext()));
    }

    static public Bitmap loadImageFromCroppedImageFile(Context context) {
        return loadImageFromCapturedPhotoFile(context, getCroppedImageFile(ApplicationEx.getContext()));
    }

    static public Bitmap loadImageFromCapturedPhotoFile(Context context, String photoFile) {
        try {
            return getBitmapAndRotateIfNeeded(photoFile);
        } catch (Exception e) {
            showToast(context, I18n.DEFAULT_ERROR_MESSAGE);
            Logger.error.log(TAG, "Failed to load Captured photo");
            return null;
        }
    }

    /**
     * Usage imageView = Tools.convertImageToGrayscale(imageView);
     * 
     * @param imageView
     * @return
     */
    public static void setGrayscaleFilter(ImageView imageView) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        imageView.setColorFilter(filter);
    }

    public static void setTranslucentFilter(ImageView imageView) {
        ColorMatrix matrix = new ColorMatrix();
        adjustTransparency(matrix, 0.5f);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        imageView.setColorFilter(filter);
    }

    public static void adjustTransparency(ColorMatrix cm, float value) {
        float[] mat = new float[]  {
                  1f, 0f, 0f, 0f, 0f, //red
                  0f, 1f, 0f, 0f, 0f, //green
                  0f, 0f, 1f, 0f, 0f, //blue
                  0f, 0f, 0f, value, 0f //alpha
                };
        cm.postConcat(new ColorMatrix(mat));
    }

    /**
     * this method is useful for debugging
     */
    public static void printCallStack(String tag) {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            Logger.debug.log(tag, ste.toString());
        }
    }
    
    /**
     * Create a unique key based on the parameters
     * 
     * @param prefix
     * @param separator
     * @param values
     * @return
     */
    public static String createKey(String prefix, String separator, String... values) {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < values.length; i++) {
            sb.append(separator);
            sb.append(values[i]);
        }
        return sb.toString();
    }

    public static List<ContextMenuItem> getContextMenuOptions(Friend data, boolean displayAllContacts,
            boolean isFusionFriendsGrouped) {
        List<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();
        menuItems.add(new ContextMenuItem(I18n.tr("View profile"), R.id.option_item_view_profile, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Chat"), R.id.option_item_chat, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Send gift"), R.id.option_item_send_gift, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Report abuse"), R.id.option_item_report_abuse, data));
        
        if (displayAllContacts && isFusionFriendsGrouped) {
            menuItems.add(new ContextMenuItem(I18n.tr("Move to group"), R.id.option_item_move_to_group, data));
        }
        
        menuItems.add(new ContextMenuItem(I18n.tr("Block/Mute"), R.id.option_item_block, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Unfriend"), R.id.option_item_remove_friend, data));
        return menuItems;
    }
    
    public static ArrayList<ContextMenuItem> getContactGroupContextMenuOptions(ContactGroup data) {
        ArrayList<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();
        menuItems.add(new ContextMenuItem(I18n.tr("Add contact group"), R.id.option_item_add_contact_group, data));
        if (data.getGroupID() > 0) {
            menuItems.add(new ContextMenuItem(I18n.tr("Rename contact group"), R.id.option_item_rename_contact_group,
                    data));
            menuItems.add(new ContextMenuItem(I18n.tr("Delete contact group"), R.id.option_item_remove_contact_group,
                    data));
        }

        if (Config.getInstance().isImEnabled()) {
            if (ThirdPartyIMController.getInstance().isImConfigured(data.getType().getImType())) {
                if (ThirdPartyIMController.getInstance().isImOnline(data.getType().getImType())) {
                    menuItems
                            .add(new ContextMenuItem(I18n.tr("Sign out"), R.id.option_item_logout_contact_group, data));
                } else {
                    menuItems.add(new ContextMenuItem(I18n.tr("Sign in"), R.id.option_item_login_contact_group, data));
                }
            }
        }

        return menuItems;
    }


    /**
     * this is workround for search requests that use page and pageperhits.
     * cause on android we don't display by page, it's better to use offset & limit
     *
     * server dependency : SE-1044
     *
     * for example:
     *          page offset limit
     *  0 - 19    0     0     20
     *  20 - 39   1     20    20
     *  40 - 59   2     40    20
     */

    public static int convertOffsetToPage(int offset, int limit) {
        // there could be difference when caculating the page
        // so we round it
        float page = (float) offset / (float) limit;
        return Math.round(page);
    }

    public static int convertPageToOffset(int page, int hitsPerPage) {
        int offset =  page * hitsPerPage;
        return offset;
    }
    
}
