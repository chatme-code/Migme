/**
 * Copyright (c) 2013 Project Goth
 *
 * ImageHandler.java
 * Created 11 Mar, 2014, 1:00:23 pm
 */

package com.projectgoth.imagefetcher;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.enums.UserProfileDisplayPictureChoiceEnum;
import com.projectgoth.common.Constants;
import com.projectgoth.common.GUIConst;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.fusion.packet.UrlHandler;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.ChatConversation.GroupChatInfo;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.ui.transformation.CircleTransformation;
import com.projectgoth.ui.widget.ImageViewEx;
import com.projectgoth.util.AndroidLogger;

/**
 * @author warrenbalcos
 * 
 */
public class ImageHandler {

    private static final String LOG_TAG          = AndroidLogger.makeLogTag(ImageHandler.class);
    
    /**
     * The biggest image we request so far is for photo posts in miniblog. We
     * use the max 800x URL for this.
     */
    private static final int        MAX_IMAGE_HEIGHT                        = 800;
    private static final int        MAX_IMAGE_WIDTH                         = 800;

    private static final int        MAX_LOW_END_IMAGE_HEIGHT                = 300;
    private static final int        MAX_LOW_END_IMAGE_WIDTH                 = 300;

    // disk cache 200M
    private static final int        DISK_CACHE_SIZE                         = 200 * 1024 * 1024;
    // disk cache image count 500
    private static final int        DISK_CACHE_IMAGE_COUNT                  = 2000;
    // memory cache size 6% of available memory
    private static final int        MEMORY_CACHE_USED_PERCENTAGE            = 6;

    private static final int        MAX_PARTICIPANTS_FOR_GROUP_CHAT_ICON    = 4;
    private static final int        DEFAULT_THREAD_POOL_SIZE                = 4;
    private static final int        MIN_THREAD_POOL_SIZE                    = 2;
    private Bitmap                  mBackgroundIcon;
    private Bitmap                  mFailedBackgroundIcon;

    private static class ImageHandlerHolder {
        static final ImageHandler sINSTANCE = new ImageHandler();
    }

    public static ImageHandler getInstance() {
        return ImageHandlerHolder.sINSTANCE;
    }

