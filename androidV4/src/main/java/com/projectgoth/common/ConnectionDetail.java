/**
 * Copyright (c) 2013 Project Goth
 *
 * ConnectionDetail.java
 * Created Aug 22, 2013, 2:47:26 AM
 */

package com.projectgoth.common;


import com.projectgoth.app.ApplicationEx;
import com.projectgoth.nemesis.utils.ConnectionConfig;

/**
 * @author warrenbalcos
 * 
 */
public class ConnectionDetail {

    public enum Type {
        PROD, STAGING, QALAB, LCQALAB, CUSTOM, MIAB;

        public static Type fromValue(String value) {
            if (value != null) {
                if (value.equals(PROD.name())) {
                    return PROD;
                } else if (value.equals(STAGING.name())) {
                    return STAGING;
                } else if (value.equals(QALAB.name())) {
                    return QALAB;
                } else if (value.equals(LCQALAB.name())) {
                    return LCQALAB;
                } else if (value.equals(CUSTOM.name())) {
                    return CUSTOM;
                } else if (value.equals(MIAB.name())) {
                    return MIAB;
                }
            }
            return DefaultConfig.DEFAULT_CONNECTION;
        }
    }

    private Type        mType;
    private String      mGateway;
    private int         mPort;
    private String      mWebServer;
    private String      mDiscoverServer;
    private String      mImageServer;
    private String      mSsoUrl;
    private String      mMigboDataservice;
    private String      mMultiPartURL;
    private String      mImagesUrl;
    private String      mFacebookRegistration;
    private String      mFacebookAppId;
    private boolean     mUseProxy               = false;
    private String      mProxyHost              = null;
    private int         mProxyPort              = 0;
    private String      mSignupServer;
    private String      mMigmeApiUrl;


    public ConnectionDetail(Type type) {
        this.setType(type);
    }

    private void init() {
        setMigmeApiUrl("https://api.mig.me");
        switch (mType) {
            case STAGING:
            {
                setGateway("74.217.68.51");
                setPort(9119);
                setWebServer("migme.stg.projectgoth.com");
                setDiscoverServer("http://discover.migme.stg.projectgoth.com");
                setImageServer("http://img.migme.stg.projectgoth.com/");
                setSsoUrl("https://login.migme.stg.projectgoth.com/touch/datasvc");
                setMigboDataservice("http://migme.stg.projectgoth.com/touch/datasvc");
                setMultiPartURL("http://migme.stg.projectgoth.com/touch/post/hidden_post");
                setImagesUrl("http://migme.stg.projectgoth.com/b/resources/img");
                setFacebookRegistration("https://register.migme.stg.projectgoth.com/touch/facebook/register?access_token=%s");
                setSignupServer("http://migme.stg.projectgoth.com");
                setFacebookAppId("147422338729791");
                break;
            }
            case QALAB:
            {
                setGateway("gway.qalab.projectgoth.com");
                setPort(9119);
                setWebServer("qalab.projectgoth.com");
                setDiscoverServer("http://discover.qalab.projectgoth.com");
                setImageServer("http://img.qalab.projectgoth.com/");
                setSsoUrl("https://login.qalab.projectgoth.com/touch/datasvc");
                setMigboDataservice("http://qalab.projectgoth.com/touch/datasvc");
                setMultiPartURL("http://qalab.projectgoth.com/touch/post/hidden_post");
                setImagesUrl("http://qalab.projectgoth.com/b/resources/img");
                setFacebookRegistration("https://register.qalab.projectgoth.com/touch/facebook/register?access_token=%s");
                setSignupServer("http://qalab.projectgoth.com");
                setFacebookAppId("201383533238944");
                break;
            }
            case LCQALAB:
            {
                setGateway("gway.qalab.projectgoth.com");
                setPort(9119);
                setWebServer("lc.qalab.projectgoth.com");
                setDiscoverServer("http://discover.lc.qalab.projectgoth.com");
                setImageServer("http://lc.qalab.projectgoth.com/");
                setSsoUrl("https://login.lc.qalab.projectgoth.com/touch/datasvc");
                setMigboDataservice("http://lc.qalab.projectgoth.com/touch/datasvc");
                setMultiPartURL("http://lc.qalab.projectgoth.com/touch/post/hidden_post");
                setImagesUrl("http://lc.qalab.projectgoth.com/b/resources/img");
                setFacebookRegistration("https://register.lc.qalab.projectgoth.com/touch/facebook/register?access_token=%s");
                setSignupServer("http://lc.qalab.projectgoth.com");
                break;
            }
            case PROD:
            {
                // MigxChat production endpoints
                setMigmeApiUrl("https://api.migxchat.net");
                setGateway("gateway.migxchat.net");
                setPort(9119);
                setWebServer("migxchat.net");
                setDiscoverServer("https://discover.migxchat.net");
                setImageServer("https://img.migxchat.net/");
                setSsoUrl("https://api.migxchat.net/touch/datasvc");
                setMigboDataservice("https://api.migxchat.net/touch/datasvc");
                setMultiPartURL("https://api.migxchat.net/touch/post/hidden_post");
                setImagesUrl("https://api.migxchat.net/resources/img");
                setFacebookRegistration("https://api.migxchat.net/touch/facebook/register?access_token=%s");
                setSignupServer("https://migxchat.net");
                setFacebookAppId("161865877194414");
                break;
            }
            case MIAB:
            {
                String ip = "192.168.2.42";             // <== Set your MIAB's public IP
                setGateway(ip);
                setPort(9119);
                setWebServer("localhost.projectgoth.com");
                setDiscoverServer("http://discover.localhost.projectgoth.com");
                setImageServer("http://img.localhost.projectgoth.com/");
                setSsoUrl("https://login.localhost.projectgoth.com/touch/datasvc");
                setMigboDataservice("http://localhost.projectgoth.com/touch/datasvc");
                setMultiPartURL("http://localhost.projectgoth.com/touch/post/hidden_post");
                setImagesUrl("http://localhost.projectgoth.com/resources/img");
                setFacebookRegistration("https://register.localhost.projectgoth.com/touch/facebook/register?access_token=%s");
                setUseProxy(true);
                setProxyHost(ip);
                setProxyPort(10080);
                setSignupServer("http://localhost.projectgoth.com");
                break;
            }
            case CUSTOM:
                if (mGateway == null) {
                    setGateway("gateway.mig33.com");
                }
                if (mPort <= 0) {
                    setPort(9119);
                }
                setWebServer("mig.me");
                setDiscoverServer("http://discover.mig.me");
                setImageServer("http://img.mig.me/");
                setSsoUrl("https://login.mig.me/touch/datasvc");
                setMigboDataservice("http://mig.me/touch/datasvc");
                setMultiPartURL("http://mig.me/touch/post/hidden_post");
                setImagesUrl("http://mig.me/resources/img");
                setFacebookRegistration("https://register.mig.me/touch/facebook/register?access_token=%s");
                setSignupServer("http://mig.me");
                break;
        }
    }

