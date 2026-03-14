/**
 * Copyright (c) 2013 Project Goth
 *
 * PostsDatastore.java
 * Created Jun 6, 2013, 12:03:13 PM
 */

package com.projectgoth.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.migme.commonlib.enums.ImageFileType;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Author;
import com.projectgoth.b.data.HotTopicsResult;
import com.projectgoth.b.data.Location;
import com.projectgoth.b.data.Photo;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.PostSearchResult;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.Relationship;
import com.projectgoth.b.data.Tag;
import com.projectgoth.b.data.mime.MimeType;
import com.projectgoth.b.enums.PostApplicationEnum;
import com.projectgoth.b.enums.PostOriginalityEnum;
import com.projectgoth.b.enums.PostTypeEnum;
import com.projectgoth.b.enums.TaggingCriteriaTypeEnum;
import com.projectgoth.common.Logger;
import com.projectgoth.common.SpannableBuilder;
import com.projectgoth.common.Tools;
import com.projectgoth.dao.PostsDAO;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.model.PostCategory;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.enums.PostCategoryTypeEnum;
import com.projectgoth.nemesis.enums.PostPrivacyEnum;
import com.projectgoth.nemesis.enums.ReplyPermissionEnum;
import com.projectgoth.nemesis.enums.RequestTypeEnum;
import com.projectgoth.nemesis.listeners.CreatePostListener;
import com.projectgoth.nemesis.listeners.GetPostsListener;
import com.projectgoth.nemesis.listeners.GetTagOptionsListener;
import com.projectgoth.nemesis.listeners.SearchPostListener;
import com.projectgoth.nemesis.listeners.SimpleResponseListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.nemesis.model.MigRequest;
import com.projectgoth.nemesis.model.MigResponse;
import com.projectgoth.nemesis.model.RequestParams.FormData;
import com.projectgoth.nemesis.model.User;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.PostUtils;

/**
 * The posts data store manages caching, validation and persistence of post
 * related data. This includes data for feeds, own posts, mentions, watched
 * posts, replies, reshares and post tags (ie; emotional footprints).
 * PostsDatastore.java
 * 
 * @author angelorohit
 */

public class PostsDatastore extends BaseDatastore {

    private static final String                    LOG_TAG                                = AndroidLogger
                                                                                                  .makeLogTag(PostsDatastore.class);
    // A lock that is obtained when working with any of the caches.
    private static final Object                    CACHE_LOCK                             = new Object();

    private static final int                       POST_LISTCACHE_EXPIRY                  = 1 * 60 * 1000;

    // The maximum size of the post cache.
    private final static int                       MAX_POST_CACHE_SIZE                    = 300;

    // The maximum size of the post category cache.
    private final static int                       MAX_POSTCATEGORY_CACHE_SIZE            = 20;

    // The maximum size of the tag options cache.
    private final static int                       MAX_TAGOPTIONS_CACHE_SIZE              = 20;

    // The max number of tags that are fetched for a post fetch related request.
    private final static int                       DEFAULT_MAX_TAGS                       = 10;
    // The default image size requested when fetching emotional footprint tag
    // options.
    private final static int                       DEFAULT_EMOTIONAL_FOOTPRINT_IMAGE_SIZE = 48;

    private final static int                       DEFAULT_POST_LIMIT                     = 15;

    // A DAO for saving posts to persistent storage.
    private PostsDAO                               mPostsDAO                              = null;

    // Thread adn handler used for persisting posts
    HandlerThread                                  mPersistPostHandlerThread;
    Handler                                        mPersistPostHandler;

    // Clean the posts which are older than is expiry time (3 months)
    private static final long                      POST_EXPIRY                            = (long) 90 * 24 * 60 * 60 * 1000;

    /**
     * A cache of all the posts - this includes feeds, own posts, mentions,
     * watched posts, replies and reshares. The key for this cache is the id of
     * each post.
     */
    private DataCache<VersionedData<Post>>         mPostCache;

    /**
     * A cache of all posts grouped by category. The different categories are
     * for feeds, mentions, own posts, watched posts, replies and reshares. The
     * key of this cache is PostCategoryTypeEnum+UserOrPostId.
     */
    private DataCache<VersionedData<PostCategory>> mPostCategoryCache;

    /**
     * A cache of all the tag options fetched from the server. The key is the
     * {@link TaggingCriteriaTypeEnum}.
     */
    private DataCache<List<Tag>>                   mTagOptionsCache;

    /**
     * Configuration for the expiry time of the hot topic cache
     */
    private static final int                       HOT_TOPIC_EXPIRY                       = 5 * 60 * 1000;

    private int                                    lastNoOfTopicsFetched;

    private long                                   lastHotTopicFetchTimestamp;

    private HotTopicsResult                        hotTopicResultCache;

    // @formatter:off
    private GetPostsListener getPostsListener = new GetPostsListener() {
        
        @Override
        public void onHomeFeedReceived(String userId, int limit, int offset, List<Post> feedPosts) {
            Logger.debug.flog(LOG_TAG, "onHomeFeedsReceived: userId: %s limit: %d offset: %d feedposts: %s", 
                    userId, limit, offset, feedPosts);
            addPostsToCategoryCache(PostCategoryTypeEnum.FEEDS, userId, feedPosts, limit, offset, true, false, true);
            BroadcastHandler.Post.sendFetchHomeFeedsCompleted(offset, limit);
        }
        
        @Override
        public void onUserPostsReceived(String userId, int limit, int offset, List<Post> userPosts) {
            Logger.debug.flog(LOG_TAG, "onUserPostsReceived: userId: %s limit: %d offset: %d userposts: %s", 
                    userId, limit, offset, userPosts);
            addPostsToCategoryCache(PostCategoryTypeEnum.USERPOSTS, userId, userPosts, limit, offset, true, false, true);
            BroadcastHandler.Post.sendFetchUserPostsCompleted(userId);
        }

        @Override
        public void onWatchListReceived(String userId, int limit, int offset, List<Post> watchedPosts) {
            Logger.debug.flog(LOG_TAG, "onWatchListReceived: userId: %s limit: %d offset: %d watchedposts: %s",
                    userId, limit, offset, watchedPosts);
            addPostsToCategoryCache(PostCategoryTypeEnum.WATCHEDPOSTS, userId, watchedPosts, limit, offset, true, false, true);
            BroadcastHandler.Post.sendFetchWatchListCompleted();
        }
        
        @Override
        public void onMentionsReceived(String userId, int limit, int offset, List<Post> mentionPosts, int unreadCount) {
            Logger.debug.flog(LOG_TAG, "onMentionsReceived: userId: %s limit: %d offset: %d watchedposts: %s", 
                    userId, limit, offset, mentionPosts);
            addPostsToCategoryCache(PostCategoryTypeEnum.MENTIONS, userId, mentionPosts, limit, offset, true, false, true);
            BroadcastHandler.Post.sendFetchMentionsCompleted();
        }        
        
        @Override
        public void onRepliesReceived(String postId, int limit, int offset, List<Post> replyPosts) {
            Logger.debug.flog(LOG_TAG, "onRepliesReceived: postId: %s limit: %d offset: %d replyPosts: %s",
                    postId, limit, offset, replyPosts);
            addPostsToCategoryCache(PostCategoryTypeEnum.REPLIES, postId, replyPosts, limit, offset, true, false, true);
            BroadcastHandler.Post.sendFetchRepliesCompleted(postId, offset, limit);
        }

        @Override
        public void onResharesReceived(String postId, int limit, int offset, List<Post> resharePosts) {
            Logger.debug.flog(LOG_TAG, "onResharesReceived: postId: %s limit: %d offset: %d replyPosts: %s",
                    postId, limit, offset, resharePosts);
            addPostsToCategoryCache(PostCategoryTypeEnum.RESHARES, postId, resharePosts, limit, offset, true, false, true);
            BroadcastHandler.Post.sendFetchResharesCompleted(postId, offset, limit);
        }
        
        @Override
        public void onGroupFeedsReceived(String groupId, int limit, int offset, List<Post> groupFeedPosts) {
            Logger.debug.flog(LOG_TAG, "onGroupFeedsReceived: groupId: %s limit: %d offset: %d groupFeedPosts: %s",
                    groupId, limit, offset, groupFeedPosts);
            addPostsToCategoryCache(PostCategoryTypeEnum.GROUP_FEEDS, groupId, groupFeedPosts, limit, offset, true, false, true);
            BroadcastHandler.Post.sendFetchGroupFeedsCompleted(groupId, offset, limit);
        }
        
        @Override
        public void onSinglePostReceived(String postId, Post post) {
            List<Post> posts = new ArrayList<>();
            posts.add(post);
            addPosts(posts, true, null);
            Logger.debug.flog(LOG_TAG, "onSinglePostReceived: postId: %s post: %s", postId, post);
            BroadcastHandler.Post.sendSinglePostReceived(postId);
        }
        
        @Override
        public void onError(MigError error) {
            super.onError(error);
            
            PostCategoryTypeEnum postCategoryKey = PostCategoryTypeEnum.UNKNOWN; 
            if (error.getRequest() != null) {
                final RequestTypeEnum requestType = error.getRequest().getRequestType();
                postCategoryKey = PostCategoryTypeEnum.fromRequestType(requestType);
            }
            
            BroadcastHandler.Post.sendFetchForCategoryError(error, postCategoryKey);
        }
    };

