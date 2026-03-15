/**
 * Mig33 Pte. Ltd.
 *
 * Copyright (c) 2012 mig33. All rights reserved.
 */

package com.projectgoth.datastore;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Labels;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.enums.UserLabelAdminEnum;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.PasswordTypeEnum;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.blackhole.fusion.packet.FusionPktImAvailable;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Theme;
import com.projectgoth.common.Tools;
import com.projectgoth.common.Version;
import com.projectgoth.events.AppEvents;
import com.projectgoth.fusion.packet.UrlHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.IMItem;
import com.projectgoth.nemesis.utils.ConnectionSession;
import com.projectgoth.service.NetworkService;
import com.projectgoth.util.AndroidLogger;


/**
 * Session.java
 * 
 * @author warrenbalcos on Jun 4, 2013
 * 
 */
public class Session extends ConnectionSession {

    private static final String  TAG                     = AndroidLogger.makeLogTag(Session.class);

    private static final String  USERNAME_KEY            = "USERNAME_KEY";
    private static final String  PASSWORD_KEY            = "PASSWORD_KEY";
    private static final String  PASSWORD_TYPE_KEY       = "PASSWORD_TYPE_KEY";
    private static final String  SERVICE_ACTIVE_KEY      = "SERVICE_ACTIVE_KEY";
    public static final String   PROFILE_PIC_GUID_KEY    = "PROFILE_PIC_GUID_KEY";
    public static final String   AVATAR_GUID_KEY         = "AVATAR_GUID_KEY";

    private static final String  USER_ID_KEY             = "USER_ID_KEY";

    private static final String  FACEBOOK_SESSION_ID_KEY = "FACEBOOK_SESSION_ID_KEY";

    public static final String   CONTACTLIST_VERSION     = "CONTACTLIST_VERSION";
    public static final String   CONTACTLIST_TIMESTAMP   = "CONTACTLIST_TIMESTAMP";

    public static final String   ACCOUNT_BALANCE         = "ACCOUNT_BALANCE";
    public static final String   LOGIN_STATUS_KEY        = "USER_LOGIN_STATUS_KEY";
    public static final String   LOGIN_LAST_STATUS_KEY   = "USER_LOGIN_LAST_STATUS_KEY";

    private String               sessionId;

    private PresenceType         presence                = PresenceType.AVAILABLE;
    private String               statusMessage           = Constants.BLANKSTR;
    private short                migLevel                = 0;
    private String               migLevelImageUrl        = Constants.BLANKSTR;

    private boolean              isFirstTimeUserLogin    = false;
    private boolean              showWelcomeScreen       = false;
    private boolean              isStickerSupported      = false;
    private boolean              showOnlineFriendsOnly   = false;
    private boolean              isNetworkConnected      = false;

    private Uri                  deepLinkUri;

    private Map<ImType, IMItem>  imList                  = new HashMap<ImType, IMItem>();

    /**
     * Cookie sync manager
     */
    private CookieSyncManager    cookieSyncManager;

    /**
     * Cookie manager
     */
    protected CookieManager      cookieManager;

    private long                 serverTimeOnLogin       = System.currentTimeMillis();
    private long                 clientTimeOnLogin       = System.currentTimeMillis();

    // username, password, sessionID, migBo userId, facebook session Id, user
    // details (account balance, verified, presence, status, etc.)

    private SystemDatastore      mSystemDS               = SystemDatastore.getInstance();



    private String               profilePicGuid;
    private String               avatarPicGuid;

    private String               mPassword;

    public static int            STATUS_LOGGING_IN       = 3;
    public static int            STATUS_LOG_IN           = 1;
    public static int            STATUS_LOG_OUT          = 0;

    private int                  loginStatus             = STATUS_LOG_OUT;

    private Session() {
    }

    private static class SessionHolder {
        static final Session sINSTANCE = new Session();
    }

    public static Session getInstance() {
        return SessionHolder.sINSTANCE;
    }

    public void init(ApplicationEx appEx) {
        this.cookieSyncManager = CookieSyncManager.createInstance(appEx);
        this.cookieManager = CookieManager.getInstance();
        this.cookieManager.setAcceptCookie(true);
        this.cookieManager.removeSessionCookie();
    }

    public int getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatusLogOut() {
        loginStatus = STATUS_LOG_OUT;
        storeLoginStatus();
    }

    public void setLoginStatusLogin() {
        loginStatus = STATUS_LOG_IN;
        storeLoginStatus();
    }

    public void setLoginStatusLogging() {
        loginStatus = STATUS_LOGGING_IN;
        storeLoginStatus();
    }

    private void storeLoginStatus() {
        mSystemDS.saveData(LOGIN_STATUS_KEY, loginStatus);
    }

    public void storeLastLoginStatus(int status) {
        mSystemDS.saveData(LOGIN_LAST_STATUS_KEY, loginStatus);
    }

