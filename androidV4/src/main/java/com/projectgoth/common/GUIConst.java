/**
 * Copyright (c) 2013 Project Goth
 *
 * GUIConst.java.java
 * Created Jun 19, 2013, 12:21:53 AM
 */

package com.projectgoth.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;

/**
 * @author cherryv
 * 
 */
public class GUIConst {

    public static Bitmap PLAY;
    public static Bitmap GIF;
    public static Bitmap IMAGE_HOLDER;
    public static Bitmap BMP_LOADING_EMOTICON;
    public static Bitmap ROUNDED_CORNER_DEFAULT_AVATAR;
    public static Bitmap DEFAULT_GROUP_CHAT_DISPLAY_ICON;

    public static void initialize(Context context) {
        PLAY = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_holder_video);
        GIF = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_holder_gif);
        IMAGE_HOLDER = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_holder_image);

        BMP_LOADING_EMOTICON = BitmapFactory.decodeResource(context.getResources(), R.drawable.ad_loadstaticchat_grey);
        int emoHeight = ApplicationEx.getInlineEmoticonDimension();
        BMP_LOADING_EMOTICON = Tools.getResizedBitmap(BMP_LOADING_EMOTICON, emoHeight, emoHeight);

        ROUNDED_CORNER_DEFAULT_AVATAR = BitmapFactory.decodeResource(ApplicationEx.getContext().getResources(),
                R.drawable.icon_default_avatar);
        
        DEFAULT_GROUP_CHAT_DISPLAY_ICON = BitmapFactory.decodeResource(ApplicationEx.getContext().getResources(), 
                R.drawable.groupchat_home);
    }

}