    /**
     * get sign server url
     * @return signup server url
     */
    public String getSignupServer() {
        return mSignupServer;
    }

    /**
     * Set signup server url
     * @param signupServer signup server url
     */
    public void setSignupServer(String signupServer) {
        mSignupServer = signupServer;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return mType;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(Type type) {
        this.mType = type;
        init();
    }

    /**
     * @return the gateway
     */
    public String getGateway() {
        return mGateway;
    }

    /**
     * @param gateway
     *            the gateway to set
     */
    public void setGateway(String gateway) {
        if (gateway != null) {
            this.mGateway = gateway;
        }
    }

    /**
     * @return the webServer
     */
    public String getWebServer() {
        return mWebServer;
    }

    /**
     * @param webServer
     *            the webServer to set
     */
    public void setWebServer(String webServer) {
        this.mWebServer = webServer;
    }
    
    /**
     * @return the discoverServer
     */
    public String getDiscoverServer() {
        return mDiscoverServer;
    }
    
    /**
     * @param discoverServer
     *              the discoverServer to set
     */
    public void setDiscoverServer(String discoverServer) {
        this.mDiscoverServer = discoverServer;
    }

    /**
     * @return the imageServer
     */
    public String getImageServer() {
        return mImageServer;
    }

    /**
     * @param imageServer
     *            the imageServer to set
     */
    public void setImageServer(String imageServer) {
        this.mImageServer = imageServer;
    }

    /**
     * @return the ssoUrl
     */
    public String getSsoUrl() {
        return mSsoUrl;
    }

    /**
     * @param ssoUrl
     *            the ssoUrl to set
     */
    public void setSsoUrl(String ssoUrl) {
        this.mSsoUrl = ssoUrl;
    }

    /**
     * @return the migboDataservice
     */
    public String getMigboDataservice() {
        return mMigboDataservice;
    }

    /**
     * @param migboDataservice
     *            the migboDataservice to set
     */
    public void setMigboDataservice(String migboDataservice) {
        this.mMigboDataservice = migboDataservice;
    }

    /**
     * @return the multiPartURL
     */
    public String getMultiPartURL() {
        return mMultiPartURL;
    }

    /**
     * @param multiPartURL
     *            the multiPartURL to set
     */
    public void setMultiPartURL(String multiPartURL) {
        this.mMultiPartURL = multiPartURL;
    }

    /**
     * @return the imagesUrl
     */
    public String getImagesUrl() {
        return mImagesUrl;
    }

    /**
     * @param imagesUrl
     *            the imagesUrl to set
     */
    public void setImagesUrl(String imagesUrl) {
        this.mImagesUrl = imagesUrl;
    }

    /**
     * @return the facebookRegistration
     */
    public String getFacebookRegistration() {
        return mFacebookRegistration;
    }

    /**
     * @param facebookRegistration
     *            the facebookRegistration to set
     */
    public void setFacebookRegistration(String facebookRegistration) {
        this.mFacebookRegistration = facebookRegistration;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return mPort;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(int port) {
        if (port > 0) {
            this.mPort = port;
        }
    }
    
    /**
     * @return the proxy host
     */
    public String getProxyHost() {
        return mProxyHost;
    }
    
    /**
     * @param proxy host
     */
    public void setProxyHost(String proxyHost) {
        this.mProxyHost = proxyHost;
    }
    
    /**
     * @return the proxy port
     */
    public int getProxyPort() {
        return mProxyPort;
    }
    
    /**
     * @param proxy port
     */
    public void setProxyPort(int proxyPort) {
        this.mProxyPort = proxyPort;
    }
    
    /**
     * @return if its using proxy
     */
    public boolean isUseProxy() {
        return mUseProxy;
    }
    
    /**
     * @param use proxy
     */
    public void setUseProxy(boolean useProxy) {
        this.mUseProxy = useProxy;
    }
    
    public boolean isDiscoverUrl(String url) {
        return url != null && url.contains(mDiscoverServer);
    }

    public void setFacebookAppId(String id) {
        mFacebookAppId = id;
    }

    public String getFacebookAppId() {
        return mFacebookAppId;
    }


    /**
     * Get Migme Api url
     * @return
     */
    public String getMigmeApiUrl() {
        return mMigmeApiUrl;
    }

    /**
     * Set Migme Api url
     * @param migmeApiUrl
     */
    public void setMigmeApiUrl(String migmeApiUrl) {
        mMigmeApiUrl = migmeApiUrl;
    }
}
