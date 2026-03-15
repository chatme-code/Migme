
package com.projectgoth.common;

import com.projectgoth.common.Theme.RoundedRectType;

public class ThemeValues {

    private static final String TAG                                          = "ThemeValues";

    // Orientation strings for gradient
    public static final String  TL_BR                                        = "TL_BR";
    public static final String  LEFT_RIGHT                                   = "LEFT_RIGHT";
    public static final String  BL_TR                                        = "BL_TR";
    public static final String  BOTTOM_TOP                                   = "BOTTOM_TOP";
    public static final String  BR_TL                                        = "BR_TL";
    public static final String  RIGHT_LEFT                                   = "RIGHT_LEFT";
    public static final String  TOP_BOTTOM                                   = "TOP_BOTTOM";
    public static final String  TR_BL                                        = "TR_BL";

    public static final String  LIGHT_TEXT_COLOR                             = "LIGHT_TEXT_COLOR";

    public static final String  LINK_TEXT_NORMAL                             = "LINK_TEXT_NORMAL";
    public static final String  LINK_TEXT_HIGHLIGHT                          = "LINK_TEXT_HIGHLIGHT";

    public static final String  ROUNDED_CORNERS_COLOR                        = "ROUNDED_CORNERS_COLOR";
    public static final String  IMAGE_BORDER_COLOR                           = "IMAGE_BORDER_COLOR";
    public static final String  IMAGE_BACKGROUND_COLOR                       = "IMAGE_BACKGROUND_COLOR";

    // Button themes
    public static final String  DISABLED_BUTTON_BG                           = "DISABLED_BUTTON_BG";
    public static final String  DISABLED_BUTTON_BORDER                       = "DISABLED_BUTTON_BORDER";
    public static final String  DISABLED_BUTTON_TEXT                         = "DISABLED_BUTTON_TEXT";

    public static final String  GRAY_BUTTON_BACKGROUND_NORMAL                = "GRAY_BUTTON_BACKGROUND_NORMAL";
    public static final String  GRAY_BUTTON_BORDER_NORMAL                    = "GRAY_BUTTON_BORDER_NORMAL";
    public static final String  GRAY_BUTTON_TEXT_NORMAL                      = "GRAY_BUTTON_TEXT_NORMAL";
    public static final String  GRAY_BUTTON_BACKGROUND_HIGHLIGHT             = "GRAY_BUTTON_BACKGROUND_HIGHLIGHT";
    public static final String  GRAY_BUTTON_BORDER_HIGHLIGHT                 = "GRAY_BUTTON_BORDER_HIGHLIGHT";
    public static final String  GRAY_BUTTON_TEXT_HIGHLIGHT                   = "GRAY_BUTTON_TEXT_HIGHLIGHT";

    public static final String  ORANGE_BUTTON_BG_NORMAL                      = "ORANGE_BUTTON_BG_NORMAL";
    public static final String  ORANGE_BUTTON_BORDER_NORMAL                  = "ORANGE_BUTTON_BORDER_NORMAL";
    public static final String  ORANGE_BUTTON_BG_HIGHLIGHT                   = "ORANGE_BUTTON_BG_HIGHLIGHT";
    public static final String  ORANGE_BUTTON_BORDER_HIGHLIGHT               = "ORANGE_BUTTON_BORDER_HIGHLIGHT";
    public static final String  ORANGE_BUTTON_TEXT_NORMAL                    = "ORANGE_BUTTON_TEXT_NORMAL";
    public static final String  ORANGE_BUTTON_TEXT_HIGHLIGHT                 = "ORANGE_BUTTON_TEXT_HIGHLIGHT";

    public static final String  TURQUOISE_BUTTON_BG_NORMAL                   = "TURQUOISE_BUTTON_BG_NORMAL";
    public static final String  TURQUOISE_BUTTON_BORDER_NORMAL               = "TURQUOISE_BUTTON_BORDER_NORMAL";
    public static final String  TURQUOISE_BUTTON_BG_HIGHLIGHT                = "TURQUOISE_BUTTON_BG_HIGHLIGHT";
    public static final String  TURQUOISE_BUTTON_BORDER_HIGHLIGHT            = "TURQUOISE_BUTTON_BORDER_HIGHLIGHT";
    public static final String  TURQUOISE_BUTTON_TEXT_NORMAL                 = "TURQUOISE_BUTTON_TEXT_NORMAL";
    public static final String  TURQUOISE_BUTTON_TEXT_HIGHLIGHT              = "TURQUOISE_BUTTON_TEXT_HIGHLIGHT";
    
