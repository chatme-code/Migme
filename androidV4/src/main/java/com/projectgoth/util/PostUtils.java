/**
 * Copyright (c) 2013 Project Goth
 *
 * PostUtils.java
 * Created Oct 14, 2013, 5:52:42 PM
 */

package com.projectgoth.util;

import android.text.TextUtils;
import android.widget.ImageView;

import com.migme.commonlib.enums.ImageFileType;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Author;
import com.projectgoth.b.data.Link;
import com.projectgoth.b.data.Photo;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.Privacy;
import com.projectgoth.b.data.Replies;
import com.projectgoth.b.data.Reshares;
import com.projectgoth.b.data.Tag;
import com.projectgoth.b.data.TagEntity;
import com.projectgoth.b.enums.EveryoneOrFollowerAndFriendPrivacyEnum;
import com.projectgoth.b.enums.PostOriginalityEnum;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.ImageUri;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.common.YoutubeUri;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.ImageHandler.ImageLoadListener;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.nemesis.enums.PostPrivacyEnum;
import com.projectgoth.nemesis.enums.ReplyPermissionEnum;
import com.projectgoth.nemesis.model.RequestParams;

/**
 * @author admin
 * 
 */
public class PostUtils {

    /**
     * @param post
     * @return
     */
    public static boolean isMyPost(Post post) {
        String username = getPostAuthorUsername(post);
        if (username != null && username.equals(Session.getInstance().getUsername())) {
            return true;
        }
        return false;
    }

    public static boolean isPostLocked(Post post) {
        if (post != null && post.getReplyPermission() == ReplyPermissionEnum.NONE.value()) {
            return true;
        }
        return false;
    }

    /**
     * @return
     */
    public static String getPostAuthorUsername(Post post) {
        if (post != null && post.getAuthor() != null) {
            return post.getAuthor().getUsername();
        }
        return null;
    }

    public static String getFormattedRepliesCounter(Post post) {
        int repliesCount = getRepliesCounter(post);
        return Tools.formatCounters(repliesCount, Constants.MAX_COUNT_DISPLAY_REPLIES);
    }

    public static String getFormattedResharesCounter(Post post) {
        int resharesCount = getResharesCounter(post);
        return Tools.formatCounters(resharesCount, Constants.MAX_COUNT_DISPLAY_REPLIES);
    }

    public static String getFormattedFootprintCounter(Post post) {
        String counter = Constants.BLANKSTR;
        int footprintsCount = getFootprintCounter(post);

        if (footprintsCount > 0) {
            counter = Tools.formatCounters(footprintsCount, Constants.MAX_COUNT_DISPLAY_REPLIES);
        }

        return counter;
    }

    public static int getRepliesCounter(Post post) {
        int repliesCount = 0;

        if (post != null) {
            Replies replies = post.getReplies();
            if (replies != null) {
                repliesCount = replies.getTotal();
            }
        }

        return repliesCount;
    }

    public static int getResharesCounter(Post post) {
        int resharesCount = 0;

        if (post != null) {
            Reshares reshares = post.getReshares();
            if (reshares != null) {
                resharesCount = reshares.getTotal();
            }
        }

        return resharesCount;
    }

    public static int getFootprintCounter(Post post) {
        int footprintsCount = 0;

        if (post != null) {
            TagEntity emotionalFootprints = post.getTagEntity();
            if (emotionalFootprints != null) {
                footprintsCount = emotionalFootprints.getTotalCount();
            }
        }

        return footprintsCount;
    }

    public static boolean canPostBeReshared(Post post) {
        if (post != null) {
            boolean isPostsOfAuthorProtected = true;
            Author author = post.getAuthor();
            if (author != null) {
                Privacy privacy = author.getPrivacy();
                if (privacy != null && privacy.getFeed() == EveryoneOrFollowerAndFriendPrivacyEnum.EVERYONE) {
                    isPostsOfAuthorProtected = false;
                }
            }

            boolean isPostPublic = false;
            if (post.getPrivacy().intValue() == PostPrivacyEnum.EVERYONE.value()) {
                isPostPublic = true;
            }

            if (isPostPublic && !isPostsOfAuthorProtected) {
                return true;
            }

        }
        return false;
    }

    private static int photoDisplayWidth              = -1;
    private static int rootPostPhotoDisplayWidth      = -1;
    private static int photoOnSppDisplayWidth         = -1;
    private static int rootPostPhotoOnSppDisplayWidth = -1;

    /**
     * update the method once the layout of post changed
     */
    public static int getPhotoDisplayWidth() {
        if (photoDisplayWidth == -1) {
            int screenWidth = Config.getInstance().getScreenWidth();
            int authorPicWidth = ApplicationEx.getDimension(R.dimen.contact_pic_size_normal);
            int normalMargin = ApplicationEx.getDimension(R.dimen.normal_margin);
            int normalPadding = ApplicationEx.getDimension(R.dimen.normal_padding);
            photoDisplayWidth = screenWidth - authorPicWidth - normalMargin - normalPadding * 2;
        }
        return photoDisplayWidth;
    }