    public int getLastLoginStatus() {
        int ret = mSystemDS.getIntegerData(LOGIN_LAST_STATUS_KEY);
        return ret < 0 ? STATUS_LOG_OUT : ret;
    }

    public boolean hasLoggedIn() {
        int lastStatus = getLastLoginStatus();
        return lastStatus == STATUS_LOG_IN;
    }

    public boolean isLoggedIn() {
        int status = mSystemDS.getIntegerData(LOGIN_STATUS_KEY);
        return status == STATUS_LOG_IN;
    }

    public boolean isLogin() {
        return loginStatus == STATUS_LOG_IN;
    }

    public boolean isBlockUsers(){
        if (getLoginStatus() != STATUS_LOG_IN && !hasLoggedIn()) {
            return true;
        } else {
            return false;
        }
    }

    // means connect to fusion service.
    public boolean isNetworkConnected(){
        return this.isNetworkConnected;
    }

    public void setNetworkConnected(Boolean isNetworkConnected) {
        this.isNetworkConnected = isNetworkConnected;
    }

    /**
     * Get the session identifier. Always return NOT url encoded session id.
     * 
     * @return string
     */
    public String getId() {
        String eid = getCookieSessionId();
        if (TextUtils.isEmpty(eid)) {
            if (isFacebookSession()) {
                eid = getFacebookSessionId();
            } else {
                eid = sessionId;
            }
            setCookieSessionId(eid);
        }
        return eid;
    }

    /**
     * Get the session identifier from the cookie.
     * 
     * @return string
     */
    private String getCookieSessionId() {
        String eid = null;
        String cookies = cookieManager.getCookie(UrlHandler.getInstance().getUrlPrefix());
        if (cookies != null) {
            int eidPos = cookies.indexOf("eid=");
            if (eidPos > -1) {
                int end = cookies.indexOf(";", eidPos);
                try {
                    if (end == -1) {
                        eid = URLDecoder.decode(cookies.substring(eidPos + 4), Constants.DEFAULT_ENCODING);
                    } else {
                        eid = URLDecoder.decode(cookies.substring(eidPos + 4, end), Constants.DEFAULT_ENCODING);
                    }
                } catch (UnsupportedEncodingException e) {
                    Logger.error.log(TAG, e);
                }
            }
        }
        return eid;
    }

    /**
     * Set the cookie session identifier
     * 
     * @param id
     */
    public void setCookieSessionId(String id) {
        String url = getUrlPrefix();

        if (id == null || !id.trim().equalsIgnoreCase("deleted")) {
            if (!TextUtils.isEmpty(id)) {
                try {
                    String eid = "eid=" + URLEncoder.encode(id, Constants.DEFAULT_ENCODING) + getCookiePrefix();
                    Logger.debug.log(TAG, "setCookieSessionId: ", url, " eid:", eid);
                    this.cookieManager.setCookie(url, eid);
                } catch (UnsupportedEncodingException e) {
                    Logger.error.log(TAG, e);
                }
            }
        }

        this.cookieManager.setCookie(url, "theme=" + Theme.getName() + getCookiePrefix());
        // TODO: handle language cookie
        this.cookieManager.setCookie(url, "lang=en-US" + getCookiePrefix());
        CookieSyncManager.getInstance().sync();
    }

    /**
     * @return the facebookSessionId
     */
    public String getFacebookSessionId() {
        return mSystemDS.getStringData(FACEBOOK_SESSION_ID_KEY);
    }

    /**
     * @param facebookSessionId
     *            the facebookSessionId to set
     */
    public void setFacebookSessionId(String facebookSessionId) {
        mSystemDS.saveData(FACEBOOK_SESSION_ID_KEY, facebookSessionId);
    }

    public boolean canLogin() {
        return getPasswordType() == PasswordTypeEnum.FACEBOOK_IM.value()
                || (!TextUtils.isEmpty(getUsername()) && !TextUtils.isEmpty(getPassword()));
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return mSystemDS.getStringData(USERNAME_KEY);
    }

    /**
     * @param username
     *            the username to se、
     */
    public void setUsername(String username) {
        // store sanitized value
        mSystemDS.saveData(USERNAME_KEY, username.trim().toLowerCase());
    }

    /**
     * @return the password
     */
    public String getPassword() {
        if (TextUtils.isEmpty(mPassword)) {  //for auto-login
            mPassword = getPasswordInLocal();
        }
        return mPassword;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        mPassword = password;
    }

    public void savePasswordToLocal() {
        if (!TextUtils.isEmpty(mPassword)) {
            mSystemDS.saveData(PASSWORD_KEY, mPassword.trim());
        } else {
            mSystemDS.saveData(PASSWORD_KEY, "");
        }
    }

