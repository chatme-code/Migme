package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;
import java.util.Arrays;

public final class SuspectGroupIce implements Cloneable, Serializable {
   public SuspectIce[] members;
   public int innocentPortCount;

   public SuspectGroupIce() {
   }

   public SuspectGroupIce(SuspectIce[] members, int innocentPortCount) {
      this.members = members;
      this.innocentPortCount = innocentPortCount;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         SuspectGroupIce _r = null;

         try {
            _r = (SuspectGroupIce)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (!Arrays.equals(this.members, _r.members)) {
               return false;
            } else {
               return this.innocentPortCount == _r.innocentPortCount;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      if (this.members != null) {
         for(int __i0 = 0; __i0 < this.members.length; ++__i0) {
            if (this.members[__i0] != null) {
               __h = 5 * __h + this.members[__i0].hashCode();
            }
         }
      }

      __h = 5 * __h + this.innocentPortCount;
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
      SuspectIceArrayHelper.write(__os, this.members);
      __os.writeInt(this.innocentPortCount);
   }

   public void __read(BasicStream __is) {
      this.members = SuspectIceArrayHelper.read(__is);
      this.innocentPortCount = __is.readInt();
   }
}