    /**
     * update the method once the layout of post changed
     */
    public static int getRootPostPhotoDisplayWidth() {
        if (rootPostPhotoDisplayWidth == -1) {
            int parentPhotoWidth = getPhotoDisplayWidth();
            int normalMargin = ApplicationEx.getDimension(R.dimen.normal_margin);
            int normalPadding = ApplicationEx.getDimension(R.dimen.normal_padding);
            rootPostPhotoDisplayWidth = parentPhotoWidth - normalMargin - normalPadding;
        }
        return rootPostPhotoDisplayWidth;
    }

    /**
     * update the method once the layout of post changed
     */
    public static int getPhotoOnSppDisplayWidth() {
        if (photoOnSppDisplayWidth == -1) {
            int screenWidth = Config.getInstance().getScreenWidth();
            int normalPadding = ApplicationEx.getDimension(R.dimen.normal_padding);
            photoOnSppDisplayWidth = screenWidth - normalPadding * 4;
        }
        return photoOnSppDisplayWidth;
    }

    /**
     * update the method once the layout of post changed
     */
    public static int getRootPostPhotoOnSppDisplayWidth() {
        if (rootPostPhotoOnSppDisplayWidth == -1) {
            int parentPhotoWidth = getPhotoOnSppDisplayWidth();
            int rootPostAuthorPicWidth = ApplicationEx.getDimension(R.dimen.contact_pic_size_small);
            int normalPadding = ApplicationEx.getDimension(R.dimen.normal_padding);
            rootPostPhotoOnSppDisplayWidth = parentPhotoWidth - rootPostAuthorPicWidth - normalPadding * 3;
        }
        return rootPostPhotoOnSppDisplayWidth;
    }
    
    /**
     * Sets the photo thumbnail of a given post on an ImageView.
     * @param view	The ImageView on which the photo is to be set.
     * @param post	The {@link Post} whose photo is to be set on the ImageView.
     * @param imageLoadListener	An {@link ImageLoadListener} that can be used as a callback once the image loads.
     * @return true on success and false if there is no photo set for the Post.
     */
    public static boolean setPhotoThumbnail(final ImageView view, final Post post, 
    		final ImageLoadListener imageLoadListener) {
        final Photo photo = post.getPhoto();
        if (photo != null) {
            final int photoDisplayLimit = Config.getInstance().getScreenWidth();
            final String photoUrl = UIUtils.getPhotoUrl(photo, null, photoDisplayLimit);            
            ImageHandler.getInstance().loadImage(photoUrl, view, imageLoadListener);
            
            return true;
        }
        
        return false;
    }
    
	/**
	 * Sets the thumbnail of a post that contains a youtube link.
	 * 
	 * @param view
	 *            The ImageView on which the thumbnail is to be set.
	 * @param post
	 *            The {@link Post} containing a youtube link.
	 * @param thumbnailQuality
	 *            The quality of the youtube thumbnail to be set.
	 * @param imageLoadListener
	 *            An {@link ImageLoadListener} that can be used as a callback
	 *            once the image loads.
	 * @return true on success and false if the post contains no valid youtube
	 *         url.
	 */
	public static boolean setYoutubeThumbnail(final ImageView view,
			final Post post,
			final YoutubeUri.ThumbnailQuality thumbnailQuality,
			final ImageLoadListener imageLoadListener) {
		Link[] links = post.getLinks();
		if (links != null && links.length > 0) {
			for (final Link link : links) {
				try {
					final String url = link.getUrl();
					final String thumbnailUrl = YoutubeUri.parse(url)
							.getThumbnailUrl(thumbnailQuality);
					view.setTag(R.id.value_url, url);
					ImageHandler.getInstance().loadImage(thumbnailUrl, view,
							imageLoadListener);

					return true;
				} catch (Exception e) {
					// Move on and try the next available link
				}
			}
		}

		return false;
	}

	/**
	 * Sets the thumbnail of a post that contains a generic image link.
	 * 
	 * @param view
	 *            The ImageView on which the thumbnail is to be set.
	 * @param post
	 *            The {@link Post} containing an image link.
	 * @param imageLoadListener
	 *            An {@link ImageLoadListener} that can be used as a callback
	 *            once the image loads.
	 * @return true on success and false if the post contains no valid image
	 *         url.
	 */
	public static boolean setImageThumbnail(final ImageView view,
			final Post post, final ImageLoadListener imageLoadListener) {
		Link[] links = post.getLinks();
		if (links != null && links.length > 0) {
			for (final Link link : links) {
				try {
					final String url = link.getUrl();
					final String thumbnailUrl = ImageUri.parse(url)
							.getThumbnailUrl();
					if (canSupportLinkThumbnailPreview(thumbnailUrl)) {
    					view.setTag(R.id.value_url, url);
    					ImageHandler.getInstance().loadImage(thumbnailUrl, view,
    							imageLoadListener);
					}
					return true;
				} catch (Exception e) {
					// Move on and try the next available link
				}
			}
		}

		return false;
	}
	
	public static boolean canSupportLinkThumbnailPreview(final String url) {
	    return (UIUtils.hasHoneycomb() || !ImageFileType.isGifUrl(url));
	}
	

