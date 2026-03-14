/**
 * Copyright (c) 2013 Project Goth
 *
 * RecommendationGroup.java
 * Created Aug 31, 2013, 7:24:14 PM
 */

package com.projectgoth.model;

import com.projectgoth.b.data.Profile;
import com.projectgoth.common.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author angelorohit
 * 
 */
public class RecommendationGroup {

    // The different types of recommendations.
    public enum RecommendationGroupType {
        CONTACTS, // Recommended contacts.
        INTERESTS; // People you may be interested in.
    }

    // The type of this RecommendationGroup.
    private RecommendationGroupType mGroupType   = RecommendationGroupType.INTERESTS;
    // Name of the group
    private String                  mGroupName   = Constants.BLANKSTR;
    // The current fetch offset. This is needed when fetching more data and will
    // be set to zero when refreshing data.
    private int                     mFetchOffset = 0;

    private List<Profile>           mProfiles    = new ArrayList<Profile>();
    
    // Indicates whether the profiles in this RecommendationGroup are being loaded.
    private boolean                 mIsLoading   = false;

    public RecommendationGroup(final RecommendationGroupType type, final String groupName) {
        setGroupType(type);
        setGroupName(groupName);
        setFetchOffset(0);
        setIsLoading(false);
    }

    public void setGroupType(final RecommendationGroupType type) {
        mGroupType = type;
    }

    public void setGroupName(final String groupName) {
        mGroupName = groupName;
    }

    public void setFetchOffset(final int fetchOffset) {
        mFetchOffset = fetchOffset;
    }
    
    public void setIsLoading(final boolean state) {
        mIsLoading = state;
    }
    
    public void clearProfiles() {
        mProfiles.clear();
    }
    
    public void addProfile(final Profile profile) {
        mProfiles.add(profile);
    }

    public RecommendationGroupType getGroupType() {
        return mGroupType;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public int getFetchOffset() {
        return mFetchOffset;
    }
    
    public List<Profile> getProfiles() {
        return mProfiles;
    }    
    
    public boolean isLoading() {
        return mIsLoading;
    }
}