    private SearchPostListener searchListener = new SearchPostListener() {
        
        @Override
        public void onSearchPostReceived(PostSearchResult result, String queryStr, int hitsPerPage, int page) {
            if (result != null) {
                Logger.debug.log(LOG_TAG, "onSearchPostReceived result: ", new Gson().toJson(result), 
                        " queryStr: ", queryStr, " hitsPerPage: ", hitsPerPage, " page: ", page);
                Post[] postArr = result.getResult();
                if (postArr != null) {
                    List<Post> postList = Arrays.asList(postArr);
                    int offset = Tools.convertPageToOffset(page, hitsPerPage);
                    addPostsToCategoryCache(PostCategoryTypeEnum.SEARCH_POSTS, queryStr, postList, hitsPerPage, offset, true, false, true);
                    BroadcastHandler.Post.sendFetchForSearchCompleted(queryStr, hitsPerPage, page, result.getTotalHits());
                }
            }
        }
        
        @Override
        public void onSearchPostForHotTopicReceived(PostSearchResult result, String queryStr, int hitsPerPage, int page) {
            if (result != null) {
                Logger.debug.log(LOG_TAG, "onSearchPostReceived result: ", new Gson().toJson(result),
                        " queryStr: ", queryStr, " hitsPerPage: ", hitsPerPage, " page: ", page);
                Post[] postArr = result.getResult();
                if (postArr != null) {
                    List<Post> postList = Arrays.asList(postArr);
                    int offset = Tools.convertPageToOffset(page, hitsPerPage);
                    addPostsToCategoryCache(PostCategoryTypeEnum.SEARCH_HOT_TOPIC_POSTS, queryStr, postList, hitsPerPage, offset, true, false, true);
                    BroadcastHandler.Post.sendFetchForHotTopicCompleted(queryStr, hitsPerPage, page, result.getTotalHits());
                }
            }
        }
        
        @Override
        public void onSearchError(MigError error, String queryStr, int hitsPerPage, int page) {
            Logger.debug.log(LOG_TAG, "onSearchError: ", queryStr, " hitsPerPage: ", hitsPerPage, " page: ", page);
            BroadcastHandler.Post.sendFetchForSearchError(error, queryStr, hitsPerPage, page);
        }
        
        @Override
        public void onGetHotTopicReceived(HotTopicsResult result, int hitsPerPage) {
            if (result != null) {
                Logger.debug.log(LOG_TAG, "onGetHotTopicReceived result: ", new Gson().toJson(result), " hitsPerPage: ", hitsPerPage);
                hotTopicResultCache = result;
                lastHotTopicFetchTimestamp = System.currentTimeMillis();
                lastNoOfTopicsFetched = hitsPerPage;
                BroadcastHandler.HotTopic.sendFetchAllCompleted();
            }
        }
        
        @Override
        public void onGetHotTopicError(MigError error, int hitsPerPage) {
            Logger.debug.log(LOG_TAG, "onGetHotTopicError: ", hitsPerPage);
            lastHotTopicFetchTimestamp = 0;
            BroadcastHandler.HotTopic.sendFetchAllError(error);
        }

    };
    
    private CreatePostListener createNewPostListener = new CreatePostListener() {
        @Override
        public void onCreatePostResponse(String id, String draftId) {
            markOwnPostsListAsAltered();
            if(draftId != null) {
                PostsDatastore.getInstance().removePostWithId(draftId);
                PostsDatastore.getInstance().removePostDraftData(draftId);
            }
            BroadcastHandler.Post.sendSent(PostOriginalityEnum.ORIGINAL);
        }
        @Override
        public void onError(MigError error) {
            super.onError(error);
            if (error != null) {
                MigRequest request = error.getRequest();
                if (request != null) {
                    if (error.getMigboError() != null &&
                            error.getMigboError().getErrorNumber() == MigError.Type.DUPLICATE_POST.value()) {
                        // migbo error code equals 1000 means duplicate post
                        // delete the duplicate post draft in local cache and local post database
                        String draftId = request.getStringTag("temp_id");
                        PostsDatastore.getInstance().removePostWithId(draftId);
                        PostsDatastore.getInstance().removePostDraftData(draftId);
                    }
                    BroadcastHandler.Post.sendSendError(error, PostOriginalityEnum.ORIGINAL);
                }
            }
        }
    };
    
    private CreatePostListener createReplyPostListener = new CreatePostListener() {
        @Override
        public void onCreatePostResponse(String id, String draftId) {
            BroadcastHandler.Post.sendSent(PostOriginalityEnum.REPLY);
        }
        @Override
        public void onError(MigError error) {
            super.onError(error);
            MigRequest request = error.getRequest();
            if (request != null) {
                BroadcastHandler.Post.sendSendError(error, PostOriginalityEnum.REPLY);
            }
        }
    };
    
    private CreatePostListener createResharePostListener = new CreatePostListener() {
        @Override
        public void onCreatePostResponse(String id, String draftId) {
            BroadcastHandler.Post.sendSent(PostOriginalityEnum.RESHARE);
        }
        @Override
        public void onError(MigError error) {
            super.onError(error);
            MigRequest request = error.getRequest();
            if (request != null) {
                BroadcastHandler.Post.sendSendError(error, PostOriginalityEnum.RESHARE);
            }
        }
    };
    
    //@formatter:on

    /**
     * Constructor
     * 
     */
    private PostsDatastore() {
        // Call initData(): create caches
        super();

        // Create the post database tables
        final Context appCtx = ApplicationEx.getContext();
        if (appCtx != null) {
            mPostsDAO = new PostsDAO(appCtx);
        }

        cleanPostDatabaseSchedule();
    }

    /**
     * Clean posts in database which are too old
     */
    private void cleanPostDatabaseSchedule() {
        mPostsDAO.deleteOldPosts(POST_EXPIRY);
    }

    private static class PostsDatastoreHolder {
        static final PostsDatastore sINSTANCE = new PostsDatastore();
    }
	
	/**
	 * A singleton point of access to an instance of this class.
	 * @return An instance of this class.
	 */
	public static PostsDatastore getInstance() {
		return PostsDatastoreHolder.sINSTANCE;
	}
	
