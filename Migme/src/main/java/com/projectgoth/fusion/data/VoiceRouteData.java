package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VoiceRouteData implements Serializable, Comparable<VoiceRouteData> {
   public Integer iddCode;
   public String areaCode;
   public Integer gatewayID;
   public Integer providerID;
   public Integer priority;
   public String dialCommand;

   public VoiceRouteData() {
   }

   public VoiceRouteData(ResultSet rs) throws SQLException {
      this.iddCode = rs.getInt("iddCode");
      this.areaCode = rs.getString("areaCode");
      this.gatewayID = rs.getInt("gatewayId");
      this.providerID = rs.getInt("providerId");
      this.priority = rs.getInt("priority");
      this.dialCommand = rs.getString("dialCommand");
   }

   public int compareTo(VoiceRouteData o) {
      return this.priority.compareTo(o.priority);
   }
}