	/**
	 * Convenience function that attempts to set the photo, youtube thumbnail
	 * and image thumbnail of a {@link Post} in that order of decreasing
	 * priority.
	 * 
	 * @param view
	 *            The ImageView on which the image is to be set.
	 * @param post
	 *            The {@link Post} containing a possible image to be set.
	 * @return true if either a photo, youtube thumbnail or generic image
	 *         thumbnail is successfully set; false otherwise.
	 */
	public static boolean setThumbnail(final ImageView view, final Post post) {
		return setPhotoThumbnail(view, post, null)
				|| setYoutubeThumbnail(view, post,
						YoutubeUri.ThumbnailQuality.MEDIUM, null)
				|| setImageThumbnail(view, post, null);
	}
	
    /**
     * Sets the emotional footprint image of the currently logged in user on a
     * given image.
     * 
     * @param tagEntity
     *            The {@link TagEntity} containing the emotional footprint set
     *            by the user.
     * @param imageView
     *            The {@link ImageView} on which the emotional footprint image
     *            is to be set.
     * @return true if the image was successfully set and false otherwise.
     */
    public static boolean setEmotionalFootprintOnImage(final TagEntity tagEntity, ImageView imageView) {
        boolean didSetEmotionalFootprint = false;
        
        // set the default image first
        if (imageView != null) {
            imageView.setImageResource(R.drawable.ad_emotibot);
        }
        String url = getEmotionalFootprintUrl(tagEntity);
        if (url != null) {
            ImageHandler.getInstance().loadImageFromUrl(imageView, url, false, R.drawable.ad_emotibot);
            didSetEmotionalFootprint = true;
        }
        return didSetEmotionalFootprint;
    }

    public static String getEmotionalFootprintUrl(final TagEntity tagEntity) {
        if (tagEntity != null) {
            final Integer myTagId = tagEntity.getRequestingUserTagId();
            if (myTagId != null && myTagId.intValue() != 0) {
                final Tag tag = PostsDatastore.getInstance().getEmotionalFootprintTag(tagEntity.getCriteriaId(),
                        myTagId);
                if (tag != null && !TextUtils.isEmpty(tag.getImage())) {
                    return tag.getImage();
                }
            }
        }

        return null;
    }

    private static RequestParams.FormData prepareFormData(final String extension, final String mimeType, byte[] photoData) {
        RequestParams.FormData data = new RequestParams.FormData();
        if (photoData != null) {
            data.name = "media";
            data.filename = System.currentTimeMillis() + "." + extension;
            data.mimeType = mimeType;
            data.data = photoData;
        }

        return data;
    }
    
    public static RequestParams.FormData preparePhotoFormData(ImageFileType imageFileType, byte[] photoData) {
        return prepareFormData(imageFileType.getExtension(), imageFileType.getMimeType(), photoData);
    }

    /**
     * Get the formatted post body with prefix based on post originality
     *
     * @param post
     * @return
     */
    public static String getFormattedPostBody(Post post) {
        PostOriginalityEnum originality = post.getOriginality();
        String bodyToDisplay = Constants.BLANKSTR;
        if (originality == PostOriginalityEnum.RESHARE &&
                TextUtils.isEmpty(post.getBody())) {
            bodyToDisplay = Constants.BLANKSTR;
        } else {
            bodyToDisplay = post.getFormattedBody();
        }

        // Replace all newlines with <br>
        final String processedText = bodyToDisplay.replace("\\n", "<br>");

        return processedText;
    }

    public static String getPostBodyPrefix(PostOriginalityEnum originality, boolean isBodyEmpty) {
        String bodyPrefix = null;
        if (originality == PostOriginalityEnum.REPLY) {
            bodyPrefix = Tools.formatPostType(I18n.tr("Reply:")) + Constants.SPACESTR;
        } else if (originality == PostOriginalityEnum.RESHARE && isBodyEmpty) {
            bodyPrefix = Tools.formatPostType(I18n.tr("Reposted"));
        }

        return bodyPrefix;
    }

    /**
     * this is workaround for the serve issue that the offset of a post can only be an integer index
     * which can change on server side once there's new posts created in the category, but clients doesn't know.
     *
     * when I fetch index 15th ~ 29th posts of home feeds, they could be already be 18th ~ 32 th, then we got
     * 3 duplicated posts and 12 new posts instead of 15 new posts. then the client increases the offset by 3
     * trying to be consistent with server
     *
     *
     * @param offset
     * @param limit
     */

    static public int adjustPostsLoadingMoreOffset(int offset, int limit, boolean isPostCategoryEnded,
                                            int clientPostsNum) {
        int offsetDelta = 0;

        if (offset == 0) {
            //reset
            offsetDelta = 0;
        } else {
            if(!isPostCategoryEnded) {
                //this is load more
                int serverPostsNum = offset + limit;
                if (serverPostsNum != clientPostsNum) {
                    offsetDelta = serverPostsNum - clientPostsNum;
                    Logger.debug.log("onHttpResponseReceived", "offset:" + offset + " limit:" + limit + " offsetDelta:" + offsetDelta);
                }

            }
        }

        return offsetDelta;
    }

}
