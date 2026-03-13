package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class VoiceGatewayData implements Serializable {
   public Integer id;
   public String name;
   public String server;
   public Integer port;
   public String username;
   public String password;
   public String callbackContext;
   public String callbackExtension;
   public Integer connectionTimeout;
   public Integer timeoutWarning;
   public Integer timeoutWarningRepeat;
   public VoiceGatewayData.StatusEnum status;
   public List<VoiceRouteData> voiceRoutes;

   public VoiceGatewayData() {
   }

   public VoiceGatewayData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.name = rs.getString("name");
      this.server = rs.getString("server");
      this.port = rs.getInt("port");
      this.username = rs.getString("username");
      this.password = rs.getString("password");
      this.callbackContext = rs.getString("callbackContext");
      this.callbackExtension = rs.getString("callbackExtension");
      this.connectionTimeout = rs.getInt("connectionTimeout");
      this.timeoutWarning = rs.getInt("timeoutWarning");
      this.timeoutWarningRepeat = rs.getInt("timeoutWarningRepeat");
      Integer intval = (Integer)rs.getObject("status");
      if (intval != null) {
         this.status = VoiceGatewayData.StatusEnum.fromValue(intval);
      }

   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static VoiceGatewayData.StatusEnum fromValue(int value) {
         VoiceGatewayData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            VoiceGatewayData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
