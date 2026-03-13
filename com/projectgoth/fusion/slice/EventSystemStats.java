package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectFactory;
import Ice.OutputStream;
import IceInternal.BasicStream;
import java.util.Arrays;
import java.util.Map;

public class EventSystemStats extends BaseServiceStats {
   private static ObjectFactory _factory = new EventSystemStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BaseServiceStats", "::com::projectgoth::fusion::slice::EventSystemStats"};
   public float eventsReceivedPerSecond;
   public long genericApplicationEvents;
   public long setProfileStatusEvents;
   public long madePhotoPublicEvents;
   public long createdPublicChatrooomStatusEvents;
   public long addedFriendEvents;
   public long updatedProfileEvents;
   public long purchasedVirtualGoodsEvents;
   public long virtualGiftsEvents;
   public long giftShowerEvents;
   public long userWallPostEvents;
   public long totalEvents;
   public long droppedEvents;
   public long streamedEvents;
   public long distributedEvents;
   public int genericApplicationEventRate;
   public int giftShowerEventRate;
   public int setProfileStatusRate;
   public int madePhotoPublicRate;
   public int createdPublicChatrooomStatusRate;
   public int addedFriendRate;
   public int updatedProfileRate;
   public int purchasedVirtualGoodsRate;
   public int virtualGiftsRate;
   public int userWallPostRate;
   public int totalRate;
   public int maxTotalRate;
   public int droppedRate;
   public int maxDroppedRate;
   public int streamedRate;
   public int maxStreamedRate;
   public int distributedRate;
   public int maxDistributedRate;

   public EventSystemStats() {
   }