    // Textfield themes
    public static final String  TEXTFIELD_TEXT_NORMAL                        = "TEXTFIELD_TEXT_NORMAL";
    public static final String  TEXTFIELD_TEXT_HIGHLIGHT                     = "TEXTFIELD_TEXT_HIGHLIGHT";
    public static final String  EMOTICON_GRID_BACKGROUND                     = "EMOTICON_GRID_BACKGROUND";
    public static final String  LIGHT_BACKGROUND_COLOR                       = "LIGHT_BACKGROUND_COLOR";
    public static final String  GRAY_SEPARATOR_COLOR                         = "AUTHOR_POST_SEPARATOR";

    // ----------- REBRANDING 2014 ----------- //
    public static final String  PAGER_TAB_STRIP_BG_COLOR                     = "PAGER_TAB_STRIP_BG_COLOR";
    public static final String  LIGHT_FONT_COLOR                             = "LIGHT_FONT_COLOR";
    public static final String  WHITE_FONT_COLOR                             = "WHITE_FONT_COLOR";
    public static final String  BOTTOM_BAR_BG_COLOR                          = "BOTTOM_BAR_BG_COLOR";
    public static final String  CHAT_INPUT_TAB_BG_COLOR                      = "CHAT_INPUT_TAB_BG_COLOR";
    public static final String  CIRCLE_PAGE_INDICATOR_COLOR                  = "CIRCLE_PAGE_INDICATOR_COLOR";
    public static final String  CIRCLE_PAGE_INDICATOR_HIGHLIGHT_COLOR        = "CIRCLE_PAGE_INDICATOR_HIGHLIGHT_COLOR";
    public static final String  LIST_CATEGORY_BACKGROUND_COLOR               = "LIST_CATEGORY_BACKGROUND_COLOR";
    public static final String  TAB_DIVIDER_COLOR                            = "TAB_DIVIDER_COLOR";
    public static final String  LIST_ROW_DESC_COLOR                          = "LIST_ROW_DESC_COLOR";
    public static final String  LIST_ITEM_DIVIDER_COLOR                      = "LIST_ITEM_DIVIDER_COLOR";
    public static final String  SPP_REPLY_SEPARATOR_COLOR                    = "SPP_REPLY_SEPARATOR_COLOR";
    public static final String  ORANGE_NORMAL_BG                             = "ORANGE_NORMAL_BG";
    public static final String  ORANGE_ROUNDED_CORNER_BG                     = "ORANGE_ROUNDED_CORNER_BG";
    public static final String  WHITE_ROUNDED_CORNER_BG                      = "WHITE_ROUNDED_CORNER_BG";
    public static final String  GREY_ROUNDED_CORNER_BG                       = "GREY_ROUNDED_CORNER_BG";
    public static final String  PROMOTED_POST_MARKER_BG_COLOR                = "PROMOTED_POST_MARKER_BG_COLOR";

