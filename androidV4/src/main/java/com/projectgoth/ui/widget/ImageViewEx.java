/**
 * Copyright (c) 2013 Project Goth
 *
 * ImageViewEx.java.java
 * Created Jun 18, 2013, 6:16:36 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.GUIConst;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.imagefetcher.UIUtils;

/**
 * @author cherryv
 *
 */
public class ImageViewEx extends ImageView {

    public enum ImageType {
        GENERAL,
        CHAT_IMAGE,
        BLOG_IMAGE,
        CHAT_PIN_IMAGE;
    }

    public enum IconOverlay {
        NONE,
        PLAY,
        GIF;
    }
    
    public static final String LOG_TAG = ImageViewEx.class.getSimpleName();
    private Paint   paint;
    private IconOverlay iconOverlay = IconOverlay.NONE;
    private boolean placeholder = false;

    private boolean isImageLoading;
    private int[] paddings;

    private int placeHolderHeight = ApplicationEx.getDimension(R.dimen.thumbnail_placeholder_height);
    private final int chatImageMaxLength = ApplicationEx.getDimension(R.dimen.chat_messages_image_max_length);
    private final int chatTinyImageMinLength = ApplicationEx.getDimension(R.dimen.chat_messages_tiny_image_min_length);
    private final int chatTinyImageCriterion = ApplicationEx.getDimension(R.dimen.chat_messages_tiny_image_criterion);
    private final int chatPinImageMaxHeight = ApplicationEx.getDimension(R.dimen.chat_pin_messages_image_max_height);
    private final int postImageMaxLength = ApplicationEx.getDimension(R.dimen.post_image_max_length);
    private final int postTinyImageMinLength = ApplicationEx.getDimension(R.dimen.post_image_min_length);
    private final int postTinyImageCriterion = ApplicationEx.getDimension(R.dimen.post_tiny_image_criterion);
    private static final int loadingImageHeight = ApplicationEx.getDimension(R.dimen.photo_loading_bot_height);
    private final float mRoundedImageRadius = 2;
    private ImageType imageType = ImageType.GENERAL;
    private Context mContext;

    /**
     * Create ImageViewEx with context defined.
     *
     * @param context
     */
    public ImageViewEx(Context context) {
        this(context, null, 0);
    }

