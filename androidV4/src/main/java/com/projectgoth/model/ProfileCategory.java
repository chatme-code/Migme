/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileCategory.java
 * Created Sep 10, 2013, 2:53:30 PM
 */

package com.projectgoth.model;

import android.text.TextUtils;
import com.projectgoth.b.data.Profile;
import com.projectgoth.common.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author angelorohit
 */
public class ProfileCategory {

    /**
     * The different types of Profile categories.
     */
    public enum Type {
        UNKNOWN("unknown"), 
        FOLLOWING("following"), 
        FOLLOWER("follower"),         
        RECOMMENDED_USERS("recommendedusers"),
        RECOMMENDED_CONTACTS("recommendedcontacts"),
        SEARCH_USERS("searchusers");

        private String mValue;

        private Type(final String value) {
            mValue = value;
        }

        public String value() {
            return mValue;
        }

        public static Type fromValue(final String value) {
            if (value != null) {
                for (Type e : Type.values()) {
                    if (e.value().equals(value)) {
                        return e;
                    }
                }
            }
            return UNKNOWN;
        }        
    }
    
    private boolean       isEnd;
    private String        usernameOrId;

    //username list of the profiles in the category, not user id
    private List<String>  usernames;
    private Type          type;
    
    /**
     * Constructor
     * @param type          The type of profile category.
     * @param usernameOrId  The user name or id for which the profiles need to be categorized.
     */
    public ProfileCategory(final Type type, final String usernameOrId) {
        this.setType(type);
        this.setUsernameOrId(usernameOrId);
    }

    /**
     * @return the usernameOrId
     */
    public String getUsernameOrId() {
        return usernameOrId;
    }

    /**
     * @param usernameOrId the usernameOrId to set
     */
    public void setUsernameOrId(String usernameOrId) {
        this.usernameOrId = usernameOrId;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return the isEnd
     */
    public boolean isEnd() {
        return isEnd;
    }

    /**
     * @param isEnd the isEnd to set
     */
    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    /**
     * Returns a copy of the usernames for this {@link ProfileCategory}
     * @return A List of usernames or null if no usernames were set for this category.
     */
    public List<String> getUsernames() {
        if (usernames != null) {
            return new ArrayList<String>(usernames);
        }
        
        return null;
    }
    
    /**
     * Returns a copy of the usernames in this category within the given offset and limit.
     * @param offset    The offset from which usernames are to be retrieved.
     * @param limit     The limit imposed on the number of usernames to be retrieved.
     * @return          A List of usernames or null if no usernames were set for this category.
     */
    public List<String> getUsernames(final int offset, final int limit) {
        if (usernames != null) {
            final int count = usernames.size();
            if (count > offset && offset >= 0) {
                int end = offset + limit;
                // We just return all of the data if limit is -1.
                if (end > count || limit < 0) {
                    end = count;
                }
                
                if (end > offset) {
                    return new ArrayList<String>(usernames.subList(offset, end));
                }
            }
        }
        
        return null;
    }

    /**
     * Generates a key based on the {@link Type} and user name or id.
     * @param type              The {@link Type} of category for which the key is to be generated. 
     * @param usernameOrId      The username or id for which the profiles have been categorized.
     * @return                  A String value
     */
    public static String getKey(Type type, String usernameOrId) {
        if (type == null) {
            type = Type.UNKNOWN;
        }
        
        if (usernameOrId == null) {
            usernameOrId = Constants.BLANKSTR;
        }
                
        return type.value() + usernameOrId;                
    }
    
    /**
     * Clears all the profiles for this category.
     */
    public void clearProfiles() {
        usernames = new ArrayList<String>();
    }
    
    /**
     * Adds usernames at from the given offset onward into the {@link #usernames} list.
     * @param offset        The offset from which to add.
     * @param profileList   A List containing the usernames to be added.
     */
    public void addProfiles(final int offset, final List<Profile> profileList) {
        if (profileList != null) {
            if (usernames == null) {
                usernames = new ArrayList<String>();
            }

            int location = offset;
            if (location > usernames.size()) {
                location = usernames.size();
            }
            for (Profile profile : profileList) {
                //always lower case the username because of this issue https://mig-me.atlassian.net/browse/CL-311
                String username = profile.getUsername().toLowerCase();
                if (!usernames.contains(username)) {
                    profile.setUsername(username);
                    usernames.add(location, username);
                    ++location;
                }
            }
        }
    }
}
