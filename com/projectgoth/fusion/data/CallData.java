package com.projectgoth.fusion.data;

import com.projectgoth.fusion.slice.CallDataIce;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class CallData implements Serializable {
   public Integer id;
   public String username;
   public Integer contactID;
   public Date dateCreated;
   public String source;
   public CallData.SourceDestinationTypeEnum sourceType;
   public Integer sourceIDDCode;
   public String destination;
   public CallData.SourceDestinationTypeEnum destinationType;
   public Integer destinationIDDCode;
   public CallData.MakeReceiveEnum makeReceive;
   public CallData.InitialLegEnum initialLeg;
   public Long sourceDuration;
   public Long destinationDuration;
   public Long billedDuration;
   public Double signallingFee;
   public Double rate;
   public CallData.TypeEnum type;
   public Boolean claimable;
   public Integer gateway;
   public Integer sourceProvider;
   public Integer destinationProvider;
   public Integer failReasonCode;
   public String failReason;
   public CallData.StatusEnum status;
   public int maxCallDuration = 0;
   public int retries = 0;
   public double amount = 0.0D;
   public String currency;
   public String actionID;
   public String accountID;
   public String uniqueID;
   public String sourceChannel;
   public String sourceChannelName;
   public Integer sourceHangupCode;
   public String sourceHangupReason;
   public String destinationChannel;
   public String destinationChannelName;
   public Integer destinationHangupCode;
   public String destinationHangupReason;
   public CallData.ProtocolEnum sourceProtocol;
   public CallData.ProtocolEnum destinationProtocol;
   public String didNumber;
   public Integer destinationFirstProvider;
   public Integer destinationNextProvider;
   public String sourceDialCommand;
   public String destinationDialCommand;
   public String destinationFirstDialCommand;
   public String destinationNextDialCommand;
   public Integer maxDuration;

   public boolean isCallback() {
      switch(this.type) {
      case MIDLET_CALLBACK:
      case SMS_CALLBACK:
      case WAP_CALLBACK:
      case WEB_CALLBACK:
      case TOOLBAR_CALL:
      case MISSED_CALL_CALLBACK:
      case MIDLET_ANONYMOUS_CALLBACK:
         return true;
      default:
         return false;
      }
   }

   public boolean isCallThrough() {
      switch(this.type) {
      case MIDLET_CALL_THROUGH:
      case DIRECT_CALL_THROUGH:
         return true;
      default:
         return false;
      }
   }

   public CallData() {
   }

   public CallData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.username = rs.getString("username");
      this.contactID = (Integer)rs.getObject("contactid");
      this.dateCreated = rs.getTimestamp("dateCreated");
      this.source = rs.getString("source");
      this.sourceIDDCode = (Integer)rs.getObject("sourceIDDCode");
      this.destination = rs.getString("destination");
      this.destinationIDDCode = (Integer)rs.getObject("destinationIDDCode");
      this.sourceDuration = (Long)rs.getObject("sourceDuration");
      this.destinationDuration = (Long)rs.getObject("destinationDuration");
      this.billedDuration = (Long)rs.getObject("billedDuration");
      this.signallingFee = (Double)rs.getObject("signallingFee");
      this.rate = (Double)rs.getObject("rate");
      this.gateway = (Integer)rs.getObject("gateway");
      this.sourceProvider = (Integer)rs.getObject("sourceProvider");
      this.destinationProvider = (Integer)rs.getObject("destinationProvider");
      this.failReasonCode = (Integer)rs.getObject("failReasonCode");
      this.failReason = rs.getString("failReason");
      Integer intval = (Integer)rs.getObject("makereceive");
      if (intval != null) {
         this.makeReceive = CallData.MakeReceiveEnum.fromValue(intval);
      }

      intval = (Integer)rs.getObject("sourcetype");
      if (intval != null) {
         this.sourceType = CallData.SourceDestinationTypeEnum.fromValue(intval);
      }

      intval = (Integer)rs.getObject("destinationtype");
      if (intval != null) {
         this.destinationType = CallData.SourceDestinationTypeEnum.fromValue(intval);
      }

      intval = (Integer)rs.getObject("initialleg");
      if (intval != null) {
         this.initialLeg = CallData.InitialLegEnum.fromValue(intval);
      }

      intval = (Integer)rs.getObject("type");
      if (intval != null) {
         this.type = CallData.TypeEnum.fromValue(intval);
      }

      intval = (Integer)rs.getObject("status");
      if (intval != null) {
         this.status = CallData.StatusEnum.fromValue(intval);
      }

   }

   public CallData(CallDataIce callIce) {
      this.id = callIce.id == Integer.MIN_VALUE ? null : callIce.id;
      this.username = callIce.username.equals("\u0000") ? null : callIce.username;
      this.contactID = callIce.contactID == Integer.MIN_VALUE ? null : callIce.contactID;
      this.dateCreated = callIce.dateCreated == Long.MIN_VALUE ? null : new Date(callIce.dateCreated);
      this.source = callIce.source.equals("\u0000") ? null : callIce.source;
      this.sourceType = callIce.sourceType == Integer.MIN_VALUE ? null : CallData.SourceDestinationTypeEnum.fromValue(callIce.sourceType);
      this.sourceProtocol = callIce.sourceProtocol == Integer.MIN_VALUE ? null : CallData.ProtocolEnum.fromValue(callIce.sourceProtocol);
      this.sourceIDDCode = callIce.sourceIDDCode == Integer.MIN_VALUE ? null : callIce.sourceIDDCode;
      this.destination = callIce.destination.equals("\u0000") ? null : callIce.destination;
      this.destinationType = callIce.destinationType == Integer.MIN_VALUE ? null : CallData.SourceDestinationTypeEnum.fromValue(callIce.destinationType);
      this.destinationProtocol = callIce.destinationProtocol == Integer.MIN_VALUE ? null : CallData.ProtocolEnum.fromValue(callIce.destinationProtocol);
      this.destinationIDDCode = callIce.destinationIDDCode == Integer.MIN_VALUE ? null : callIce.destinationIDDCode;
      this.makeReceive = callIce.makeReceive == Integer.MIN_VALUE ? null : CallData.MakeReceiveEnum.fromValue(callIce.makeReceive);
      this.initialLeg = callIce.initialLeg == Integer.MIN_VALUE ? null : CallData.InitialLegEnum.fromValue(callIce.initialLeg);
      this.sourceDuration = callIce.sourceDuration == Long.MIN_VALUE ? null : callIce.sourceDuration;
      this.destinationDuration = callIce.destinationDuration == Long.MIN_VALUE ? null : callIce.destinationDuration;
      this.billedDuration = callIce.billedDuration == Long.MIN_VALUE ? null : callIce.billedDuration;
      this.signallingFee = callIce.signallingFee == Double.MIN_VALUE ? null : callIce.signallingFee;
      this.rate = callIce.rate == Double.MIN_VALUE ? null : callIce.rate;
      this.type = callIce.type == Integer.MIN_VALUE ? null : CallData.TypeEnum.fromValue(callIce.type);
      this.claimable = callIce.claimable == Integer.MIN_VALUE ? null : callIce.claimable == 1;
      this.gateway = callIce.gateway == Integer.MIN_VALUE ? null : callIce.gateway;
      this.sourceProvider = callIce.sourceProvider == Integer.MIN_VALUE ? null : callIce.sourceProvider;
      this.destinationProvider = callIce.destinationProvider == Integer.MIN_VALUE ? null : callIce.destinationProvider;
      this.failReasonCode = callIce.failReasonCode == Integer.MIN_VALUE ? null : callIce.failReasonCode;
      this.failReason = callIce.failReason.equals("\u0000") ? null : callIce.failReason;
      this.status = callIce.status == Integer.MIN_VALUE ? null : CallData.StatusEnum.fromValue(callIce.status);
      this.didNumber = callIce.didNumber.equals("\u0000") ? null : callIce.didNumber;
      this.destinationFirstProvider = callIce.destinationFirstProvider == Integer.MIN_VALUE ? null : callIce.destinationFirstProvider;
      this.destinationNextProvider = callIce.destinationNextProvider == Integer.MIN_VALUE ? null : callIce.destinationNextProvider;
      this.sourceDialCommand = callIce.sourceDialCommand.equals("\u0000") ? null : callIce.sourceDialCommand;
      this.destinationDialCommand = callIce.destinationDialCommand.equals("\u0000") ? null : callIce.destinationDialCommand;
      this.destinationFirstDialCommand = callIce.destinationFirstDialCommand.equals("\u0000") ? null : callIce.destinationFirstDialCommand;
      this.destinationNextDialCommand = callIce.destinationNextDialCommand.equals("\u0000") ? null : callIce.destinationNextDialCommand;
      this.maxDuration = callIce.maxDuration == Integer.MIN_VALUE ? null : callIce.maxDuration;
   }

   public CallDataIce toIceObject() {
      CallDataIce callIce = new CallDataIce();
      callIce.id = this.id == null ? Integer.MIN_VALUE : this.id;
      callIce.username = this.username == null ? "\u0000" : this.username;
      callIce.contactID = this.contactID == null ? Integer.MIN_VALUE : this.contactID;
      callIce.dateCreated = this.dateCreated == null ? Long.MIN_VALUE : this.dateCreated.getTime();
      callIce.source = this.source == null ? "\u0000" : this.source;
      callIce.sourceType = this.sourceType == null ? Integer.MIN_VALUE : this.sourceType.value();
      callIce.sourceProtocol = this.sourceProtocol == null ? Integer.MIN_VALUE : this.sourceProtocol.value();
      callIce.sourceIDDCode = this.sourceIDDCode == null ? Integer.MIN_VALUE : this.sourceIDDCode;
      callIce.destination = this.destination == null ? "\u0000" : this.destination;
      callIce.destinationType = this.destinationType == null ? Integer.MIN_VALUE : this.destinationType.value();
      callIce.destinationProtocol = this.destinationProtocol == null ? Integer.MIN_VALUE : this.destinationProtocol.value();
      callIce.destinationIDDCode = this.destinationIDDCode == null ? Integer.MIN_VALUE : this.destinationIDDCode;
      callIce.makeReceive = this.makeReceive == null ? Integer.MIN_VALUE : this.makeReceive.value();
      callIce.initialLeg = this.initialLeg == null ? Integer.MIN_VALUE : this.initialLeg.value();
      callIce.sourceDuration = this.sourceDuration == null ? Long.MIN_VALUE : this.sourceDuration;
      callIce.destinationDuration = this.destinationDuration == null ? Long.MIN_VALUE : this.destinationDuration;
      callIce.billedDuration = this.billedDuration == null ? Long.MIN_VALUE : this.billedDuration;
      callIce.signallingFee = this.signallingFee == null ? Double.MIN_VALUE : this.signallingFee;
      callIce.rate = this.rate == null ? Double.MIN_VALUE : this.rate;
      callIce.type = this.type == null ? Integer.MIN_VALUE : this.type.value();
      callIce.claimable = this.claimable == null ? Integer.MIN_VALUE : (this.claimable ? 1 : 0);
      callIce.gateway = this.gateway == null ? Integer.MIN_VALUE : this.gateway;
      callIce.sourceProvider = this.sourceProvider == null ? Integer.MIN_VALUE : this.sourceProvider;
      callIce.destinationProvider = this.destinationProvider == null ? Integer.MIN_VALUE : this.destinationProvider;
      callIce.failReasonCode = this.failReasonCode == null ? Integer.MIN_VALUE : this.failReasonCode;
      callIce.failReason = this.failReason == null ? "\u0000" : this.failReason;
      callIce.status = this.status == null ? Integer.MIN_VALUE : this.status.value();
      callIce.didNumber = this.didNumber == null ? "\u0000" : this.didNumber;
      callIce.destinationFirstProvider = this.destinationFirstProvider == null ? Integer.MIN_VALUE : this.destinationFirstProvider;
      callIce.destinationNextProvider = this.destinationNextProvider == null ? Integer.MIN_VALUE : this.destinationNextProvider;
      callIce.sourceDialCommand = this.sourceDialCommand == null ? "\u0000" : this.sourceDialCommand;
      callIce.destinationDialCommand = this.destinationDialCommand == null ? "\u0000" : this.destinationDialCommand;
      callIce.destinationFirstDialCommand = this.destinationFirstDialCommand == null ? "\u0000" : this.destinationFirstDialCommand;
      callIce.destinationNextDialCommand = this.destinationNextDialCommand == null ? "\u0000" : this.destinationNextDialCommand;
      callIce.maxDuration = this.maxDuration == null ? Integer.MIN_VALUE : this.maxDuration;
      return callIce;
   }

   public static enum ProtocolEnum {
      IAX2(1),
      SIP(2);

      private int value;

      private ProtocolEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CallData.ProtocolEnum fromValue(int value) {
         CallData.ProtocolEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CallData.ProtocolEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum SourceDestinationTypeEnum {
      MIG33_USER(1),
      PSTN_PHONE(2);

      private int value;

      private SourceDestinationTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CallData.SourceDestinationTypeEnum fromValue(int value) {
         CallData.SourceDestinationTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CallData.SourceDestinationTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      SMS_CALLBACK(1),
      MIDLET_CALLBACK(2),
      WEB_CALLBACK(3),
      WAP_CALLBACK(4),
      TOOLBAR_CALL(5),
      MIDLET_CALL_THROUGH(6),
      DIRECT_CALL_THROUGH(7),
      MISSED_CALL_CALLBACK(8),
      MIDLET_ANONYMOUS_CALLBACK(9);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CallData.TypeEnum fromValue(int value) {
         CallData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CallData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum StatusEnum {
      PENDING(0),
      IN_PROGRESS(1),
      COMPLETED(2),
      FAILED(3);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CallData.StatusEnum fromValue(int value) {
         CallData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CallData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum InitialLegEnum {
      SOURCE(1),
      DESTINATION(2);

      private int value;

      private InitialLegEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CallData.InitialLegEnum fromValue(int value) {
         CallData.InitialLegEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CallData.InitialLegEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum MakeReceiveEnum {
      MAKE(1),
      RECEIVE(2);

      private int value;

      private MakeReceiveEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CallData.MakeReceiveEnum fromValue(int value) {
         CallData.MakeReceiveEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CallData.MakeReceiveEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