    /**
     * Create ImageViewEx with context and attributes defined.
     *
     * @param context
     * @param attrs
     */
    public ImageViewEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Create ImageViewEx with context, attributes and style defined.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ImageViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
        setBorder(true);
        iconOverlay = IconOverlay.NONE;
        mContext = context;
    }

    /**
     * Enable or disable border of the image.
     *
     * @param enable
     */
    public void setBorder(boolean enable) {
        if (enable) {
            ShapeDrawable border = new ShapeDrawable();
            int xSmallPadding = ApplicationEx.getDimension(R.dimen.xsmall_padding);
            border.setPadding(xSmallPadding, xSmallPadding, xSmallPadding, xSmallPadding);
            border.getPaint().setColor(Theme.getColor(ThemeValues.IMAGE_BORDER_COLOR));

            UIUtils.setBackground(this, border);
        } else {
            UIUtils.setBackground(this, null);
        }
    }

    /**
     * Calculate the image size according to {@link ImageType}
     * For CHAT_IMAGE, BLOG_IMAGE, rescale the image size if the image is too large or too tiny
     * For GENERAL, resize with aspect ratio.
     * If the bitmap is not null considered wants the aspect ratio.
     * The aspect ratio will follow the width, so width is fixed, height will be adjusted.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Bitmap bitmap = getBitmap();

        switch (imageType) {
            case CHAT_IMAGE: {
                // Image in Chat
                if (isImageLoading || placeholder) {
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = placeHolderHeight;
                    setMeasuredDimension(width, height);
                } else if (bitmap != null) {

                    int bitmapWidth = bitmap.getWidth();
                    int bitmapHeight = bitmap.getHeight();
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = MeasureSpec.getSize(heightMeasureSpec);
                    //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                    //int heightMode = MeasureSpec.getMode(heightMeasureSpec);

                    if (bitmapWidth < chatTinyImageCriterion && bitmapHeight < chatTinyImageCriterion) {
                        // Tiny image case
                        if (bitmapWidth >= bitmapHeight) {
                            width = chatTinyImageMinLength;
                            height = width * bitmapHeight / bitmapWidth;
                        } else {
                            height = chatTinyImageMinLength;
                            width = height * bitmapWidth / bitmapHeight;
                        }
                    } else if (bitmapWidth >= bitmapHeight && width > chatImageMaxLength) {
                        // Width too long case
                        width = chatImageMaxLength;
                        height = width * bitmapHeight / bitmapWidth;
                    } else if (bitmapHeight >= bitmapWidth && (height > postImageMaxLength || height == 0)) {
                        // Height too long case || height unlimited case
                        height = chatImageMaxLength;
                        width = height * bitmapWidth / bitmapHeight;
                    } else {
                        // Normal image size case
                        height = width * bitmapHeight / bitmapWidth;
                    }
                    setMeasuredDimension(width, height);
                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
                break;
            }

            case BLOG_IMAGE: {
                // Image in Blog
                if (isImageLoading || placeholder) {
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = placeHolderHeight;
                    setMeasuredDimension(width, height);
                } else if (bitmap != null) {

                    int bitmapWidth = bitmap.getWidth();
                    int bitmapHeight = bitmap.getHeight();
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = MeasureSpec.getSize(heightMeasureSpec);
                    //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                    //int heightMode = MeasureSpec.getMode(heightMeasureSpec);

                    if (bitmapWidth < postTinyImageCriterion && bitmapHeight < postTinyImageCriterion) {
                        // Tiny image case
                        if (bitmapWidth >= bitmapHeight) {
                            width = postTinyImageMinLength;
                            height = width * bitmapHeight / bitmapWidth;
                        } else {
                            height = postTinyImageMinLength;
                            width = height * bitmapWidth / bitmapHeight;
                        }
                    } else if (bitmapHeight >= bitmapWidth && (height > postImageMaxLength || height == 0)) {
                        // Height too long case || height unlimited case
                        height = postImageMaxLength;
                        width = height * bitmapWidth / bitmapHeight;
                    } else {
                        // Normal image size case
                        height = width * bitmapHeight / bitmapWidth;
                    }
                    setMeasuredDimension(width, height);
                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
                break;
            }

            case CHAT_PIN_IMAGE: {
                if (isImageLoading || placeholder) {
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = placeHolderHeight;
                    setMeasuredDimension(width, height);
                } else if (bitmap != null) {
                    int bitmapWidth = bitmap.getWidth();
                    int bitmapHeight = bitmap.getHeight();
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = MeasureSpec.getSize(heightMeasureSpec);
                    //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                    //int heightMode = MeasureSpec.getMode(heightMeasureSpec);
                    height = chatPinImageMaxHeight;
                    width = height * bitmapWidth / bitmapHeight;
                    setMeasuredDimension(width, height);
                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
                break;
            }

            default: {
                // General image type
                if (isImageLoading || placeholder) {
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = placeHolderHeight;
                    setMeasuredDimension(width, height);
                } else if (bitmap != null) {
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = width * bitmap.getHeight() / bitmap.getWidth();
                    setMeasuredDimension(width, height);
                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
                break;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isImageLoading) {
            // draw overlay icon if needed
            Bitmap icon = null;
            if (isPlayable()) {
                icon = GUIConst.PLAY;
            } else if (isGif()) {
                icon = GUIConst.GIF;
            }

            if (icon != null) {
                int x = (canvas.getWidth() - icon.getWidth()) >> 1;
                int y = (canvas.getHeight() - icon.getHeight()) >> 1;
                if (x >= 0 && y >= 0) {
                    canvas.drawBitmap(icon, x, y, paint);
                }
            }
        }
    }

    public void setIconOverlay(IconOverlay iconOverlay) {
        this.iconOverlay = iconOverlay;
    }

    public boolean isPlayable() {
        return iconOverlay.equals(IconOverlay.PLAY);
    }

    public boolean isGif() {
        return iconOverlay.equals(IconOverlay.GIF);
    }

    /**
     * Display placeholder when the actual image is not yet displayed. When placeholder displayed,
     * fixed height will be used. To use the normal width/height spec e.g. fill_parent set
     * placeholder to false.
     * @param display true to display the placeholder, false otherwise
     */
    public void setPlaceholder(boolean display) {
        this.placeholder = display;
    }

    /**
     * Get placeholder state.
     * @return true if displayed, false otherwise
     */
    public boolean isPlaceholderDisplayed() {
        return this.placeholder;
    }

    /**
     * Get the image bitmap.
     * @return a {@link Bitmap} if the {@link Drawable} member is an instance of {@link BitmapDrawable}, null otherwise.
     */
    public Bitmap getBitmap() {
        Drawable drawable = getDrawable();
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }

    public boolean isImageLoading() {
        return isImageLoading;
    }

    public void setImageLoadingPosition(boolean isImageLoading) {
        if (!this.isImageLoading && isImageLoading) {
            paddings = new int[] {getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom()};
            int paddingTopBottom = (placeHolderHeight - loadingImageHeight) >> 1;
            setPadding(0, paddingTopBottom, 0, paddingTopBottom);
        } else if (this.isImageLoading && !isImageLoading ) {
            setPadding(paddings[0], paddings[1], paddings[2], paddings[3]);
        }
        this.isImageLoading = isImageLoading;

    }

    public void setLoadingImageBmp(Bitmap bmp) {
        super.setImageBitmap(bmp);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        setImageLoadingPosition(false);

        //we only crop rounded-corner of chat image above android 3.0 to avoid possible OOM
        if (imageType == ImageType.CHAT_IMAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            bm = getRoundedCornerBitmap(bm);
        }
        super.setImageBitmap(bm);
    }

    public void setLoadingImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        setImageLoadingPosition(true);
    }

    public void setPlaceHolderHeight(int placeHolderHeight) {
        this.placeHolderHeight = placeHolderHeight;
    }

    public void setImageTyep(ImageType imageType) {
        this.imageType = imageType;

    }

    private Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap roundedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(roundedBitmap);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        float roundPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mRoundedImageRadius
                , mContext.getResources().getDisplayMetrics());

        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return roundedBitmap;
    }

}
