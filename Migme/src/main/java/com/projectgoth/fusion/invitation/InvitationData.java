package com.projectgoth.fusion.invitation;

import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.leto.common.event.invites.InviteActivity;
import com.projectgoth.leto.common.event.invites.InviteChannel;
import com.projectgoth.leto.common.utils.enums.ValueToEnumMap;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class InvitationData implements Serializable {
   public int id;
   public int inviterUserId;
   public InvitationData.ActivityType type;
   public InvitationData.ChannelType channel;
   public Date createdTime;
   public Date expireTime;
   public InvitationData.StatusFieldValue status;
   public String destination;
   private HashMap<InvitationData.ParamType, String> parameters = new HashMap();

   public Collection<InvitationData.ParamType> getParamTypes() {
      return this.parameters.keySet();
   }

   public Collection<String> getParamValues() {
      return this.parameters.values();
   }

   public Collection<Entry<InvitationData.ParamType, String>> getParameterEntries() {
      return this.parameters.entrySet();
   }

   public int getParamCount() {
      return this.parameters.size();
   }

   public String getParameter(InvitationData.ParamType paramType) {
      return paramType == null ? null : (String)this.parameters.get(paramType);
   }

   public void initializeMainData(ResultSet rsRow) throws SQLException {
      this.channel = InvitationData.ChannelType.fromTypeCode(rsRow.getInt("channel"));
      this.createdTime = rsRow.getTimestamp("createdTime");
      this.destination = rsRow.getString("destination");
      this.expireTime = rsRow.getTimestamp("expireTime");
      this.id = rsRow.getInt("id");
      this.inviterUserId = rsRow.getInt("inviterUserId");
      int status = rsRow.getInt("status");
      this.status = InvitationData.StatusFieldValue.fromTypeCode(status);
      this.type = InvitationData.ActivityType.fromTypeCode(rsRow.getInt("type"));
   }

   public void addParameters(ResultSet rsRow) throws SQLException {
      int paramTypeInt = rsRow.getInt("invparamType");
      InvitationData.ParamType paramTypeEnum = InvitationData.ParamType.fromTypeCode(paramTypeInt);
      if (paramTypeEnum != null) {
         String paramValue = rsRow.getString("invparamValue");
         this.parameters.put(paramTypeEnum, paramValue);
      }

   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("InvitationData [id=");
      builder.append(this.id);
      builder.append(", inviterUserId=");
      builder.append(this.inviterUserId);
      builder.append(", type=");
      builder.append(this.type);
      builder.append(", channel=");
      builder.append(this.channel);
      builder.append(", createdTime=");
      builder.append(this.createdTime);
      builder.append(", expireTime=");
      builder.append(this.expireTime);
      builder.append(", status=");
      builder.append(this.status);
      builder.append(", destination=[");
      builder.append(this.destination);
      builder.append("], parameters=");
      builder.append(this.parameters);
      builder.append("]");
      return builder.toString();
   }

   public static enum ParamType implements EnumUtils.IEnumValueGetter<Integer> {
      RETURN_URL(1),
      FACEBOOK_REQUEST_ID(2),
      RESERVED_TYPE_FOR_INTERNAL_TEST_3(3),
      RESERVED_TYPE_FOR_INTERNAL_TEST_4(4),
      GAMEID(5);

      private Integer typeCode;
      private static HashMap<Integer, InvitationData.ParamType> lookupByCode = new HashMap();

      private ParamType(int typeCode) {
         this.typeCode = typeCode;
      }

      public static InvitationData.ParamType fromTypeCode(int typeCode) {
         return (InvitationData.ParamType)lookupByCode.get(typeCode);
      }

      public Integer getEnumValue() {
         return this.getTypeCode();
      }

      public int getTypeCode() {
         return this.typeCode;
      }

      static {
         EnumUtils.populateLookUpMap(lookupByCode, InvitationData.ParamType.class);
      }
   }

   public static enum ChannelType implements EnumUtils.IEnumValueGetter<Integer> {
      EMAIL(InviteChannel.EMAIL, "email"),
      SMS(InviteChannel.SMS, "sms"),
      FB(InviteChannel.FB, "facebook"),
      INTERNAL(InviteChannel.INTERNAL, "internal"),
      MIGBO(InviteChannel.MIGBO, "migbo"),
      CHAT(InviteChannel.CHAT, "chat");

      private InviteChannel inviteChannel;
      private String typeName;
      private static HashMap<Integer, InvitationData.ChannelType> lookupByCode = new HashMap();
      private static HashMap<String, InvitationData.ChannelType> lookupByName = new HashMap();

      private ChannelType(InviteChannel inviteChannel, String typeName) {
         this.inviteChannel = inviteChannel;
         this.typeName = StringUtil.trimmedLowerCase(typeName);
      }

      public static InvitationData.ChannelType fromTypeCode(int typeCode) {
         return (InvitationData.ChannelType)lookupByCode.get(typeCode);
      }

      public static InvitationData.ChannelType fromTypeName(String typeName) {
         return typeName == null ? null : (InvitationData.ChannelType)lookupByName.get(StringUtil.trimmedLowerCase(typeName));
      }

      public InviteChannel toInviteChannelType() {
         return this.inviteChannel;
      }

      public Integer getEnumValue() {
         return this.inviteChannel.getEnumValue();
      }

      public int getTypeCode() {
         return this.getEnumValue();
      }

      static {
         EnumUtils.populateLookUpMap(lookupByCode, InvitationData.ChannelType.class);
         EnumUtils.populateLookUpMap(lookupByName, InvitationData.ChannelType.class, new EnumUtils.IEnumValueExtractor<String, InvitationData.ChannelType>() {
            public String getValue(InvitationData.ChannelType enumConst) {
               return enumConst.typeName;
            }
         });
      }
   }

   public static enum StatusFieldValue implements EnumUtils.IEnumValueGetter<Integer> {
      INVALID(-1),
      DISABLED(0),
      NO_RESPONSE(1),
      EXPIRED(2),
      CLOSED(3);

      private Integer typeCode;
      private static HashMap<Integer, InvitationData.StatusFieldValue> lookupByCode = new HashMap();

      private StatusFieldValue(int typeCode) {
         this.typeCode = typeCode;
      }

      public Integer getEnumValue() {
         return this.getTypeCode();
      }

      public int getTypeCode() {
         return this.typeCode;
      }

      public static InvitationData.StatusFieldValue fromTypeCode(int typeCode) {
         return (InvitationData.StatusFieldValue)lookupByCode.get(typeCode);
      }

      static {
         EnumUtils.populateLookUpMap(lookupByCode, InvitationData.StatusFieldValue.class);
      }
   }

   public static enum ActivityType implements EnumUtils.IEnumValueGetter<Integer> {
      JOIN_MIG33(InviteActivity.JOIN_MIG33),
      BE_MY_FRIEND(InviteActivity.BE_MY_FRIEND),
      PLAY_A_GAME(InviteActivity.PLAY_A_GAME),
      GAME_HELP(InviteActivity.GAME_HELP),
      SHARE_PROFILE(InviteActivity.SHARE_PROFILE);

      private InviteActivity inviteActivity;

      private ActivityType(InviteActivity inviteActivity) {
         this.inviteActivity = inviteActivity;
      }

      public Integer getEnumValue() {
         return this.inviteActivity.getEnumValue();
      }

      public int getTypeCode() {
         return this.getEnumValue();
      }

      public InviteActivity toInviteActivityType() {
         return this.inviteActivity;
      }

      public static InvitationData.ActivityType fromTypeCode(int typeCode) {
         return (InvitationData.ActivityType)InvitationData.ActivityType.ValueToEnumMapInstance.INSTANCE.toEnum(typeCode);
      }

      private static final class ValueToEnumMapInstance {
         private static final ValueToEnumMap<Integer, InvitationData.ActivityType> INSTANCE = new ValueToEnumMap(InvitationData.ActivityType.class);
      }
   }
}
