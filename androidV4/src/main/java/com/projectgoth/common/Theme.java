
package com.projectgoth.common;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.ui.widget.RoundedRectDrawable;
import com.projectgoth.ui.widget.RoundedRectDrawable.RoundedRectParams;

import java.io.InputStream;
import java.util.HashMap;

public class Theme {

    private static String                           name;
    private static String                           id;
    private static String                           version;

    private static HashMap<String, String>          orientation        = new HashMap<String, String>();
    private static HashMap<String, Integer>         colors             = new HashMap<String, Integer>();
    private static HashMap<String, RoundedRectType> roundedRects       = new HashMap<String, RoundedRectType>();
    private static HashMap<String, Integer>         roundedRectBorders = new HashMap<String, Integer>();

    private static final String                     ORIENTATION_FLAG   = "_ORIENTATION";
    private static final String                     COLOR_1_FLAG       = "_COLOR_1";
    private static final String                     COLOR_2_FLAG       = "_COLOR_2";
    private static final String                     COLOR_FLAG         = "_COLOR";

    private static final String                     BG_COLOR           = "_BG_COLOR";
    private static final String                     BORDER_COLOR       = "_BORDER_COLOR";
    private static final String                     BORDER_WIDTH       = "_BORDER_WIDTH";

    public static final String                      KEY_ID             = "THEME_ID";
    public static final String                      KEY_NAME           = "THEME_NAME";
    public static final String                      KEY_VERSION        = "THEME_VERSION";

    private static final String                     DEFAULT_THEME_NAME = "blue";

    public static enum RoundedRectType {
        ROUND_ALL_CORNERS, ROUND_TOP_CORNERS, ROUND_BOTTOM_CORNERS;
    }

    /**
     * Set the Theme Packet
     * 
     * @param in
     * @throws Exception
     */
    public static void setTheme(InputStream in) throws Exception {

        orientation = new HashMap<String, String>();
        colors = new HashMap<String, Integer>();

        ThemeValues.init();

        /*
         * FusionPacket themePacket = null; try { themePacket = new
         * FusionPacket(); themePacket.read(in);
         * 
         * HashMap<String, String> themeData = new HashMap<String, String>();
         * 
         * int size = themePacket.size(); for (short i = 1; i <= size; i++) {
         * 
         * String value = themePacket.getStringField(i); if
         * (value.endsWith(IMAGE_FLAG)) { i = processImage(i, themePacket); }
         * else { i++; themeData.put(value, themePacket.getStringField(i)); } }
         * extractData(themeData); } catch (Exception e) { e.printStackTrace();
         * }
         */
    }

    /**
     * Get Color from Theme
     * 
     * usage: Theme.color (<Name/ID>, <color>)
     * 
     * @param id
     * @param color
     *            - default if not found
     * @return
     */
    public static int color(String id, int color) {
        Integer result = color;
        if (!colors.containsKey(id + COLOR_FLAG)) {
            colors.put(id + COLOR_FLAG, result);
        } else {
            result = colors.get(id + COLOR_FLAG);
            if (null == result) {
                return color;
            }
        }
        return result;
    }

    /**
     * Get color from Theme ID
     * 
     * @param id
     *            - theme item id
     * @return
     */
    public static int getColor(String id) {
        if (!colors.containsKey(id + COLOR_FLAG)) {
            return 0xFF000000;
        }
        return colors.get(id + COLOR_FLAG);
    }

    /**
     * Adds a Description for the Theme Item
     * 
     * @param id
     * @param desc
     */
    public static void description(String id, String desc) {

    }

    /**
     * Get a Gradient Drawable from Theme
     * 
     * orientation strings TL_BR - draw the gradient from the top-left to the
     * bottom-right LEFT_RIGHT - draw the gradient from the left to the right
     * BL_TR - draw the gradient from the bottom-left to the top-right
     * BOTTOM_TOP - draw the gradient from the bottom to the top BR_TL - draw
     * the gradient from the bottom-right to the top-left RIGHT_LEFT - draw the
     * gradient from the right to the left TOP_BOTTOM - draw the gradient from
     * the top to the bottom TR_BL - draw the gradient from the top-right to the
     * bottom-left
     * 
     * usage: Theme.drawable (<Name/ID>, <Orientation>, <color>, <color>)
     * 
     * @param id
     * @param o
     *            - orientation
     * @param color1
     * @param color2
     * @return
     */
    public static Drawable drawable(String id, String o, int color1, int color2) {
        if (!orientation.containsKey(id + ORIENTATION_FLAG)) {
            orientation.put(id + ORIENTATION_FLAG, o);
            colors.put(id + COLOR_1_FLAG, color1);
            colors.put(id + COLOR_2_FLAG, color2);
        }

        return getDrawable(id);
    }

    public static Drawable getDrawable(String id) {
        if (!orientation.containsKey(id + ORIENTATION_FLAG) || !colors.containsKey(id + COLOR_1_FLAG)
                || !colors.containsKey(id + COLOR_2_FLAG)) {
            return null;
        }
        return Theme.createDrawable(orientation.get(id + ORIENTATION_FLAG), colors.get(id + COLOR_1_FLAG),
                colors.get(id + COLOR_2_FLAG));
    }

