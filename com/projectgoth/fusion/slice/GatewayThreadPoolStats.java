package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;

public final class GatewayThreadPoolStats implements Cloneable, Serializable {
   public String name;
   public int threadPoolSize;
   public int maxThreadPoolSize;
   public int threadPoolQueueSize;
   public int maxThreadPoolQueueSize;
   public float requestsPerSecond;
   public float maxRequestsPerSecond;

   public GatewayThreadPoolStats() {
   }

   public GatewayThreadPoolStats(String name, int threadPoolSize, int maxThreadPoolSize, int threadPoolQueueSize, int maxThreadPoolQueueSize, float requestsPerSecond, float maxRequestsPerSecond) {
      this.name = name;
      this.threadPoolSize = threadPoolSize;
      this.maxThreadPoolSize = maxThreadPoolSize;
      this.threadPoolQueueSize = threadPoolQueueSize;
      this.maxThreadPoolQueueSize = maxThreadPoolQueueSize;
      this.requestsPerSecond = requestsPerSecond;
      this.maxRequestsPerSecond = maxRequestsPerSecond;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         GatewayThreadPoolStats _r = null;

         try {
            _r = (GatewayThreadPoolStats)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.name != _r.name && this.name != null && !this.name.equals(_r.name)) {
               return false;
            } else if (this.threadPoolSize != _r.threadPoolSize) {
               return false;
            } else if (this.maxThreadPoolSize != _r.maxThreadPoolSize) {
               return false;
            } else if (this.threadPoolQueueSize != _r.threadPoolQueueSize) {
               return false;
            } else if (this.maxThreadPoolQueueSize != _r.maxThreadPoolQueueSize) {
               return false;
            } else if (this.requestsPerSecond != _r.requestsPerSecond) {
               return false;
            } else {
               return this.maxRequestsPerSecond == _r.maxRequestsPerSecond;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      if (this.name != null) {
         __h = 5 * __h + this.name.hashCode();
      }

      __h = 5 * __h + this.threadPoolSize;
      __h = 5 * __h + this.maxThreadPoolSize;
      __h = 5 * __h + this.threadPoolQueueSize;
      __h = 5 * __h + this.maxThreadPoolQueueSize;
      __h = 5 * __h + Float.floatToIntBits(this.requestsPerSecond);
      __h = 5 * __h + Float.floatToIntBits(this.maxRequestsPerSecond);
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
      __os.writeString(this.name);
      __os.writeInt(this.threadPoolSize);
      __os.writeInt(this.maxThreadPoolSize);
      __os.writeInt(this.threadPoolQueueSize);
      __os.writeInt(this.maxThreadPoolQueueSize);
      __os.writeFloat(this.requestsPerSecond);
      __os.writeFloat(this.maxRequestsPerSecond);
   }

   public void __read(BasicStream __is) {
      this.name = __is.readString();
      this.threadPoolSize = __is.readInt();
      this.maxThreadPoolSize = __is.readInt();
      this.threadPoolQueueSize = __is.readInt();
      this.maxThreadPoolQueueSize = __is.readInt();
      this.requestsPerSecond = __is.readFloat();
      this.maxRequestsPerSecond = __is.readFloat();
   }
}
