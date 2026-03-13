package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;

public final class SessionMetricsIce implements Cloneable, Serializable {
   public short uniqueUsersPrivateChattedWith;
   public short privateMessagesSent;
   public short groupMessagesSent;
   public short chatroomMessagesSent;
   public short groupChatsEntered;
   public short chatroomsEntered;
   public short uniqueChatroomsEntered;
   public short statusMessagesSet;
   public short profileEdited;
   public short photosUploaded;
   public short inviteByPhoneNumber;
   public short inviteByUsername;
   public short themeUpdated;

   public SessionMetricsIce() {
   }

   public SessionMetricsIce(short uniqueUsersPrivateChattedWith, short privateMessagesSent, short groupMessagesSent, short chatroomMessagesSent, short groupChatsEntered, short chatroomsEntered, short uniqueChatroomsEntered, short statusMessagesSet, short profileEdited, short photosUploaded, short inviteByPhoneNumber, short inviteByUsername, short themeUpdated) {
      this.uniqueUsersPrivateChattedWith = uniqueUsersPrivateChattedWith;
      this.privateMessagesSent = privateMessagesSent;
      this.groupMessagesSent = groupMessagesSent;
      this.chatroomMessagesSent = chatroomMessagesSent;
      this.groupChatsEntered = groupChatsEntered;
      this.chatroomsEntered = chatroomsEntered;
      this.uniqueChatroomsEntered = uniqueChatroomsEntered;
      this.statusMessagesSet = statusMessagesSet;
      this.profileEdited = profileEdited;
      this.photosUploaded = photosUploaded;
      this.inviteByPhoneNumber = inviteByPhoneNumber;
      this.inviteByUsername = inviteByUsername;
      this.themeUpdated = themeUpdated;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         SessionMetricsIce _r = null;

         try {
            _r = (SessionMetricsIce)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.uniqueUsersPrivateChattedWith != _r.uniqueUsersPrivateChattedWith) {
               return false;
            } else if (this.privateMessagesSent != _r.privateMessagesSent) {
               return false;
            } else if (this.groupMessagesSent != _r.groupMessagesSent) {
               return false;
            } else if (this.chatroomMessagesSent != _r.chatroomMessagesSent) {
               return false;
            } else if (this.groupChatsEntered != _r.groupChatsEntered) {
               return false;
            } else if (this.chatroomsEntered != _r.chatroomsEntered) {
               return false;
            } else if (this.uniqueChatroomsEntered != _r.uniqueChatroomsEntered) {
               return false;
            } else if (this.statusMessagesSet != _r.statusMessagesSet) {
               return false;
            } else if (this.profileEdited != _r.profileEdited) {
               return false;
            } else if (this.photosUploaded != _r.photosUploaded) {
               return false;
            } else if (this.inviteByPhoneNumber != _r.inviteByPhoneNumber) {
               return false;
            } else if (this.inviteByUsername != _r.inviteByUsername) {
               return false;
            } else {
               return this.themeUpdated == _r.themeUpdated;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      int __h = 5 * __h + this.uniqueUsersPrivateChattedWith;
      __h = 5 * __h + this.privateMessagesSent;
      __h = 5 * __h + this.groupMessagesSent;
      __h = 5 * __h + this.chatroomMessagesSent;
      __h = 5 * __h + this.groupChatsEntered;
      __h = 5 * __h + this.chatroomsEntered;
      __h = 5 * __h + this.uniqueChatroomsEntered;
      __h = 5 * __h + this.statusMessagesSet;
      __h = 5 * __h + this.profileEdited;
      __h = 5 * __h + this.photosUploaded;
      __h = 5 * __h + this.inviteByPhoneNumber;
      __h = 5 * __h + this.inviteByUsername;
      __h = 5 * __h + this.themeUpdated;
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
      __os.writeShort(this.uniqueUsersPrivateChattedWith);
      __os.writeShort(this.privateMessagesSent);
      __os.writeShort(this.groupMessagesSent);
      __os.writeShort(this.chatroomMessagesSent);
      __os.writeShort(this.groupChatsEntered);
      __os.writeShort(this.chatroomsEntered);
      __os.writeShort(this.uniqueChatroomsEntered);
      __os.writeShort(this.statusMessagesSet);
      __os.writeShort(this.profileEdited);
      __os.writeShort(this.photosUploaded);
      __os.writeShort(this.inviteByPhoneNumber);
      __os.writeShort(this.inviteByUsername);
      __os.writeShort(this.themeUpdated);
   }

   public void __read(BasicStream __is) {
      this.uniqueUsersPrivateChattedWith = __is.readShort();
      this.privateMessagesSent = __is.readShort();
      this.groupMessagesSent = __is.readShort();
      this.chatroomMessagesSent = __is.readShort();
      this.groupChatsEntered = __is.readShort();
      this.chatroomsEntered = __is.readShort();
      this.uniqueChatroomsEntered = __is.readShort();
      this.statusMessagesSet = __is.readShort();
      this.profileEdited = __is.readShort();
      this.photosUploaded = __is.readShort();
      this.inviteByPhoneNumber = __is.readShort();
      this.inviteByUsername = __is.readShort();
      this.themeUpdated = __is.readShort();
   }
}
