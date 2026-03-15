/**
 * Copyright (c) 2013 Project Goth
 *
 * PostCategory.java
 * Created Aug 19, 2013, 11:51:02 AM
 */

package com.projectgoth.model;

import com.projectgoth.b.data.Post;
import com.projectgoth.nemesis.enums.PostCategoryTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @author warrenbalcos
 * 
 */
public class PostCategory {

    private boolean         isEnd;

    private String          query;

    private List<String>    postIdList;

    private PostCategoryTypeEnum  type;

    public PostCategory(PostCategoryTypeEnum type, String query) {
        setType(type);
        setQuery(query);
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query
     *            the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the posts
     */
    public List<String> getPostIds() {
        if (postIdList != null) {
            return new ArrayList<String>(postIdList);
        }
        
        return null;
    }

    public List<String> getPostIds(int limit, int offset) {
        if (postIdList != null) {
            int length = postIdList.size();
            if (length > offset && offset >= 0) {
                int end = offset + limit;
                if (end > length) {
                    end = length;
                }
                if (end > offset) {
                    return new ArrayList<String>(postIdList.subList(offset, end));
                }
            }
        }
        return null;
    }

    /**
     * @return the type
     */
    public PostCategoryTypeEnum getType() {
        return type;
    }

    /**
     * Generates a key for the {@link #mPostCategoryCache}
     * 
     * @param categoryKey
     *            The PostCategory.Type for the cache
     * @param id
     *            The id for the categorized posts that are to be retrieved.
     *            This could be a userId or a postId.
     * @return A String key.
     */
    public static String getKey(PostCategoryTypeEnum type, String query) {
        if (type != null) {
            return type.value() + query;
        }
        return null;
    }

    public String getKey() {
        return getKey(getType(), query);
    }
    
    public void clearPosts() {
        postIdList = new ArrayList<String>();
    }

    public void clearPostsButKeep(List<String> postTempIdList) {
        postIdList = postTempIdList;
    }

    /**
     *  add a list of posts in the existing post category
     *
     * @param offset
     * there's a server dependency about the offset of a post.  the offset of a post is a integer index
     * which can change on server side once there's new posts created in the category, but clients doesn't know.
     * and there's no other kinds of offset when fetching posts now.
     *
     * @param postList
     */
    public void addPosts(int offset, List<Post> postList) {
        if (postList != null) {
            if (this.postIdList == null) {
                this.postIdList = new ArrayList<String>();
            }
            
            int location = offset;
            if (location > postIdList.size()) {
                location = postIdList.size();
            }
            for (Post post : postList) {
                if (!this.postIdList.contains(post.getId())) {
                    this.postIdList.add(location, post.getId());
                    location++;
                }
            }

        }
    }

    /**
     * add posts at the end
     *
     * @param postList
     */
    public void addPosts(List<Post> postList) {
       int offset = 0;
       if (this.postIdList != null) {
            offset = postIdList.size();
       }

       addPosts(offset, postList);

    }

    public boolean removePost(String postId) {
        boolean ret = false;
        if (this.postIdList != null ) {
            ret = postIdList.remove(postId);
        }

        return ret;
    }

    public void resetPostIds(List<String> postIdList) {
        clearPosts();
        if(postIdList != null && !postIdList.isEmpty()) {
            for (String postId : postIdList) {
                if (!this.postIdList.contains(postId)) {
                    this.postIdList.add(postId);
                }
            }
        }
    }

    /**
     * @param type
     *            the type to set
     */
    private void setType(PostCategoryTypeEnum type) {
        this.type = type;
    }

    /**
     * @return the isEnd
     */
    public boolean isEnd() {
        return isEnd;
    }

    /**
     * @param isEnd
     *            the isEnd to set
     */
    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }
}