    public static void init() {

        Logger.debug.log(TAG, "Initializing Theme Values");

        Theme.color(LIGHT_TEXT_COLOR, ColorPalette.TEXT_LIGHT);
        Theme.description(LIGHT_TEXT_COLOR, "Light text color");

        Theme.color(LINK_TEXT_NORMAL, ColorPalette.TEXT_LIGHT);
        Theme.description(LINK_TEXT_NORMAL, "Link text normal");

        Theme.color(LINK_TEXT_HIGHLIGHT, ColorPalette.TEXT_LIGHT_GRAY);
        Theme.description(LINK_TEXT_HIGHLIGHT, "Link text highlight");

        Theme.color(ROUNDED_CORNERS_COLOR, 0xff424242);
        Theme.description(ROUNDED_CORNERS_COLOR, "Rounded corner color");

        Theme.color(IMAGE_BORDER_COLOR, 0xffd6d6d6);
        Theme.description(IMAGE_BORDER_COLOR, "Border color for images");

        Theme.color(IMAGE_BACKGROUND_COLOR, 0xffefefef);
        Theme.description(IMAGE_BACKGROUND_COLOR, "Background color for images");

        Theme.drawable(DISABLED_BUTTON_BG, TOP_BOTTOM, ColorPalette.BG_ORANGE, ColorPalette.BG_ORANGE);
        Theme.description(DISABLED_BUTTON_BG, "Background of disabled buttons");

        Theme.color(DISABLED_BUTTON_BORDER, 0xFFC63819);
        Theme.description(DISABLED_BUTTON_BORDER, "Border color of disabled buttons");

        Theme.color(DISABLED_BUTTON_TEXT, 0xFFFFB49D);
        Theme.description(DISABLED_BUTTON_TEXT, "Text color of disabled buttons");

        Theme.drawable(GRAY_BUTTON_BACKGROUND_NORMAL, TOP_BOTTOM, ColorPalette.BG_GRAY, ColorPalette.BG_GRAY);
        Theme.description(GRAY_BUTTON_BACKGROUND_NORMAL, "Gray button normal background");

        Theme.color(GRAY_BUTTON_BORDER_NORMAL, ColorPalette.BG_GRAY);
        Theme.description(GRAY_BUTTON_BORDER_NORMAL, "Gray button normal border");

        Theme.color(GRAY_BUTTON_TEXT_NORMAL, ColorPalette.TEXT_WHITE);
        Theme.description(GRAY_BUTTON_TEXT_NORMAL, "Gray button normal text");

        Theme.drawable(GRAY_BUTTON_BACKGROUND_HIGHLIGHT, TOP_BOTTOM, ColorPalette.TEXT_WHITE, ColorPalette.TEXT_WHITE);
        Theme.description(GRAY_BUTTON_BACKGROUND_HIGHLIGHT, "Gray button highlight background");

        Theme.color(GRAY_BUTTON_BORDER_HIGHLIGHT, ColorPalette.TEXT_WHITE);
        Theme.description(GRAY_BUTTON_BORDER_HIGHLIGHT, "Gray button highlight border");

        Theme.color(GRAY_BUTTON_TEXT_HIGHLIGHT, ColorPalette.TEXT_WHITE);
        Theme.description(GRAY_BUTTON_TEXT_HIGHLIGHT, "Gray button highlight text");

        Theme.drawable(ORANGE_BUTTON_BG_NORMAL, TOP_BOTTOM, ColorPalette.BG_ORANGE, ColorPalette.BG_ORANGE);
        Theme.description(ORANGE_BUTTON_BG_NORMAL, "Background of orange Button");

        Theme.color(ORANGE_BUTTON_BORDER_NORMAL, ColorPalette.BG_ORANGE);
        Theme.description(ORANGE_BUTTON_BORDER_NORMAL, "Border color of orange button");

        Theme.color(ORANGE_BUTTON_TEXT_NORMAL, ColorPalette.TEXT_LIGHT);
        Theme.description(ORANGE_BUTTON_TEXT_NORMAL, "Text color of orange button");

        Theme.drawable(ORANGE_BUTTON_BG_HIGHLIGHT, TOP_BOTTOM, ColorPalette.BG_ORANGE, ColorPalette.BG_ORANGE);
        Theme.description(ORANGE_BUTTON_BG_HIGHLIGHT, "Background of highlighted orange button");

        Theme.color(ORANGE_BUTTON_BORDER_HIGHLIGHT, ColorPalette.BG_ORANGE);
        Theme.description(ORANGE_BUTTON_BORDER_HIGHLIGHT, "Border color of highlighted orange button");

        Theme.color(ORANGE_BUTTON_TEXT_HIGHLIGHT, ColorPalette.TEXT_LIGHT);
        Theme.description(ORANGE_BUTTON_TEXT_HIGHLIGHT, "Text color of highliighted orange button");

        Theme.drawable(TURQUOISE_BUTTON_BG_NORMAL, TOP_BOTTOM, ColorPalette.BG_TURQUOISE, ColorPalette.BG_TURQUOISE);
        Theme.description(TURQUOISE_BUTTON_BG_NORMAL, "Background of button using turquoise color");

        Theme.color(TURQUOISE_BUTTON_BORDER_NORMAL, ColorPalette.BG_WHITE);
        Theme.description(TURQUOISE_BUTTON_BORDER_NORMAL, "Border color of button using turquoise color");

        Theme.color(TURQUOISE_BUTTON_TEXT_NORMAL, ColorPalette.TEXT_WHITE);
        Theme.description(TURQUOISE_BUTTON_TEXT_NORMAL, "Text color of button using turquoise color");

        Theme.drawable(TURQUOISE_BUTTON_BG_HIGHLIGHT, TOP_BOTTOM, ColorPalette.BG_TURQUOISE, ColorPalette.BG_TURQUOISE);
        Theme.description(TURQUOISE_BUTTON_BG_HIGHLIGHT, "Background of highlighted button using turquoise color");

        Theme.color(TURQUOISE_BUTTON_BORDER_HIGHLIGHT, ColorPalette.BG_WHITE);
        Theme.description(TURQUOISE_BUTTON_BORDER_HIGHLIGHT, "Border color of highlighted button using turquoise color");

        Theme.color(TURQUOISE_BUTTON_TEXT_HIGHLIGHT, ColorPalette.TEXT_WHITE);
        Theme.description(TURQUOISE_BUTTON_TEXT_HIGHLIGHT, "Text color of highlighted button using turquoise color");

        Theme.color(TEXTFIELD_TEXT_NORMAL, ColorPalette.TEXT_DARK);
        Theme.description(TEXTFIELD_TEXT_NORMAL, "Textfield text normal");

        Theme.color(TEXTFIELD_TEXT_HIGHLIGHT, ColorPalette.TEXT_DARK);
        Theme.description(TEXTFIELD_TEXT_HIGHLIGHT, "Textfield text highlight");

        Theme.roundedRectDrawable(EMOTICON_GRID_BACKGROUND, RoundedRectType.ROUND_ALL_CORNERS, 0xFFFFFFFF);
        Theme.description(EMOTICON_GRID_BACKGROUND, "Background of emoticon grid on sharebox screen");
        
        Theme.color(GRAY_SEPARATOR_COLOR, 0xFFD0D0D0);
        Theme.description(GRAY_SEPARATOR_COLOR, "separator between author and post on SPP and on full profile fragment");

        Theme.color(LIGHT_FONT_COLOR, ColorPalette.TEXT_GREY);
        Theme.description(LIGHT_FONT_COLOR, "Default light font color");

        Theme.color(WHITE_FONT_COLOR, ColorPalette.TEXT_WHITE);
        Theme.description(WHITE_FONT_COLOR, "a common color used in many places");

        Theme.color(PAGER_TAB_STRIP_BG_COLOR, ColorPalette.BG_LIGHT_GREY);
        Theme.description(PAGER_TAB_STRIP_BG_COLOR, "Background color of the pager tab strip");

        Theme.color(LIGHT_BACKGROUND_COLOR, ColorPalette.BG_WHITE);
        Theme.description(LIGHT_BACKGROUND_COLOR, "Default light background color");

        Theme.color(BOTTOM_BAR_BG_COLOR, ColorPalette.BG_WHITE);
        Theme.description(BOTTOM_BAR_BG_COLOR, "Background color of the bottom bar");

        Theme.color(CHAT_INPUT_TAB_BG_COLOR, ColorPalette.BG_LIGHT_GREY);
        Theme.description(CHAT_INPUT_TAB_BG_COLOR, "Background color of tab at the bottom of chat input drawer");

        Theme.color(CIRCLE_PAGE_INDICATOR_COLOR, ColorPalette.BG_LIGHT_GREY);
        Theme.description(CIRCLE_PAGE_INDICATOR_COLOR, "Color of circle page indicator in chat input drawer");

        Theme.color(CIRCLE_PAGE_INDICATOR_HIGHLIGHT_COLOR, ColorPalette.TEXT_LIGHT_BROWN);
        Theme.description(CIRCLE_PAGE_INDICATOR_HIGHLIGHT_COLOR,
                "Color of highlighted circle page indicator in chat input drawer");

        Theme.color(LIST_CATEGORY_BACKGROUND_COLOR, ColorPalette.BG_LIGHT_BEIGE);
        Theme.description(LIST_CATEGORY_BACKGROUND_COLOR, "Background color of list category");

        Theme.color(TAB_DIVIDER_COLOR, ColorPalette.DIVIDER_SAND_LIGHT);
        Theme.description(TAB_DIVIDER_COLOR, "divider of tabs at the bottom of chat input drawer");

        Theme.color(LIST_ROW_DESC_COLOR, ColorPalette.TEXT_LIGHT_BROWN);
        Theme.description(LIST_ROW_DESC_COLOR, "Description color of a list row");

        Theme.color(LIST_ITEM_DIVIDER_COLOR, ColorPalette.BG_LIGHT_BEIGE);
        Theme.description(LIST_ITEM_DIVIDER_COLOR, "List divider color");

        Theme.color(SPP_REPLY_SEPARATOR_COLOR, ColorPalette.DIVIDER_SAND);
        Theme.description(SPP_REPLY_SEPARATOR_COLOR, "color of separators of replies and reshares on spp");

        Theme.roundedRectDrawable(ORANGE_ROUNDED_CORNER_BG, RoundedRectType.ROUND_ALL_CORNERS, ColorPalette.BG_ORANGE);
        Theme.description(ORANGE_ROUNDED_CORNER_BG, "default orange background with small rounded corner");

        Theme.color(ORANGE_NORMAL_BG, ColorPalette.BG_ORANGE);
        Theme.description(ORANGE_NORMAL_BG, "default orange background");

        Theme.roundedRectDrawable(WHITE_ROUNDED_CORNER_BG, RoundedRectType.ROUND_ALL_CORNERS, ColorPalette.BG_WHITE);
        Theme.description(WHITE_ROUNDED_CORNER_BG, "default white background with small rounded corner");

        Theme.roundedRectDrawable(GREY_ROUNDED_CORNER_BG, RoundedRectType.ROUND_ALL_CORNERS, ColorPalette.BG_LIGHT_GREY);
        Theme.description(GREY_ROUNDED_CORNER_BG, "default grey background with small rounded corner");

        Theme.color(PROMOTED_POST_MARKER_BG_COLOR, ColorPalette.BG_ORANGE);
        Theme.description(PROMOTED_POST_MARKER_BG_COLOR, "promoted post side marker color");
    }
}
