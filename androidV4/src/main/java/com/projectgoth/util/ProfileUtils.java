/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileUtils.java
 * Created Nov 4, 2014, 5:24:23 PM
 */

package com.projectgoth.util;

import com.projectgoth.b.data.Labels;
import com.projectgoth.b.data.Privacy;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.Relationship;
import com.projectgoth.b.enums.EveryoneOrFollowerAndFriendPrivacyEnum;
import com.projectgoth.b.enums.UserLabelAdminEnum;
import com.projectgoth.b.enums.UserLabelMerchantEnum;

/**
 * @author mapet
 * 
 */
public class ProfileUtils {

    public static boolean isProfilePrivate(Profile profile) {
        if (profile != null) {
            Privacy privacy = profile.getPrivacy();
            
            if (privacy != null) {
                EveryoneOrFollowerAndFriendPrivacyEnum feedPrivacy = privacy.getFeed();
                
                if (feedPrivacy == EveryoneOrFollowerAndFriendPrivacyEnum.FRIEND_OR_FOLLOWER) {
                    Relationship relationship = profile.getRelationship();
                    if (relationship != null && !relationship.isFriend() && !relationship.isFollower()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static boolean isMerchant(final Labels labels) {
        return !(labels.getMerchant() == null || labels.getMerchant().equals(UserLabelMerchantEnum.NOT_MERCHANT));
    }
    
    public static boolean isGlobalAdmin(final Labels labels) {
        return (labels.getAdmin() != null && labels.getAdmin().equals(UserLabelAdminEnum.GLOBAL_ADMIN));
    }
}