    /**
     * Get the Drawable from the Theme ID
     * 
     * @param id
     *            - theme item id
     * @return
     */
    private static Drawable createDrawable(String orientation, int color1, int color2) {
        Orientation temp = Orientation.TOP_BOTTOM;
        if (orientation.equalsIgnoreCase(ThemeValues.TOP_BOTTOM)) {
            temp = Orientation.TOP_BOTTOM;
        } else if (orientation.equalsIgnoreCase(ThemeValues.TR_BL)) {
            temp = Orientation.TR_BL;
        } else if (orientation.equalsIgnoreCase(ThemeValues.RIGHT_LEFT)) {
            temp = Orientation.RIGHT_LEFT;
        } else if (orientation.equalsIgnoreCase(ThemeValues.BR_TL)) {
            temp = Orientation.BR_TL;
        } else if (orientation.equalsIgnoreCase(ThemeValues.BOTTOM_TOP)) {
            temp = Orientation.BOTTOM_TOP;
        } else if (orientation.equalsIgnoreCase(ThemeValues.BL_TR)) {
            temp = Orientation.BL_TR;
        } else if (orientation.equalsIgnoreCase(ThemeValues.LEFT_RIGHT)) {
            temp = Orientation.LEFT_RIGHT;
        } else if (orientation.equalsIgnoreCase(ThemeValues.TL_BR)) {
            temp = Orientation.TL_BR;
        }
        return new GradientDrawable(temp, new int[] { color1, color2 });
    }

    public static GradientDrawable getGradient(Orientation orientation, String themeColor1, String themeColor2) {
        return new GradientDrawable(orientation, new int[] { Theme.getColor(themeColor1), Theme.getColor(themeColor2) });
    }

    /**
     * @return the name
     */
    public static String getName() {
        if (null == name) {
            return DEFAULT_THEME_NAME;
        }
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public static void setName(String name) {
        Theme.name = name;
    }

    /**
     * @return the id
     */
    public static String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public static void setId(String id) {
        Theme.id = id;
    }

    /**
     * @return the version
     */
    public static String getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public static void setVersion(String version) {
        Theme.version = version;
    }

    public static Drawable getRoundedRectDrawable(String id) {
        if (!roundedRects.containsKey(id) || !colors.containsKey(id + BG_COLOR)) {
            return null;
        }
        
        RoundedRectType type = roundedRects.get(id);
        int bgColor = colors.get(id + BG_COLOR);
        int borderColor = -1;
        int borderWidth = 0;
        if (roundedRectBorders.containsKey(id + BORDER_WIDTH)) {
            borderWidth = roundedRectBorders.get(id + BORDER_WIDTH);
            
            if(colors.containsKey(id + BORDER_COLOR)) {
                borderColor = colors.get(id + BORDER_COLOR);
            }
        }
        
        return createRoundedRectDrawable(type, bgColor, borderColor, borderWidth);
        
    }

    public static Drawable roundedRectDrawable(String id, RoundedRectType type, int bgColor) {
        return roundedRectDrawable(id, type, bgColor, -1, 0);
    }

    public static Drawable roundedRectDrawable(String id, RoundedRectType type, int bgColor, int borderColor, int borderWidthInDp) {
        if (!roundedRects.containsKey(id)) {
            roundedRects.put(id, type);
            colors.put(id + BG_COLOR, bgColor);
            if (borderWidthInDp > 0) {
                roundedRectBorders.put(id + BORDER_WIDTH, borderWidthInDp);
                colors.put(id + BORDER_COLOR, borderColor);
            }
        }
        
        return getRoundedRectDrawable(id);
    }

    //TODO: Borders are not yet working nicely
    private static Drawable createRoundedRectDrawable(RoundedRectType type, int bgColor, int borderColor, int borderWidthInDp) {
        float[] radii = new float[8];
        int radius = ApplicationEx.getDimension(R.dimen.cornered_box_radius);
        switch (type) {
            case ROUND_ALL_CORNERS:
                radii = new float[] {radius, radius, radius, radius, radius, radius, radius, radius};
                break;
            case ROUND_TOP_CORNERS:
                radii = new float[] {radius, radius, radius, radius, 0, 0, 0, 0};
                break;
            case ROUND_BOTTOM_CORNERS:
                radii = new float[] {0, 0, 0, 0, radius, radius, radius, radius};
                break;
        }
        
        RoundedRectParams rectParams = new RoundedRectParams(bgColor);
        rectParams.setRadiusArray(radii);
        if (borderWidthInDp > 0) {
            rectParams.setBorderWidth(borderWidthInDp);
            rectParams.setBorderColor(borderColor);
        }
        
        return new RoundedRectDrawable(rectParams);
    }

    /**
     * @param color
     *            color to fill the rounded rectangle
     * @param radiusArray
     *            An array of 8 radius values. The first two floats are for the
     *            top-left corner (remaining pairs correspond clockwise). For no
     *            rounded corners on the outer rectangle, pass null.
     * 
     */
    public static Drawable getRoundedRectDrawable(int color, float[] radiusArray) {
        RoundedRectDrawable drawable = new RoundedRectDrawable(color);
        drawable.setCornerRadiusArray(radiusArray);
        return drawable;
    }
}