    private ImageHandler() {

        //fine-tune memory usage in Universal Image Loader
        //base on https://github.com/nostra13/Android-Universal-Image-Loader/wiki/Useful-Info
        Context context = ApplicationEx.getContext();
        ImageLoaderConfiguration.Builder configBuilder = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(DISK_CACHE_SIZE)
                .diskCacheFileCount(DISK_CACHE_IMAGE_COUNT)
                .memoryCacheSizePercentage(MEMORY_CACHE_USED_PERCENTAGE)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .threadPoolSize(MIN_THREAD_POOL_SIZE);
        if (UIUtils.isLegacyDevice()) {
            configBuilder.diskCacheExtraOptions(MAX_LOW_END_IMAGE_WIDTH, MAX_LOW_END_IMAGE_HEIGHT, null);
        }

        ImageLoaderConfiguration config = configBuilder.build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        mBackgroundIcon = BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.ad_gallery_grey));
        mFailedBackgroundIcon = BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.ad_pictureerror_grey));
    }

    public static interface ImageLoadListener {

        public void onImageLoaded(Bitmap bitmap);

        public void onImageFailed(ImageView imageView);
    }

    public void loadImage(final String url, final ImageLoadListener listener) {

        ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (listener != null) {
                    listener.onImageFailed(null);
                    Logger.error.log(LOG_TAG, "Failed to load image: ", url);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (listener != null) {
                    listener.onImageLoaded(loadedImage);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        };
        ImageLoader.getInstance().loadImage(url, imageLoadingListener);

    }

    public void loadImage(final String url, final ImageView imageView, final int resourceId) {
        loadImage(url, imageView, resourceId, -1, -1, null);
    }

    public void loadImage(final String url, final ImageView imageView, final ImageLoadListener listener) {
        loadImage(url, imageView, -1, -1, -1, listener);
    }

    public void loadImage(final String url, final ImageView imageView) {
        loadImage(url, imageView, -1, -1, -1, null);
    }

    class BitmapHolder {

        Bitmap bitmap;

    }

    public Bitmap loadImage(final String url, final ImageView imageView,
            final int resourceId, int width, int height, final ImageLoadListener listener) {
        return loadImage(url, imageView, resourceId, width, height, false, listener);
    }

    public Bitmap loadImage(final String url, final ImageView imageView,
            final int resourceId, final int width, final int height, final boolean forceResize, final ImageLoadListener listener) {

        final BitmapHolder bitmapHolder = getInstance().new BitmapHolder();
        try {
            int mWidth = MAX_IMAGE_WIDTH;
            int mHeight = MAX_IMAGE_HEIGHT;
            if (width > 0 && width < MAX_IMAGE_WIDTH && height > 0 && height < MAX_IMAGE_HEIGHT) {
                mWidth = width;
                mHeight = height;
            }

            BitmapProcessor bp = new BitmapProcessor() {
                @Override
                public Bitmap process(Bitmap bitmap) {
                    if (!forceResize
                            || width <= 0 || height <= 0
                            || (width == bitmap.getWidth() && height == bitmap.getHeight())) {
                        return bitmap;
                    }
                    // If there's more than a 2x expansion in size and bitmap is below threshold width, then disable filtering
                    final int NO_FILTER_THRESHOLD = 20;
                    final boolean disableFilter = width >= bitmap.getWidth() * 2 && bitmap.getWidth() <= NO_FILTER_THRESHOLD;
                    return Bitmap.createScaledBitmap(bitmap, width, height, !disableFilter);
                }
            };

            final ImageSize targetSize = new ImageSize(mWidth, mHeight);
            DisplayImageOptions.Builder displayOptionsBuilder = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .preProcessor(bp);

            //Use RGB 565 bitmap in 2.3.x devices
            if (UIUtils.isLegacyDevice()) {
                displayOptionsBuilder.bitmapConfig(Bitmap.Config.RGB_565)
                        .imageScaleType(ImageScaleType.EXACTLY);
            }

            final DisplayImageOptions options;
            if (resourceId > 0) {
                options = displayOptionsBuilder.showImageOnLoading(resourceId).build();
            } else {
                options = displayOptionsBuilder.build();
            }

            final String fetchUrl = url;

            final ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    if (imageView != null && imageView instanceof ImageViewEx) {
                        ImageViewEx imageViewEx = (ImageViewEx) imageView;
                        if (resourceId != -1) {
                            imageViewEx.setLoadingImageBitmap(mBackgroundIcon);
                        }
                        imageViewEx.setIconOverlay(ImageViewEx.IconOverlay.NONE);
                    }
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    if (imageView != null && imageView instanceof ImageViewEx && resourceId != -1) {
                        ((ImageViewEx) imageView).setLoadingImageBitmap(mFailedBackgroundIcon);
                    }
                    if (listener != null) {
                        listener.onImageFailed(imageView);
                    }
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (loadedImage != null && !loadedImage.isRecycled()) {
                        bitmapHolder.bitmap = loadedImage;
                        if (imageView != null) {
                            //if imageView has tag overlay_icon like video thumbnail, we show overlay icon.
                            ImageViewEx.IconOverlay iconOverlay = (ImageViewEx.IconOverlay) imageView.getTag(R.id.overlay_icon);
                            if (imageView instanceof ImageViewEx && iconOverlay != null) {
                                ((ImageViewEx) imageView).setIconOverlay(iconOverlay);
                            }
                            imageView.setImageBitmap(loadedImage);
                        }
                        if (listener != null) {
                            listener.onImageLoaded(loadedImage);
                        }
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    //AD-1317 sometimes the new post image loading can be cancelled which make the loading gif always display.
                    if (imageView != null && imageView instanceof ImageViewEx && ((ImageViewEx) imageView).isImageLoading()) {
                        ImageLoader.getInstance().displayImage(fetchUrl, imageView);
                    }
                }
            };

            ImageLoader.getInstance().loadImage(fetchUrl, targetSize, options, imageLoadingListener);

        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }

        return bitmapHolder.bitmap;
    }

    /**
     * Helper method for retrieving any image from the supplied Http URL. If the
     * image fetcher or the supplied URL is null or invalid, this method will
     * just automatically set the default image drawable in the supplied
     * ImageView.
     *
     * @param fetcher         Image fetcher object to handle retrieval of the image
     * @param view            ImageView where the retrieved image will be set
     * @param url             Complete HTTP URL where the image can be retrieved
     * @param roundedCorners
     * @param defaultResImage
     */
    public void loadImageFromUrl(final ImageView view, String url, boolean transformToCircle, int defaultResImage) {
        loadImageFromUrl(view, url, transformToCircle, defaultResImage, -1);
    }

    /**
     * Helper method for retrieving any image from the supplied Http URL. If the
     * image fetcher or the supplied URL is null or invalid, this method will
     * just automatically set the default image drawable in the supplied
     * ImageView.
     *
     * @param fetcher         Image fetcher object to handle retrieval of the image
     * @param view            ImageView where the retrieved image will be set
     * @param url             Complete HTTP URL where the image can be retrieved
     * @param roundedCorners
     * @param defaultResImage
     * @param size
     */
    public void loadImageFromUrl(final ImageView view, final String url, boolean transformToCircle,
            int defaultResImage, int size) {

        loadImageFromUrlWithCallback(view, url, transformToCircle, defaultResImage, size, null);

    }

    public void loadImageFromDeezerUrl(final ImageView view, final String url, boolean transformToCircle
            , int defaultResImage, int size, ImageLoadListener listener) {
        loadImageFromUrlWithCallback(view, url, transformToCircle, defaultResImage, -1, listener);
    }

    public void loadImageFromUrlWithCallback(final ImageView view, final String url, boolean transformToCircle,
            int defaultResImage, int size, final ImageLoadListener callback) {
        if (view != null) {
            view.setTag(R.id.image_loading, new Boolean(true));

            if (!TextUtils.isEmpty(url)) {

                DisplayImageOptions options = null;

                if (transformToCircle) {
                    options = new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .showImageOnLoading(defaultResImage)
                            .displayer(new CircleBitmapDisplayer())
                            .build();
                } else {
                    options = new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .showImageOnLoading(defaultResImage)
                            .build();
                }

                ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View mView, FailReason failReason) {
                        if (callback != null) {
                            callback.onImageFailed(view);
                        }
                        Logger.error.log(LOG_TAG, "Failed loading image: ", url);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View mView, Bitmap loadedImage) {
                        if (callback != null) {
                            callback.onImageLoaded(loadedImage);

                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                    }

                };

                ImageLoader.getInstance().displayImage(url, view, options, imageLoadingListener);

            } else if (defaultResImage > 0) {
                view.setImageResource(defaultResImage);
            }
        }
    }

    /**
     * Helper method to retrieve the display picture of a user. Displays a
     * default profile picture while the actual image is still being retrieved.
     * The size of the display picture is automatically set and the placeholder
     * icon is also defined.
     *
     * @param fetcher        Image fetcher object to handle retrieval of the image
     * @param view           ImageView where the retrieved image will be set
     * @param username       User whose display picture we need to retrieve
     * @param type           Used to identify whether to retrieve avatar or profile picture
     * @param roundedCorners True if the image to be loaded should have rounded corners.
     *                       The roundness of the image will be based on the value returned
     *                       in {@link #getDefaultCornerRadiusInPx()}
     * @deprecated it is better to use another one with size parameter so that
     * we can control it to save memory for small phone or a better
     * quality for big phone if we want
     */
    public void loadDisplayPictureOfUser(ImageView view, String username,
            UserProfileDisplayPictureChoiceEnum type, boolean roundedCorners) {
        if (!TextUtils.isEmpty(username)) {
            String avatarUrl = getDisplayPictureUrl(username, type);
            int loading_icon = roundedCorners ? R.drawable.icon_default_avatar
                    : R.drawable.icon_default_avatar;
            loadImageFromUrl(view, avatarUrl, roundedCorners, loading_icon);
        }
    }

    /**
     * Helper method to retrieve the display picture of a user. Displays a
     * default profile picture while the actual image is still being retrieved.
     * The size of the display picture is automatically set and the placeholder
     * icon is also defined.
     *
     * @param fetcher        Image fetcher object to handle retrieval of the image
     * @param view           ImageView where the retrieved image will be set
     * @param username       User whose display picture we need to retrieve
     * @param type           Used to identify whether to retrieve avatar or profile picture
     * @param size           size of the display picture to fetch
     * @param roundedCorners True if the image to be loaded should have rounded corners.
     *                       The roundness of the image will be based on the value returned
     *                       in {@link #getDefaultCornerRadiusInPx()}
     */
    public void loadDisplayPictureOfUser(ImageView view, String username,
            UserProfileDisplayPictureChoiceEnum type, int size, boolean roundedCorners) {
        loadDisplayPictureOfUser(view, username, type, size, roundedCorners, null);
    }

    public void loadDisplayPictureOfUser(ImageView view, String username,
            UserProfileDisplayPictureChoiceEnum type, int size, boolean roundedCorners, ImageLoadListener listener) {
        if (!TextUtils.isEmpty(username)) {
            Session session = Session.getInstance();
            // Load the profile image from GUID for self user
            if (session != null && session.isSelfByUsername(username)) {
                loadDisplayPictureFromGuid(view, Session.getInstance().getDisplayableGuid(), size, roundedCorners);
            } else {
                String avatarUrl = getDisplayPictureUrl(username, type, size);
                int loading_icon = roundedCorners ? R.drawable.icon_default_avatar
                        : R.drawable.icon_default_avatar;
                loadImageFromUrlWithCallback(view, avatarUrl, roundedCorners, loading_icon, -1, listener);
            }
        }
    }

    /**
     * Loads the display picture type that is specified in the user's profile.
     *
     * @param imgView        The {@link ImageView} where the retrieved image will be set.
     * @param username       The name of the user whose display picture will be fetched.
     * @param size           The size of the display picture to fetch.
     * @param roundedCorners true if the image to be loaded should have rounded corners and
     *                       false otherwise. The roundness of the image will be based on
     *                       the value returned in {@link #getDefaultCornerRadiusInPx()}
     * @return The {@link UserProfileDisplayPictureChoiceEnum} that was fetched
     * and set on the imgView. If the user's profile could not be
     * retrieved from cache, then null is returned.
     */
    public UserProfileDisplayPictureChoiceEnum loadProfileDisplayPictureOfUser(ImageView imgView,
            String username, int size, boolean roundedCorners) {
        UserProfileDisplayPictureChoiceEnum userProfileDisplayPictureChoice = null;
        final Profile profile = UserDatastore.getInstance().getProfileWithUsername(username, false);
        if (profile != null) {
            userProfileDisplayPictureChoice = profile.getDisplayPictureType();
            loadDisplayPictureOfUser(imgView, username, userProfileDisplayPictureChoice, size,
                    roundedCorners);
        }

        return userProfileDisplayPictureChoice;
    }

    /**
     * Loads the display picture type that is specified in the user's profile.
     * If the profile of the user could not be retrieved from cache, the avatar
     * set by the user is loaded.
     *
     * @param imgView        The {@link ImageView} where the retrieved image will be set.
     * @param username       The name of the user whose display picture will be fetched.
     * @param size           The size of the display picture to fetch.
     * @param roundedCorners true if the image to be loaded should have rounded corners and
     *                       false otherwise. The roundness of the image will be based on
     *                       the value returned in {@link #getDefaultCornerRadiusInPx()}
     * @return The {@link UserProfileDisplayPictureChoiceEnum} that was fetched
     * and set on the imgView.
     */
    public UserProfileDisplayPictureChoiceEnum loadDisplayPictureOfUser(ImageView imgView, String username,
            int size, boolean roundedCorners) {
       return loadDisplayPictureOfUser(imgView, username, size, roundedCorners, null);
    }

    public UserProfileDisplayPictureChoiceEnum loadDisplayPictureOfUser(ImageView imgView, String username,
            int size, boolean roundedCorners, ImageLoadListener listener) {
        UserProfileDisplayPictureChoiceEnum userProfileDisplayPictureChoice = loadProfileDisplayPictureOfUser(imgView,
                username, size, roundedCorners);
        if (userProfileDisplayPictureChoice == null) {
            loadDisplayPictureOfUser(imgView, username, UserProfileDisplayPictureChoiceEnum.AVATAR, size,
                    roundedCorners, listener);
            return UserProfileDisplayPictureChoiceEnum.AVATAR;
        }

        return userProfileDisplayPictureChoice;
    }

    /**
     * Loads the display picture type that is specified in the user's profile.
     * If the user's profile could not be found, then the image from the given
     * guid is loaded instead.
     *
     * @param imgView         The {@link ImageView} where the retrieved image will be set.
     * @param username        The name of the user whose display picture is to be fetched.
     * @param size            The size of the display picture to fetch.
     * @param roundedCorners  true if the image to be loaded should have rounded corners and
     *                        false otherwise. The roundness of the image will be based on
     *                        the value returned in {@link #getDefaultCornerRadiusInPx()}
     * @param guid            GUID representing the image will be retrieved
     * @param defaultResImage The resource Id of the default image that will be used if the
     *                        image URL cannot be created, or while the image is still being
     *                        downloaded. This parameter is only used if fetching a GUID.
     * @return true if the display picture from the user's profile is loaded and
     * false if the guid represented image is loaded instead.
     */
    public boolean loadDisplayPictureOrGuid(ImageView imgView, String username, int size,
            boolean roundedCorners, String guid, int defaultResImage) {
        if (loadProfileDisplayPictureOfUser(imgView, username, size, roundedCorners) == null) {
            loadImageFromGuid(imgView, guid, size, roundedCorners, defaultResImage);

            return false;
        }

        return true;
    }

    /**
     * Helper method for retrieving any image from the Image Server. The image
     * URL is also constructed by this method based on the supplied GUID.
     *
     * @param fetcher         Image fetcher object to handle retrieval of the image
     * @param view            ImageView where the retrieved image will be set
     * @param guid            GUID where the image will be retrieved
     * @param size            Size of the image that will be requested from the image server
     * @param roundedCorners  True if the image to be loaded should have rounded corners.
     *                        The roundness of the image will be based on the value returned
     *                        in {@link #getDefaultCornerRadiusInPx()}
     * @param defaultResImage The resource Id of the default image that will be used if the
     *                        image URL cannot be created, or while the image is still being
     *                        downloaded
     */
    public void loadImageFromGuid(ImageView view, String guid, int size, boolean roundedCorners,
            int defaultResImage) {
        String imageURL = constructFullImageLink(guid, size);
        loadImageFromUrl(view, imageURL, roundedCorners, defaultResImage);
    }

    /**
     * Helper method to retrieve the display picture based on GUID. Displays a
     * default profile picture while the actual image is still being retrieved.
     * The size of the display picture is automatically set and the placeholder
     * icon is also defined.
     * <p/>
     * * @deprecated it is better to use another one with size parameter so that
     * we can control it to save memory for small phone or a better quality for
     * big phone if we want
     *
     * @param fetcher        Image fetcher object to handle retrieval of the image
     * @param view           ImageView where the retrieved image will be set
     * @param guid           GUID where the image will be retrieved
     * @param roundedCorners True if the image to be loaded should have rounded corners.
     *                       The roundness of the image will be based on the value returned
     *                       in {@link #getDefaultCornerRadiusInPx()}
     */
    public void loadDisplayPictureFromGuid(ImageView view, String guid, boolean roundedCorners) {
        int defaultResImage = roundedCorners ? R.drawable.icon_default_avatar
                : R.drawable.icon_default_avatar;
        loadImageFromGuid(view, guid, ApplicationEx.getDimension(R.dimen.contact_pic_size_normal), roundedCorners,
                defaultResImage);
    }

    public void loadDisplayPictureFromGuid(ImageView view, String guid, int size, boolean roundedCorners) {
        int defaultResImage = roundedCorners ? R.drawable.icon_default_avatar
                : R.drawable.icon_default_avatar;
        loadImageFromGuid(view, guid, size, roundedCorners, defaultResImage);
    }

    /**
     * Constructs the full URL for retrieving a user's display picture
     *
     * @param username User whose display picture we need to retrieve
     * @param type     Used to identify whether to retrieve avatar or profile picture
     * @param size     Size (height) of the image to be returned
     * @return Full URL where image can be retrieved via HTTP
     */
    private static String getDisplayPictureUrl(String username, UserProfileDisplayPictureChoiceEnum type, int size) {
        String typeStr = type == UserProfileDisplayPictureChoiceEnum.AVATAR ? "a/" : "u/";
        String url = constructFullImageLink(typeStr + username, size, 1, 1);
        return url;
    }

    /**
     * Helper method for constructing the full URL for retrieving a user's
     * display picture. Calls
     * {@link #getDisplayPictureUrl(String, UserProfileDisplayPictureChoiceEnum)}
     * with default size
     *
     * @param username User whose display picture we need to retrieve
     * @param type     Used to identify whether to retrieve avatar or profile picture
     * @return Full URL where image can be retrieved via HTTP
     */
    private static String getDisplayPictureUrl(String username, UserProfileDisplayPictureChoiceEnum type) {
        return getDisplayPictureUrl(username, type, ApplicationEx.getDimension(R.dimen.contact_pic_size_normal));
    }

    /**
     * Helper method to retrieve the full URL of an image based on supplied
     * image path (e.g. GUID). Calls
     * {@link #constructFullImageLink(String, int, int, int)} with default
     * aspect ratio and crop values supplied.
     *
     * @param stringRep Path to the image to retrieve
     * @param size      Height of the image to be returned
     * @return
     */
    public static String constructFullImageLink(String stringRep, int size) {
        return constructFullImageLink(stringRep, size, 1, 1);
    }

    /**
     * Constructs the full URL to retrieve an image based on supplied image path
     * (e.g. GUID) Size, aspect ratio and crop values can be set as parameters
     *
     * @param stringRep   Path to the image to retrieve
     * @param size        Height of the image to be returned
     * @param aspectRatio Aspect ratio of image to be returned
     * @param crop        Cropping parameter
     * @return Full URL where image can be retrieved via HTTP
     */
    private static String constructFullImageLink(String stringRep, int size, int aspectRatio, int crop) {
        return constructFullImageLink(stringRep, size, size, aspectRatio, crop);
    }

    /**
     * Constructs the full URL to retrieve an image based on supplied image path
     * (e.g. GUID) Size, aspect ratio and crop values can be set as parameters
     *
     * @param stringRep   Path to the image to retrieve
     * @param size        Height of the image to be returned
     * @param aspectRatio Aspect ratio of image to be returned
     * @param crop        Cropping parameter
     * @return Full URL where image can be retrieved via HTTP
     */
    public static String constructFullImageLink(String stringRep, int width, int height, int aspectRatio, int crop) {
        if (stringRep == null) {
            return Constants.BLANKSTR;
        }

        String result = UrlHandler.getInstance().getImageServerUrl() + stringRep + "?w=" + width + "&h=" + height
                + "&a=" + aspectRatio + "&c=" + crop;
        if (stringRep.startsWith(Constants.LINK_HTTP) || stringRep.startsWith(Constants.LINK_DRAWABLE)) {
            result = stringRep;
        }
        return result;
    }

    public static String constructFullImageLinkForChatThumbnail(String guid, int size) {
        return constructFullImageLink(guid, size, 1, 0);
    }

    public static Bitmap getBitmapFromImageView(ImageView imgView) {
        // Get the Bitmap from the photo image.
        Bitmap imageBmp = null;
        try {
            imageBmp = ((BitmapDrawable) imgView.getDrawable()).getBitmap();
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, "Failed to get bitmap from photo image: ", e.getMessage());
        }

        return imageBmp;
    }

    public static void loadGroupChatIcon(ImageView imageView, ChatConversation conversation, int size) {
        GroupChatInfo groupChatInfo = conversation.getGroupChatInfo();
        Bitmap displayIcon = GUIConst.DEFAULT_GROUP_CHAT_DISPLAY_ICON;
        if (groupChatInfo != null) {
            final List<ChatParticipant> participants = conversation.getParticipants(false);
            if (participants == null || participants.isEmpty()) {
                groupChatInfo.setDisplayIcon(null);
            } else {
                displayIcon = groupChatInfo.getDisplayIcon();
                if (displayIcon == null) {
                    displayIcon = GUIConst.DEFAULT_GROUP_CHAT_DISPLAY_ICON;

                    //get url array of 4 urls from participant list
                    ArrayList<String> urls = new ArrayList<String>();
                    for (ChatParticipant participant : participants) {
                        if (urls.size() >= MAX_PARTICIPANTS_FOR_GROUP_CHAT_ICON) {
                            break;
                        }
                        //get display picture choice
                        UserProfileDisplayPictureChoiceEnum displayPictureChoice = null;
                        final Profile profile = UserDatastore.getInstance().getProfileWithUsername(participant.getUsername(), false);
                        displayPictureChoice = profile == null ? UserProfileDisplayPictureChoiceEnum.AVATAR : profile.getDisplayPictureType();
                        //get url
                        String avatarUrl = getDisplayPictureUrl(participant.getUsername(), displayPictureChoice, size);
                        urls.add(avatarUrl);
                    }
                    //start task of downloading bitmaps
                    DownloadBitmapsTask task = new DownloadBitmapsTask(imageView, urls, size, conversation);
                    Void[] voids = null;
                    task.execute(voids);
                }
            }
        }

        imageView.setImageBitmap(displayIcon);
    }

    public static void imageRotationAnimationStart(ImageView image) {
        Animation animation = AnimationUtils.loadAnimation(image.getContext(),
                R.anim.loading_icon_rotate);
        image.startAnimation(animation);
    }

    private static Bitmap createGroupChatIcon(Bitmap[] bitmaps, int size) {
        Bitmap source = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(source);

        Rect srcRect = new Rect();
        Bitmap bmp;

        int num = bitmaps.length;
        Rect[] dstRectArray = null;
        if (num >= 4) {
            dstRectArray = new Rect[]{new Rect(0, 0, size / 2, size / 2),
                    new Rect(size / 2, size / 2, size, size),
                    new Rect(size / 2, 0, size, size / 2),
                    new Rect(0, size / 2, size / 2, size)
            };
        } else if (num == 3) {
            dstRectArray = new Rect[]{new Rect(0, size / 4, size / 2, size * 3 / 4),
                    new Rect(size / 2, size / 2, size, size),
                    new Rect(size / 2, 0, size, size / 2)
            };
        } else if (num == 2) {
            dstRectArray = new Rect[]{new Rect(0, size / 4, size / 2, size * 3 / 4),
                    new Rect(size / 2, size / 4, size, size * 3 / 4)
            };
        } else if (num == 1) {
            dstRectArray = new Rect[]{new Rect(0, 0, size, size),
            };
        }

        //draw default background
        canvas.drawColor(Color.WHITE);

        for (int i = 0; i < num; i++) {
            bmp = bitmaps[i];
            if (bmp != null) {
                srcRect.set(0, 0, bmp.getWidth(), bmp.getHeight());
                Rect dstRect = dstRectArray[i];
                canvas.drawBitmap(bmp, srcRect, dstRect, null);
            }
        }

        CircleTransformation transformation = new CircleTransformation();
        Bitmap output = transformation.transform(source);

        return output;
    }

    private static class DownloadBitmapsTask extends AsyncTask<Void, Void, Void> {

        private ImageView imageView;
        private ArrayList<String> urls;
        private int size;
        private Bitmap groupIcon;
        private ChatConversation conversation;

        public DownloadBitmapsTask(ImageView imageView, ArrayList<String> urls, int size, ChatConversation conversation) {
            this.imageView = imageView;
            this.urls = urls;
            this.size = size;
            this.conversation = conversation;
        }

        @Override
        protected void onPreExecute() {
            //when loading
            imageView.setImageResource(R.drawable.groupchat_home);

        }

        protected Void doInBackground(Void... params) {
            int count = urls.size();
            ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();

            for (int i = 0; i < count; i++) {
                try {
                    Bitmap bmp = ImageLoader.getInstance().loadImageSync(urls.get(i));
                    bitmaps.add(bmp);
                } catch (Exception e) {
                    Logger.error.log(LOG_TAG, e);
                }
            }

            Bitmap[] bmpArr = new Bitmap[bitmaps.size()];
            groupIcon = createGroupChatIcon(bitmaps.toArray(bmpArr), size);

            return null;
        }

        protected void onPostExecute(Void v) {
            imageView.setImageBitmap(groupIcon);
            if (conversation.getGroupChatInfo() != null) {
                conversation.getGroupChatInfo().setDisplayIcon(groupIcon);
            }
        }

    }

    /**
     * Tints a given {@link Drawable} with given color by applying a MULTIPLY
     * Color blend mode.
     *
     * @param drawable The {@link Drawable} to be tinted.
     * @param colorRes A color resource.
     * @return The tinted {@link Drawable}
     */
    public static Drawable tintDrawable(Drawable drawable, final int colorRes) {
        drawable.setColorFilter(ApplicationEx.getColor(colorRes), Mode.MULTIPLY);
        return drawable;
    }

    /**
     * Tints a given {@link Drawable} with given color by applying a MULTIPLY
     * Color blend mode.
     *
     * @param drawable The {@link Drawable} to be tinted.
     * @param color    A color.
     * @return The tinted {@link Drawable}
     */
    public static Drawable tintDrawableByColor(Drawable drawable, final int color) {
        drawable.setColorFilter(color, Mode.MULTIPLY);
        return drawable;
    }

    public void clearImageCache() {
        ImageLoader.getInstance().clearMemoryCache();
    }
}