	@Override
	protected void initData() {
	    try {
            mPostCache = new DataCache<VersionedData<Post>>(MAX_POST_CACHE_SIZE);
            mPostCategoryCache = new DataCache<VersionedData<PostCategory>>(MAX_POSTCATEGORY_CACHE_SIZE);
            mTagOptionsCache = new DataCache<List<Tag>>(MAX_TAGOPTIONS_CACHE_SIZE);

            mPersistPostHandlerThread = new HandlerThread("persist post thread");
            mPersistPostHandlerThread.start();
            mPersistPostHandler = new Handler(mPersistPostHandlerThread.getLooper());

        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
	}
	
	@Override
    public void clearData() {
	    if (mPostsDAO != null) {
	        mPostsDAO.clearTables();
	    }

        initData();
	}
	
	/**
	 * Removes a Post matching the given id from the post conversation category.
	 * @param userId   The id of the user whose post conversations are to be altered.
	 * @param postId   The id of the post to be removed from the post conversations.
	 * @param postCategoryType The {@link PostCategoryTypeEnum} to which the post to be removed belongs.
	 * @return true if a matching post was successfully found and removed, false otherwise.
	 */
	public boolean removePostWithIdFromPostCategory(final String userId, final String postId, final PostCategoryTypeEnum postCategoryType) {
	    synchronized (CACHE_LOCK) {
	        boolean didRemove = false;
	        
	        if (!TextUtils.isEmpty(userId)) {
    	        final String key = PostCategory.getKey(postCategoryType, userId);	        
    	        VersionedData<PostCategory> versionedPostCategory = mPostCategoryCache.getData(key);	        
    	        if (versionedPostCategory != null) {
    	            PostCategory postCategory = versionedPostCategory.getData();
    	            if (postCategory != null) {
        	            List<String> postIdList = postCategory.getPostIds();    	            
        	            if (postIdList != null && !postIdList.isEmpty()) {
        	               VersionedData<Post> versionedPostToRemove = null;
        	               for (String id : postIdList) {
        	                   if (!TextUtils.isEmpty(id) && id.equals(postId)) {
        	                       versionedPostToRemove = getVersionedDataPost(postId);     
        	                       break;
        	                   }
        	               }
        	               
        	               if (versionedPostToRemove != null && versionedPostToRemove.getData() != null) {
        	                   Post postToRemove = versionedPostToRemove.getData();
                               postIdList.remove(postToRemove.getId());
                               postCategory.resetPostIds(postIdList);

                               versionedPostCategory.setData(postCategory);
                                                              
                               mPostCategoryCache.cacheData(key, versionedPostCategory);
                               didRemove = true;
                           }
        	            }    	                	            
    	            }
    	        }
	        }
	        
	        return didRemove;
        }
	}
	
	public void markPostWithIdAsWatchedOrUnwatched(final String postId, final boolean didWatch) {
	    synchronized (CACHE_LOCK) {
            VersionedData<Post> versionedDataPost = getVersionedDataPost(postId);
            
            if (versionedDataPost != null && versionedDataPost.getData() != null) {
                final Post post = versionedDataPost.getData();
                post.setIsWatching(didWatch);
                
                final String userId = Session.getInstance().getUserId();
                if (!TextUtils.isEmpty(userId)) {
                    if (!didWatch) {
                        // Immediately remove from the watched posts category, if a post was unwatched.
                        removePostWithIdFromPostCategory(userId, postId, PostCategoryTypeEnum.WATCHEDPOSTS);
                    }
                    
                    final String watchedPostsKey = PostCategory.getKey(PostCategoryTypeEnum.WATCHEDPOSTS, userId);
                    final VersionedData<PostCategory> versionedPostCategory = mPostCategoryCache.getData(watchedPostsKey); 
                    if (versionedPostCategory != null) {
                        if (didWatch) {
                            // Add the post to the post category if it was just watched.
                            List<Post> postListToAdd = new ArrayList<Post>();
                            postListToAdd.add(post);
                            versionedPostCategory.getData().addPosts(0, postListToAdd);
                        }
                        
                        savePostsForCategoryToPersistentStorage(watchedPostsKey, versionedPostCategory);
                    }
                }                
            }
        }
	}
	
	public void markPostWithIdAsLockedOrUnlocked(final String postId, final boolean didLock) {
	    synchronized (CACHE_LOCK) {
            VersionedData<Post> versionedDataPost = getVersionedDataPost(postId);
            
            if (versionedDataPost != null) {
                final Post post = versionedDataPost.getData();
                if (post != null) {
                    if (didLock) {
                        post.setReplyPermission(ReplyPermissionEnum.NONE.value());
                    } else {
                        post.setReplyPermission(ReplyPermissionEnum.EVERYONE.value());
                    }
                    
                    versionedDataPost.setData(post);
                }
            }
	    }
	}
	
	public void markPostWithIdAsTagged(final String postId, final int tagId) {
	    // Update the tagId content of the post.
	    VersionedData<Post> versionedDataPost = getVersionedDataPost(postId);
	    if (versionedDataPost != null) {
	        Post post = versionedDataPost.getData();
            if (post.updateWithTagIdForUsername(tagId, Session.getInstance().getUsername(), TaggingCriteriaTypeEnum.EMOTIONAL_FOOTPRINT)) {
	            versionedDataPost.setData(post);
	        }
	    }
	}

    public void markPostWithIdAsAlteredOrRemoved(final String postId, final boolean shouldMarkAsRemoved,
            final boolean shouldPersist) {
        VersionedData<Post> versionedDataPost = getVersionedDataPost(postId);
        markPostAsAlteredOrRemoved(versionedDataPost, shouldMarkAsRemoved, shouldPersist);
    }
	
	/**
	 * Marks the relevant caches for a VersionedData<Post> as altered or removed.
	 * @param versionedDataPost    The versioned data post to be marked as altered.
	 */
	private void markPostAsAlteredOrRemoved(final VersionedData<Post> versionedDataPost, 
	        final boolean shouldMarkAsRemoved, 
	        final boolean shouldPersist) {
	    synchronized (CACHE_LOCK) {
	        if (versionedDataPost != null) {
	            if (shouldMarkAsRemoved) {
	                versionedDataPost.setIsRemoved(true);
	            } else {
	                versionedDataPost.setIsAltered(true);
	            }

	            if (shouldPersist) {
	                if (!savePostToPersistentStorage(versionedDataPost)) {
	                    Logger.error.log(LOG_TAG, "Failed to persist with savePostToPersistentStorage");
	                }
	            }
	        }
        }	    
	}
	
	private void markOwnPostsListAsAltered() {
	    final String userId = Session.getInstance().getUserId();
	    markPostCategoryCacheWithKeyAsAltered(PostCategoryTypeEnum.USERPOSTS, userId);
	}	
	
	private void markPostCategoryCacheWithKeyAsAltered(final PostCategoryTypeEnum type, final String queryStr) {
	    synchronized (CACHE_LOCK) {
	        VersionedData<PostCategory> data = mPostCategoryCache.getData(PostCategory.getKey(type, queryStr));
	        if (data != null) {
	            data.setIsAltered(true);
	        }
        }
	}
	
	/**
	 * Returns all the cached tag options.
	 * If the cache is empty or expired then a request is sent to fetch the tag options from server.
	 * @param taggingCriteriaType  The type of tagging criteria to be retrieved.
	 * @return A List containing the Tag options for the given criteria and null if the list is empty.
	 */
	public List<Tag> getTagOptions(final TaggingCriteriaTypeEnum taggingCriteriaType) {
	    synchronized (CACHE_LOCK) {
	        if (mTagOptionsCache != null && taggingCriteriaType != null) {
    	        final String key = String.valueOf(taggingCriteriaType.value());
    	        List<Tag> resultList = mTagOptionsCache.getData(key);
    	        
    	        if (resultList == null || mTagOptionsCache.isExpired(key)) {
    	            requestTagOptions(taggingCriteriaType);
    	        }
    	        
    	        return resultList;
	        }
	        
	        return null;
        }	    	    	    
	}
	
    /**
     * Get the tag based on the tagging criteria
     * 
     * @param taggingCriteriaType
     * @param id
     * @return
     */
    public Tag getEmotionalFootprintTag(final Integer taggingCriteriaType, Integer id) {
        if (taggingCriteriaType != null) {
            return getEmotionalFootprintTag(TaggingCriteriaTypeEnum.fromValue(taggingCriteriaType), id);
        }
        return null;
    }

    /**
     * Get the tag based on the tagging criteria
     * 
     * @param taggingCriteriaType
     * @param id
     * @return
     */
    public Tag getEmotionalFootprintTag(final TaggingCriteriaTypeEnum taggingCriteriaType, Integer id) {
        List<Tag> tagOptions = getTagOptions(taggingCriteriaType);
        if (tagOptions != null) {
            for (Tag tag : tagOptions) {
                Integer tagId = tag.getId();
                if (tagId != null && tagId.equals(id)) {
                    return tag;
                }
            }
        }
        return null;
    }


	public Post getPost(final String postId, final boolean shouldForceFetch) {
	    return getPost(postId, false, shouldForceFetch);
	}

    /**
     * Gets a post with the given id from the post cache.
     *
     * @param postId
     *            The id of the post to be retrieved.
     * @param justLoadFromCache
     *            in some case we don't fetch the single post, eg. getting the post before fetch a category of posts
     * @param shouldForceFetch
     *            Whether the data should be force fetched from the server.
     * @return The associated post on success and null if the postId could not
     *         be found in the cache or was expired.
     */
    public Post getPost(final String postId, final boolean justLoadFromCache, final boolean shouldForceFetch) {
        synchronized (CACHE_LOCK) {
            if (mPostCache != null && postId != null) {
                VersionedData<Post> versionedDataPost = mPostCache.getData(postId);

                if (versionedDataPost == null || (versionedDataPost != null && !versionedDataPost.isRemoved())) {
                    if (versionedDataPost == null || versionedDataPost.getData() == null) {
                        versionedDataPost = loadPostWithIdFromPersistentStorage(postId);
                    }

                    if (!justLoadFromCache) {
                        if ((versionedDataPost == null ||
                                versionedDataPost.getData() == null ||
                                versionedDataPost.isAltered() ||
                                mPostCache.isExpired(postId) ||
                                shouldForceFetch) && !getPostDraftData().contains(postId)) {

                            requestGetSinglePost(postId);
                        }
                    }
                }

                if (versionedDataPost != null) {
                    return versionedDataPost.getData();
                }
            }

            return null;
        }
    }
	
	/**
	 * Returns a VersionedData post from the post cache matching the given id.
	 * @param postId   The id of the VersionedData Post to be retrieved.
	 * @return         The associated VersionedData Post on success and null if the 
	 *                 postId could not be found in cache or persistent storage or
	 *                 was marked as removed.
	 */
	private VersionedData<Post> getVersionedDataPost(final String postId) {
	    synchronized (CACHE_LOCK) {
            if (mPostCache != null) {
                VersionedData<Post> versionedDataPost = mPostCache.getData(postId);
                                                        
                // Look in persistent storage for the versioned data post. 
                if (versionedDataPost == null) {
                    versionedDataPost = loadPostWithIdFromPersistentStorage(postId);                                 
                } 
                
                return versionedDataPost;
            }  
            
            return null;
        }
	}

    /**
     * Gets a list of posts for a given category key from cache. NOTE: This
     * method is to be used internally only.
     * 
     * @param type
     *            The {@link PostCategoryTypeEnum} corresponding to the category of posts to be
     *            retrieved.
     * @param queryStr
     *            The id of the user whose categorized posts are to be retrieved
     *            or the id of the post whose replies / reshares are to be
     *            retrieved or a search query string
     * @param offset
     *            The offset from which the posts should be retrieved.
     * @param limit
     *            The limit of posts to be retrieved.
     * @param shouldForceFetch
     *            Whether the data should be force fetched from the server.
     * @return A list containing all the posts for the given category.
     */
    private List<Post> getPostsListForCategory(final PostCategoryTypeEnum type, String queryStr,
            final int offset, final int limit, final boolean shouldForceFetch, final boolean justLoadFromCache) {
        synchronized (CACHE_LOCK) {
            final List<Post> resultList = new ArrayList<Post>();
            //for [non-login] user, use fake userId
            if (ApplicationEx.getInstance().getPreviewStatus() && !Session.getInstance().isLoggedIn() && !Session.getInstance().hasLoggedIn()) {
                queryStr = "0";
            }

            if (!TextUtils.isEmpty(queryStr)) {

                final String key = PostCategory.getKey(type, queryStr);

                boolean shouldFetchFromServer = false;

                PostCategory category = null;
                VersionedData<PostCategory> data = mPostCategoryCache.getData(key);
                
                if (data != null) {
                    category = data.getData();                    
                    shouldFetchFromServer = ((System.currentTimeMillis() > data.getLastUpdateTimestamp()
                            + POST_LISTCACHE_EXPIRY) || data.isAltered());
                }

                if (category == null) {
                    
                    // Step1: No data in cache? Look in db.
                    // Step2: If found data in db, then pull data from db into cache
                    // Step3: Begin recompute of cache
                    // Step4: If any of the posts were altered or removed in the
                    // cached data, set a flag locally
                    // Step5: End recompute of cache
                    // Step6: If local flag is set then fetch from server.

                    final VersionedData<List<String>> persistedData = loadPostIdsForCategoryFromPersistentStorage(key);
                    List<String> postIdList = null;
                    if (persistedData != null) {
                        shouldFetchFromServer = (shouldFetchFromServer
                                || (System.currentTimeMillis() > persistedData.getLastUpdateTimestamp()
                                        + POST_LISTCACHE_EXPIRY) || persistedData.isAltered());
                        postIdList = persistedData.getData();
                    }

                    // Begin recompute of cache.
                    if (postIdList != null && !postIdList.isEmpty()) {
                        List<Post> postList = new ArrayList<Post>();
                        for (String postId : postIdList) {
                            final VersionedData<Post> versionedDataPost = getVersionedDataPost(postId);
                            if (!shouldFetchFromServer) {
                                shouldFetchFromServer = (versionedDataPost == null 
                                        || versionedDataPost.isAltered() 
                                        || versionedDataPost.isRemoved());
                            }
                            if (versionedDataPost != null && !versionedDataPost.isRemoved()) {
                                postList.add(versionedDataPost.getData());
                            }
                        }

                        addPostsToCategoryCache(type, queryStr, postList, limit, offset, false, false, false);
                        
                        data = mPostCategoryCache.getData(key);
                        category = data.getData();
                    } else {
                        shouldFetchFromServer = true;
                    }
                }
                
                if (category != null) {
                    final List<String> postIdList = category.getPostIds(limit, offset);
                    if (postIdList != null) {
                        // See if there is a corresponding post with the same id in the post cache.
                        for (String postId : postIdList) {
                            if (!TextUtils.isEmpty(postId)) {
                                //shouldn't fetch single post before fetching the category of posts here
                                //AD-962
                                final Post post = getPost(postId, true, false);
                                if (post != null) {
                                    resultList.add(post);
                                }
                            }
                        }
                    }
                    
                    if (!shouldFetchFromServer) {
                        int length = resultList.size();
                        if (length < limit && !category.isEnd()) {
                            shouldFetchFromServer = true;
                        }
                    }
                }
                
                if ((shouldFetchFromServer || shouldForceFetch) && !justLoadFromCache) {
                    requestCategorizedPosts(queryStr, offset, limit, type);
                }
                
                Logger.debug.log(LOG_TAG, "Getting posts list for category: ", type.name(),
                        ". Did fetch from server: ", String.valueOf(shouldFetchFromServer && !justLoadFromCache));
            }                        

            return resultList;
        }
    }

    public List<Post> getFeedsPostsList(final int offset, final int limit, final boolean shouldForceFetch,
                                        final boolean justLoadFromCache) {
        final String userId = Session.getInstance().getUserId();
        return getPostsListForCategory(PostCategoryTypeEnum.FEEDS, userId, offset, limit, shouldForceFetch, justLoadFromCache);
    }

    public List<Post> getUserPostsList(final String userId, final int offset, final int limit, final boolean shouldForceFetch,
                                       final boolean justLoadFromCache) {
        return getPostsListForCategory(PostCategoryTypeEnum.USERPOSTS, userId, offset, limit, shouldForceFetch, justLoadFromCache);
    }

    public List<Post> getMentionsPostsList(final int offset, final int limit, final boolean shouldForceFetch,
                                           final boolean justLoadFromCache) {
        final String userId = Session.getInstance().getUserId();
        return getPostsListForCategory(PostCategoryTypeEnum.MENTIONS, userId, offset, limit, shouldForceFetch, justLoadFromCache);
    }

    public List<Post> getWatchedPostsList(final int offset, final int limit, final boolean shouldForceFetch,
                                          final boolean justLoadFromCache) {
        final String userId = Session.getInstance().getUserId();
        return getPostsListForCategory(PostCategoryTypeEnum.WATCHEDPOSTS, userId, offset, limit, shouldForceFetch, justLoadFromCache);
    }

    public List<Post> getRepliesForPostWithId(final String postId, final int offset, final int limit, final boolean shouldForceFetch,
                                              final boolean justLoadFromCache) {
        return getPostsListForCategory(PostCategoryTypeEnum.REPLIES, postId, offset, limit, shouldForceFetch, justLoadFromCache);
    }

    public List<Post> getResharesForPostWithId(final String postId, final int offset, final int limit, final boolean shouldForceFetch,
                                               final boolean justLoadFromCache) {
        return getPostsListForCategory(PostCategoryTypeEnum.RESHARES, postId, offset, limit, shouldForceFetch, justLoadFromCache);
    }
    
    public List<Post> getGroupFeedsPostsList(final String groupId, final int offset, final int limit, final boolean shouldForceFetch,
                                             final boolean justLoadFromCache) {
        return getPostsListForCategory(PostCategoryTypeEnum.GROUP_FEEDS, groupId, offset, limit, shouldForceFetch, justLoadFromCache);
    }

    /**
     *  get all cached posts of the category
     * @param type
     * @param queryStr
     * @return
     */
    public List<Post> getPostsListForCategory(final PostCategoryTypeEnum type, final String queryStr) {
        synchronized (CACHE_LOCK) {
            final List<Post> resultList = new ArrayList<Post>();
            if (!TextUtils.isEmpty(queryStr)) {

                final String key = PostCategory.getKey(type, queryStr);

                PostCategory category = null;
                VersionedData<PostCategory> data = mPostCategoryCache.getData(key);

                if (data != null) {
                    category = data.getData();
                }

                if (category != null) {
                    final List<String> postIdList = category.getPostIds();
                    if (postIdList != null) {
                        // See if there is a corresponding post with the same id in the post cache.
                        for (String postId : postIdList) {
                            if (!TextUtils.isEmpty(postId)) {
                                //shouldn't fetch single post before fetching the category of posts here
                                //AD-962
                                final Post post = getPost(postId, true, false);
                                if (post != null) {
                                    resultList.add(post);
                                }
                            }
                        }
                    }
                }
            }

            return resultList;
        }
    }

    public List<Post> getFeedsPostsList() {
        String userId = Session.getInstance().getUserId();
        // for [non-login] users
        if(!Session.getInstance().isLoggedIn() && !Session.getInstance().hasLoggedIn()) {
            userId = "0";
        }
        return getPostsListForCategory(PostCategoryTypeEnum.FEEDS, userId);
    }

    public List<Post> getUserPostsList(final String userId) {
        return getPostsListForCategory(PostCategoryTypeEnum.USERPOSTS, userId);
    }

    public List<Post> getMentionsPostsList() {
        final String userId = Session.getInstance().getUserId();
        return getPostsListForCategory(PostCategoryTypeEnum.MENTIONS, userId);
    }

    public List<Post> getWatchedPostsList() {
        final String userId = Session.getInstance().getUserId();
        return getPostsListForCategory(PostCategoryTypeEnum.WATCHEDPOSTS, userId);
    }

    public List<Post> getRepliesForPostWithId(final String postId) {
        return getPostsListForCategory(PostCategoryTypeEnum.REPLIES, postId);
    }

    public List<Post> getResharesForPostWithId(final String postId) {
        return getPostsListForCategory(PostCategoryTypeEnum.RESHARES, postId);
    }

    public List<Post> getGroupFeedsPostsList(final String groupId) {
        return getPostsListForCategory(PostCategoryTypeEnum.GROUP_FEEDS, groupId);
    }

    public List<Post> getPostsFromSearch(String queryStr) {
        return getPostsListForCategory(PostCategoryTypeEnum.SEARCH_POSTS, queryStr);
    }

    public List<Post> getPostsFromTopic(String queryStr) {
        return getPostsListForCategory(PostCategoryTypeEnum.SEARCH_HOT_TOPIC_POSTS, queryStr);
    }

    public boolean isPostCategoryEnded(final PostCategoryTypeEnum type, final String queryStr) {
        boolean isEnded = false;
        PostCategory postCategory = getPostCategory(type, queryStr);
        if (postCategory != null) {
            isEnded = postCategory.isEnd();
        }

        return isEnded;
    }

    public PostCategory getPostCategory(final PostCategoryTypeEnum type, final String queryStr) {
        if (!TextUtils.isEmpty(queryStr)) {

            final String key = PostCategory.getKey(type, queryStr);

            PostCategory category = null;
            VersionedData<PostCategory> data = mPostCategoryCache.getData(key);

            if (data != null) {
                category = data.getData();
            }

            if (category != null) {
                return category;
            }
        }
        return null;
    }

    public boolean isFeedsEnded() {
        final String userId = Session.getInstance().getUserId();
        return isPostCategoryEnded(PostCategoryTypeEnum.FEEDS, userId);
    }

    public boolean isUserPostsEnded(final String userId) {
        return isPostCategoryEnded(PostCategoryTypeEnum.USERPOSTS, userId);
    }

    public boolean isMentionsPostsEnded() {
        final String userId = Session.getInstance().getUserId();
        return isPostCategoryEnded(PostCategoryTypeEnum.MENTIONS, userId);
    }

    public boolean isWatchedPostsEnded() {
        final String userId = Session.getInstance().getUserId();
        return isPostCategoryEnded(PostCategoryTypeEnum.WATCHEDPOSTS, userId);
    }

    public boolean isRepliesForPostEnded(final String postId) {
        return isPostCategoryEnded(PostCategoryTypeEnum.REPLIES, postId);
    }

    public boolean isResharesForPostEnded(final String postId) {
        return isPostCategoryEnded(PostCategoryTypeEnum.RESHARES, postId);
    }

    public boolean isGroupFeedsPostsEnded(final String groupId) {
        return isPostCategoryEnded(PostCategoryTypeEnum.GROUP_FEEDS, groupId);
    }

    public boolean isPostsFromSearchEnded(String queryStr) {
        return isPostCategoryEnded(PostCategoryTypeEnum.SEARCH_POSTS, queryStr);
    }

    public boolean isPostsFromTopicEnded(String queryStr) {
        return isPostCategoryEnded(PostCategoryTypeEnum.SEARCH_HOT_TOPIC_POSTS, queryStr);
    }

    /**
     * Adds a Post to the cache.
     * 
     * @param post
     *            The Post to be cached.
     * @param shouldPersist
     *            Indicates whether the cached Post should immediately be
     *            persisted to data storage.
     * @return The Versioned Data post that was just created and added to the
     *         cache.
     */
    private VersionedData<Post> addPost(final Post post, final boolean shouldPersist, final PostCategoryTypeEnum type) {
        synchronized (CACHE_LOCK) {
            if (post != null) {

                //AD-1735 should not let incomplete repost replace existing complete repost
                if (type == PostCategoryTypeEnum.RESHARES) {
                    if (!shouldReplaceRepost(post)) {
                        return null;
                    }
                }

                VersionedData<Post> versionedDataPost = new VersionedData<>(post);
                mPostCache.cacheData(post.getId(), versionedDataPost);

                if (shouldPersist) {
                    if (!savePostToPersistentStorage(versionedDataPost)) {
                        Logger.error.log(LOG_TAG, "Failed to persist with savePostToPersistentStorage");
                    }
                }

                return versionedDataPost;
            }

            return null;
        }
    }

    private boolean shouldReplaceRepost(final Post repost) {
        VersionedData<Post> data = mPostCache.getData(repost.getId());
        if (data != null) {
            Post existingRepost = data.getData();
            String existingAuthorName = PostUtils.getPostAuthorUsername(existingRepost.getRootPost());
            String authorName = PostUtils.getPostAuthorUsername(repost.getRootPost());
            if (!TextUtils.isEmpty(existingAuthorName) && TextUtils.isEmpty(authorName)) {
                return false;
            }

        }
        return true;
    }

    /**
     * Adds a list of posts to the cache.
     * 
     * @param postList
     *            A list of posts to be cached.
     * @param shouldPersist
     *            Indicates whether the cached posts should immediately be
     *            persisted to data storage.
     */
    private void addPosts(final List<Post> postList, final boolean shouldPersist, final PostCategoryTypeEnum type) {
        synchronized (CACHE_LOCK) {
            if (postList != null && !postList.isEmpty()) {
                final List<VersionedData<Post>> versionedDataPostList = new ArrayList<VersionedData<Post>>();
                for (Post post : postList) {
                    final VersionedData<Post> versionedDataPost = addPost(post, false, type);
                    if (shouldPersist && versionedDataPost != null) {
                        versionedDataPostList.add(versionedDataPost);
                    }

                    // Also add replies and reshares to the post cache.
                    if (post.getReplies() != null) {
                        Post[] replyPostsArr = post.getReplies().getReplies();
                        if (replyPostsArr != null && replyPostsArr.length > 0) {
                            for (final Post replyPost : replyPostsArr) {
                                final VersionedData<Post> versionedDataReplyPost = addPost(replyPost, false, type);
                                if (shouldPersist && versionedDataReplyPost != null) {
                                    versionedDataPostList.add(versionedDataReplyPost);
                                }
                            }
                        }
                    }

                    if (post.getReshares() != null) {
                        Post[] resharePostsArr = post.getReshares().getReshares();
                        if (resharePostsArr != null && resharePostsArr.length > 0) {
                            for (Post resharePost : resharePostsArr) {
                                final VersionedData<Post> versionedDataResharePost = addPost(resharePost, false, type);
                                if (shouldPersist && versionedDataResharePost != null) {
                                    versionedDataPostList.add(versionedDataResharePost);
                                }
                            }
                        }
                    }
                }

                // We persist all the added posts in one shot for efficiency
                // purposes.
                if (shouldPersist && !versionedDataPostList.isEmpty()) {

                    // Delegate the work of persisting posts to another thread in order to avoid UI thread being blocked
                    Runnable savePostListToPersistentStorageTask = new Runnable() {
                        @Override
                        public void run() {
                            if (!savePostListToPersistentStorage(versionedDataPostList)) {
                                Logger.error.log(LOG_TAG, "Failed to persist with savePostListToPersistentStorage");
                            }
                        }
                    };

                    mPersistPostHandler.post(savePostListToPersistentStorageTask) ;

                }
            }
        }
    }

    private List<String> getPostDraftData(){
        String postIdListJsonString = SystemDatastore.getInstance().getPostDraftData(Session.getInstance().getUsername());
        List<String> postIdList = new Gson().fromJson(postIdListJsonString, new TypeToken<List<String>>(){}.getType());
        if(postIdList == null) {
            postIdList = new ArrayList<String>();
        }
        return postIdList;
    }

    private void addPostDraftData(String id) {
        List<String> currentDraftIds = getPostDraftData();
        currentDraftIds.add(id);
        String currentDraftIdsJson = new Gson().toJson(currentDraftIds);
        SystemDatastore.getInstance().savePostDraftData(Session.getInstance().getUsername(), currentDraftIdsJson);
    }

    public void removePostDraftData(String id) {
        List<String> currentDraftIds = getPostDraftData();
        currentDraftIds.remove(id);
        String currentDraftIdsJson = new Gson().toJson(currentDraftIds);
        SystemDatastore.getInstance().savePostDraftData(Session.getInstance().getUsername(), currentDraftIdsJson);
    }
	
    /**
     * Adds a list of post ids to the cache.
     * 
     * @param type
     *            The {@link PostCategoryTypeEnum} corresponding to the category of post ids to be
     *            cached.
     * @param query
     *            The id of the user whose posts are being cached. Or the id of
     *            the post whose replies are being cached.
     * @param posts
     *            The list of posts to be cached.
     * @param shouldPersist
     *            Indicates whether the cached posts should immediately be
     *            persisted to data storage.
     *
     */
    private void addPostsToCategoryCache(final PostCategoryTypeEnum type, String query, List<Post> posts, int limit,
            int offset, final boolean shouldPersist, boolean isDraftData, boolean isFetchedFromServer) {
        synchronized (CACHE_LOCK) {
            if (type != null && posts != null) {
                final String key = PostCategory.getKey(type, query);

                VersionedData<PostCategory> data = mPostCategoryCache.getData(key);
                PostCategory cachedData = null;
                if (data != null) {
                    cachedData = data.getData();
                } else {
                    data = new VersionedData<PostCategory>();
                }
                if (cachedData == null) {
                    cachedData = new PostCategory(type, query);
                }
                
                if (offset == 0 && !isDraftData) {
                    List<String> draftList = getPostDraftData();
                    for (String draftID : draftList) {
                        Post draftPost = getPost(draftID, false);
                        if (draftPost != null && draftPost.getBody() != null) {
                            for (Post post : posts) {
                                if (!post.isDraft() && draftPost.getBody().length() > 0
                                        && draftPost.getBody().equals(post.getBody())) {
                                    removePostWithId(draftID);
                                    removePostDraftData(draftID);
                                    break;
                                }
                            }
                        }
                    }
                    Collections.reverse(draftList);
                    cachedData.clearPostsButKeep(draftList);
                }

                if (isDraftData) {
                    for(Post post: posts) {
                        addPostDraftData(post.getId());
                    }
                    cachedData.addPosts(offset, posts);
                } else {
                    //add posts at end of existing post list instead of at offset index previously.
                    //if adding posts at a certain offset index, we need to exclude the existing promoted post
                    cachedData.addPosts(posts);
                }

                int length = posts.size();
                if (isFetchedFromServer) {
                    cachedData.setEnd(length < limit);
                }

                data.setData(cachedData);
                mPostCategoryCache.cacheData(key, data);

                boolean shouldPersistPost = isDraftData || isFetchedFromServer;
                addPosts(posts, shouldPersistPost, type);

                if (shouldPersist) {
                    if (!savePostsForCategoryToPersistentStorage(key, data)) {
                        Logger.error.log(LOG_TAG, "Failed to persist with savePostsForCategoryToPersistentStorage");
                    }
                }
            }
        }
    }

	/**
	 * Removes a post with the given id from all relevant caches.
	 * The post isn't actually removed from the cache, rather it is simply marked as removed.
	 * @param postId   The id of the post to be removed.
	 * @return         The Post that was removed, null if the Post could not be found in cache.
	 */
	public Post removePostWithId(final String postId) {
	    synchronized (CACHE_LOCK) {
	        if (mPostCache != null) {
	            final VersionedData<Post> versionedDataPost = mPostCache.getData(postId);
	            if (versionedDataPost != null) {	                	                
	                final Post post = versionedDataPost.getData();
	                
	                if (post != null) {
	                    // Keep a list of posts that were marked as removed or altered 
	                    // within this block and persist them at the end of the block.	        
	                    final List<VersionedData<Post>> versionedDataPostsToPersist = new ArrayList<VersionedData<Post>>();
	                    
	                    markPostAsAlteredOrRemoved(versionedDataPost, true, false);
	                    versionedDataPostsToPersist.add(versionedDataPost);

                        //remove the post from cache, a same post may exist in different post categories
                        Map<String, CachedData<VersionedData<PostCategory>>> snapshot = mPostCategoryCache.snapshot();
                        for (String key : snapshot.keySet()) {
                            VersionedData<PostCategory> categoryData = mPostCategoryCache.getData(key);
                            if (categoryData == null) {
                                continue;
                            }
                            PostCategory category = categoryData.getData();
                            category.removePost(post.getId());
                        }
	                    
    	                // Remove all replies and reshares for this post as well.
    	                if (post.getReplies() != null) {
    	                    final Post[] replyPostsArr = post.getReplies().getReplies();
    	                    if (replyPostsArr != null) {
        	                    for (Post replyPost : replyPostsArr) {
        	                        final VersionedData<Post> versionedDataReplyPost = mPostCache.getData(replyPost.getId());
        	                        if (versionedDataReplyPost != null) { 
        	                            markPostAsAlteredOrRemoved(versionedDataReplyPost, true, false);
        	                            versionedDataPostsToPersist.add(versionedDataReplyPost);
        	                        }    	                        
        	                    }
    	                    }
    	                }
    	                
    	                if (post.getReshares() != null) {
    	                    final Post[] resharePostsArr = post.getReshares().getReshares();
    	                    if (resharePostsArr != null) {
        	                    for (Post resharePost : resharePostsArr) {
        	                        final VersionedData<Post> versionedDataResharePost = mPostCache.getData(resharePost.getId());
                                    if (versionedDataResharePost != null) { 
                                        markPostAsAlteredOrRemoved(versionedDataResharePost, true, false);
                                        versionedDataPostsToPersist.add(versionedDataResharePost);
                                    }
        	                    }
    	                    }
    	                }
    	                
    	                // Mark the root post and parent post as altered.
    	                if (post.getRootPost() != null) {
    	                    final VersionedData<Post> versionedDataRootPost = mPostCache.getData(post.getRootPost().getId());
    	                    if (versionedDataRootPost != null) {
    	                        markPostAsAlteredOrRemoved(versionedDataRootPost, false, false);
    	                        versionedDataPostsToPersist.add(versionedDataRootPost);   	                        	  
    	                    }
    	                }
    	                
    	                if (post.getParentPost() != null) {
                            final VersionedData<Post> versionedDataParentPost = mPostCache.getData(post.getParentPost().getId());
                            if (versionedDataParentPost != null) {
                                markPostAsAlteredOrRemoved(versionedDataParentPost, false, false);
                                versionedDataPostsToPersist.add(versionedDataParentPost);      
                            }
                        }    	   
    	                
    	                if (!versionedDataPostsToPersist.isEmpty()) {
    	                    if (!savePostListToPersistentStorage(versionedDataPostsToPersist)) {
    	                        Logger.error.log(LOG_TAG, "Failed to persist with savePostListToPersistentStorage");
    	                    }
    	                }
	                }
	                
	                return post;
	            }	            
	        }
	        
	        return null;
	    }
	}			
	
	/**
	 * Sets a list of tag options to the cache.
	 * @param tagOptionsList   A list containing the tag options to be cached.
	 */
	private void setTagOptions(final List<Tag> tagOptionsList, final TaggingCriteriaTypeEnum taggingCriteriaType) {
	    synchronized (CACHE_LOCK) {
            if (mTagOptionsCache != null && taggingCriteriaType != null && tagOptionsList != null) {
                final String key = String.valueOf(taggingCriteriaType.value());
                mTagOptionsCache.cacheData(key, tagOptionsList);
            }
	    }
	}
	
	/**
	 * Sends a request to fetch a single Post.
	 * @param postId   The post id of the Post to be fetched.
	 */
	private void requestGetSinglePost(final String postId) {
	    final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {            
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendGetSinglePost(getPostsListener, postId, 
                        0, 0, DEFAULT_MAX_TAGS);
            }
        }
	}
	
