/**
 * Copyright (c) 2013 Project Goth
 *
 * UserDataStore.java
 * Created Jun 3, 2013, 12:03:13 PM
 */

package com.projectgoth.datastore;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Badge;
import com.projectgoth.b.data.BadgesResponse;
import com.projectgoth.b.data.Followers;
import com.projectgoth.b.data.Following;
import com.projectgoth.b.data.PresetCoverPhoto;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.Relationship;
import com.projectgoth.b.data.SearchResult;
import com.projectgoth.b.enums.AddFollowingResultEnum;
import com.projectgoth.b.enums.DeleteFollowingResultEnum;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.blackhole.fusion.packet.FusionPktLatestMessagesDigest;
import com.projectgoth.blackhole.fusion.packet.FusionPktLoginOk;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.ChatController;
import com.projectgoth.controller.FriendsController;
import com.projectgoth.controller.ThirdPartyIMController;
import com.projectgoth.dao.ContactGroupsDAO;
import com.projectgoth.dao.ContactsDAO;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.model.ProfileCategory;
import com.projectgoth.nemesis.NetworkResponseListener;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.enums.ActivitySourceEnum;
import com.projectgoth.nemesis.enums.FriendGroupTypeEnum;
import com.projectgoth.nemesis.enums.ProfileRelationshipChangeActionEnum;
import com.projectgoth.nemesis.listeners.*;
import com.projectgoth.nemesis.model.ContactGroup;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.nemesis.model.MigResponse;
import com.projectgoth.nemesis.model.User;
import com.projectgoth.notification.system.NativeNotificationManager;
import com.projectgoth.service.NetworkService;
import com.projectgoth.util.AndroidLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author angelorohit
 */
public class UserDatastore extends BaseDatastore {

    private static final String                       LOG_TAG                        = AndroidLogger
                                                                                             .makeLogTag(UserDatastore.class);

    // A lock that is obtained when working with any of the caches.
    private static final Object                       CACHE_LOCK                     = new Object();

    /**
     * A cache of the logged in user's friends. The key is the contact id of the
     * Friend.
     */
    private SparseArray<Friend>                       friendCache;

    /**
     * A cache of available groups. The key is the group id.
     */
    private SparseArray<ContactGroup>                 contactGroupCache;

    /**
     * A cache of categorized Profiles. The key is {@link ProfileCategory} +
     * usernameOrId.
     */
    private DataCache<VersionedData<ProfileCategory>> profileCategoryCache;

    /**
     * A cache of user profiles. The key is the user name of the profile. TODO:
     * Angelo This cache is a candidate for deprecation when the userCache gains
     * traction in UI.
     */
    private DataCache<VersionedData<Profile>>         profileCache;

    /**
     * A cache of users. The key is the user name of the User.
     */
    private DataCache<VersionedData<User>>            userCache;

    /**
     * A cache of {@link Badge} for users. The key is the username of the user
     * whose badges were previously fetched.
     */
    private DataCache<List<Badge>>                    badgesCache;
    
    /**
     * A cache of {@link PresetCoverPhoto} for users. The key is the username of
     * the user whose preset cover photos were fetched.
     */
    private DataCache<List<PresetCoverPhoto>>         presetCoverPhotosCache;

    /**
     * The current contact list version that is received from the LOGIN_OK
     * packet.
     */
    private Integer                                   currentContactListVersion;

    // Cache size limits.
    private static final int                          MAX_PROFILECATEGORY_CACHE_SIZE    = 20;
    private static final int                          MAX_PROFILE_CACHE_SIZE            = 500;
    private static final int                          MAX_BADGES_CACHE_SIZE             = 20;
    private static final int                          MAX_PRESET_COVERPHOTOS_CACHE_SIZE = 10;

    private static final int                          PROFILE_DATA_EXPIRY_TIME          = 10 * 60;

    // A DAO for saving Friends to cache.
    private ContactsDAO                               contactsDAO                       = null;

    // A DAO for saving ContactGroups to cache.
    private ContactGroupsDAO                          contactGroupsDAO                  = null;

    // The number of new followers that is sent via the server in an action
    // alert.
    // This will be used by UI as a visual indication to the user.
    // It will also be reset by the UI when it is no longer needed.
    private int                                       newFollowersCount                 = 0;

    // Profile Cache data expiry time.
    private static final int                          PROFILECATEGORY_CACHE_EXPIRY      = 1 * 60 * 1000;
    

    //@formatter:off
    private SearchUserListener searchListener = new SearchUserListener() {
        
        @Override
        public void onSearchUserReceived(SearchResult result, String queryStr, int hitsPerPage, int page) {
            Logger.debug.log(LOG_TAG, "onSearchPostReceived result: ", result, " Q: ", queryStr, " H: ", hitsPerPage, " P: ", page);
            Profile[] profileArr = Profile.cloneFromBaseProfileArray(result.getResult());      
            if (profileArr != null) {
                int offset = Tools.convertPageToOffset(page, hitsPerPage);
                addProfilesToCategoryCache(queryStr, ProfileCategory.Type.SEARCH_USERS, Arrays.asList(profileArr), offset, hitsPerPage, true);
            }

            BroadcastHandler.Profile.sendFetchSearchedUsersCompleted(queryStr, hitsPerPage, page, result.getTotalHits());
        }

        @Override
        public void onSearchError(MigError error, String queryStr, int hitsPerPage, int page) {
            Logger.debug.log(LOG_TAG, "onSearchError Q: ", queryStr, " H: ", hitsPerPage, " P: ", page);
            BroadcastHandler.Profile.sendFetchSearchedUsersError(error, queryStr, hitsPerPage, page);
        }
    };

    private RecommendedUserListener recommendedUserListener = new RecommendedUserListener(){

        @Override
        public void onRecommendedUsersReceived(SearchResult result, int limit, int offset, String requestingUserId) {
            Logger.debug.log(LOG_TAG, "onRecommendedUsersReceived result: ", result, " L: ", limit, " O: ", offset , " RID: ", requestingUserId);
            Profile[] profileArr = Profile.cloneFromBaseProfileArray(result.getResult());
            if (profileArr != null) {
                addProfilesToCategoryCache(requestingUserId, ProfileCategory.Type.RECOMMENDED_USERS, Arrays.asList(profileArr), offset, limit, true);
            }

            BroadcastHandler.Profile.sendFetchRecommendedUsersCompleted(limit, offset);
        }

        @Override
        public void onRecommendedContactsReceived(SearchResult result, int limit, int offset, String requestingUserId) {
            Logger.debug.log(LOG_TAG, "onRecommendedContactsReceived result: ", result, " L: ", limit, " O: ", offset, " RID: ", requestingUserId);
            Profile[] profileArr = Profile.cloneFromBaseProfileArray(result.getResult());
            if (profileArr != null) {
                addProfilesToCategoryCache(requestingUserId, ProfileCategory.Type.RECOMMENDED_CONTACTS, Arrays.asList(profileArr), offset, limit, true);
            }

            BroadcastHandler.Profile.sendFetchRecommendedContactsCompleted(limit, offset);
        }

        @Override
        public void onGetRecommendedUsersError(MigError error, int limit, int offset) {
            BroadcastHandler.Profile.sendFetchRecommendedUsersError(error, limit, offset);
        }

        @Override
        public void onGetRecommendedContactsError(MigError error, int limit, int offset) {
            BroadcastHandler.Profile.sendFetchRecommendedContactsError(error, limit, offset);
        }
    };

    //@formatter:on

    private UserDatastore() {
        super();

        final Context appCtx = ApplicationEx.getContext();
        if (appCtx != null) {
            contactsDAO = new ContactsDAO(appCtx);
            contactGroupsDAO = new ContactGroupsDAO(appCtx);
        }

        loadFromPersistentStorage();
    }

    private static class UserDatastoreHolder {
        static final UserDatastore sINSTANCE = new UserDatastore();
    }

    /**
     * A singleton point of access to an instance of this class.
     * 
     * @return An instance of this class.
     */
    public static UserDatastore getInstance() {
        return UserDatastoreHolder.sINSTANCE;
    }