    public void clearPasswordInLocal() {
        mSystemDS.saveData(PASSWORD_KEY, "");
    }

    public String getPasswordInLocal() {
        return mSystemDS.getStringData(PASSWORD_KEY);
    }

    /**
     * Checks whether the given username is the same as the currently logged in
     * user.
     * 
     * @param username
     *            the username to be checked.
     * @return true if the given username matches the currently logged in user
     *         and false otherwise.
     */    
    public boolean isSelfByUsername(String username) {
        return (!TextUtils.isEmpty(getUsername()) && 
                !TextUtils.isEmpty(username) && 
                this.getUsername().equalsIgnoreCase(username));
    }
    
    /**
     * @param userId
     *          the userid to check
     * @return boolean if its own/self user
     */
    public boolean isSelfByUserId(String userId) {
        return this.getUserId().equals(userId) ? true : false;
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        if (isFacebookSession()) {
            this.sessionId = getFacebookSessionId();
        }
        return sessionId;
    }

    /**
     * Set id from Fusion service.
     */
    public void setSessionId(String sessionId) {
        Logger.debug.log(TAG, "setSessionId: ", sessionId);
        this.sessionId = sessionId;
        if (!TextUtils.isEmpty(sessionId) && isFacebookSession()) {
            setFacebookSessionId(sessionId);
        }
        setCookieSessionId(sessionId);
    }

    public boolean isFacebookSession() {
        return getPasswordTypeEnum() == PasswordTypeEnum.FACEBOOK_IM;
    }

    /**
     * @return the serviceActive
     */
    public boolean isServiceActive() {
        return mSystemDS.getBooleanData(SERVICE_ACTIVE_KEY);
    }

    /**
     * @param serviceActive
     *            the serviceActive to set
     */
    public void setServiceActive(boolean serviceActive) {
        mSystemDS.saveData(SERVICE_ACTIVE_KEY, serviceActive);
    }

    public void setPasswordType(int passwordType) {
        PasswordTypeEnum type = PasswordTypeEnum.fromValue(passwordType);
        if (type == null) {
            type = PasswordTypeEnum.FUSION;
        }
        mSystemDS.saveData(PASSWORD_TYPE_KEY, type.value());
    }

    public int getPasswordType() {
        return mSystemDS.getIntegerData(PASSWORD_TYPE_KEY);
    }

    public PasswordTypeEnum getPasswordTypeEnum() {
        Integer v = getPasswordType();
        if (v != null) {
            return PasswordTypeEnum.fromValue(v);
        }
        return PasswordTypeEnum.FUSION;
    }

    public void clearSession() {

        // [non-login] clear account data
        sessionId = null;
        this.cookieManager.removeAllCookie();
        isFirstTimeUserLogin = false;

    }

    public long getServerTimeOnLogin() {
        return serverTimeOnLogin;
    }

    public void setServerTimeOnLogin(long serverTimeOnLogin) {
        this.serverTimeOnLogin = serverTimeOnLogin;
    }

    public long getClientTimeOnLogin() {
        return clientTimeOnLogin;
    }

    public void setClientTimeOnLogin(long clientTimeOnLogin) {
        this.clientTimeOnLogin = clientTimeOnLogin;
    }

    /**
     * Need to call syncCookies after set all the cookies.
     * 
     * @param url
     * @param value
     */
    public void setCookie(String url, String value) {
        cookieManager.setCookie(url, value);
    }

    @Override
    public void syncCookies() {
        cookieSyncManager.sync();
    }

    @Override
    public String getUserAgent() {
        return Version.getUserAgent();
    }

    public String getCookiesForHTTPHeader(String url) {
        return cookieManager.getCookie(url);
    }

    /**
     * @return the cookiePrefix
     */
    public String getCookiePrefix() {
        return UrlHandler.getInstance().getCookiePrefix();
    }

    /**
     * @return the urlPrefix
     */
    public String getUrlPrefix() {
        return UrlHandler.getInstance().getUrlPrefix();
    }

