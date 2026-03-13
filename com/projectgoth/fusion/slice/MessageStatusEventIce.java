package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;

public final class MessageStatusEventIce implements Cloneable, Serializable {
   public int messageType;
   public String messageSource;
   public int messageDestinationType;
   public String messageDestination;
   public String messageGUID;
   public int messageStatus;
   public boolean serverGenerated;
   public long messageTimestamp;

   public MessageStatusEventIce() {
   }

   public MessageStatusEventIce(int messageType, String messageSource, int messageDestinationType, String messageDestination, String messageGUID, int messageStatus, boolean serverGenerated, long messageTimestamp) {
      this.messageType = messageType;
      this.messageSource = messageSource;
      this.messageDestinationType = messageDestinationType;
      this.messageDestination = messageDestination;
      this.messageGUID = messageGUID;
      this.messageStatus = messageStatus;
      this.serverGenerated = serverGenerated;
      this.messageTimestamp = messageTimestamp;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         MessageStatusEventIce _r = null;

         try {
            _r = (MessageStatusEventIce)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.messageType != _r.messageType) {
               return false;
            } else if (this.messageSource != _r.messageSource && this.messageSource != null && !this.messageSource.equals(_r.messageSource)) {
               return false;
            } else if (this.messageDestinationType != _r.messageDestinationType) {
               return false;
            } else if (this.messageDestination != _r.messageDestination && this.messageDestination != null && !this.messageDestination.equals(_r.messageDestination)) {
               return false;
            } else if (this.messageGUID != _r.messageGUID && this.messageGUID != null && !this.messageGUID.equals(_r.messageGUID)) {
               return false;
            } else if (this.messageStatus != _r.messageStatus) {
               return false;
            } else if (this.serverGenerated != _r.serverGenerated) {
               return false;
            } else {
               return this.messageTimestamp == _r.messageTimestamp;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      int __h = 5 * __h + this.messageType;
      if (this.messageSource != null) {
         __h = 5 * __h + this.messageSource.hashCode();
      }

      __h = 5 * __h + this.messageDestinationType;
      if (this.messageDestination != null) {
         __h = 5 * __h + this.messageDestination.hashCode();
      }

      if (this.messageGUID != null) {
         __h = 5 * __h + this.messageGUID.hashCode();
      }

      __h = 5 * __h + this.messageStatus;
      __h = 5 * __h + (this.serverGenerated ? 1 : 0);
      __h = 5 * __h + (int)this.messageTimestamp;
      return __h;
   }

   public Object clone() {
      Object o = null;

      try {
         o = super.clone();
      } catch (CloneNotSupportedException var3) {
         assert false;
      }

      return o;
   }

   public void __write(BasicStream __os) {
      __os.writeInt(this.messageType);
      __os.writeString(this.messageSource);
      __os.writeInt(this.messageDestinationType);
      __os.writeString(this.messageDestination);
      __os.writeString(this.messageGUID);
      __os.writeInt(this.messageStatus);
      __os.writeBool(this.serverGenerated);
      __os.writeLong(this.messageTimestamp);
   }

   public void __read(BasicStream __is) {
      this.messageType = __is.readInt();
      this.messageSource = __is.readString();
      this.messageDestinationType = __is.readInt();
      this.messageDestination = __is.readString();
      this.messageGUID = __is.readString();
      this.messageStatus = __is.readInt();
      this.serverGenerated = __is.readBool();
      this.messageTimestamp = __is.readLong();
   }
}
