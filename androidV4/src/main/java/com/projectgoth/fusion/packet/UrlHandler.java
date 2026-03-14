
package com.projectgoth.fusion.packet;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.fusion.packet.FusionPktGetUrl;
import com.projectgoth.common.Config;
import com.projectgoth.common.ConnectionDetail;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetUrlListener;
import com.projectgoth.nemesis.utils.ConnectionConfig;
import com.projectgoth.service.NetworkService;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlHandler {

    private static final String     TAG                    = AndroidLogger.makeLogTag(UrlHandler.class);

    private static final UrlHandler INSTANCE               = new UrlHandler();

    private String                  IMAGE_SERVER_URL_KEY   = "UrlHandler.IMAGE_SERVER_URL_KEY";

    private String                  PAGELET_SERVER_URL_KEY = "UrlHandler.PAGELET_SERVER_URL_KEY";
    
    private String                  cookiePrefix;

    private String                  urlPrefix;

// @formatter:off
    private GetUrlListener          urlListener            = 
        new GetUrlListener() {
            public void onURLReceived(FusionPktGetUrl.UrlType urlType, String url) {
                Logger.debug.flog(TAG, "onURLReceived type: %s url: %s", urlType.name(), url);
                setUrl(urlType, url);
           }
        };
// @formatter:on

    private UrlHandler() {
    }

    public static UrlHandler getInstance() {
        return INSTANCE;
    }

    /**
     * get the key used to store the {@link FusionPktGetUrl.UrlType} in the persistent
     * storage
     * 
     * @param type
     * @return
     */
    private String getUrlKey(FusionPktGetUrl.UrlType type) {
        if (type != null) {
            return this.getClass().getName() + type.name();
        }
        return null;
    }

    public String getUrl(FusionPktGetUrl.UrlType type) {
        if (type != null) {
            String result = SystemDatastore.getInstance().getStringData(getUrlKey(type));
            if (result == null) {
                sendGetUrlRequest(type);
            }
            return result;
        }
        return null;
    }

    private void sendGetUrlRequest(FusionPktGetUrl.UrlType type) {
        try {
            if (type != null) {
                RequestManager requestManager = ApplicationEx.getInstance().getNetworkService().getRequestManager();
                requestManager.sendGetUrl(urlListener, type);
            }
        } catch (Exception e) {
            // IGNORE NULL POINTER EXCEPTION
        }
    }

    public void setUrl(FusionPktGetUrl.UrlType type, String url) {
        if (type != null) {
            saveUrl(getUrlKey(type), url);
            updateConnectionConfig(type);
        }
    }
    
    private void saveUrl(String key, String url) {
        SystemDatastore.getInstance().saveData(key, url);
    }

    private void updateConnectionConfig(FusionPktGetUrl.UrlType type) {
        try {
            ConnectionConfig config = ApplicationEx.getInstance().getNetworkService().getConnectionConfig();
            switch (type) {
                case MIGBO_DATASVC:
                    config.setMigboDataServiceUrl(getMigboDataServiceUrl());
                    break;
                case SSO:
                    config.setSsoUrl(getSsoUrl());
                    break;
                case MIGBO_UPLOAD:
                    config.setMultiPartPostUrl(getMultiPartPostUrl());
                    break;
                case MIGBO_IMAGES:
                    config.setImagesUrl(getImagesUrl());
                    break;
                default:
                        break;
            }
        } catch (Exception e) {
            // IGNORE NULL POINTER EXCEPTION
        }
    }

    /**
     * @return the imageServerUrl
     */
    public String getImageServerUrl() {
        String imageServerUrl = SystemDatastore.getInstance().getStringData(IMAGE_SERVER_URL_KEY);
        if (imageServerUrl == null) {
            ConnectionDetail detail = Config.getInstance().getConnectionDetail();
            imageServerUrl = detail.getImageServer();
            Logger.debug.log(TAG, "using default image server url");
        }
        return imageServerUrl;
    }

    /**
     * @param imageServerUrl
     *            the imageServerUrl to set
     */
    public void setImageServerUrl(String imageServerUrl) {
        Logger.debug.log(TAG, "setting image url: ", imageServerUrl);
        if (imageServerUrl != null) {
            saveUrl(IMAGE_SERVER_URL_KEY, imageServerUrl);
        }
    }

    /**
     * @return the pageletServerUrl
     */
    public String getPageletServerUrl() {
        String pageletServerUrl = SystemDatastore.getInstance().getStringData(PAGELET_SERVER_URL_KEY);
        
        if (null == pageletServerUrl) {
            ConnectionDetail detail = Config.getInstance().getConnectionDetail();
            pageletServerUrl = detail.getWebServer();
            Logger.debug.log(TAG, "using default pagelet url");
        }
        return pageletServerUrl;
    }

    /**
     * @param pageletServerUrl
     *            the pageletServerUrl to set
     */
    public void setPageletServerUrl(String pageletServerUrl) {
        Logger.debug.log(TAG, "setting pageletServer url: ", pageletServerUrl);
        if (pageletServerUrl != null) {
            // TODO: this is a hack fix it from server
            pageletServerUrl = pageletServerUrl.substring(0, pageletServerUrl.indexOf("/", 7));
            saveUrl(PAGELET_SERVER_URL_KEY, pageletServerUrl);

            cookiePrefix = null;
            urlPrefix = null;
        }
    }

    /**
     * @return the ssoUrl
     */
    public String getSsoUrl() {
        String url = getUrl(FusionPktGetUrl.UrlType.SSO);
        if (url == null) {
            ConnectionDetail detail = Config.getInstance().getConnectionDetail();
            url = detail.getSsoUrl();
        }
        return url;
    }

    /**
     * @param ssoUrl
     *            the ssoUrl to set
     */
    public void setSsoUrl(String ssoUrl) {
        setUrl(FusionPktGetUrl.UrlType.SSO, ssoUrl);
    }

    /**
     * @return the multiPartPostUrl
     */
    public String getMultiPartPostUrl() {
        String url = getUrl(FusionPktGetUrl.UrlType.MIGBO_UPLOAD);
        if (url == null) {
            ConnectionDetail detail = Config.getInstance().getConnectionDetail();
            url = detail.getMultiPartURL();
        }
        return url;
    }

    /**
     * @return the migboDataServiceUrl
     */
    public String getMigboDataServiceUrl() {
        String url = getUrl(FusionPktGetUrl.UrlType.MIGBO_DATASVC);
        if (url == null) {
            ConnectionDetail detail = Config.getInstance().getConnectionDetail();
            url = detail.getMigboDataservice();
        }
        return url;
    }

    /**
     * @param migboDataServiceUrl
     *            the migboDataServiceUrl to set
     */
    public void setMigboDataServiceUrl(String migboDataServiceUrl) {
        setUrl(FusionPktGetUrl.UrlType.MIGBO_DATASVC, migboDataServiceUrl);
    }

    /**
     * @return the imagesUrl
     */
    public String getImagesUrl() {
        String url = getUrl(FusionPktGetUrl.UrlType.MIGBO_IMAGES);
        if (url == null) {
            ConnectionDetail detail = Config.getInstance().getConnectionDetail();
            url = detail.getImagesUrl();
        }
        return url;
    }

    /**
     * @param imagesUrl
     *            the imagesUrl to set
     */
    public void setImagesUrl(String imagesUrl) {
        setUrl(FusionPktGetUrl.UrlType.MIGBO_IMAGES, imagesUrl);
    }

    public void refreshServerUrls() {
        refreshServerUrls(ApplicationEx.getInstance().getNetworkService());
    }
    
    public void refreshServerUrls(NetworkService service) {
        // TODO: Warren Temp fix while we investigate the cause of flooding.
        /*
         * sendGetUrlRequest(GetUrlTypeEnum.MIGBO_DATASVC_URL);
         * sendGetUrlRequest(GetUrlTypeEnum.SSO_URL);
         * sendGetUrlRequest(GetUrlTypeEnum.MULTIPART_POST_URL);
         * sendGetUrlRequest(GetUrlTypeEnum.IMAGES_URL);
         */

        try {
            ConnectionConfig config = service.getConnectionConfig();
            config.setMigboDataServiceUrl(getMigboDataServiceUrl());
            config.setSsoUrl(getSsoUrl());
            config.setMultiPartPostUrl(getMultiPartPostUrl());
            config.setImagesUrl(getImagesUrl());
        } catch (Exception e) {
            // IGNORE NULL POINTER
        }
    }
    
    
    public void clearCachedUrls() {
        SystemDatastore.getInstance().clearData(IMAGE_SERVER_URL_KEY);
        SystemDatastore.getInstance().clearData(PAGELET_SERVER_URL_KEY);
        SystemDatastore.getInstance().clearData(getUrlKey(FusionPktGetUrl.UrlType.MIGBO_DATASVC));
    }

    public String getCookiePrefix() {
        if (cookiePrefix == null) {
            StringBuilder domainForCookie = new StringBuilder();
            String pageletServerUrl = getPageletServerUrl();

            if (!pageletServerUrl.startsWith(Constants.LINK_HTTP)) {
                pageletServerUrl = Constants.LINK_HTTP + pageletServerUrl;
            }

            try {
                URL url = new URL(pageletServerUrl);
                String[] hostParts = url.getHost().split("\\.");
                int n = hostParts.length > 2 ? n = hostParts.length - 2 : 0;
                while (n < hostParts.length) {
                    domainForCookie.append("." + hostParts[n]);
                    n++;
                }
            } catch (MalformedURLException e) {
                Logger.error.log(TAG, e);
            }

            if (domainForCookie.length() == 0) {
                domainForCookie.append(".mig33.com");
            }
            cookiePrefix = String.format("; domain=%s; path=/", domainForCookie.toString());
        }
        return cookiePrefix;
    }

    /**
     * @return the urlPrefix
     */
    public String getUrlPrefix() {
        if (urlPrefix == null) {
            urlPrefix = getPageletServerUrl();
            if (urlPrefix.startsWith(Constants.LINK_HTTP)) {
                return urlPrefix;
            }
            urlPrefix = Constants.LINK_HTTP + getPageletServerUrl();
        }
        return urlPrefix;
    }
    
}
