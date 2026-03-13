package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;

public final class CallDataIce implements Cloneable, Serializable {
   public int id;
   public String username;
   public int contactID;
   public long dateCreated;
   public String source;
   public int sourceType;
   public int sourceProtocol;
   public int sourceIDDCode;
   public String destination;
   public int destinationType;
   public int destinationProtocol;
   public int destinationIDDCode;
   public int makeReceive;
   public int initialLeg;
   public long sourceDuration;
   public long destinationDuration;
   public long billedDuration;
   public double signallingFee;
   public double rate;
   public int type;
   public int claimable;
   public int gateway;
   public int sourceProvider;
   public int destinationProvider;
   public int failReasonCode;
   public String failReason;
   public int status;
   public String didNumber;
   public int destinationFirstProvider;
   public int destinationNextProvider;
   public String sourceDialCommand;
   public String destinationDialCommand;
   public String destinationFirstDialCommand;
   public String destinationNextDialCommand;
   public int maxDuration;

   public CallDataIce() {
   }

   public CallDataIce(int id, String username, int contactID, long dateCreated, String source, int sourceType, int sourceProtocol, int sourceIDDCode, String destination, int destinationType, int destinationProtocol, int destinationIDDCode, int makeReceive, int initialLeg, long sourceDuration, long destinationDuration, long billedDuration, double signallingFee, double rate, int type, int claimable, int gateway, int sourceProvider, int destinationProvider, int failReasonCode, String failReason, int status, String didNumber, int destinationFirstProvider, int destinationNextProvider, String sourceDialCommand, String destinationDialCommand, String destinationFirstDialCommand, String destinationNextDialCommand, int maxDuration) {
      this.id = id;
      this.username = username;
      this.contactID = contactID;
      this.dateCreated = dateCreated;
      this.source = source;
      this.sourceType = sourceType;
      this.sourceProtocol = sourceProtocol;
      this.sourceIDDCode = sourceIDDCode;
      this.destination = destination;
      this.destinationType = destinationType;
      this.destinationProtocol = destinationProtocol;
      this.destinationIDDCode = destinationIDDCode;
      this.makeReceive = makeReceive;
      this.initialLeg = initialLeg;
      this.sourceDuration = sourceDuration;
      this.destinationDuration = destinationDuration;
      this.billedDuration = billedDuration;
      this.signallingFee = signallingFee;
      this.rate = rate;
      this.type = type;
      this.claimable = claimable;
      this.gateway = gateway;
      this.sourceProvider = sourceProvider;
      this.destinationProvider = destinationProvider;
      this.failReasonCode = failReasonCode;
      this.failReason = failReason;
      this.status = status;
      this.didNumber = didNumber;
      this.destinationFirstProvider = destinationFirstProvider;
      this.destinationNextProvider = destinationNextProvider;
      this.sourceDialCommand = sourceDialCommand;
      this.destinationDialCommand = destinationDialCommand;
      this.destinationFirstDialCommand = destinationFirstDialCommand;
      this.destinationNextDialCommand = destinationNextDialCommand;
      this.maxDuration = maxDuration;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         CallDataIce _r = null;

         try {
            _r = (CallDataIce)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.id != _r.id) {
               return false;
            } else if (this.username != _r.username && this.username != null && !this.username.equals(_r.username)) {
               return false;
            } else if (this.contactID != _r.contactID) {
               return false;
            } else if (this.dateCreated != _r.dateCreated) {
               return false;
            } else if (this.source != _r.source && this.source != null && !this.source.equals(_r.source)) {
               return false;
            } else if (this.sourceType != _r.sourceType) {
               return false;
            } else if (this.sourceProtocol != _r.sourceProtocol) {
               return false;
            } else if (this.sourceIDDCode != _r.sourceIDDCode) {
               return false;
            } else if (this.destination != _r.destination && this.destination != null && !this.destination.equals(_r.destination)) {
               return false;
            } else if (this.destinationType != _r.destinationType) {
               return false;
            } else if (this.destinationProtocol != _r.destinationProtocol) {
               return false;
            } else if (this.destinationIDDCode != _r.destinationIDDCode) {
               return false;
            } else if (this.makeReceive != _r.makeReceive) {
               return false;
            } else if (this.initialLeg != _r.initialLeg) {
               return false;
            } else if (this.sourceDuration != _r.sourceDuration) {
               return false;
            } else if (this.destinationDuration != _r.destinationDuration) {
               return false;
            } else if (this.billedDuration != _r.billedDuration) {
               return false;
            } else if (this.signallingFee != _r.signallingFee) {
               return false;
            } else if (this.rate != _r.rate) {
               return false;
            } else if (this.type != _r.type) {
               return false;
            } else if (this.claimable != _r.claimable) {
               return false;
            } else if (this.gateway != _r.gateway) {
               return false;
            } else if (this.sourceProvider != _r.sourceProvider) {
               return false;
            } else if (this.destinationProvider != _r.destinationProvider) {
               return false;
            } else if (this.failReasonCode != _r.failReasonCode) {
               return false;
            } else if (this.failReason != _r.failReason && this.failReason != null && !this.failReason.equals(_r.failReason)) {
               return false;
            } else if (this.status != _r.status) {
               return false;
            } else if (this.didNumber != _r.didNumber && this.didNumber != null && !this.didNumber.equals(_r.didNumber)) {
               return false;
            } else if (this.destinationFirstProvider != _r.destinationFirstProvider) {
               return false;
            } else if (this.destinationNextProvider != _r.destinationNextProvider) {
               return false;
            } else if (this.sourceDialCommand != _r.sourceDialCommand && this.sourceDialCommand != null && !this.sourceDialCommand.equals(_r.sourceDialCommand)) {
               return false;
            } else if (this.destinationDialCommand != _r.destinationDialCommand && this.destinationDialCommand != null && !this.destinationDialCommand.equals(_r.destinationDialCommand)) {
               return false;
            } else if (this.destinationFirstDialCommand != _r.destinationFirstDialCommand && this.destinationFirstDialCommand != null && !this.destinationFirstDialCommand.equals(_r.destinationFirstDialCommand)) {
               return false;
            } else if (this.destinationNextDialCommand != _r.destinationNextDialCommand && this.destinationNextDialCommand != null && !this.destinationNextDialCommand.equals(_r.destinationNextDialCommand)) {
               return false;
            } else {
               return this.maxDuration == _r.maxDuration;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      int __h = 5 * __h + this.id;
      if (this.username != null) {
         __h = 5 * __h + this.username.hashCode();
      }

      __h = 5 * __h + this.contactID;
      __h = 5 * __h + (int)this.dateCreated;
      if (this.source != null) {
         __h = 5 * __h + this.source.hashCode();
      }

      __h = 5 * __h + this.sourceType;
      __h = 5 * __h + this.sourceProtocol;
      __h = 5 * __h + this.sourceIDDCode;
      if (this.destination != null) {
         __h = 5 * __h + this.destination.hashCode();
      }

      __h = 5 * __h + this.destinationType;
      __h = 5 * __h + this.destinationProtocol;
      __h = 5 * __h + this.destinationIDDCode;
      __h = 5 * __h + this.makeReceive;
      __h = 5 * __h + this.initialLeg;
      __h = 5 * __h + (int)this.sourceDuration;
      __h = 5 * __h + (int)this.destinationDuration;
      __h = 5 * __h + (int)this.billedDuration;
      __h = 5 * __h + (int)Double.doubleToLongBits(this.signallingFee);
      __h = 5 * __h + (int)Double.doubleToLongBits(this.rate);
      __h = 5 * __h + this.type;
      __h = 5 * __h + this.claimable;
      __h = 5 * __h + this.gateway;
      __h = 5 * __h + this.sourceProvider;
      __h = 5 * __h + this.destinationProvider;
      __h = 5 * __h + this.failReasonCode;
      if (this.failReason != null) {
         __h = 5 * __h + this.failReason.hashCode();
      }

      __h = 5 * __h + this.status;
      if (this.didNumber != null) {
         __h = 5 * __h + this.didNumber.hashCode();
      }

      __h = 5 * __h + this.destinationFirstProvider;
      __h = 5 * __h + this.destinationNextProvider;
      if (this.sourceDialCommand != null) {
         __h = 5 * __h + this.sourceDialCommand.hashCode();
      }

      if (this.destinationDialCommand != null) {
         __h = 5 * __h + this.destinationDialCommand.hashCode();
      }

      if (this.destinationFirstDialCommand != null) {
         __h = 5 * __h + this.destinationFirstDialCommand.hashCode();
      }

      if (this.destinationNextDialCommand != null) {
         __h = 5 * __h + this.destinationNextDialCommand.hashCode();
      }

      __h = 5 * __h + this.maxDuration;
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
      __os.writeInt(this.id);
      __os.writeString(this.username);
      __os.writeInt(this.contactID);
      __os.writeLong(this.dateCreated);
      __os.writeString(this.source);
      __os.writeInt(this.sourceType);
      __os.writeInt(this.sourceProtocol);
      __os.writeInt(this.sourceIDDCode);
      __os.writeString(this.destination);
      __os.writeInt(this.destinationType);
      __os.writeInt(this.destinationProtocol);
      __os.writeInt(this.destinationIDDCode);
      __os.writeInt(this.makeReceive);
      __os.writeInt(this.initialLeg);
      __os.writeLong(this.sourceDuration);
      __os.writeLong(this.destinationDuration);
      __os.writeLong(this.billedDuration);
      __os.writeDouble(this.signallingFee);
      __os.writeDouble(this.rate);
      __os.writeInt(this.type);
      __os.writeInt(this.claimable);
      __os.writeInt(this.gateway);
      __os.writeInt(this.sourceProvider);
      __os.writeInt(this.destinationProvider);
      __os.writeInt(this.failReasonCode);
      __os.writeString(this.failReason);
      __os.writeInt(this.status);
      __os.writeString(this.didNumber);
      __os.writeInt(this.destinationFirstProvider);
      __os.writeInt(this.destinationNextProvider);
      __os.writeString(this.sourceDialCommand);
      __os.writeString(this.destinationDialCommand);
      __os.writeString(this.destinationFirstDialCommand);
      __os.writeString(this.destinationNextDialCommand);
      __os.writeInt(this.maxDuration);
   }

   public void __read(BasicStream __is) {
      this.id = __is.readInt();
      this.username = __is.readString();
      this.contactID = __is.readInt();
      this.dateCreated = __is.readLong();
      this.source = __is.readString();
      this.sourceType = __is.readInt();
      this.sourceProtocol = __is.readInt();
      this.sourceIDDCode = __is.readInt();
      this.destination = __is.readString();
      this.destinationType = __is.readInt();
      this.destinationProtocol = __is.readInt();
      this.destinationIDDCode = __is.readInt();
      this.makeReceive = __is.readInt();
      this.initialLeg = __is.readInt();
      this.sourceDuration = __is.readLong();
      this.destinationDuration = __is.readLong();
      this.billedDuration = __is.readLong();
      this.signallingFee = __is.readDouble();
      this.rate = __is.readDouble();
      this.type = __is.readInt();
      this.claimable = __is.readInt();
      this.gateway = __is.readInt();
      this.sourceProvider = __is.readInt();
      this.destinationProvider = __is.readInt();
      this.failReasonCode = __is.readInt();
      this.failReason = __is.readString();
      this.status = __is.readInt();
      this.didNumber = __is.readString();
      this.destinationFirstProvider = __is.readInt();
      this.destinationNextProvider = __is.readInt();
      this.sourceDialCommand = __is.readString();
      this.destinationDialCommand = __is.readString();
      this.destinationFirstDialCommand = __is.readString();
      this.destinationNextDialCommand = __is.readString();
      this.maxDuration = __is.readInt();
   }
}
