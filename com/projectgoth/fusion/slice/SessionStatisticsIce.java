package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;

public final class SessionStatisticsIce implements Cloneable, Serializable {
   public int uniquePrivateChatUsers;
   public int profileEdits;

   public SessionStatisticsIce() {
   }

   public SessionStatisticsIce(int uniquePrivateChatUsers, int profileEdits) {
      this.uniquePrivateChatUsers = uniquePrivateChatUsers;
      this.profileEdits = profileEdits;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         SessionStatisticsIce _r = null;

         try {
            _r = (SessionStatisticsIce)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.uniquePrivateChatUsers != _r.uniquePrivateChatUsers) {
               return false;
            } else {
               return this.profileEdits == _r.profileEdits;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      int __h = 5 * __h + this.uniquePrivateChatUsers;
      __h = 5 * __h + this.profileEdits;
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
      __os.writeInt(this.uniquePrivateChatUsers);
      __os.writeInt(this.profileEdits);
   }

   public void __read(BasicStream __is) {
      this.uniquePrivateChatUsers = __is.readInt();
      this.profileEdits = __is.readInt();
   }
}
