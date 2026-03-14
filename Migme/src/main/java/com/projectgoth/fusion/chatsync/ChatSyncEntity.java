package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.slice.FusionException;
import java.util.List;

public interface ChatSyncEntity {
   int SECONDS_IN_HOUR = 3600;

   String getKey();

   String getValue();

   void store(ChatSyncStore[] var1) throws FusionException;

   void unpack(String var1, String var2) throws FusionException;

   ChatSyncEntity.ChatSyncEntityType getEntityType();

   void retrieve(ChatSyncStore[] var1) throws FusionException;

   void retrievePipeline(ChatSyncStore var1) throws FusionException;

   void storePipeline(ChatSyncStore var1) throws FusionException;

   int loadPipeline(List<Object> var1, int var2) throws FusionException;

   int processPipelineStoreResults(List<Object> var1, int var2) throws FusionException;

   boolean canRetryReads() throws FusionException;

   boolean canRetryWrites() throws FusionException;

   public static enum ChatSyncEntityType {
      CONVERSATION,
      MESSAGE,
      USER,
      MESSAGE_STATUS_EVENT;
   }
}
