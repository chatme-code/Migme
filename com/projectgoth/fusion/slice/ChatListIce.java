package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;
import java.util.Arrays;

public final class ChatListIce implements Cloneable, Serializable {
   public int userID;
   public int chatListVersion;
   public String[] chatIDs;

   public ChatListIce() {
   }

   public ChatListIce(int userID, int chatListVersion, String[] chatIDs) {
      this.userID = userID;
      this.chatListVersion = chatListVersion;
      this.chatIDs = chatIDs;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         ChatListIce _r = null;

         try {
            _r = (ChatListIce)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.userID != _r.userID) {
               return false;
            } else if (this.chatListVersion != _r.chatListVersion) {
               return false;
            } else {
               return Arrays.equals(this.chatIDs, _r.chatIDs);
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      int __h = 5 * __h + this.userID;
      __h = 5 * __h + this.chatListVersion;
      if (this.chatIDs != null) {
         for(int __i0 = 0; __i0 < this.chatIDs.length; ++__i0) {
            if (this.chatIDs[__i0] != null) {
               __h = 5 * __h + this.chatIDs[__i0].hashCode();
            }
         }
      }

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
      __os.writeInt(this.userID);
      __os.writeInt(this.chatListVersion);
      StringArrayHelper.write(__os, this.chatIDs);
   }

   public void __read(BasicStream __is) {
      this.userID = __is.readInt();
      this.chatListVersion = __is.readInt();
      this.chatIDs = StringArrayHelper.read(__is);
   }
}