    /**
     * Sends a request to fetch all posts for a given category.
     * 
     * @param queryStr
     *            The id of the user whose posts are to be fetched OR The id of
     *            the post whose replies / reshares are to be fetched. Or the
     *            query from a search
     * @param offset
     *            The offset from which the posts are to be fetched.
     * @param limit
     *            The limit imposed on the results fetched from the server.
     * @param key
     *            The {@link PostCategoryTypeEnum} that corresponds to the category of the
     *            posts to be fetched.
     */
    private void requestCategorizedPosts(String queryStr, int offset, int limit, final PostCategoryTypeEnum key) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                final String excludedPosts = String.valueOf(PostTypeEnum.ACTIVITY.value());
                switch (key) {
                    case FEEDS:
                        requestManager.sendGetHomeFeeds(getPostsListener, queryStr, offset, limit, 0, 0, excludedPosts,
                                DEFAULT_MAX_TAGS);
                        break;
                    case USERPOSTS:
                        requestManager.sendGetPosts(getPostsListener, queryStr, offset, limit, 0, 0, excludedPosts,
                                DEFAULT_MAX_TAGS);
                        break;
                    case WATCHEDPOSTS:
                        requestManager.sendGetWatchList(getPostsListener, queryStr, offset, limit, 0, 0, null,
                                DEFAULT_MAX_TAGS);
                        break;
                    case MENTIONS:
                        requestManager.sendGetMentions(getPostsListener, queryStr, offset, limit, 0, 0, excludedPosts,
                                DEFAULT_MAX_TAGS);
                        break;
                    case REPLIES:
                        requestManager.sendGetRepliesForPost(getPostsListener, queryStr, offset, limit);
                        break;
                    case RESHARES:
                        requestManager.sendGetResharesForPost(getPostsListener, queryStr, offset, limit);
                        break;
                    case SEARCH_POSTS:
                        {
                            int page = Tools.convertOffsetToPage(offset, limit);
                            requestManager.searchForPost(searchListener, queryStr, page, limit);
                        }
                        break;
                    case SEARCH_HOT_TOPIC_POSTS:
                        {
                            int page = Tools.convertOffsetToPage(offset, limit);
                            requestManager.getPostsForHotTopic(searchListener, queryStr, page, limit);
                        }
                    break;
                    case GROUP_FEEDS:
                        requestManager.sendGetGroupFeeds(getPostsListener, queryStr, offset, limit);
                        break;
                    case UNKNOWN:
                        break;
                }

