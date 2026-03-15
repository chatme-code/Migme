package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;

public final class EmailAlertStats implements Cloneable, Serializable {
   public String hostName;
   public float numNotificationsReceivedPerSecond;
   public float maxNotificationsReceivedPerSecond;
   public float numNotificationsProcessedPerSecond;
   public float maxNotificationsProcessedPerSecond;
   public int notificationsThreadPoolSize;
   public int notificationsMaxThreadPoolSize;
   public int notificationsThreadPoolQueueSize;
   public int gatewayQueriesThreadPoolSize;
   public int gatewayQueriesMaxThreadPoolSize;
   public int gatewayQueriesThreadPoolQueueSize;
   public float numGatewayQueriesReceivedPerSecond;
   public float maxGatewayQueriesReceivedPerSecond;
   public float numGatewayQueriesProcessedPerSecond;
   public float maxGatewayQueriesProcessedPerSecond;
   public float numGatewayQueriesDiscardedPerSecond;
   public float maxGatewayQueriesDiscardedPerSecond;
   public long jvmTotalMemory;
   public long jvmFreeMemory;
   public long uptime;

   public EmailAlertStats() {
   }

   public EmailAlertStats(String hostName, float numNotificationsReceivedPerSecond, float maxNotificationsReceivedPerSecond, float numNotificationsProcessedPerSecond, float maxNotificationsProcessedPerSecond, int notificationsThreadPoolSize, int notificationsMaxThreadPoolSize, int notificationsThreadPoolQueueSize, int gatewayQueriesThreadPoolSize, int gatewayQueriesMaxThreadPoolSize, int gatewayQueriesThreadPoolQueueSize, float numGatewayQueriesReceivedPerSecond, float maxGatewayQueriesReceivedPerSecond, float numGatewayQueriesProcessedPerSecond, float maxGatewayQueriesProcessedPerSecond, float numGatewayQueriesDiscardedPerSecond, float maxGatewayQueriesDiscardedPerSecond, long jvmTotalMemory, long jvmFreeMemory, long uptime) {
      this.hostName = hostName;
      this.numNotificationsReceivedPerSecond = numNotificationsReceivedPerSecond;
      this.maxNotificationsReceivedPerSecond = maxNotificationsReceivedPerSecond;
      this.numNotificationsProcessedPerSecond = numNotificationsProcessedPerSecond;
      this.maxNotificationsProcessedPerSecond = maxNotificationsProcessedPerSecond;
      this.notificationsThreadPoolSize = notificationsThreadPoolSize;
      this.notificationsMaxThreadPoolSize = notificationsMaxThreadPoolSize;
      this.notificationsThreadPoolQueueSize = notificationsThreadPoolQueueSize;
      this.gatewayQueriesThreadPoolSize = gatewayQueriesThreadPoolSize;
      this.gatewayQueriesMaxThreadPoolSize = gatewayQueriesMaxThreadPoolSize;
      this.gatewayQueriesThreadPoolQueueSize = gatewayQueriesThreadPoolQueueSize;
      this.numGatewayQueriesReceivedPerSecond = numGatewayQueriesReceivedPerSecond;
      this.maxGatewayQueriesReceivedPerSecond = maxGatewayQueriesReceivedPerSecond;
      this.numGatewayQueriesProcessedPerSecond = numGatewayQueriesProcessedPerSecond;
      this.maxGatewayQueriesProcessedPerSecond = maxGatewayQueriesProcessedPerSecond;
      this.numGatewayQueriesDiscardedPerSecond = numGatewayQueriesDiscardedPerSecond;
      this.maxGatewayQueriesDiscardedPerSecond = maxGatewayQueriesDiscardedPerSecond;
      this.jvmTotalMemory = jvmTotalMemory;
      this.jvmFreeMemory = jvmFreeMemory;
      this.uptime = uptime;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         EmailAlertStats _r = null;

         try {
            _r = (EmailAlertStats)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.hostName != _r.hostName && this.hostName != null && !this.hostName.equals(_r.hostName)) {
               return false;
            } else if (this.numNotificationsReceivedPerSecond != _r.numNotificationsReceivedPerSecond) {
               return false;
            } else if (this.maxNotificationsReceivedPerSecond != _r.maxNotificationsReceivedPerSecond) {
               return false;
            } else if (this.numNotificationsProcessedPerSecond != _r.numNotificationsProcessedPerSecond) {
               return false;
            } else if (this.maxNotificationsProcessedPerSecond != _r.maxNotificationsProcessedPerSecond) {
               return false;
            } else if (this.notificationsThreadPoolSize != _r.notificationsThreadPoolSize) {
               return false;
            } else if (this.notificationsMaxThreadPoolSize != _r.notificationsMaxThreadPoolSize) {
               return false;
            } else if (this.notificationsThreadPoolQueueSize != _r.notificationsThreadPoolQueueSize) {
               return false;
            } else if (this.gatewayQueriesThreadPoolSize != _r.gatewayQueriesThreadPoolSize) {
               return false;
            } else if (this.gatewayQueriesMaxThreadPoolSize != _r.gatewayQueriesMaxThreadPoolSize) {
               return false;
            } else if (this.gatewayQueriesThreadPoolQueueSize != _r.gatewayQueriesThreadPoolQueueSize) {
               return false;
            } else if (this.numGatewayQueriesReceivedPerSecond != _r.numGatewayQueriesReceivedPerSecond) {
               return false;
            } else if (this.maxGatewayQueriesReceivedPerSecond != _r.maxGatewayQueriesReceivedPerSecond) {
               return false;
            } else if (this.numGatewayQueriesProcessedPerSecond != _r.numGatewayQueriesProcessedPerSecond) {
               return false;
            } else if (this.maxGatewayQueriesProcessedPerSecond != _r.maxGatewayQueriesProcessedPerSecond) {
               return false;
            } else if (this.numGatewayQueriesDiscardedPerSecond != _r.numGatewayQueriesDiscardedPerSecond) {
               return false;
            } else if (this.maxGatewayQueriesDiscardedPerSecond != _r.maxGatewayQueriesDiscardedPerSecond) {
               return false;
            } else if (this.jvmTotalMemory != _r.jvmTotalMemory) {
               return false;
            } else if (this.jvmFreeMemory != _r.jvmFreeMemory) {
               return false;
            } else {
               return this.uptime == _r.uptime;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      if (this.hostName != null) {
         __h = 5 * __h + this.hostName.hashCode();
      }

      __h = 5 * __h + Float.floatToIntBits(this.numNotificationsReceivedPerSecond);
      __h = 5 * __h + Float.floatToIntBits(this.maxNotificationsReceivedPerSecond);
      __h = 5 * __h + Float.floatToIntBits(this.numNotificationsProcessedPerSecond);
      __h = 5 * __h + Float.floatToIntBits(this.maxNotificationsProcessedPerSecond);
      __h = 5 * __h + this.notificationsThreadPoolSize;
      __h = 5 * __h + this.notificationsMaxThreadPoolSize;
      __h = 5 * __h + this.notificationsThreadPoolQueueSize;
      __h = 5 * __h + this.gatewayQueriesThreadPoolSize;
      __h = 5 * __h + this.gatewayQueriesMaxThreadPoolSize;
      __h = 5 * __h + this.gatewayQueriesThreadPoolQueueSize;
      __h = 5 * __h + Float.floatToIntBits(this.numGatewayQueriesReceivedPerSecond);
      __h = 5 * __h + Float.floatToIntBits(this.maxGatewayQueriesReceivedPerSecond);
      __h = 5 * __h + Float.floatToIntBits(this.numGatewayQueriesProcessedPerSecond);
      __h = 5 * __h + Float.floatToIntBits(this.maxGatewayQueriesProcessedPerSecond);
      __h = 5 * __h + Float.floatToIntBits(this.numGatewayQueriesDiscardedPerSecond);
      __h = 5 * __h + Float.floatToIntBits(this.maxGatewayQueriesDiscardedPerSecond);
      __h = 5 * __h + (int)this.jvmTotalMemory;
      __h = 5 * __h + (int)this.jvmFreeMemory;
      __h = 5 * __h + (int)this.uptime;
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
      __os.writeString(this.hostName);
      __os.writeFloat(this.numNotificationsReceivedPerSecond);
      __os.writeFloat(this.maxNotificationsReceivedPerSecond);
      __os.writeFloat(this.numNotificationsProcessedPerSecond);
      __os.writeFloat(this.maxNotificationsProcessedPerSecond);
      __os.writeInt(this.notificationsThreadPoolSize);
      __os.writeInt(this.notificationsMaxThreadPoolSize);
      __os.writeInt(this.notificationsThreadPoolQueueSize);
      __os.writeInt(this.gatewayQueriesThreadPoolSize);
      __os.writeInt(this.gatewayQueriesMaxThreadPoolSize);
      __os.writeInt(this.gatewayQueriesThreadPoolQueueSize);
      __os.writeFloat(this.numGatewayQueriesReceivedPerSecond);
      __os.writeFloat(this.maxGatewayQueriesReceivedPerSecond);
      __os.writeFloat(this.numGatewayQueriesProcessedPerSecond);
      __os.writeFloat(this.maxGatewayQueriesProcessedPerSecond);
      __os.writeFloat(this.numGatewayQueriesDiscardedPerSecond);
      __os.writeFloat(this.maxGatewayQueriesDiscardedPerSecond);
      __os.writeLong(this.jvmTotalMemory);
      __os.writeLong(this.jvmFreeMemory);
      __os.writeLong(this.uptime);
   }

   public void __read(BasicStream __is) {
      this.hostName = __is.readString();
      this.numNotificationsReceivedPerSecond = __is.readFloat();
      this.maxNotificationsReceivedPerSecond = __is.readFloat();
      this.numNotificationsProcessedPerSecond = __is.readFloat();
      this.maxNotificationsProcessedPerSecond = __is.readFloat();
      this.notificationsThreadPoolSize = __is.readInt();
      this.notificationsMaxThreadPoolSize = __is.readInt();
      this.notificationsThreadPoolQueueSize = __is.readInt();
      this.gatewayQueriesThreadPoolSize = __is.readInt();
      this.gatewayQueriesMaxThreadPoolSize = __is.readInt();
      this.gatewayQueriesThreadPoolQueueSize = __is.readInt();
      this.numGatewayQueriesReceivedPerSecond = __is.readFloat();
      this.maxGatewayQueriesReceivedPerSecond = __is.readFloat();
      this.numGatewayQueriesProcessedPerSecond = __is.readFloat();
      this.maxGatewayQueriesProcessedPerSecond = __is.readFloat();
      this.numGatewayQueriesDiscardedPerSecond = __is.readFloat();
      this.maxGatewayQueriesDiscardedPerSecond = __is.readFloat();
      this.jvmTotalMemory = __is.readLong();
      this.jvmFreeMemory = __is.readLong();
      this.uptime = __is.readLong();
   }
}
