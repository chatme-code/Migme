package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectFactory;
import Ice.OutputStream;
import IceInternal.BasicStream;
import java.util.Arrays;

public class CollectedRewardProgramTriggerSummaryDataIce extends CollectedDataIce {
   private static ObjectFactory _factory = new CollectedRewardProgramTriggerSummaryDataIce.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::CollectedDataIce", "::com::projectgoth::fusion::slice::CollectedRewardProgramTriggerSummaryDataIce"};
   public String id;
   public String host;
   public String instance;
   public int programType;
   public long minReceivedTimestamp;
   public long maxReceivedTimestamp;
   public long receivedCount;
   public long droppedCount;
   public long failedCount;
   public long successfulCount;
   public long dequeuedCount;
   public long minTimeSpentInQueue;
   public long maxTimeSpentInQueue;
   public double varianceTimeSpentInQueue;
   public double meanTimeSpentInQueue;
   public long minProcessingTimeAfterDequeue;
   public long maxProcessingTimeAfterDequeue;
   public double varianceProcessingTimeAfterDequeue;
   public double meanProcessingTimeAfterDequeue;

   public CollectedRewardProgramTriggerSummaryDataIce() {
   }

   public CollectedRewardProgramTriggerSummaryDataIce(int dataType, long createTimestamp, String id, String host, String instance, int programType, long minReceivedTimestamp, long maxReceivedTimestamp, long receivedCount, long droppedCount, long failedCount, long successfulCount, long dequeuedCount, long minTimeSpentInQueue, long maxTimeSpentInQueue, double varianceTimeSpentInQueue, double meanTimeSpentInQueue, long minProcessingTimeAfterDequeue, long maxProcessingTimeAfterDequeue, double varianceProcessingTimeAfterDequeue, double meanProcessingTimeAfterDequeue) {
      super(dataType, createTimestamp);
      this.id = id;
      this.host = host;
      this.instance = instance;
      this.programType = programType;
      this.minReceivedTimestamp = minReceivedTimestamp;
      this.maxReceivedTimestamp = maxReceivedTimestamp;
      this.receivedCount = receivedCount;
      this.droppedCount = droppedCount;
      this.failedCount = failedCount;
      this.successfulCount = successfulCount;
      this.dequeuedCount = dequeuedCount;
      this.minTimeSpentInQueue = minTimeSpentInQueue;
      this.maxTimeSpentInQueue = maxTimeSpentInQueue;
      this.varianceTimeSpentInQueue = varianceTimeSpentInQueue;
      this.meanTimeSpentInQueue = meanTimeSpentInQueue;
      this.minProcessingTimeAfterDequeue = minProcessingTimeAfterDequeue;
      this.maxProcessingTimeAfterDequeue = maxProcessingTimeAfterDequeue;
      this.varianceProcessingTimeAfterDequeue = varianceProcessingTimeAfterDequeue;
      this.meanProcessingTimeAfterDequeue = meanProcessingTimeAfterDequeue;
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
      __os.writeString(this.id);
      __os.writeString(this.host);
      __os.writeString(this.instance);
      __os.writeInt(this.programType);
      __os.writeLong(this.minReceivedTimestamp);
      __os.writeLong(this.maxReceivedTimestamp);
      __os.writeLong(this.receivedCount);
      __os.writeLong(this.droppedCount);
      __os.writeLong(this.failedCount);
      __os.writeLong(this.successfulCount);
      __os.writeLong(this.dequeuedCount);
      __os.writeLong(this.minTimeSpentInQueue);
      __os.writeLong(this.maxTimeSpentInQueue);
      __os.writeDouble(this.varianceTimeSpentInQueue);
      __os.writeDouble(this.meanTimeSpentInQueue);
      __os.writeLong(this.minProcessingTimeAfterDequeue);
      __os.writeLong(this.maxProcessingTimeAfterDequeue);
      __os.writeDouble(this.varianceProcessingTimeAfterDequeue);
      __os.writeDouble(this.meanProcessingTimeAfterDequeue);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.id = __is.readString();
      this.host = __is.readString();
      this.instance = __is.readString();
      this.programType = __is.readInt();
      this.minReceivedTimestamp = __is.readLong();
      this.maxReceivedTimestamp = __is.readLong();
      this.receivedCount = __is.readLong();
      this.droppedCount = __is.readLong();
      this.failedCount = __is.readLong();
      this.successfulCount = __is.readLong();
      this.dequeuedCount = __is.readLong();
      this.minTimeSpentInQueue = __is.readLong();
      this.maxTimeSpentInQueue = __is.readLong();
      this.varianceTimeSpentInQueue = __is.readDouble();
      this.meanTimeSpentInQueue = __is.readDouble();
      this.minProcessingTimeAfterDequeue = __is.readLong();
      this.maxProcessingTimeAfterDequeue = __is.readLong();
      this.varianceProcessingTimeAfterDequeue = __is.readDouble();
      this.meanProcessingTimeAfterDequeue = __is.readDouble();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::CollectedRewardProgramTriggerSummaryDataIce was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::CollectedRewardProgramTriggerSummaryDataIce was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(CollectedRewardProgramTriggerSummaryDataIce.ice_staticId());

         return new CollectedRewardProgramTriggerSummaryDataIce();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
