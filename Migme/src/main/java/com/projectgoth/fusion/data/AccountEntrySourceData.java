package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.DataUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.objectcache.ChatSourceSession;
import com.projectgoth.fusion.slice.SessionPrx;
import java.io.Serializable;
import java.net.InetAddress;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountEntrySourceData implements Serializable {
   public Long id;
   public Long accountEntryID;
   public String ipAddress;
   public String sessionID;
   public String mobileDevice;
   public String userAgent;
   public String imei;
   public Integer merchantUserID;

   public AccountEntrySourceData(String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      if (ipAddress != null && ipAddress.length() > 0) {
         this.ipAddress = ipAddress;
      }

      if (sessionID != null && sessionID.length() > 0) {
         this.sessionID = sessionID;
      }

      if (mobileDevice != null && mobileDevice.length() > 0) {
         this.mobileDevice = DataUtils.truncateMobileDevice(mobileDevice, true, String.format("in AccountEntrySourceData ip='%s', sessionId='%s', userAgent='%s'", ipAddress, sessionID, userAgent));
      }

      if (userAgent != null && userAgent.length() > 0) {
         this.userAgent = DataUtils.truncateUserAgent(userAgent, true, String.format("in AccountEntrySourceData ip='%s', sessionId='%s', mobileDevice='%s'", ipAddress, sessionID, mobileDevice));
      }

   }

   public AccountEntrySourceData(Class applicationClass) {
      try {
         this.ipAddress = InetAddress.getLocalHost().getHostAddress();
      } catch (Exception var3) {
         this.ipAddress = "Unknown";
      }

      this.mobileDevice = DataUtils.truncateMobileDevice(applicationClass.getSimpleName(), true, String.format("in AccountEntrySourceData by class, ip='%s'", this.ipAddress));
      this.userAgent = this.mobileDevice;
   }

   public AccountEntrySourceData(ConnectionI gatewayConnection) {
      this.ipAddress = gatewayConnection.getRemoteAddress();
      this.sessionID = gatewayConnection.getSessionID();
      this.mobileDevice = gatewayConnection.getMobileDevice();
      this.userAgent = gatewayConnection.getUserAgent();
   }

   public AccountEntrySourceData(ChatSourceSession sessionI) {
      this.ipAddress = sessionI.getRemoteAddress();
      this.sessionID = sessionI.getSessionID();
      this.mobileDevice = sessionI.getMobileDevice();
      this.userAgent = sessionI.getUserAgent();
   }

   public AccountEntrySourceData(SessionPrx sessionProxy) {
      this.ipAddress = sessionProxy.getRemoteIPAddress();
      this.sessionID = sessionProxy.getSessionID();
      this.mobileDevice = sessionProxy.getMobileDeviceIce();
      this.userAgent = sessionProxy.getUserAgentIce();
   }

   public AccountEntrySourceData(JSONObject jsonObject) throws JSONException {
      if (jsonObject.has("ipAddress") && !StringUtil.isBlank(jsonObject.getString("ipAddress"))) {
         this.ipAddress = jsonObject.getString("ipAddress");
      }

      if (jsonObject.has("sessionID") && !StringUtil.isBlank(jsonObject.getString("sessionID"))) {
         this.sessionID = jsonObject.getString("sessionID");
      }

      if (jsonObject.has("mobileDevice") && !StringUtil.isBlank(jsonObject.getString("mobileDevice"))) {
         this.mobileDevice = DataUtils.truncateMobileDevice(jsonObject.getString("mobileDevice"), true, String.format("in AccountEntrySourceData ip='%s', sessionId='%s', userAgent='%s'", this.ipAddress, this.sessionID, this.userAgent));
      }

      if (jsonObject.has("userAgent") && !StringUtil.isBlank(jsonObject.getString("userAgent"))) {
         this.userAgent = DataUtils.truncateMobileDevice(jsonObject.getString("userAgent"), true, String.format("in AccountEntrySourceData ip='%s', sessionId='%s', userAgent='%s'", this.ipAddress, this.sessionID, this.userAgent));
      }

   }
}