                if (key != PostCategoryTypeEnum.UNKNOWN) {
                    BroadcastHandler.Post.sendBeginFetchForCategory(key);
                }
            }
        }
    }
    
    /**
     * Sends a request to fetch emotional footprint tagging options.
     * @param taggingCriteriaType   The tagging criteria type to be fetched.
     */
    private void requestTagOptions(final TaggingCriteriaTypeEnum taggingCriteriaType) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {            
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendGetTagOptions(new GetTagOptionsListener() {

                    @Override
                    public void onTagOptionsReceived(final List<Tag> tagOptionsList) {
                        setTagOptions(tagOptionsList, taggingCriteriaType);
                        BroadcastHandler.Post.sendFetchTagOptionsCompleted(taggingCriteriaType);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.Post.sendFetchTagOptionsError(error, taggingCriteriaType);
                    }

                }, taggingCriteriaType, DEFAULT_EMOTIONAL_FOOTPRINT_IMAGE_SIZE);
            }
        }
    }
    
    /**
     * Sends a request to delete a post.
     * @param postId    The id of the post to be deleted.
     */
    public void requestDeletePost(final String postId) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {            
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendDeletePost(new SimpleResponseListener() {

                    @Override
                    public void onSuccess(MigResponse response) {
                        removePostWithId(postId);
                        BroadcastHandler.Post.sendDeleted(postId);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.Post.sendDeleteError(error, postId);
                    }
                }, postId);
            }
        }
    } 

    /**
     * Loads a post with the given id from persistent storage.
     * @param postId    The id of the post to be loaded.
     * @return          The VersionedData Post to be returned or null 
     *                  if content matching the postId could not be found. 
     */
    private VersionedData<Post> loadPostWithIdFromPersistentStorage(final String postId) {
        if (mPostsDAO != null) {
            return mPostsDAO.loadPostWithIdFromDatabase(postId);
        }
        
        return null;
    }
    
    /**
     * Loads all post ids for the given key from persistent storage.
     * @param key       The key matching the category of post ids to be loaded.
     * @return          A List containing the ids of the posts in the category or null
     *                  if the content mathcing the key could not be found.
     */
    private VersionedData<List<String>> loadPostIdsForCategoryFromPersistentStorage(final String key) {
        if (mPostsDAO != null) {
            return mPostsDAO.loadPostIdsForCategoryFromDatabase(key);
        }
        
        return null;
    }
    
    /**
     * Saves a VersionedData post to persistent storage.
     * Inserts a new post or updates the existing post (matched by post id)
     * @param versionedDataPost The VersionedData Post to be persisted.
     * @return true on success, false otherwise.
     */
    private boolean savePostToPersistentStorage(final VersionedData<Post> versionedDataPost) {
        if (mPostsDAO != null) {
            return mPostsDAO.savePostToDatabase(versionedDataPost);
        }
        
        return false;
    }
    
    /**
     * Saves a list of VersionedData posts to persistent storage.
     * Inserts new posts or updates the existing posts (matched by post id)
     * @param versionedDataPostList The list of VersionedData posts to be persisted.
     * @return  true on success and false otherwise.
     */
    private boolean savePostListToPersistentStorage(final List<VersionedData<Post>> versionedDataPostList) {
        if (mPostsDAO != null) {
            return mPostsDAO.savePostListToDatabase(versionedDataPostList);
        }
        
        return false;
    }
    
    /**
     * Saves a list of Post corresponding to a category to persistent storage.
     * 
     * @param key
     *            The category key
     * @param versionedPostCategory
     *            The versioned data of the {@link PostCategory} to be persisted.
     * @return true on success and false otherwise.
     */
    private boolean savePostsForCategoryToPersistentStorage(final String key, final VersionedData<PostCategory> versionedPostCategory) {
        if (mPostsDAO != null) {
            return mPostsDAO.savePostsForCategoryToDatabase(key, versionedPostCategory);
        }
        return false;
    }

    public List<Post> getPostsFromSearch(String queryStr, int offset, int limit, final boolean shouldForceFetch,
                                         final boolean justLoadFromCache) {
        Logger.debug.log(LOG_TAG, "searchPosts: ", queryStr, " limit: ", limit, " offset: ", offset);
        return getPostsListForCategory(PostCategoryTypeEnum.SEARCH_POSTS, queryStr, offset, limit, shouldForceFetch, justLoadFromCache);
    }

    public List<Post> getPostsFromTopic(String queryStr, int offset, int limit, final boolean shouldForceFetch,
                                        final boolean justLoadFromCache) {
        Logger.debug.log(LOG_TAG, "getPostsFromTopic: ", queryStr, " limit: ", limit, " offset: ", offset);
        return getPostsListForCategory(PostCategoryTypeEnum.SEARCH_HOT_TOPIC_POSTS, queryStr, offset, limit, shouldForceFetch
                , justLoadFromCache);
    }
    
    public HotTopicsResult getHotTopics(int noOfTopics) {
        Logger.debug.log(LOG_TAG, "getHotTopics: ", noOfTopics);

        if (System.currentTimeMillis() > (lastHotTopicFetchTimestamp + HOT_TOPIC_EXPIRY)
                || lastNoOfTopicsFetched != noOfTopics) {
            final ApplicationEx appEx = ApplicationEx.getInstance();
            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    lastHotTopicFetchTimestamp = System.currentTimeMillis();
                    requestManager.getHotTopics(searchListener, noOfTopics);
                }
            }
        }
        return hotTopicResultCache;
    }

    public void createNewPost(String body, FormData photo, boolean postToFacebook, boolean postToTwitter,
            ReplyPermissionEnum replyPermision, PostPrivacyEnum privacy, Location location, String mimeData) {

        String draftId = createTempNewPost(body, postToFacebook, postToTwitter, replyPermision, privacy, location, photo, mimeData);

        PostsDatastore.getInstance().createNewPostFromDraft(draftId);
    }

    public void createNewPostFromDraft(String draftId){
        Post draftPost = getPost(draftId, false);
        FormData photoData = null;
        if(draftPost.getPhoto() != null) {
            photoData = PostUtils.preparePhotoFormData(ImageFileType.JPG, draftPost.getPhoto().getBitMapByte());
        }

        createPost(draftPost.getBody(), draftPost.getOriginality(), null, null, false, photoData,
                draftPost.isPostToFacebook(), draftPost.isPostToTwitter(), true,
                ReplyPermissionEnum.fromValue(draftPost.getReplyPermission())
                , PostPrivacyEnum.fromValue(draftPost.getPrivacy()), draftPost.getLocation(), draftId, draftPost.getMimeTypeData());
    }

    private String createTempNewPost(String body, boolean postToFacebook, boolean postToTwitter,
                                   ReplyPermissionEnum replyPermision, PostPrivacyEnum privacy, Location location, FormData photo,
                                   String mimeData){
        List<Post> posts = new ArrayList<Post>();
        Post post = new Post();
        post.setIsDraft(true);
        post.setAuthor(createSelfDefaultAuthor());
        post.setBody(body);
        post.setOriginality(PostOriginalityEnum.ORIGINAL);
        post.setId(UUID.randomUUID().toString());
        post.setLocation(location);
        post.setPrivacy(privacy.value());
        post.setReplyPermission(replyPermision.value());
        post.setPostToFacebook(postToFacebook);
        post.setTimestamp(System.currentTimeMillis());
        post.setPostToTwitter(postToTwitter);
        post.setApplication(PostApplicationEnum.ANDROID);
        post.setLinks(SpannableBuilder.processUrlFromString(body));
        if(photo != null) {
            post.setPhoto(createPostPhotoFromFormData(photo));
        }
        post.setRawMimeContent(MimeType.MULTIPART.getValue(), mimeData);
        posts.add(post);
        addPostsToCategoryCache(PostCategoryTypeEnum.FEEDS, Session.getInstance().getUserId(), posts, DEFAULT_POST_LIMIT, 0, true, true, false);
        BroadcastHandler.Post.sendDraftCreated(post.getId());

        return post.getId();
    }

    private Photo createPostPhotoFromFormData(FormData photoFormData){
        Photo photo = new Photo();
        photo.setId((long)((Long.MAX_VALUE*(Math.random()))));
        if (photoFormData.data instanceof byte[]) {
            photo.setBitMapByte((byte[]) photoFormData.data);
        }
        photo.setDescription("");
        return photo;
    }

    private Author createSelfDefaultAuthor(){
        Author author = new Author();
        author.setUsername(Session.getInstance().getUsername());
        //TODO: create default user, to be addressed in another ticket AD-975, once get the details
        User user = UserDatastore.getInstance().getUserWithUsername(Session.getInstance().getUsername(), false);
        if(user != null) {
            Profile profile = user.getProfile();
            if(profile != null) {
                author.setPrivacy(profile.getPrivacy());
                author.setLabels(profile.getLabels());
                author.setDisplayPictureType(profile.getDisplayPictureType());
                author.setFirstName(profile.getFirstName());
                author.setLastName(profile.getLastName());
                author.setId(profile.getId());
                author.setRelationship(new Relationship() {
                    @Override
                    public boolean isSelf() {
                        return true;
                    }
                });
                return author;
            }
            return null;
        }
        return null;
    }

    public void replyPost(String body, FormData photo, String parentId, String rootId, boolean replyToRoot,
            boolean showInFeeds, ReplyPermissionEnum replyPermision, PostPrivacyEnum privacy, Location location) {
        createPost(body, PostOriginalityEnum.REPLY, parentId, rootId, replyToRoot, photo, false, false, showInFeeds,
                replyPermision, privacy, location, null, null);
    }

    public void resharePost(String body, String parentId, String rootId, ReplyPermissionEnum replyPermision,
            PostPrivacyEnum privacy, Location location) {
        createPost(body, PostOriginalityEnum.RESHARE, parentId, rootId, false, null, false, false, true,
                replyPermision, privacy, location, null, null);
    }

    public void resharePost(String body, String parentId, String rootId, boolean postToFacebook, boolean postToTwitter,
            ReplyPermissionEnum replyPermision, PostPrivacyEnum privacy, Location location) {
        createPost(body, PostOriginalityEnum.RESHARE, parentId, rootId, false, null, postToFacebook, postToTwitter,
                true, replyPermision, privacy, location, null, null);
    }
    
    public void createPost(String body, PostOriginalityEnum originality, String parentId, String rootId,
            boolean replyToRoot, FormData photo, boolean postToFacebook, boolean postToTwitter, boolean showInFeeds,
            ReplyPermissionEnum replyPermision, PostPrivacyEnum privacy, Location location, String postDraftId,
            String mimeData) {

        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                body = sanitizePostBody(body);
                CreatePostListener createPostListener = createNewPostListener;
                if (originality.equals(PostOriginalityEnum.REPLY)) {
                    createPostListener = createReplyPostListener;
                } else if (originality.equals(PostOriginalityEnum.RESHARE)) {
                    createPostListener = createResharePostListener;
                }
                requestManager.createPost(createPostListener, body, originality, parentId, rootId,
                        replyToRoot, photo, postToFacebook, postToTwitter, showInFeeds, replyPermision, privacy, location,
                        postDraftId, mimeData);
            }
        }
    }
    
    /*
     * Replaces newlines with "\n" and other such substitutions.
     * Essentially makes the content safe for posting.
     */
    private String sanitizePostBody(final String content) {
        String result = content;        
        result = result.trim();

        // EncodeHtml is unnecessary
        // result = StringUtils.encodeHtml(result);

        // Move to call of convertToJsonValidString() to com.projectgoth.nemesis.model.RequestParams#toJsonString,
        // since now we have different treatments for plain text post and non-plain text post
        // result = StringUtils.convertToJsonValidString(result);

        return result;
    }
}
