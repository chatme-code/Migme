/**
 * Copyright (c) 2013 Project Goth
 *
 * PostData.java
 * Created Jan 28, 2014, 4:31:25 PM
 */

package com.projectgoth.model;

import com.projectgoth.nemesis.enums.PostPrivacyEnum;
import com.projectgoth.nemesis.enums.ReplyPermissionEnum;


/**
 * @author sarmadsangi
 *
 */
public class ShareBoxStateData {


    private String body;
    private String photoPath;
    private PhotoTypeEnum photoType;
    private boolean postToFacebook;
    private boolean postToTwitter;
    private ReplyPermissionEnum replyPermission;
    private PostPrivacyEnum      postPrivacy;
    
    
    public enum PhotoTypeEnum {
        GALLERY(0), CAMERA(1);

        private int mValue;

        private PhotoTypeEnum(final int value) {
            mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }
    
    public ShareBoxStateData() {
        this(null, null, null, false, false, ReplyPermissionEnum.EVERYONE, PostPrivacyEnum.EVERYONE);
    }
    
    /**
     * @param body
     * @param photo
     * @param postToFacebook
     * @param postToTwitter
     * @param replyPermission
     * @param postPrivacy
     */
    public ShareBoxStateData(String body, String photoPath, PhotoTypeEnum photoType, boolean postToFacebook, boolean postToTwitter,
            ReplyPermissionEnum replyPermission, PostPrivacyEnum postPrivacy) {
        super();
        this.body = body;
        this.photoPath = photoPath;
        this.photoType = photoType;
        this.postToFacebook = postToFacebook;
        this.postToTwitter = postToTwitter;
        this.replyPermission = replyPermission;
        this.postPrivacy = postPrivacy;
    }
    
    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    
    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    
    /**
     * @return the photo
     */
    public String getPhotoPath() {
        return photoPath;
    }

    
    /**
     * @param photo the photo to set
     */
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    
    
    /**
     * @param photo the photo to set
     */
    public void setTypeWithPhotoPath(PhotoTypeEnum photoType,String photoPath) {
        this.photoPath = photoPath;
        this.photoType = photoType;
    }

    
    /**
     * @return the postToFacebook
     */
    public boolean isPostToFacebook() {
        return postToFacebook;
    }

    
    /**
     * @param postToFacebook the postToFacebook to set
     */
    public void setPostToFacebook(boolean postToFacebook) {
        this.postToFacebook = postToFacebook;
    }

    
    /**
     * @return the postToTwitter
     */
    public boolean isPostToTwitter() {
        return postToTwitter;
    }

    
    /**
     * @param postToTwitter the postToTwitter to set
     */
    public void setPostToTwitter(boolean postToTwitter) {
        this.postToTwitter = postToTwitter;
    }

    
    /**
     * @return the replyPermission
     */
    public ReplyPermissionEnum getReplyPermission() {
        return replyPermission;
    }

    
    /**
     * @param replyPermission the replyPermission to set
     */
    public void setReplyPermission(ReplyPermissionEnum replyPermission) {
        this.replyPermission = replyPermission;
    }

    
    /**
     * @return the postPrivacy
     */
    public PostPrivacyEnum getPostPrivacy() {
        return postPrivacy;
    }

    
    /**
     * @param postPrivacy the postPrivacy to set
     */
    public void setPostPrivacy(PostPrivacyEnum postPrivacy) {
        this.postPrivacy = postPrivacy;
    }

    
    /**
     * @return the photoType
     */
    public PhotoTypeEnum getPhotoType() {
        return photoType;
    }

    
    /**
     * @param photoType the photoType to set
     */
    public void setPhotoType(PhotoTypeEnum photoType) {
        this.photoType = photoType;
    }
    
    public void clearContent() {
        setBody(null);
        setTypeWithPhotoPath(null, null);
    }
}