    @Override
    protected void initData() {
        try {
            friendCache = new SparseArray<Friend>();
            contactGroupCache = new SparseArray<ContactGroup>();
            profileCategoryCache = new DataCache<VersionedData<ProfileCategory>>(MAX_PROFILECATEGORY_CACHE_SIZE);
            profileCache = new DataCache<VersionedData<Profile>>(MAX_PROFILE_CACHE_SIZE, PROFILE_DATA_EXPIRY_TIME);
            userCache = new DataCache<VersionedData<User>>(MAX_PROFILE_CACHE_SIZE, PROFILE_DATA_EXPIRY_TIME);
            badgesCache = new DataCache<List<Badge>>(MAX_BADGES_CACHE_SIZE);
            presetCoverPhotosCache = new DataCache<List<PresetCoverPhoto>>(MAX_PRESET_COVERPHOTOS_CACHE_SIZE);

            currentContactListVersion = -1;
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, e);
        }
    }

    @Override
    public void clearData() {
        super.clearData();

        if (contactsDAO != null) {
            contactsDAO.clearTables();
        }

        if (contactGroupsDAO != null) {
            contactGroupsDAO.clearTables();
        }

        initData();
        
        resetNewFollowersCount();
    }

    /**
     * Gets a friend with the given contact id from cache.
     * 
     * @param contactId
     *            The id of the Friend to be retrieved.
     * @return The associated Friend on success and null if the contactId could
     *         not be found in cache.
     */
    public Friend getFriendWithContactId(final Integer contactId) {
        synchronized (CACHE_LOCK) {
            return (contactId != null) ? friendCache.get(contactId) : null;
        }
    }

    /**
     * Get a list of the available groups.
     * 
     * @return A list of the available groups.
     */
    public List<ContactGroup> getContactGroups(boolean showIM, boolean groupFusionFriends) {
        synchronized (CACHE_LOCK) {
            List<ContactGroup> resultList = new ArrayList<ContactGroup>();
            for (int i = 0; i < contactGroupCache.size(); ++i) {
                int groupId = contactGroupCache.keyAt(i);
                final ContactGroup contactGroup = getContactGroupWithId(groupId);

                if (contactGroup != null) {
                    if (FriendGroupTypeEnum.fromGroupTypeValue(groupId) == FriendGroupTypeEnum.DEFAULT) {
                        if (groupFusionFriends) {
                            resultList.add(contactGroup);
                        } else {
                            if (contactGroup.getGroupName()
                                    .equalsIgnoreCase(FriendGroupTypeEnum.DEFAULT.getGroupName())) {
                                resultList.add(contactGroup);
                            }
                        }
                    } else if (showIM
                            && ThirdPartyIMController.getInstance().isImConfigured(contactGroup.getType().getImType())) {
                        resultList.add(contactGroup);
                    }
                }

            }

            // Sort contact groups alphabetically but place "mig33" on top.
            sortContactGroups(resultList);

            return resultList;
        }
    }

    /**
     * Sorts ContactGroups alphabetically but places "mig33" on top.
     * 
     * @param contactGroupList
     *            The List of ContactGroup to sort.
     */
    private void sortContactGroups(List<ContactGroup> contactGroupList) {
        Collections.sort(contactGroupList, new Comparator<ContactGroup>() {

            @Override
            public int compare(ContactGroup lhs, ContactGroup rhs) {
                if (lhs.getGroupName().equalsIgnoreCase(FriendGroupTypeEnum.DEFAULT.getGroupName())) {
                    return -1;
                } else if (rhs.getGroupName().equalsIgnoreCase(FriendGroupTypeEnum.DEFAULT.getGroupName())) {
                    return 1;
                }

                return (lhs.getGroupName().compareTo(rhs.getGroupName()));
            }
        });
    }

    /**
     * Finds a group whose groupId matches the given group id.
     * 
     * @param groupId
     *            The id of the group to be found.
     * @return The matching group or null if a group matching the given id could
     *         not be found in cache.
     */
    public ContactGroup getContactGroupWithId(final int groupId) {
        synchronized (CACHE_LOCK) {
            return contactGroupCache.get(groupId);
        }
    }

    /**
     * Get a list of friends for a particular group.
     * 
     * @param groupId
     *            The id of the group.
     * @param onlineFriendsOnly
     *            Indicates whether only online friends should be returned.
     * @param showSMSContacts
     *            Indicates whether SMS contacts should be returned
     * @return A list containing all the Friends in the group matching the
     *         specified groupId.
     */
    public List<Friend> getFriendsForContactGroupWithId(final int groupId, final boolean onlineFriendsOnly,
            final boolean showSMSContacts, final boolean groupFusionFriends) {
        synchronized (CACHE_LOCK) {
            List<Friend> resultList = new ArrayList<Friend>();
            final ContactGroup contactGroup = getContactGroupWithId(groupId);

            if (contactGroup != null) {

                if (!groupFusionFriends
                        && contactGroup.getGroupName().equalsIgnoreCase(FriendGroupTypeEnum.DEFAULT.getGroupName())) {
                    resultList = getAllFusionFriends(onlineFriendsOnly, showSMSContacts);
                    return resultList;
                }

                // return with empty list if IM is offline
                if (contactGroup.isIMGroup()
                        && !ThirdPartyIMController.getInstance().isImOnline(contactGroup.getType().getImType())) {
                    return resultList;
                }

                final Set<Integer> friendContactIdSet = contactGroup.getFriendIds();

                if (friendContactIdSet != null) {
                    for (Integer contactId : friendContactIdSet) {
                        final Friend friend = getFriendWithContactId(contactId);
                        if (friend != null && (!onlineFriendsOnly || friend.getPresence().isOnline())) {
                            resultList.add(friend);
                        }
                    }
                }
            }

            // Sort based on presence (available, busy, away, offline) and then
            // alphabetically.
            sortFriendList(resultList);

            return resultList;
        }
    }

    /**
     * Sorts a list of Friends based on presence (available, busy, away,
     * offline) and then alphabetically.
     * 
     * @param friendList
     *            The List of Friend to sort.
     */
    public void sortFriendList(List<Friend> friendList) {
        Collections.sort(friendList, new Comparator<Friend>() {

            @Override
            public int compare(Friend lhs, Friend rhs) {
                final PresenceType lhsPresence = lhs.getPresence();
                final PresenceType rhsPresence = rhs.getPresence();

                return (lhsPresence.getValue() < rhsPresence.getValue()) ? -1
                        : ((lhsPresence.getValue() > rhsPresence.getValue()) ? 1 : lhs.getDisplayName().compareTo(
                        rhs.getDisplayName()));
            }
        });
    }

    /**
     * Gets a Friend matching a given username in a ContactGroup whose id is
     * specified.
     * 
     * @param username
     *            The username of the Friend to be retrieved.
     * @param groupId
     *            The id of the ContactGroup of the friend (this is needed
     *            because the username of the Friend does not uniquely identify
     *            it.
     * @return The appropriate Friend if it was found in cache and null
     *         otherwise.
     */
    public Friend getFriendInContactGroupWithUsername(final String username, final int groupId) {
        synchronized (CACHE_LOCK) {
            Friend result = null;
            final ContactGroup contactGroup = getContactGroupWithId(groupId);
            if (contactGroup != null) {
                final Set<Integer> friendContactIdSet = contactGroup.getFriendIds();

                if (friendContactIdSet != null) {
                    for (Integer contactId : friendContactIdSet) {
                        final Friend friend = getFriendWithContactId(contactId);
                        if (friend != null && friend.getUsername() != null && friend.getUsername().equals(username)) {
                            result = friend;
                            break;
                        }
                    }
                }
            }

            return result;
        }
    }

    /**
     * Get friends in all groups ordered by group id.
     * 
     * @param onlineFriendsOnly
     *            Indicates whether only online friends should be returned.
     * @param showSMSContacts
     *            Indicated whether SMS contacts should be returned
     * @return A SparseArray containing a list of Friend as value and the group
     *         id as key.
     */
    public SparseArray<List<Friend>> getGroupedFriends(final boolean onlineFriendsOnly, final boolean showSMSContacts,
            final boolean groupFusionFriends) {
        SparseArray<List<Friend>> resultArray = new SparseArray<List<Friend>>();
        final List<ContactGroup> groupList = getContactGroups(Config.getInstance().isImEnabled(), groupFusionFriends);

        for (ContactGroup contactGroup : groupList) {
            final List<Friend> friendList = getFriendsForContactGroupWithId(contactGroup.getGroupID(),
                    onlineFriendsOnly, showSMSContacts, groupFusionFriends);
            resultArray.put(contactGroup.getGroupID(), friendList);
        }

        return resultArray;
    }

    /**
     * Gets a List of fusion Friends
     * 
     * @param onlineFriendsOnly
     *            Indicates whether only online friends should be returned.
     * @param showSMSContacts
     *            Indicates whether SMS contacts should be returned
     * 
     * @return A List containing all fusion Friends or an empty list if there
     *         are no friends.
     */
    public List<Friend> getAllFusionFriends(final boolean onlineFriendsOnly, final boolean showSMSContacts) {
        List<Friend> resultList = new ArrayList<Friend>();

        synchronized (CACHE_LOCK) {
            for (int i = 0; i < friendCache.size(); ++i) {
                Friend friend = friendCache.valueAt(i);
                if (friend != null
                        && FriendGroupTypeEnum.fromGroupTypeValue(friend.getGroupID()) == FriendGroupTypeEnum.DEFAULT
                        && (!onlineFriendsOnly || friend.getPresence().isOnline()) ) {
                    resultList.add(friend);
                }
            }
        }

        sortFriendList(resultList);

        return resultList;
    }

    /**
     * Find a mig33 user based on the username.
     * 
     * @param username
     * @return
     */
    public Friend findMig33User(String username) {
        //Looks like it is immutable data, we can remove synchronized to prevent block
        for (int i = 0; i < friendCache.size(); i++) {
            int key = friendCache.keyAt(i);
            Friend friend = friendCache.get(key);
            if (friend != null && friend.isFusionContact() && friend.getUsername() != null
                    && friend.getUsername().equals(username)) {
                return friend;
            }
        }
        return null;
    }

    public Friend findUser(String username) {
        //Looks like it is immutable data, we can remove synchronized to prevent block
        for (int i = 0; i < friendCache.size(); i++) {
            int key = friendCache.keyAt(i);
            Friend friend = friendCache.get(key);

            if (friend != null) {
                String friendUsername = friend.getUsername();
                if (friendUsername != null && friendUsername.equals(username)) {
                    return friend;
                }
            }
        }
        return null;
    }

    /**
     * Gets a profile with the given username from cache.
     * 
     * @param username
     *            The username of the Profile to be retrieved.
     * @param shouldForceFetch
     *            Whether the data should be force fetched from the server.
     * @return The associated Profile on success and null if the username could
     *         not be found in cache. TODO: Angelo This function will be
     *         deprecated as {@link #getUserWithUsername(String, boolean)} gets
     *         traction in UI.
     */
    public Profile getProfileWithUsername(String username, final boolean shouldForceFetch) {
        return getProfileWithUsername(username, false, null, shouldForceFetch, false);
    }

    public Profile getProfileFromCache(String username) {
        return getProfileWithUsername(username, false, null, false, true);
    }

    /**
     * Gets a profile with a given username from cache. If the profile could not
     * be found in cache, a request to fetch the profile is sent. Subsequently,
     * the {@link #followOrUnfollowUser(String, ActivitySourceEnum)} function
     * will be invoked on fetch success.
     * 
     * @see #requestGetProfile(String, boolean, ActivitySourceEnum)
     * @param username
     *            The username of the Profile to be retrieved.
     * @param shouldFollowOrUnfollow
     *            true to invoke followOrUnfollow on a successful fetch.
     * @param activitySource
     *            The source from where the follow/unfollow request originates.
     *            Can be null.
     * @param shouldForceFetch
     *            Whether the data should be force fetched from the server.
     * @return The associated Profile on success and null if the username could
     *         not be found in cache.
     */
    private Profile getProfileWithUsername(String username, final boolean shouldFollowOrUnfollow,
            final ActivitySourceEnum activitySource, final boolean shouldForceFetch, final boolean justLoadFromCache) {
        synchronized (CACHE_LOCK) {
            if (profileCache != null && !TextUtils.isEmpty(username)) {
                //always use lower case in UserDatastore since CL-311
                username = username.toLowerCase();
                VersionedData<Profile> versionedDataProfile = profileCache.getData(username);

                final long profileDataExpiryTimeMs = PROFILE_DATA_EXPIRY_TIME * 1000;
                if (versionedDataProfile == null || !versionedDataProfile.isRemoved()) {
                    
                    if (versionedDataProfile == null 
                            || versionedDataProfile.getData() == null) {
                        versionedDataProfile = loadProfileWithUsernameFromPersistentStorage(username);
                        
                        // Since the VersionedData was not found in the cache previously,
                        // we add it to the cache at this time.
                        if (versionedDataProfile != null) {
                            profileCache.cacheData(
                                    username, 
                                    versionedDataProfile, 
                                    versionedDataProfile.getRemainingExpiryTime(profileDataExpiryTimeMs) / 1000);
                        }
                    }
                }

                // Don't force-fetch if the profile was removed.
                if (versionedDataProfile != null && versionedDataProfile.isRemoved()) {
                    return null;
                } else if (!justLoadFromCache && (versionedDataProfile == null
                        || versionedDataProfile.getData() == null
                        || versionedDataProfile.isAltered() 
                        || versionedDataProfile.isExpired(profileDataExpiryTimeMs) 
                        || shouldForceFetch)) {
                    requestGetProfile(username, shouldFollowOrUnfollow, activitySource);
                }

                if (versionedDataProfile != null) {
                    return versionedDataProfile.getData();
                }
            }
            return null;
        }
    }

    /**
     * Gets a {@link User} with the given username from cache. If the User could
     * not be found in cache, a request to fetch the profile is sent.
     * 
     * @param username
     * @param shouldForceFetch
     *            Whether the data should be force fetched from the server.
     * @return The associated {@link User} on success if the username could not
     *         be found in cache.
     */
    public User getUserWithUsername(String username, final boolean shouldForceFetch) {
        synchronized (CACHE_LOCK) {
            if (userCache != null && !TextUtils.isEmpty(username)) {
                //always use lower case in UserDatastore since CL-311
                username = username.toLowerCase();
                VersionedData<User> versionedDataUser = userCache.getData(username);

                if (versionedDataUser == null || !versionedDataUser.isRemoved()) {
                    if (versionedDataUser == null || versionedDataUser.getData() == null
                            || userCache.isExpired(username)) {
                        versionedDataUser = loadUserWithUsernameFromPersistentStorage(username);
                    }

                    if (versionedDataUser == null || versionedDataUser.getData() == null
                            || versionedDataUser.isAltered() || shouldForceFetch
                            || !versionedDataUser.getData().hasProfileData()) {
                        requestGetProfile(username);
                    }
                }

                if (versionedDataUser != null) {
                    return versionedDataUser.getData();
                }
            }

            return null;
        }
    }

    /**
     * Retrieves the profile id of a user with the given username. If the
     * username passed is the currently logged in user, then we retrieve the id
     * from the {@link Session}. Otherwise, we check the profile cache and see
     * if a {@link Profile} matching the given username can be found. If not, we
     * send a request to fetch the {@link Profile}.
     * 
     * @param username
     *            The username of the user we need to look for
     * @return The userId of the user matching the passed username, null if the
     *         matching Profile of the user could not be found in cache.
     */
    public String getUserIdFromUsername(String username) {
        if (!TextUtils.isEmpty(username)) {
            username = username.toLowerCase();
            final String loggedInUsername = Session.getInstance().getUsername();
            if (!TextUtils.isEmpty(loggedInUsername) && loggedInUsername.equals(username)) {
                final String loggedInUserId = Session.getInstance().getUserId();
                if (!TextUtils.isEmpty(loggedInUserId)) {
                    return loggedInUserId;
                }
            } else {
                final Profile profile = getProfileWithUsername(username, false);
                if (profile != null && profile.getId() != null) {
                    return String.valueOf(profile.getId());
                }
            }
        }

        return null;
    }

    /**
     * Sends the appropriate request to fetch profile data from the server
     * depending on the profile category key.
     * 
     * @param usernameOrId
     *            The username or id of the user whose data is to be fetched.
     * @param offset
     *            The offset from which the profiles are to be fetched.
     * @param limit
     *            The limit imposed on the results fetched from the server.
     * @param profileCategoryType
     *            A {@link ProfileCategory.Type} that indicates what category of
     *            profiles need to be fetched.
     */
    private void requestCategorizedProfiles(final String usernameOrId, final int offset, final int limit,
            final ProfileCategory.Type profileCategoryType) {
        switch (profileCategoryType) {
            case FOLLOWER:
                requestGetFollowers(usernameOrId, offset, limit);
                break;
            case FOLLOWING:
                requestGetFollowing(usernameOrId, offset, limit);
                break;
            case RECOMMENDED_USERS:
                requestGetRecommendedUsers(usernameOrId, offset, limit);
                break;
            case RECOMMENDED_CONTACTS:
                requestGetRecommendedContacts(usernameOrId, offset, limit);
                break;
            case SEARCH_USERS:
                requestGetUsersFromSearch(usernameOrId, offset, limit);
                break;
            case UNKNOWN:
                break;
        }

        if (profileCategoryType != ProfileCategory.Type.UNKNOWN) {
            BroadcastHandler.Profile.sendBeginFetchForCategory(profileCategoryType.value());
        }
    }

    /**
     * Returns a VersionedData profile from the profile cache matching the given
     * username.
     * 
     * @param username
     *            The username of the VersionedData Profile to be retrieved.
     * @return The associated VersionedData Profile on success and null if the
     *         username could not be found in cache or persistent storage or was
     *         marked as removed.
     */
    private VersionedData<Profile> getVersionedDataProfile(String username) {
        synchronized (CACHE_LOCK) {
            if (profileCache != null && !TextUtils.isEmpty(username)) {
                //always use lower case in UserDatastore since CL-311
                username = username.toLowerCase();
                VersionedData<Profile> versionedDataProfile = profileCache.getData(username);

                // Look in persistent storage for the versioned data profile.
                if (versionedDataProfile == null) {
                    versionedDataProfile = loadProfileWithUsernameFromPersistentStorage(username);
                }

                return versionedDataProfile;
            }

            return null;
        }
    }

    private VersionedData<User> getVersionedDataUser(String username) {
        synchronized (CACHE_LOCK) {
            if (userCache != null && !TextUtils.isEmpty(username)) {
                //always use lower case in UserDatastore since CL-311
                username = username.toLowerCase();
                VersionedData<User> versionedDataUser = userCache.getData(username);

                // Look in persistent storage for the versioned data user.
                if (versionedDataUser == null) {
                    versionedDataUser = loadUserWithUsernameFromPersistentStorage(username);
                }

                return versionedDataUser;
            }

            return null;
        }
    }

    private List<User> getUsersWithCategory(final ProfileCategory.Type profileCategoryType, final String usernameOrId,
            final int offset, final int limit, final boolean shouldForceFetch, final boolean justLoadFromCache) {
        final List<Profile> profileList = getProfilesWithCategory(profileCategoryType, usernameOrId, offset, limit,
                shouldForceFetch, justLoadFromCache);

        List<User> resultList = getUserListFromProfileList(profileList);

        return resultList;
    }

    private List<User> getUsersOfCategory(final ProfileCategory.Type profileCategoryType, final String usernameOrId) {
        final List<Profile> profileList = getProfilesWithCategory(profileCategoryType, usernameOrId);

        List<User> resultList = getUserListFromProfileList(profileList);

        return resultList;
    }

    private  List<User> getUserListFromProfileList(List<Profile> profileList) {
        List<User> resultList = new ArrayList<User>();

        if (profileList != null && !profileList.isEmpty()) {
            for (Profile profile : profileList) {
                if (profile != null) {
                    final User user = getUserWithUsername(profile.getUsername(), false);
                    if (user != null) {
                        resultList.add(user);
                    }
                }
            }
        }

        return resultList;
    }

    public List<User> getFollowingUsersWithName(final String username, final int offset, final int limit,
            final boolean shouldForceFetch, final boolean justLoadFromCache) {
        return getUsersWithCategory(ProfileCategory.Type.FOLLOWING, username, offset, limit,
                shouldForceFetch, justLoadFromCache);
    }

    public List<User> getFollowerUsersWithName(final String username, final int offset, final int limit,
            final boolean shouldForceFetch, final boolean justLoadFromCache) {
        return getUsersWithCategory(ProfileCategory.Type.FOLLOWER, username, offset, limit,
                shouldForceFetch, justLoadFromCache);
    }

    public List<User> getUsersFromSearch(final String query, final int offset, final int limit,
            final boolean shouldForceFetch, final boolean justLoadFromCache) {
        return getUsersWithCategory(ProfileCategory.Type.SEARCH_USERS, query, offset, limit,
                shouldForceFetch, justLoadFromCache);
    }

    public List<User> getRecommendedUsers(final String userId, final int offset, final int limit,
                                          final boolean shouldForceFetch, final boolean justLoadFromCache) {
        return getUsersWithCategory(ProfileCategory.Type.RECOMMENDED_USERS, userId, offset, limit,
                shouldForceFetch, justLoadFromCache);
    }

    public List<User> getRecommendedContacts(final String userId, final int offset, final int limit,
            final boolean shouldForceFetch, final boolean justLoadFromCache) {
        return getUsersWithCategory(ProfileCategory.Type.RECOMMENDED_CONTACTS, userId, offset, limit,
                shouldForceFetch, justLoadFromCache);
    }

    /**
     * Gets a list of profiles matching a given category for a user.
     * 
     * @param profileCategoryType
     *            The category of the profiles to be retrieved.
     * @param usernameOrId
     *            The profile username or id of the user whose related
     *            categorized profiles are to be retrieved.
     * @param offset
     *            The offset from which the profiles are to be retrieved.
     * @param limit
     *            The limit imposed on the results retrieved.
     * @param shouldForceFetch
     *            Indicates whether the data should be force fetched from the
     *            server.
     * @return A list of profiles or null if no profiles matching the given
     *         category exist for the user in cache.
     */
    private List<Profile> getProfilesWithCategory(final ProfileCategory.Type profileCategoryType,
            final String usernameOrId, final int offset, final int limit, final boolean shouldForceFetch,
                                                  final boolean justLoadFromCache) {

        //no synchronized if we need to get data, otherwise ui thread may be blocked
        final List<Profile> resultList = new ArrayList<Profile>();
        if (usernameOrId != null && profileCategoryType != null) {

            final String key = ProfileCategory.getKey(profileCategoryType, usernameOrId);

            boolean shouldFetchFromServer = false;

            ProfileCategory category = null;
            VersionedData<ProfileCategory> data = profileCategoryCache.getData(key);

            if (data != null) {
                category = data.getData();
                shouldFetchFromServer = ((System.currentTimeMillis() > data.getLastUpdateTimestamp()
                        + PROFILECATEGORY_CACHE_EXPIRY) || data.isAltered());
            }

            if (category == null) {

                // Step1: No data in cache? Look in db.
                // Step2: If found data in db, then pull data from db into
                // cache
                // Step3: Begin recompute of cache
                // Step4: If any of the profiles were altered or removed in
                // the
                // cached data, set a flag locally
                // Step5: End recompute of cache
                // Step6: If local flag is set then fetch from server.

                final VersionedData<List<String>> persistedData = loadProfileUsernamesForCategoryFromPersistentStorage(key);
                List<String> profileUsernameList = null;
                if (persistedData != null) {
                    shouldFetchFromServer = (shouldFetchFromServer
                            || (System.currentTimeMillis() > persistedData.getLastUpdateTimestamp()
                                    + PROFILECATEGORY_CACHE_EXPIRY) || persistedData.isAltered());
                    profileUsernameList = persistedData.getData();
                }

                // Begin recompute of cache.
                if (profileUsernameList != null && !profileUsernameList.isEmpty()) {
                    List<Profile> profileList = new ArrayList<Profile>();
                    for (String username : profileUsernameList) {
                        final VersionedData<Profile> versionedDataProfile = getVersionedDataProfile(username);
                        if (!shouldFetchFromServer) {
                            shouldFetchFromServer = (versionedDataProfile == null
                                    || versionedDataProfile.isAltered() || versionedDataProfile.isRemoved());
                        }
                        if (versionedDataProfile != null && !versionedDataProfile.isRemoved()) {
                            profileList.add(versionedDataProfile.getData());
                        }
                    }

                    addProfilesToCategoryCache(usernameOrId, profileCategoryType, profileList, offset, limit, false);

                    data = profileCategoryCache.getData(key);
                    category = data.getData();
                } else {
                    shouldFetchFromServer = true;
                }
            }

            if (category != null) {
                // Use the profiles from the profile cache if possible.
                final List<String> usernameList = category.getUsernames();
                if (usernameList != null) {
                    for (String username : usernameList) {
                        Profile profileFromCache = getProfileFromCache(username);
                        ;
                        if (profileFromCache != null) {
                            resultList.add(profileFromCache);
                        }
                    }
                }

                if (!shouldFetchFromServer && limit > -1) {
                    int length = resultList.size();
                    if (length < limit && !category.isEnd()) {
                        shouldFetchFromServer = true;
                    }
                }
            }

            if (!justLoadFromCache && (shouldFetchFromServer || shouldForceFetch)) {
                requestCategorizedProfiles(usernameOrId, offset, limit, profileCategoryType);
            }

            Logger.debug.log(LOG_TAG, "Getting profiles for category: ", profileCategoryType.name(),
                    ". Did fetch from server: ", String.valueOf(shouldFetchFromServer && !justLoadFromCache));
        }

        return resultList;
    }

    private List<Profile> getProfilesWithCategory(final ProfileCategory.Type profileCategoryType, final String usernameOrId,
                                                  final int offset, final int limit, final boolean shouldForceFetch) {
        return  getProfilesWithCategory(profileCategoryType, usernameOrId, offset, limit, shouldForceFetch, false);
    }

    public List<User> getFollowingUsersWithName(final String username) {
        return getUsersOfCategory(ProfileCategory.Type.FOLLOWING, username);
    }

    public List<User> getFollowerUsersWithName(final String username) {
        return getUsersOfCategory(ProfileCategory.Type.FOLLOWER, username);
    }

    public List<User> getUsersFromSearch(final String query) {
        return getUsersOfCategory(ProfileCategory.Type.SEARCH_USERS, query);
    }

    public List<User> getRecommendedUsers(final String userId) {
        return getUsersOfCategory(ProfileCategory.Type.RECOMMENDED_USERS, userId);
    }

    public List<User> getRecommendedContacts(final String userId) {
        return getUsersOfCategory(ProfileCategory.Type.RECOMMENDED_CONTACTS, userId);
    }


    /**
    *   just return the profiles in the category cache
    * */
    private List<Profile> getProfilesWithCategory(final ProfileCategory.Type profileCategoryType, final String usernameOrId) {

        final List<Profile> resultList = new ArrayList<Profile>();
        if (usernameOrId != null && profileCategoryType != null) {

            final String key = ProfileCategory.getKey(profileCategoryType, usernameOrId);

            ProfileCategory category = null;
            VersionedData<ProfileCategory> data = profileCategoryCache.getData(key);

            if (data != null) {
                category = data.getData();
            }

            if (category != null) {
                // Use the profiles from the profile cache if possible.
                final List<String> usernameList = category.getUsernames();
                if (usernameList != null) {
                    for (String username : usernameList) {
                        Profile profileFromCache = getProfileFromCache(username);
                        if (profileFromCache != null) {
                            resultList.add(profileFromCache);
                        }
                    }
                }
            }
        }

        return resultList;
    }

    public List<Profile> getFollowingProfilesForUserWithName(final String username, final int offset, final int limit,
            final boolean shouldForceFetch) {
        return getProfilesWithCategory(ProfileCategory.Type.FOLLOWING, username, offset, limit, shouldForceFetch);
    }

    public List<Profile> getFollowerProfilesForUserWithName(final String username, final int offset, final int limit,
            final boolean shouldForceFetch) {
        return getProfilesWithCategory(ProfileCategory.Type.FOLLOWER, username, offset, limit, shouldForceFetch);
    }

    public List<Profile> getProfileFromSearch(final String query, final int offset, final int limit,
            final boolean shouldForceFetch) {
        return getProfilesWithCategory(ProfileCategory.Type.SEARCH_USERS, query, offset, limit, shouldForceFetch);
    }


    public boolean isFollowingListEnded(String usernameOrId) {
        return isProfileCategoryEnded(ProfileCategory.Type.FOLLOWING, usernameOrId);
    }

    public boolean isFollowerListEnded(String usernameOrId) {
        return isProfileCategoryEnded(ProfileCategory.Type.FOLLOWER, usernameOrId);
    }

    public boolean isSearchUsersListEnded(String usernameOrId) {
        return isProfileCategoryEnded(ProfileCategory.Type.SEARCH_USERS, usernameOrId);
    }

    public boolean isRecommendedUsersEnded(String usernameOrId) {
        return isProfileCategoryEnded(ProfileCategory.Type.RECOMMENDED_USERS, usernameOrId);
    }

    public boolean isRecommendedContactsEnded(String usernameOrId) {
        return isProfileCategoryEnded(ProfileCategory.Type.RECOMMENDED_CONTACTS, usernameOrId);
    }


    public boolean isProfileCategoryEnded(ProfileCategory.Type categoryType, String usernameOrId) {
        ProfileCategory category = getProfileCategory(categoryType, usernameOrId);
        if (category != null) {
            return category.isEnd();
        }

        return false;
    }

    private ProfileCategory getProfileCategory(ProfileCategory.Type categoryType, String usernameOrId) {
        final String key = ProfileCategory.getKey(categoryType, usernameOrId);

        ProfileCategory category = null;
        VersionedData<ProfileCategory> data = profileCategoryCache.getData(key);

        if (data != null) {
            category = data.getData();
        }

        return category;
    }


    /**
     * Gets a list of Badge for a given user. If the badges could not be found
     * in cache or were expired, then a request is sent to fetch them from the
     * server.
     * 
     * @param username
     *            The username of the user whose badges are to be retrieved.
     * @param shouldForceFetch
     *            Whether the data should be force fetched from the server.
     * @return A List containing the user's badges and null if the badges were
     *         not found in cache.
     */
    public List<Badge> getBadgesForUserWithName(String username, final boolean shouldForceFetch) {
        if (badgesCache != null) {
            if (username != null) {
                username = username.toLowerCase();
            }
            final List<Badge> badgesList = badgesCache.getData(username);
            if (badgesList == null || badgesCache.isExpired(username) || shouldForceFetch) {
                requestGetBadges(username, true);
            }

            return badgesList;
        }

        return null;
    }

    public List<PresetCoverPhoto> getMyPresetCoverPhotos(final boolean shouldForceFetch) {
        if (presetCoverPhotosCache != null) {
            final String myUsername = Session.getInstance().getUsername();
            if (!TextUtils.isEmpty(myUsername)) {
                final List<PresetCoverPhoto> presetCoverPhotosList = presetCoverPhotosCache.getData(myUsername);
                if (presetCoverPhotosList == null || presetCoverPhotosCache.isExpired(myUsername) || shouldForceFetch) {
                    requestGetMyPresetCoverPhotos();
                }

                return presetCoverPhotosList;
            }
        }

        return null;
    }
    
    /**
     * @return the number of new followers for the logged in user for visual indication.
     */
    public int getNewFollowersCount() {
        return newFollowersCount;
    }

    /**
     * Sets the new followers count for the logged in user.
     *
     * @param count The count to be set.
     */
    public void setNewFollowersCount(final int count) {

        newFollowersCount = count;

        if (newFollowersCount > 0) {
            //fetch my followers
            final int LOAD_MORE_INCREMENT = 20;
            UserDatastore.getInstance().getFollowerUsersWithName(Session.getInstance().getUsername(),
                    0, LOAD_MORE_INCREMENT, true, false);
            BroadcastHandler.Notification.sendNewFollowerUpdate();
        }
    }

    /**
     * Resets the number of new followers for the logged in user.
     */
    public void resetNewFollowersCount() {
        setNewFollowersCount(0);
    }

    /**
     * Gets the number of unlocked badges for a given user. This requires that
     * the badges were previously fetched with
     * {@link #getBadgesForUserWithName(String, boolean)}
     * 
     * @param username
     *            The username of the user whose badge count is to be retrieved.
     * @return The number of unlocked badges for the given user.
     */
    public int getUnlockedBadgesCounter(final String username) {
        final List<Badge> badges = UserDatastore.getInstance().getBadgesForUserWithName(username, false);
        int unlockedBadgesCounter = 0;
        if (badges != null) {
            for (Badge badge : badges) {
                if (badge.getUnlockedTimestamp() != null) {
                    ++unlockedBadgesCounter;
                }
            }
        }

        return unlockedBadgesCounter;
    }

    /**
     * Sets the presence of a friend matching the given contact id.
     * 
     * @param contactId
     *            The contact id of the friend whose presence is to be changed.
     * @param presence
     *            The new presence of the friend.
     */
    public void setPresenceForFriendWithContactId(final Integer contactId, final PresenceType presence) {
        final Friend friend = getFriendWithContactId(contactId);

        if (friend != null) {
            final PresenceType previousPresence = friend.getPresence();
            friend.setPresence(presence);

            // Update the group as well.
            final ContactGroup group = getContactGroupWithId(friend.getGroupID());
            if (group != null) {
                group.updatePresenceCountersForFriend(friend, previousPresence, false);
            }

            BroadcastHandler.Contact.sendPresenceChanged(friend.getGroupID(), friend.getUsername());
        }
    }

    /**
     * Sets the status message of a friend matching the given contact id.
     * 
     * @param contactId
     *            The contact id of the friend whose status message is to be
     *            changed.
     * @param statusMessage
     *            The new status message of the friend.
     */
    public void setStatusMessageForFriendWithContactId(final Integer contactId, final String statusMessage) {
        final Friend friend = getFriendWithContactId(contactId);

        if (friend != null) {
            friend.setStatusMessage(statusMessage);

            BroadcastHandler.Contact.sendStatusMessageChanged();
        }
    }

    /**
     * Sets the display pic guid of a friend matching the given contact id.
     * 
     * @param contactId
     *            The contact id of the friend whose display pic guid is to be
     *            changed.
     * @param guid
     *            The new display pic guid.
     */
    public void setDisplayPicGuidForFriendWithContactId(final Integer contactId, final String guid) {
        final Friend friend = getFriendWithContactId(contactId);

        if (friend != null) {
            friend.setGUID(guid);

            BroadcastHandler.Contact.sendDisplayPictureChanged();
        }
    }

    /**
     * Adds a ContactGroup to cache.
     * 
     * @param contactGroup
     *            The ContactGroup to be added.
     */
    private void addContactGroup(final ContactGroup contactGroup, final boolean shouldPersist) {
        if (contactGroup != null) {
            synchronized (CACHE_LOCK) {

                ContactGroup existingContactGroup = getContactGroupWithId(contactGroup.getGroupID());
                if (existingContactGroup != null) {
                    Set<Integer> friendsOfGroup = existingContactGroup.getFriendIds();
                    for (Integer friendId : friendsOfGroup) {
                        Friend friend = getFriendWithContactId(friendId);
                        if (friend != null) {
                            contactGroup.addFriend(friend);
                        }
                    }
                    contactGroupCache.put(contactGroup.getGroupID(), contactGroup);
                } else {
                    contactGroupCache.append(contactGroup.getGroupID(), contactGroup);
                }
            }

            if (shouldPersist) {
                saveContactGroupToPersistentStorage(contactGroup);
            }
        }
    }

    /**
     * Creates and adds a ContactGroup with a given groupId to cache. If there
     * is an exisiting ContactGroup with the given id, no new ContactGroup is
     * added.
     * 
     * @param groupId
     *            The id of the ContactGroup to be created.
     * @return The existing ContactGroup or the new one that was created.
     */
    private ContactGroup addContactGroupWithId(final int groupId, final boolean shouldPersist) {
        ContactGroup contactGroup = getContactGroupWithId(groupId);
        if (contactGroup == null) {
            // If there isn't already a contact group,
            // then create one.
            String groupName = Constants.BLANKSTR;
            for (FriendGroupTypeEnum groupType : FriendGroupTypeEnum.values()) {
                if (groupType.getGroupId() == groupId) {
                    groupName = groupType.getGroupName();
                    break;
                }
            }

            contactGroup = new ContactGroup(groupId, groupName);
        }

        addContactGroup(contactGroup, shouldPersist);
        return contactGroup;
    }

    /**
     * Removes a Contactgroup matching the given id from cache. Note that this
     * routine will not remove the ContactGroup if there are any friends in it.
     * 
     * @param groupId
     *            The id of the ContactGroup to be removed.
     * @param shouldPersist
     *            Indicates whether the cached contact groups should immediately
     *            be persisted to data storage.
     * @return The ContactGroup that was removed, null if the ContactGroup could
     *         not be found in cache or had friends in it.
     */
    private ContactGroup removeContactGroupWithId(final int groupId, final boolean shouldPersist) {
        synchronized (CACHE_LOCK) {
            final ContactGroup contactGroup = getContactGroupWithId(groupId);
            if (contactGroup != null && contactGroup.getFriendIds().isEmpty()) {
                contactGroupCache.delete(groupId);
                return contactGroup;
            }

            return null;
        }
    }

    /**
     * Adds a friend to the cache.
     * 
     * @param friend
     *            The friend to be added to the cache.
     * @param shouldPersist
     *            Indicates whether the cached friends should immediately be
     *            persisted to data storage.
     */
    private void addFriend(final Friend friend, final boolean shouldPersist) {
        synchronized (CACHE_LOCK) {
            if (friend != null && friendCache != null) {
                // Check whether the friend is already in the cache.
                // If yes, then first remove the friend that is already in the
                // cache
                final int contactId = friend.getContactID();
                final Friend existingFriend = getFriendWithContactId(friend.getContactID());
                if (existingFriend != null) {
                    removeFriendWithContactId(contactId, true);
                }

                // Add the friend...
                friendCache.put(contactId, friend);

                final int groupId = friend.getGroupID();
                ContactGroup group = getContactGroupWithId(groupId);

                if (group == null) {
                    group = addContactGroupWithId(groupId, true);
                }

                group.addFriend(friend);

                addUser(friend, shouldPersist);

                if (shouldPersist) {
                    saveFriendToPersistentStorage(friend);
                }
            }
        }
    }

    /**
     * Removes a friend with the specified contactId from cache.
     * 
     * @param contactId
     *            The contact id of the Friend to be removed.
     * @param shouldPersist
     *            Indicates that the cached friends should immediately be
     *            persisted to data storage.
     * @return The friend that was removed or null if the friend could not be
     *         found in cache.
     */
    private Friend removeFriendWithContactId(Integer contactId, final boolean shouldPersist) {
        final Friend friend = getFriendWithContactId(contactId);

        if (friend != null) {
            final int groupId = friend.getGroupID();
            ContactGroup group = getContactGroupWithId(groupId);
            if (group != null) {
                group.removeFriend(friend);
            }

            synchronized (CACHE_LOCK) {
                friendCache.delete(contactId);
            }
        }
        return friend;
    }

    public void removeIMContactsInMemory() {
        ArrayList<Integer> friendsIdsToRemove = new ArrayList<Integer>();
        synchronized (CACHE_LOCK) {
            // find all the IM contacts
            for (int i = 0; i < friendCache.size(); i++) {
                int key = friendCache.keyAt(i);
                Friend friend = friendCache.get(key);
                if (friend != null && !friend.isFusionContact()) {
                    friendsIdsToRemove.add(friend.getContactID());
                }
            }
            // delete all the IM contacts in memory
            for (int i = 0; i < friendsIdsToRemove.size(); i++) {
                Integer contactId = friendsIdsToRemove.get(i);
                removeFriendWithContactId(contactId, false);
            }
        }
    }

    /**
     * Adds a profile to the cache
     * 
     * @param profile
     *            The profile to be added to the cache.
     * @param shouldPersist
     *            Indicates whether the cached profiles should immediately be
     *            persisted to data storage.
     * @param shouldMergeExistingProfile
     *           some requests return the profile without all fields, we need to merge it
     *           with existing profile instead of simply replace it
     * @return The VersionedData Profile that was just added to the cache.
     */
    private VersionedData<Profile> addProfile(final Profile profile, final boolean isAltered,
            final boolean shouldPersist, boolean shouldMergeExistingProfile) {
        synchronized (CACHE_LOCK) {
            if (profileCache != null && profile != null && !TextUtils.isEmpty(profile.getUsername())) {
                //always use lower case in UserDatastore since CL-311
                profile.setUsername(profile.getUsername().toLowerCase());
                VersionedData<Profile> versionedDataProfile = new VersionedData<Profile>(profile);
                versionedDataProfile.setIsAltered(isAltered);

                if(shouldMergeExistingProfile) {
                    mergeWithExistingProfile(profile);
                }

                profileCache.cacheData(profile.getUsername(), versionedDataProfile);

                addUser(profile, isAltered, shouldPersist);
                if (shouldPersist) {
                    if (!saveProfileToPersistentStorage(versionedDataProfile)) {
                        Logger.error.log(LOG_TAG, "Failed to persist with saveProfileToPersistentStorage");
                    }
                }

                return versionedDataProfile;
            }

            return null;
        }
    }

    private void mergeWithExistingProfile(Profile profile) {
        String username = profile.getUsername();
        if (profileCache.getData(username) != null ) {
            Profile existingProfile = profileCache.getData(username).getData();
            profile.merge(existingProfile);
        }
    }

    /**
     * Adds a user to the cache.
     * 
     * @param friend
     *            The {@link Friend} data to be contained in the {@link User}.
     * @param shouldPersist
     *            Indicates whether the cached User should immediately be
     *            persisted to data storage.
     */
    private void addUser(final Friend friend, final boolean shouldPersist) {
        synchronized (CACHE_LOCK) {
            if (userCache != null && friend != null && friend.getUsername() != null) {
                //always use lower case in UserDatastore since CL-311
                final String username = friend.getUsername().toLowerCase();
                if (!TextUtils.isEmpty(username)) {
                    VersionedData<User> versionedDataUser = getVersionedDataUser(username);
                    if (versionedDataUser == null || versionedDataUser.getData() == null) {
                        final User user = new User(friend);
                        versionedDataUser = new VersionedData<User>(user);
                    } else {
                        final User user = versionedDataUser.getData();
                        user.setFriend(friend);
                    }

                    userCache.cacheData(username, versionedDataUser);

                    if (shouldPersist) {
                        if (!saveUserToPersistentStorage(versionedDataUser)) {
                            Logger.error.log(LOG_TAG, "Failed to persist with saveUserToPersistentStorage");
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a user to the cache.
     * 
     * @param profile
     *            The {@link Profile} data to be contained in the {@link User}.
     * @param isAltered
     *            Whether the {@link Profile} of the {@link User} was altered or
     *            not.
     * @param shouldPersist
     *            Indicates whether the cached User should immediately be
     *            persisted to data storage.
     */
    private void addUser(final Profile profile, final boolean isAltered, final boolean shouldPersist) {
        synchronized (CACHE_LOCK) {
            if (userCache != null && profile != null) {
                //always use lower case in UserDatastore since CL-311
                final String username = profile.getUsername().toLowerCase();
                if (!TextUtils.isEmpty(username)) {
                    VersionedData<User> versionedDataUser = getVersionedDataUser(username);
                    if (versionedDataUser == null || versionedDataUser.getData() == null) {
                        final User user = new User(profile);
                        versionedDataUser = new VersionedData<User>(user);
                    } else {
                        final User user = versionedDataUser.getData();
                        user.setProfile(profile);
                        versionedDataUser.setIsAltered(isAltered);
                    }

                    versionedDataUser.setIsAltered(isAltered);
                    userCache.cacheData(username, versionedDataUser);

                    if (shouldPersist) {
                        if (!saveUserToPersistentStorage(versionedDataUser)) {
                            Logger.error.log(LOG_TAG, "Failed to persist with saveUserToPersistentStorage");
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a list of categorized profiles to cache. The categorized list does
     * not have a limit or offset.
     * 
     * @param usernameOrId
     *            The username or id of the profile whose categorized profiles
     *            are to be cached.
     * @param profileCategoryType
     *            The {@link ProfileCategory.Type} that represents the category
     *            of profiles to be cached.
     * @param profileList
     *            A list containing the profiles to be cached.
     * @param shouldPersist
     *            Indicates whether the cached cotegorized profiles should
     *            immediately be persisted to data storage.
     */
    private void addProfilesToCategoryCache(final String usernameOrId, final ProfileCategory.Type profileCategoryType,
            final List<Profile> profileList, final boolean shouldPersist) {
        addProfilesToCategoryCache(usernameOrId, profileCategoryType, profileList, 0, -1, shouldPersist);
    }

    /**
     * Adds a list of categorized profiles to cache.
     * 
     * @param usernameOrId
     *            The username or id of the profile whose categorized profiles
     *            are to be cached.
     * @param profileCategoryType
     *            The {@link ProfileCategory.Type} that represents the category
     *            of profiles to be cached.
     * @param profileList
     *            A list containing the profiles to be cached.
     * @param offset
     *            The offset at which the profileList is to be cached.
     * @param limit
     *            The limit imposed on the cached profileList.
     * @param shouldPersist
     *            Indicates whether the cached cotegorized profiles should
     *            immediately be persisted to data storage.
     */
    private void addProfilesToCategoryCache(final String usernameOrId, final ProfileCategory.Type profileCategoryType,
            final List<Profile> profileList, final int offset, final int limit, final boolean shouldPersist) {

        synchronized (CACHE_LOCK) {
            if (profileCategoryType != null && profileList != null) {

                final String key = ProfileCategory.getKey(profileCategoryType, usernameOrId);

                VersionedData<ProfileCategory> data = profileCategoryCache.getData(key);
                ProfileCategory category = null;
                if (data != null) {
                    category = data.getData();
                } else {
                    data = new VersionedData<ProfileCategory>();
                }
                if (category == null) {
                    category = new ProfileCategory(profileCategoryType, usernameOrId);
                }

                if (offset == 0) {
                    category.clearProfiles();
                }

                for (Profile profile : profileList) {
                    //Sync profile cache
                    addProfile(profile, false, true, true);
                }
                category.addProfiles(offset, profileList);

                final int length = profileList.size();

                // If limit is -1. It means that there is nothing more to fetch.
                // We set limit to -1, in cases where there is no offset or
                // limit on the data to be fetched.
                category.setEnd(length < limit || limit < 0);

                data.setData(category);
                profileCategoryCache.cacheData(key, data);

                if (shouldPersist) {
                    if (!saveProfilesForCategoryToPersistentStorage(key, data)) {
                        Logger.error.log(LOG_TAG, "Failed to persist with saveProfilesForCategoryToPersistentStorage");
                    }
                }
            }
        }
    }

    /**
     * Sets the badges in cache for a given username
     * 
     * @param username
     *            The username of the user whose badges are to be cached.
     * @param badgesList
     *            A List containing the Badge to be cached.
     */
    private void setBadgesForUserWithName(String username, final List<Badge> badgesList) {
        if (badgesCache != null) {
            if (username != null) {
                username = username.toLowerCase();
            }
            badgesCache.cacheData(username, badgesList);
        }
    }

    private void setMyPresetCoverPhotos(final List<PresetCoverPhoto> presetCoverPhotosList) {
        if (presetCoverPhotosCache != null && !TextUtils.isEmpty(Session.getInstance().getUsername())) {
            presetCoverPhotosCache.cacheData(Session.getInstance().getUsername(), presetCoverPhotosList);
        }
    }

    /**
     * Called on LOGIN_OK. This datastore extracts and uses the data in the
     * packet for internal purposes.
     * 
     * @param fusionPktLoginOk
     *            The LOGIN_OK packet.
     */
    public void onLoginOkReceived(final FusionPktLoginOk fusionPktLoginOk) {
        NativeNotificationManager.getInstance().setIsLogined(true);
        currentContactListVersion = fusionPktLoginOk.getContactListVersion();
    }

    /**
     * Called whenever a CONTACT packet is received.
     * 
     * @param friend
     *            The Friend to be added.
     */
    public void onContactReceived(final Friend friend) {
        boolean shouldPersist = friend.isFusionContact();
        addFriend(friend, shouldPersist);
        BroadcastHandler.Contact.sendReceived(friend.getContactID());
    }

    /**
     * Called whenever a GROUP packet is received.
     * 
     * @param contactGroup
     *            The ContactGroup to be added.
     */
    public void onContactGroupReceived(final ContactGroup contactGroup) {
        addContactGroup(contactGroup, true);
        BroadcastHandler.ContactGroup.sendReceived(contactGroup.getGroupID());
    }

    /**
     * Called whenever a REMOVE_CONTACT is received.
     * 
     * @param contactId
     *            The contactId of the Friend to be removed.
     */
    public void onContactRemoved(final Integer contactId) {
        final Friend friend = removeFriendWithContactId(contactId, true);

        if (friend != null) {
            // AD-1115 Re-request get profile to sync friend status
            requestGetProfile(friend.getUsername());
            BroadcastHandler.Contact.sendRemoved(friend.getUsername());
        }
    }

    /**
     * Called whenever a REMOVE_GROUP is received.
     * 
     * @param groupId
     *            The id of the ContactGroup to be removed.
     */
    public void onContactGroupRemoved(final int groupId) {
        final ContactGroup contactGroup = UserDatastore.this.removeContactGroupWithId(groupId, true);
        if (contactGroup != null) {
            BroadcastHandler.ContactGroup.sendRemoved(contactGroup.getGroupName());
        }
    }

    // Network requests.
    
    /**
     * Sends a request to server to get all contacts.
     */
    public void requestContacts() {
        requestContacts(ApplicationEx.getInstance().getNetworkService());
    }
    
    /**
     * Sends a request to server to get all contacts.
     */
    public void requestContacts(NetworkService service) {
        
        RequestManager requestManager = service == null ? null : service.getRequestManager();
        if (requestManager != null) {
            final Session session = Session.getInstance();
            final Integer contactListVersion = session.getContactListVersion();
            final Long contactListTimestamp = session.getContactListTimestamp();

            if (currentContactListVersion.equals(contactListVersion) && contactListTimestamp != null) {
                requestManager.sendHaveLatestContactList(contactListTimestamp);
            } else {
                // Send request for full contact list.
                requestManager.sendGetContacts(new GetContactsListener() {

                    @Override
                    public void onContactReceived(final Friend friend) {
                        UserDatastore.this.onContactReceived(friend);
                    }

                    @Override
                    public void onGetContactsCompleteReceived() {
                        BroadcastHandler.Contact.sendFetchAllCompleted();
                    }

                    @Override
                    public void onGetContactListVersionReceived(Integer version, Long timestamp) {
                        if (version != null) {
                            session.setContactListVersion(version);
                        }
                        
                        if (timestamp != null) {
                            session.setContactListTimestamp(timestamp);
                        }
                        // No need to broadcast anything here.
                    }

                    @Override
                    public void onContactGroupReceived(ContactGroup contactGroup) {
                        UserDatastore.this.onContactGroupReceived(contactGroup);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.Contact.sendFetchAllError(error);
                    }

                    @Override
                    public void onLatestMessagesDigestReceived(FusionPktLatestMessagesDigest digestPkt) {
                        ChatController.getInstance().processLatestMessageDigest(digestPkt);
                    }
                });

                BroadcastHandler.Contact.sendBeginFetchAll();
            }
        }
    }

    /**
     * Sends a request to the server to add a new ContactGroup.
     * 
     * @param groupName
     *            The name of the ContactGroup to be added.
     */
    public void requestAddContactGroup(final String groupName) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendAddGroup(new NetworkResponseListener() {

                    @Override
                    public void onResponseReceived(MigResponse response) {
                        // Nothing to do here.
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.ContactGroup.sendAddError(error, groupName);
                    }
                }, groupName);
            }
        }
    }

    /**
     * Sends a request to the server to update the name of an existing
     * ContactGroup.
     * 
     * @param groupId
     *            The id of the ContactGroup to be updated.
     * @param newGroupName
     *            The new name of the ContactGroup.
     */
    public void requestUpdateContactGroup(final int groupId, final String newGroupName) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendUpdateGroup(new NetworkResponseListener() {

                    @Override
                    public void onResponseReceived(MigResponse response) {
                        // Nothing to do here.
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.ContactGroup.sendUpdateError(error, groupId, newGroupName);
                    }
                }, groupId, newGroupName);
            }
        }
    }

    /**
     * Sends a request to remove a contact group.
     * 
     * @param groupId
     *            The id of the ContactGroup to be removed.
     * @return false if a ContactGroup matching the given groupId could not be
     *         found or there are friends in the ContactGroup, true if the
     *         request was successfully sent.
     */
    public boolean requestRemoveContactGroup(final int groupId) {

        // Check whether a ContactGroup with the given id exists in cache and
        // that there are no friends in it.
        final ContactGroup contactGroup = getContactGroupWithId(groupId);
        synchronized (CACHE_LOCK) {
            if (contactGroup == null || !contactGroup.getFriendIds().isEmpty()) {
                return false;
            }

            final ApplicationEx appEx = ApplicationEx.getInstance();
            if (appEx != null) {
                RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    final String groupName = contactGroup.getGroupName();
                    requestManager.sendRemoveContactGroup(new RemoveGroupListener() {

                        @Override
                        public void onContactGroupRemoved(int groupId) {
                            UserDatastore.this.onContactGroupRemoved(groupId);
                        }

                        @Override
                        public void onError(MigError error) {
                            super.onError(error);
                            BroadcastHandler.ContactGroup.sendRemoveError(error, groupName, groupId);
                        }

                    }, groupId);
                }
            }

            return true;
        }
    }

    /**
     * Sends a request to the server to remove a contact.
     * 
     * @param contactId
     *            The contact id of the Friend to be removed.
     */
    public void requestRemoveFriend(final int contactId) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendRemoveContact(new RemoveContactListener() {

                    @Override
                    public void onContactRemoved(final Integer contactId) {
                        UserDatastore.this.onContactRemoved(contactId);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);

                        BroadcastHandler.Contact.sendRemoveError(error, contactId);
                    }

                }, contactId);
            }
        }
    }

    /**
     * Sends a request to the server to move a Friend to a different
     * ContactGroup
     * 
     * @param contactId
     *            The contactId of the Friend to be moved.
     * @param contactGroupId
     *            The groupId of the ContactGroup to be moved to.
     */
    public void requestMoveFriend(final int contactId, final int contactGroupId) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendMoveContact(new MoveContactListener() {

                    @Override
                    public void onContactMoved(Integer contactId, Integer contactGroupId) {
                        // Do nothing.
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.Contact.sendMoveError(error, contactId, contactGroupId);
                    }
                }, contactId, contactGroupId);
            }
        }
    }
    
    /**
     * Sends a request to fetch the Profile of a user with a given name.
     * 
     * @param username
     *            The username of the user whose Profile is to be fetched.
     */
    public void requestGetProfile(final String username) {
        requestGetProfile(ApplicationEx.getInstance().getNetworkService(), username);
    }

    /**
     * Sends a request to fetch the Profile of a user with a given name.
     * 
     * @param username
     *            The username of the user whose Profile is to be fetched.
     */
    public void requestGetProfile(NetworkService service, final String username) {
        requestGetProfile(service, username, false, null);
    }

    /**
     * Sends a request to fetch the Profile of a user with a given name. On
     * success, the {@link #followOrUnfollowUser(String, ActivitySourceEnum)}
     * function is called.
     * 
     * @param username
     *            The username of the user whose Profile is to be fetched.
     * @param shouldFollowOrUnfollow
     *            Whether the followOrUnfollow function should be invoked on
     *            success.
     * @param activitySource
     *            The source from where the follow/unfollow request originates.
     *            Can be null.
     */
    private void requestGetProfile(final String username, final boolean shouldFollowOrUnfollow,
            final ActivitySourceEnum activitySource) {
        requestGetProfile(ApplicationEx.getInstance().getNetworkService(), username, shouldFollowOrUnfollow,
                activitySource);
    }
    
    /**
     * Sends a request to fetch the Profile of a user with a given name. On
     * success, the {@link #followOrUnfollowUser(String, ActivitySourceEnum)}
     * function is called.
     * 
     * @param username
     *            The username of the user whose Profile is to be fetched.
     * @param shouldFollowOrUnfollow
     *            Whether the followOrUnfollow function should be invoked on
     *            success.
     * @param activitySource
     *            The source from where the follow/unfollow request originates.
     *            Can be null.
     */
    private void requestGetProfile(NetworkService service, final String username, final boolean shouldFollowOrUnfollow,
            final ActivitySourceEnum activitySource) {
        RequestManager requestManager = service == null ? null : service.getRequestManager();
        if (requestManager != null) {
            GetProfileListener getProfileListener = new GetProfileListener() {

                @Override
                public void onProfileReceived(final Profile profile) {
                    addProfile(profile, false, true, false);

                    Friend friend = findUser(username);

                    if (friend != null) {
                        friend.setProfile(profile);
                        boolean shouldPersist = friend.isFusionContact();
                        addFriend(friend, shouldPersist);
                    }

                    BroadcastHandler.Profile.sendReceived(profile.getUsername());

                    if (shouldFollowOrUnfollow) {
                        followOrUnfollowUser(username, activitySource);
                    }
                }

                @Override
                public void onError(MigError error) {
                    super.onError(error);
                    BroadcastHandler.Profile.sendFetchError(error, username);
                }
            };

            // for [non-login] users
            requestManager.sendGetProfileWithUsername(getProfileListener, username);
        }
    }

    public void requestGetMentionAutoCompleteList(final String userId) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.getMentionAutoCompleteList(new AutoCompleteListener() {

                    @Override
                    public void onMentionListReceived(int total, Profile[] profiles) {
                        if (profiles != null) {
                            ArrayList<String> mentionList = new ArrayList<>();
                            for (int i = 0; i < profiles.length; i++) {
                                mentionList.add(profiles[i].getUsername());
                            }

                            SystemDatastore.getInstance().setMentions(userId, mentionList);

                            BroadcastHandler.Profile.sendFetchMentionAutoCompleteCompleted();
                        }
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.Profile.sendFetchMentionAutocompleteError();
                    }
                }, userId);
            }
        }
    }

    /**
     * Sends a request to the server to fetch all Followers for given Profile
     * username.
     * 
     * @param username
     *            The username of the Profile whose followers are to be fetched.
     * @param offset
     *            The offset to fetch from (request parameter).
     * @param limit
     *            The limit of items to fetch (request parameter).
     */
    private void requestGetFollowers(final String username, final int offset, final int limit) {
        requestGetRelatedProfiles(ProfileCategory.Type.FOLLOWER, username, offset, limit);
    }

    /**
     * Sends a request to the server to fetch all Following for given Profile
     * username.
     * 
     * @param username
     *            The username of the Profile whose following profiles are to be
     *            fetched.
     * @param offset
     *            The offset to fetch from (request parameter).
     * @param limit
     *            The limit of items to fetch (request parameter).
     */
    private void requestGetFollowing(final String username, final int offset, final int limit) {
        requestGetRelatedProfiles(ProfileCategory.Type.FOLLOWING, username, offset, limit);
    }

    /**
     * Sends a request to the server to fetch all related Profiles for given
     * Profile username.
     * 
     * @param relationshipKey
     *            Determines the relationship of the profiles to be fetched.
     * @param username
     *            The username of the Profile whose related profiles are to be
     *            fetched.
     * @param offset
     *            The offset to fetch from (request parameter).
     * @param limit
     *            The limit of items to fetch (request parameter).
     */
    private void requestGetRelatedProfiles(final ProfileCategory.Type relationshipKey, final String username,
            final int offset, final int limit) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                final String userId = getUserIdFromUsername(username);

                if (!TextUtils.isEmpty(userId)) {
                    final ProfileRelationshipListener profileRelationshipListener = new ProfileRelationshipListener() {

                        @Override
                        public void onFollowingReceived(final Following following) {
                            if (following != null && following.getFollowing() != null) {
                                addProfilesToCategoryCache(username, ProfileCategory.Type.FOLLOWING,
                                        Arrays.asList(following.getFollowing()), offset, limit, true);

                                BroadcastHandler.Profile.sendFetchFollowingCompleted(username, offset, limit);
                            }
                        }

                        @Override
                        public void onFollowersReceived(final Followers followers) {
                            if (followers != null && followers.getFollowers() != null) {
                                addProfilesToCategoryCache(username, ProfileCategory.Type.FOLLOWER,
                                        Arrays.asList(followers.getFollowers()), offset, limit, true);

                                BroadcastHandler.Profile.sendFetchFollowersCompleted(username, offset, limit);
                            }
                        }

                        @Override
                        public void onError(MigError error) {
                            super.onError(error);

                            if (relationshipKey == ProfileCategory.Type.FOLLOWER) {

                                BroadcastHandler.Profile.sendFetchFollowersError(error, username, offset, limit);
                            } else if (relationshipKey == ProfileCategory.Type.FOLLOWING) {
                                BroadcastHandler.Profile.sendFetchFollowingError(error, username, offset, limit);
                            }
                        }
                    };

                    // Send the right request depending on what relationship is
                    // being requested for.
                    if (relationshipKey == ProfileCategory.Type.FOLLOWER) {
                        requestManager.sendGetFollowers(profileRelationshipListener, userId, offset, limit);
                    } else if (relationshipKey == ProfileCategory.Type.FOLLOWING) {
                        requestManager.sendGetFollowing(profileRelationshipListener, userId, offset, limit);
                    }
                } else {
                    Logger.error.log(LOG_TAG, "Could not fetch related profiles for profile with username: ", username,
                            ". Profile not found in cache!");
                }
            }
        }
    }

    /**
     * Follows or unfollows a user based on the relationship with that user.
     * 
     * @param username
     *            The username of the user to be be followed or unfollowed.
     * @param activitySource
     *            The source from where the follow request originates. Can be
     *            null.
     */
    public void followOrUnfollowUser(final String username, final ActivitySourceEnum activitySource) {
        final Profile otherProfile = getProfileWithUsername(username, true, activitySource, false, false);

        // If other profile is null, a request to fetch that profile has already
        // been sent.
        // So, there is no need to do it here.
        if (otherProfile != null) {
            final Relationship relationship = otherProfile.getRelationship();
            if (relationship != null && (relationship.isFollower())) {
                // I am already a follower, so unfollow.
                requestToUnfollowUser(username, activitySource);
            } else  {
                // I am not a follower, so follow.
                // If relationship is null, we follow also.
                requestToFollowUser(username, activitySource);
            }
        }
    }

    /**
     * Sends a POST request to follow a user with the given username.
     * 
     * @param username
     *            The username of the Profile to be followed.
     * @param activitySource
     *            The source from where the follow request originated. Can be
     *            null.
     */
    private void requestToFollowUser(final String username, final ActivitySourceEnum activitySource) {
        requestChangeRelationship(ProfileRelationshipChangeActionEnum.FOLLOW, username, activitySource);
    }

    /**
     * Sends a POST request to unfollow a user with the given username.
     * 
     * @param username
     *            The username of the Profile to be unfollowed.
     * @param activitySource
     *            The source from where the unfollow request originated. Can be
     *            null.
     */
    private void requestToUnfollowUser(final String username, final ActivitySourceEnum activitySource) {
        requestChangeRelationship(ProfileRelationshipChangeActionEnum.UNFOLLOW, username, activitySource);
    }

    /**
     * Sends a POST request to change relationship with a user.
     * 
     * @param changeAction
     *            A valid ProfileRelationshipChangeActionEnum
     * @param username
     *            The username of the Profile to change relationship with.
     * @param activitySource
     *            The source from where the request originated. Can be null.
     */
    private void requestChangeRelationship(final ProfileRelationshipChangeActionEnum changeAction,
            final String username, final ActivitySourceEnum activitySource) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                final String userId = getUserIdFromUsername(username);

                if (!TextUtils.isEmpty(userId)) {
                    final ChangeRelationshipListener changeRelationshipListener = new ChangeRelationshipListener() {

                        @Override
                        public void onFollowRequestCompleted(AddFollowingResultEnum addFollowingResult) {
                            if (addFollowingResult == AddFollowingResultEnum.FOLLOWED
                                    || addFollowingResult == AddFollowingResultEnum.ALREADY_FOLLOWING) {
                                Profile otherProfile = getProfileWithUsername(username, false);
                                if (otherProfile != null && otherProfile.getRelationship() != null) {
                                    otherProfile.getRelationship().setFollower(true);
                                }

                            } else if (addFollowingResult == AddFollowingResultEnum.PENDING_APPROVAL) {
                                Profile otherProfile = getProfileWithUsername(username, false);
                                if (otherProfile != null && otherProfile.getRelationship() != null) {
                                    otherProfile.getRelationship().setFollowerPendingApproval(true);
                                }
                            }

                            FriendsController.getInstance().onUserChangeRelationshipDone(username);

                            //add it in the mention auto complete list
                            SystemDatastore.getInstance().addMention(username);

                            BroadcastHandler.User.sendFollowed(addFollowingResult, username);
                        }

                        @Override
                        public void onUnfollowRequestCompleted(DeleteFollowingResultEnum deleteFollowingResult) {
                            if (deleteFollowingResult == DeleteFollowingResultEnum.UNFOLLOWED
                                    || deleteFollowingResult == DeleteFollowingResultEnum.NOT_FOLLOWING) {
                                Profile otherProfile = getProfileWithUsername(username, false);
                                if (otherProfile != null && otherProfile.getRelationship() != null) {
                                    otherProfile.getRelationship().setFollower(false);
                                }
                            }

                            FriendsController.getInstance().onUserChangeRelationshipDone(username);
                            BroadcastHandler.User.sendUnfollowed(deleteFollowingResult, username);
                        }

                        @Override
                        public void onError(MigError error) {
                            super.onError(error);
                            FriendsController.getInstance().onUserChangeRelationshipDone(username);
                            if (changeAction == ProfileRelationshipChangeActionEnum.FOLLOW) {
                                BroadcastHandler.User.sendFollowError(error, username);
                            } else if (changeAction == ProfileRelationshipChangeActionEnum.UNFOLLOW) {
                                BroadcastHandler.User.sendUnfollowError(error, username);
                            }
                        }
                    };

                    requestManager.sendChangeRelationship(changeRelationshipListener, changeAction, userId,
                            activitySource);
                }
            }
        }
    }
    
    private void requestGetMyPresetCoverPhotos() {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                BroadcastHandler.User.CoverPhoto.sendBeginFetchPresets();
                requestManager.sendGetMyPresetCoverPhotos(new GetPresetCoverPhotosListener() {

                    @Override
                    public void onPresetCoverPhotosResponseReceived(
                            PresetCoverPhoto[] presetCoverPhotosArr) {
                        if (presetCoverPhotosArr != null) {
                            setMyPresetCoverPhotos(Arrays.asList(presetCoverPhotosArr));
                        }
                        
                        BroadcastHandler.User.CoverPhoto.sendFetchPresetsCompleted();
                    }
                    
                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.User.CoverPhoto.sendFetchPresetsError(error);
                    }
                });
            }
        }
    }
    
    public void requestSetMyCoverPhoto(final String url) {
        if (!TextUtils.isEmpty(url)) {
            final ApplicationEx appEx = ApplicationEx.getInstance();
            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.sendSetMyPresetCoverPhoto(new SetCoverPhotoListener() {

                        @Override
                        public void onCoverPhotoSet(String url) {
                            Logger.debug.log(LOG_TAG, "Successfully set cover photo with url:", url);
                            BroadcastHandler.User.CoverPhoto.sendSetCompleted(url);
                        }
                        
                        @Override
                        public void onError(MigError error) {
                            super.onError(error);
                            
                            Logger.debug.log(LOG_TAG, "Failed to set cover photo:", url, " Reported error msg:", error.getErrorMsg());
                            BroadcastHandler.User.CoverPhoto.sendSetError(error, url);
                        }
                        
                    }, url);
                }
            }
        }
    }

    /**
     * Sends a request to fetch badges for a given user.
     * 
     * @param username
     *            The username of the user whose badges are to be fetched.
     * @param shouldShowAll
     *            Indicates whether all of the user's badges are to be fetched
     *            in one go.
     */
    private void requestGetBadges(final String username, final boolean shouldShowAll) {
        requestGetBadges(username, shouldShowAll, 0, -1);
    }

    private void requestGetBadges(final String username, final boolean shouldShowAll, final int offset, final int limit) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                final String userId = getUserIdFromUsername(username);

                if (!TextUtils.isEmpty(userId)) {
                    // Don't send the request if the user's profile could not be
                    // found.
                    requestManager.sendGetBadges(new GetBadgesListener() {

                        @Override
                        public void onBadgesReceived(final BadgesResponse badgesResponse) {
                            if (badgesResponse.getBadges() != null) {
                                setBadgesForUserWithName(username, Arrays.asList(badgesResponse.getBadges()));
                            }

                            BroadcastHandler.User.sendFetchBadgesCompleted(username);
                        }

                        @Override
                        public void onError(MigError error) {
                            super.onError(error);
                            BroadcastHandler.User.sendFetchBadgesError(error, username);
                        }

                    }, userId, shouldShowAll, offset, limit);
                } else {
                    Logger.error.log(LOG_TAG, "Profile for username ", username,
                            " could not be found when attempting to fetch badges.");
                }
            }
        }
    }

    /**
     * Sends a request to fetch the recommended people you may be interested in.
     * NOTE: This is not the same as fetching recommended address book contacts.
     * 
     * @param offset
     *            The offset from which results should be fetched.
     * @param limit
     *            The limit imposed on the results to be fetched.
     */
    private void requestGetRecommendedUsers(final String requestingUserId, final int offset, final int limit) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.getRecommendedUsers(recommendedUserListener, limit, offset, requestingUserId);
            }
        }
    }

    /**
     * Sends a request to fetch the recommended contacts. Recommended contacts
     * are mig33 users that are recommended based on the address book contacts
     * that were synced with the server.
     * 
     * @param offset
     *            The offset from which results should be fetched.
     * @param limit
     *            The limit imposed on the results to be fetched.
     */
    private void requestGetRecommendedContacts(final String requestingUserId, final int offset, final int limit) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.getRecommendedContacts(recommendedUserListener, limit, offset, requestingUserId);
            }
        }
    }

    /**
     * Sends a request to fetch search user results based on a query.
     * 
     * @param queryStr
     *            The search query
     * @param offset
     *            The offset from which the results should be fetched.
     * @param limit
     *            The limit imposed on the results to be fetched.
     */
    private void requestGetUsersFromSearch(String queryStr, int offset, int limit) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                int page = Tools.convertOffsetToPage(offset, limit);
                requestManager.searchForUser(searchListener, queryStr, limit, page);
            }
        }
    }



    /**
     * Sends a request to set the display picture of the user.
     * 
     * @param guid
     *            The guid of the picture to be sent.
     */
    public void requestSetDisplayPicture(final String guid) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendSetDisplayPicture(new SimpleResponseListener() {

                    @Override
                    public void onSuccess(MigResponse response) {
                        BroadcastHandler.User.sendDisplayPictureSet();
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.User.sendSetDisplayPictureError(error);
                    }
                }, guid);
            }
        }
    }

    public void requestUploadProfilePhoto(byte[] imageData) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendUploadProfilePhoto(new SimpleResponseListener() {

                    @Override
                    public void onSuccess(MigResponse response) {
                        BroadcastHandler.User.sendDisplayPictureSet();
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.User.sendSetDisplayPictureError(error);
                    }
                }, imageData, true);
            }
            ;
        }
    }

    public void requestUploadPhotoToPhotoAlbum(byte[] imageData) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendPhotoToPhotoAlbum(new SimpleResponseListener() {

                    @Override
                    public void onSuccess(MigResponse response) {
                        BroadcastHandler.User.sendUploadedToPhotoAlbum();
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.User.sendUploadToPhotoAlbumError(error);
                    }
                }, imageData);
            }
            ;
        }
    }

    /**
     * Loads all data from persistent storage into cache.
     */
    private void loadFromPersistentStorage() {
        // First load the contact groups, then load the contacts.
        if (contactGroupsDAO != null) {
            contactGroupCache = contactGroupsDAO.loadContactGroupsFromDatabase();
        }

        if (contactsDAO != null) {
            final SparseArray<Friend> friendsSparseArray = contactsDAO.loadFriends();
            for (int i = 0; i < friendsSparseArray.size(); ++i) {
                Friend friend = friendsSparseArray.valueAt(i);
                addFriend(friend, false);
            }
        }
    }

    private VersionedData<Profile> loadProfileWithUsernameFromPersistentStorage(final String username) {
        return contactsDAO.loadProfile(username);
    }

    private VersionedData<User> loadUserWithUsernameFromPersistentStorage(final String username) {
        return contactsDAO.loadUser(username);
    }

    private VersionedData<List<String>> loadProfileUsernamesForCategoryFromPersistentStorage(final String key) {
        return null;
    }

    private boolean saveProfileToPersistentStorage(final VersionedData<Profile> versionedDataProfile) {
        return contactsDAO.saveProfile(versionedDataProfile);
    }

    private boolean saveUserToPersistentStorage(final VersionedData<User> versionedDataUser) {
        return contactsDAO.saveUser(versionedDataUser);
    }

    private boolean saveProfilesForCategoryToPersistentStorage(final String key,
            VersionedData<ProfileCategory> versionedDataCategory) {
        return true;
    }

    /**
     * Persists a given Friend to database.
     * 
     * @param friend
     *            The Friend to persist.
     * @return true on success and false otherwise
     */
    private boolean saveFriendToPersistentStorage(final Friend friend) {
        if (contactsDAO != null) {
            return contactsDAO.saveFriend(friend);
        }

        return false;
    }

    /**
     * Persists a given ContactGroup to database.
     * 
     * @param contactGroup
     *            The ContactGroup to persist.
     * @return true on success and false otherwise.
     */
    private boolean saveContactGroupToPersistentStorage(final ContactGroup contactGroup) {
        if (contactGroupsDAO != null) {
            return contactGroupsDAO.saveContactGroupToDatabase(contactGroup);
        }

        return false;
    }

    public boolean hasFriend() {
        boolean groupFusionFriends = true;
        List<ContactGroup> contactGroups = getContactGroups(Config.getInstance().isImEnabled(), groupFusionFriends);
        for(ContactGroup group : contactGroups) {
            Set<Integer> userIdSet = group.getFriendIds();
            if (userIdSet != null && userIdSet.size() > 0 ) {
                return true;
            }
        }
        return false;
    }

}