    public void setLanguageId() {
        // TODO:
        String id = getId();
        // Tools.clearCache(appEx);
        if (!TextUtils.isEmpty(id)) {

            try {
                this.cookieManager.setCookie(getUrlPrefix(), "eid=" + URLEncoder.encode(id, Constants.DEFAULT_ENCODING)
                        + getCookiePrefix());
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        this.cookieManager.setCookie(getUrlPrefix(), "theme=" + Theme.getName() + getCookiePrefix());
        this.cookieManager.setCookie(getUrlPrefix(), "lang=" + I18n.getLanguageID() + getCookiePrefix());
        CookieSyncManager.getInstance().sync();
    }

    public String getUserId() {
        Integer data = mSystemDS.getIntegerData(USER_ID_KEY);
        if (data != null) {
            return String.valueOf(data);
        }
        // TODO: implement this, and get the user details and other data from
        // the pushed packets from login.
        return null;
    }

    public void setUserId(Integer id) {
        mSystemDS.saveData(USER_ID_KEY, id);
    }

    @Override
    public PresenceType getPresence() {
        return presence;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public short getMigLevel() {
        return migLevel;
    }

    public String getMigLevelImageUrl() {
        return migLevelImageUrl;
    }

    /**
     * @param presence
     *            the presence to set
     */
    public void setPresence(final PresenceType presence) {
        this.presence = presence;
    }

    public void setStatusMessage(final String message) {
        statusMessage = message;
    }

    public void setMigLevel(final Short migLevel) {
        if (migLevel != null) {
            this.migLevel = migLevel;
        }
    }

    public void setMigLevelImageUrl(final String migLevelImageUrl) {
        Logger.debug.log(TAG, "mig level URL: ", migLevelImageUrl);
        if (migLevelImageUrl != null) {
            this.migLevelImageUrl = Tools.constructFullMigLevelImageURL(migLevelImageUrl, -1);
        }
    }

    public boolean getIsStickerSupported() {
        return isStickerSupported;
    }

    public void setIsStickerSupported(final boolean state) {
        isStickerSupported = state;
    }

    public Integer getContactListVersion() {
        return mSystemDS.getContactListVersion();
    }

    public void setContactListVersion(final Integer version) {
        mSystemDS.setContactListVersion(version);
    }

    public Long getContactListTimestamp() {
        return mSystemDS.getContactListTimestamp();
    }

    public void setContactListTimestamp(final Long timestamp) {
        mSystemDS.setContactListTimestamp(timestamp);
    }

    public String getAccountBalance() {
        return mSystemDS.getAccountBalance();
    }

    public void setAccountBalance(final String balance) {
        mSystemDS.setAccountBalance(balance);
    }

    public boolean isGlobalAdmin() {
        boolean result = false;
        String username = Session.getInstance().getUsername();
        Profile profile = UserDatastore.getInstance().getProfileWithUsername(username, false);
        if (profile != null) {
            Labels labels = profile.getLabels();
            if (labels != null && labels.getAdmin() == UserLabelAdminEnum.GLOBAL_ADMIN) {
                result = true;
            }
        }
        return result;
    }
    
    public boolean isNewlyRegisteredUser() {
        //+ TODO: this needs to be implemented in a better way.
        return getMigLevel() < 2;
    }

    public boolean getIsFirstTimeUserLogin() {
        return isFirstTimeUserLogin;
    }

    public void setIsFirstTimeUserLogin(final boolean state) {
        isFirstTimeUserLogin = state;
    }

    public boolean showWelcomeScreen() {
        return showWelcomeScreen;
    }

    public void setShowWelcomeScreen(final boolean state) {
        showWelcomeScreen = state;
    }

    /**
     * @param profilePicGuid
     */
    public void setProfilePicGuid(String profilePicGuid) {
        this.profilePicGuid = profilePicGuid;
        SystemDatastore.getInstance().saveData(PROFILE_PIC_GUID_KEY, profilePicGuid);
    }

    /**
     * @param avatarPicGuid
     */
    public void setAvatarPicGuid(String avatarPicGuid) {
        this.avatarPicGuid = avatarPicGuid;
        SystemDatastore.getInstance().saveData(AVATAR_GUID_KEY, avatarPicGuid);
    }

    public String getDisplayableGuid() {
        String result = getProfilePicGuid();
        if (TextUtils.isEmpty(result)) {
            result = getAvatarPicGuid();
        }
        return result;
    }

    public String getProfilePicGuid() {
        profilePicGuid = SystemDatastore.getInstance().getStringData(PROFILE_PIC_GUID_KEY);
        return profilePicGuid;
    }

    public String getAvatarPicGuid() {
        avatarPicGuid = SystemDatastore.getInstance().getStringData(AVATAR_GUID_KEY);
        return avatarPicGuid;
    }

    public boolean showOnlineFriendsOnly() {
        return showOnlineFriendsOnly;
    }

    public void setShowOnlineFriendsOnly(final boolean state) {
        showOnlineFriendsOnly = state;
    }
    
    
    public void setIMItem(ImType imtype,FusionPktImAvailable imAvailPacket) {
        if(imAvailPacket != null) {
            imList.put(imtype, new IMItem(imAvailPacket));
        }
    }
    
    public IMItem getIMItem(ImType imtype) {
        return imList.get(imtype);
    }
    
    public Map<ImType, IMItem> getAllIMItem() {
        return imList;
    }
    
    public Uri getDeepLinkUri() {
        return deepLinkUri;
    }

    public void setDeepLinkUri(Uri deepLinkUri) {
        this.deepLinkUri = deepLinkUri;
    }
    
}
