package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;

public final class ServiceStatsLongFieldValue implements Cloneable, Serializable {
   public long value;
   public long lastUpdatedTime;

   public ServiceStatsLongFieldValue() {
   }

   public ServiceStatsLongFieldValue(long value, long lastUpdatedTime) {
      this.value = value;
      this.lastUpdatedTime = lastUpdatedTime;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         ServiceStatsLongFieldValue _r = null;

         try {
            _r = (ServiceStatsLongFieldValue)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.value != _r.value) {
               return false;
            } else {
               return this.lastUpdatedTime == _r.lastUpdatedTime;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      int __h = 5 * __h + (int)this.value;
      __h = 5 * __h + (int)this.lastUpdatedTime;
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
      __os.writeLong(this.value);
      __os.writeLong(this.lastUpdatedTime);
   }

   public void __read(BasicStream __is) {
      this.value = __is.readLong();
      this.lastUpdatedTime = __is.readLong();
   }
}
