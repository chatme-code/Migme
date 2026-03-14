/**
 * Copyright (c) 2013 Project Goth
 *
 * FacebookLoginController.java
 * Created Oct 4, 2013, 3:21:17 PM
 */

package com.projectgoth.controller;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.LoginResponse;
import com.projectgoth.b.enums.PasswordTypeEnum;
import com.projectgoth.common.Config;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.NetworkResponseListener;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.nemesis.model.MigResponse;
import com.projectgoth.ui.activity.ActionHandler;

import java.util.Arrays;
import java.util.List;


/**
 * @author sarmadsangi
 *
 */
public class FacebookLoginController {
    public static final String FACEBOOK_PACKAGE_NAME = "com.facebook.katana";
    private com.facebook.Session.StatusCallback statusCallback = new SessionStatusCallback();
    public static final List<String> permissions = Arrays.asList(new String[]{"public_profile", "email"});
    private Context context;
    private GraphUser mUser;
    
    private final static FacebookLoginController INSTANCE = new FacebookLoginController();

    /**
     * Constructor
     */
    private FacebookLoginController() {
    }

    /**
     * A single point of entry for this controller.
     * 
     * @return An instance of the controller.
     */
    public static synchronized FacebookLoginController getInstance() {
        return INSTANCE;
    }
    
    public void setControllerContext(Context context) {
        this.context = context;
    }
    
    private NetworkResponseListener  ssoLoginResponseListener  = new NetworkResponseListener() {
        @Override
        protected void onResponseReceived(MigResponse response) {
            LoginResponse login = new Gson().fromJson(response.getResponseData().toString(), LoginResponse.class);
            if(login.getSessionId() != null) {
                ActionHandler.getInstance().startSSOLogin(PasswordTypeEnum.FACEBOOK_IM.value(), login.getSessionId(), true);
            }
        }

        public void onError(MigError error) {
            if (error != null && error.isUserCredentialError()) {
                BroadcastHandler.Login.Facebook.sendAccountLinkError();
            } else {
                BroadcastHandler.Login.Facebook.sendError();
            }
        }
   };
   
   /**
    * To be used on login screen. This initiates FB Session.
    * @param savedInstanceState
    */
   public void initializeFBSession(Bundle savedInstanceState) {
       Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
       Session session = null;
       if (!Config.getInstance().isDebug()) {
           session = Session.getActiveSession();
       }
       if (session == null) {
           if (savedInstanceState != null) {
               session = Session.restoreSession(this.context, null, statusCallback, savedInstanceState);
           }
           if (session == null) {
               String FbAppId = Config.getInstance().getConnectionDetail().getFacebookAppId();
               session = new Session.Builder(context).setApplicationId(FbAppId).build();
           }
           Session.setActiveSession(session);
           if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
               session.openForRead(new Session.OpenRequest((Activity) this.context).setCallback(statusCallback));
           }
       }
   }
   
   /**
    * To get authenticated Session object. It is important that this controller
    * be called in an activity and has onActivityResult 
    * com.facebook.Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    * 
    * Else session object to this callback will be empty.
    * @author sarmadsangi
    *
    */
   private class SessionStatusCallback implements Session.StatusCallback {
       @Override
       public void call(Session session, SessionState state, Exception exception) {
           onSessionStateChange(session, state, exception);
       }
   }
   
   private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
       if (session.isOpened()) {
           
           BroadcastHandler.Login.Facebook.sendInitialized();
           Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if(user != null) {
                        mUser = user;
                        sendSSOLoginRequest(user.getId(), session.getAccessToken(), PasswordTypeEnum.FACEBOOK_IM.value());
                     } else {
                        mUser = null;
                        BroadcastHandler.Login.Facebook.sendError();
                     }
                }
           }).executeAsync();
       }
   }
   
   
   public void sendSSOLoginRequest(String username,String accessToken,int passwordType) {
       try {
             RequestManager requestManager = ApplicationEx.getInstance().getNetworkService().getRequestManager();
             requestManager.sendSSOLoginRequest(ssoLoginResponseListener, username, accessToken, passwordType, true);  
       } catch (Exception e) {
           // IGNORE NULL POINTER EXCEPTION
       }
   }
   
   public void startLogin() {
       Session session = Session.getActiveSession();
        if (session != null && !session.isOpened() && !session.isClosed()) {
           OpenRequest op = new Session.OpenRequest((Activity) this.context);

           op.setCallback(statusCallback);
           op.setPermissions(permissions);
           
           session.openForRead(op);
       } else {
           Session.openActiveSession((Activity) this.context, true, statusCallback);
       }
   }

    public String getFacebookId() {
        return mUser.getId();
    }

    public String getFacebookEmail() {
        if (mUser != null) {
            if (mUser.getProperty("email") != null) {
                return mUser.getProperty("email").toString();
            }
        }
        return "";
    }
    public String getAccessToken() {
        return Session.getActiveSession().getAccessToken();
    }
}
