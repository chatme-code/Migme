package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.slice.FusionException;
import java.util.List;

public class ChatSyncPipelineOp {
   private ChatSyncEntity entity;
   private ChatSyncPipelineOp.OpType opType;

   public ChatSyncPipelineOp(ChatSyncEntity entity, ChatSyncPipelineOp.OpType opType) {
      this.entity = entity;
      this.opType = opType;
   }

   public ChatSyncEntity getEntity() {
      return this.entity;
   }

   public ChatSyncPipelineOp.OpType getOpType() {
      return this.opType;
   }

   public int processResults(List<Object> pipelineResults, int startIndex) throws FusionException {
      return this.opType.equals(ChatSyncPipelineOp.OpType.READ) ? this.entity.loadPipeline(pipelineResults, startIndex) : this.entity.processPipelineStoreResults(pipelineResults, startIndex);
   }

   public boolean isRead() {
      return this.opType.equals(ChatSyncPipelineOp.OpType.READ);
   }

   public boolean isWrite() {
      return this.opType.equals(ChatSyncPipelineOp.OpType.WRITE);
   }

   public static enum OpType {
      READ,
      WRITE;
   }
}