   public EventSystemStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats, float eventsReceivedPerSecond, long genericApplicationEvents, long setProfileStatusEvents, long madePhotoPublicEvents, long createdPublicChatrooomStatusEvents, long addedFriendEvents, long updatedProfileEvents, long purchasedVirtualGoodsEvents, long virtualGiftsEvents, long giftShowerEvents, long userWallPostEvents, long totalEvents, long droppedEvents, long streamedEvents, long distributedEvents, int genericApplicationEventRate, int giftShowerEventRate, int setProfileStatusRate, int madePhotoPublicRate, int createdPublicChatrooomStatusRate, int addedFriendRate, int updatedProfileRate, int purchasedVirtualGoodsRate, int virtualGiftsRate, int userWallPostRate, int totalRate, int maxTotalRate, int droppedRate, int maxDroppedRate, int streamedRate, int maxStreamedRate, int distributedRate, int maxDistributedRate) {
      super(hostname, version, jvmTotalMemory, jvmFreeMemory, uptime, lastUpdatedTime, izeRequestStatsEnabled, izeRequestCount, izeRequestCountByOrigin, izeRequestCountByOperation, izeRequestMeanProcessingTimeByOperation, izeRequestMaxProcessingTimeByOperation, izeRequestStdevProcessingTimeByOperation, izeRequest95thPercentileProcessingTimeByOperation, izeRequestTotalProcessingTimeByOperation, izeThreadStatsEnabled, izeObjectAdapterThreadPoolRunning, izeObjectAdapterThreadPoolInUse, izeObjectAdapterThreadPoolInUseHighWatermark, izeObjectAdapterThreadPoolLoad, izeObjectAdapterThreadPoolSize, izeObjectAdapterThreadPoolSizeMax, izeObjectAdapterThreadPoolSizeWarn, izeClientThreadPoolStatsEnabled, izeClientThreadPoolRunning, izeClientThreadPoolInUse, izeClientThreadPoolInUseHighWatermark, izeClientThreadPoolLoad, izeClientThreadPoolSize, izeClientThreadPoolSizeMax, izeClientThreadPoolSizeWarn, amdObjectAdapterThreadPoolRunning, amdObjectAdapterThreadPoolInUse, amdObjectAdapterThreadPoolInUseHighWatermark, amdObjectAdapterThreadPoolSize, amdObjectAdapterThreadPoolSizeMax, amdObjectAdapterThreadPoolQueueLength, dataGridStats);
      this.eventsReceivedPerSecond = eventsReceivedPerSecond;
      this.genericApplicationEvents = genericApplicationEvents;
      this.setProfileStatusEvents = setProfileStatusEvents;
      this.madePhotoPublicEvents = madePhotoPublicEvents;
      this.createdPublicChatrooomStatusEvents = createdPublicChatrooomStatusEvents;
      this.addedFriendEvents = addedFriendEvents;
      this.updatedProfileEvents = updatedProfileEvents;
      this.purchasedVirtualGoodsEvents = purchasedVirtualGoodsEvents;
      this.virtualGiftsEvents = virtualGiftsEvents;
      this.giftShowerEvents = giftShowerEvents;
      this.userWallPostEvents = userWallPostEvents;
      this.totalEvents = totalEvents;
      this.droppedEvents = droppedEvents;
      this.streamedEvents = streamedEvents;
      this.distributedEvents = distributedEvents;
      this.genericApplicationEventRate = genericApplicationEventRate;
      this.giftShowerEventRate = giftShowerEventRate;
      this.setProfileStatusRate = setProfileStatusRate;
      this.madePhotoPublicRate = madePhotoPublicRate;
      this.createdPublicChatrooomStatusRate = createdPublicChatrooomStatusRate;
      this.addedFriendRate = addedFriendRate;
      this.updatedProfileRate = updatedProfileRate;
      this.purchasedVirtualGoodsRate = purchasedVirtualGoodsRate;
      this.virtualGiftsRate = virtualGiftsRate;
      this.userWallPostRate = userWallPostRate;
      this.totalRate = totalRate;
      this.maxTotalRate = maxTotalRate;
      this.droppedRate = droppedRate;
      this.maxDroppedRate = maxDroppedRate;
      this.streamedRate = streamedRate;
      this.maxStreamedRate = maxStreamedRate;
      this.distributedRate = distributedRate;
      this.maxDistributedRate = maxDistributedRate;
   }

   public static ObjectFactory ice_factory() {
      return _factory;
   }

   public boolean ice_isA(String s) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public boolean ice_isA(String s, Current __current) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public String[] ice_ids() {
      return __ids;
   }

   public String[] ice_ids(Current __current) {
      return __ids;
   }

   public String ice_id() {
      return __ids[2];
   }

   public String ice_id(Current __current) {
      return __ids[2];
   }

   public static String ice_staticId() {
      return __ids[2];
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.writeFloat(this.eventsReceivedPerSecond);
      __os.writeLong(this.genericApplicationEvents);
      __os.writeLong(this.setProfileStatusEvents);
      __os.writeLong(this.madePhotoPublicEvents);
      __os.writeLong(this.createdPublicChatrooomStatusEvents);
      __os.writeLong(this.addedFriendEvents);
      __os.writeLong(this.updatedProfileEvents);
      __os.writeLong(this.purchasedVirtualGoodsEvents);
      __os.writeLong(this.virtualGiftsEvents);
      __os.writeLong(this.giftShowerEvents);
      __os.writeLong(this.userWallPostEvents);
      __os.writeLong(this.totalEvents);
      __os.writeLong(this.droppedEvents);
      __os.writeLong(this.streamedEvents);
      __os.writeLong(this.distributedEvents);
      __os.writeInt(this.genericApplicationEventRate);
      __os.writeInt(this.giftShowerEventRate);
      __os.writeInt(this.setProfileStatusRate);
      __os.writeInt(this.madePhotoPublicRate);
      __os.writeInt(this.createdPublicChatrooomStatusRate);
      __os.writeInt(this.addedFriendRate);
      __os.writeInt(this.updatedProfileRate);
      __os.writeInt(this.purchasedVirtualGoodsRate);
      __os.writeInt(this.virtualGiftsRate);
      __os.writeInt(this.userWallPostRate);
      __os.writeInt(this.totalRate);
      __os.writeInt(this.maxTotalRate);
      __os.writeInt(this.droppedRate);
      __os.writeInt(this.maxDroppedRate);
      __os.writeInt(this.streamedRate);
      __os.writeInt(this.maxStreamedRate);
      __os.writeInt(this.distributedRate);
      __os.writeInt(this.maxDistributedRate);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.eventsReceivedPerSecond = __is.readFloat();
      this.genericApplicationEvents = __is.readLong();
      this.setProfileStatusEvents = __is.readLong();
      this.madePhotoPublicEvents = __is.readLong();
      this.createdPublicChatrooomStatusEvents = __is.readLong();
      this.addedFriendEvents = __is.readLong();
      this.updatedProfileEvents = __is.readLong();
      this.purchasedVirtualGoodsEvents = __is.readLong();
      this.virtualGiftsEvents = __is.readLong();
      this.giftShowerEvents = __is.readLong();
      this.userWallPostEvents = __is.readLong();
      this.totalEvents = __is.readLong();
      this.droppedEvents = __is.readLong();
      this.streamedEvents = __is.readLong();
      this.distributedEvents = __is.readLong();
      this.genericApplicationEventRate = __is.readInt();
      this.giftShowerEventRate = __is.readInt();
      this.setProfileStatusRate = __is.readInt();
      this.madePhotoPublicRate = __is.readInt();
      this.createdPublicChatrooomStatusRate = __is.readInt();
      this.addedFriendRate = __is.readInt();
      this.updatedProfileRate = __is.readInt();
      this.purchasedVirtualGoodsRate = __is.readInt();
      this.virtualGiftsRate = __is.readInt();
      this.userWallPostRate = __is.readInt();
      this.totalRate = __is.readInt();
      this.maxTotalRate = __is.readInt();
      this.droppedRate = __is.readInt();
      this.maxDroppedRate = __is.readInt();
      this.streamedRate = __is.readInt();
      this.maxStreamedRate = __is.readInt();
      this.distributedRate = __is.readInt();
      this.maxDistributedRate = __is.readInt();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::EventSystemStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::EventSystemStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(EventSystemStats.ice_staticId());

         return new EventSystemStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
