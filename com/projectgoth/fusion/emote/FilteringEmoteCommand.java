package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.FusionExceptionWithErrorCauseCode;
import org.apache.log4j.Logger;

public abstract class FilteringEmoteCommand extends EmoteCommand {
   protected abstract Logger getLog();

   public FilteringEmoteCommand(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] cmdArgs = messageData.getArgs();
      this.filterAndValidate(cmdArgs, messageData, chatSource);

      try {
         FilteringEmoteCommand.ProcessingResult result = this.doExecute(cmdArgs, messageData, chatSource);
         if (!result.accepted) {
            this.getLog().warn(String.format("Emote %s rejected", this.getCommand(cmdArgs)));
         }

         return result.resultType;
      } catch (EmoteCommandException var5) {
         if (var5.getErrorCause() == ErrorCause.EmoteCommandError.INTERNAL_ERROR) {
            if (var5.getCause() instanceof FusionException) {
               throw (FusionException)var5.getCause();
            }

            this.getLog().error("Internal error", var5);
         } else {
            this.getLog().warn(String.format("Emote %s failed %s", this.getCommand(cmdArgs), var5.getErrorCause().getCode()));
         }

         throw new FusionExceptionWithErrorCauseCode(var5.getMessage(), var5.getErrorCause().getCode());
      }
   }

   protected String getCommand(String[] cmdArgs) {
      String cmd = "";
      if (cmdArgs.length > 0) {
         cmd = cmdArgs[0];
      } else {
         cmd = this.getClass().getName().toLowerCase();
      }

      return cmd;
   }

   protected abstract FilteringEmoteCommand.ProcessingResult doExecute(String[] var1, MessageData var2, ChatSource var3) throws EmoteCommandException;

   private void filterAndValidate(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws FusionException {
      this.checkSyntax(cmdArgs, messageData, chatSource);
      this.checkRateLimit(this.getClass(), MemCachedKeyUtils.getFullKeyFromStrings("s:" + messageData.source, this.getInstigatorRateLimitLocalKeySuffix(cmdArgs, messageData, chatSource)), this.getRateLimitThreshold(cmdArgs, messageData, chatSource));
      this.checkDevice(cmdArgs, messageData, chatSource);
   }

   protected void checkDevice(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws FusionException {
      if (!EmoteCommandUtils.clientMeetsMinVersion(chatSource.getSessionI().getClientVersion(), chatSource.getSessionI().getDeviceType())) {
         throw new FusionExceptionWithErrorCauseCode(this.getCommand(cmdArgs) + " is not supported on this client device and version", ErrorCause.EmoteCommandError.UNSUPPORTED_CLIENT_DEVICE.getCode());
      }
   }

   protected String getInstigatorRateLimitLocalKeySuffix(String[] cmdArgs, MessageData messageData, ChatSource chatSource) {
      return "";
   }

   protected abstract void checkSyntax(String[] var1, MessageData var2, ChatSource var3) throws FusionException;

   protected abstract String getRateLimitThreshold(String[] var1, MessageData var2, ChatSource var3);

   public static class ProcessingResult {
      public EmoteCommand.ResultType resultType;
      public boolean accepted;

      public ProcessingResult(EmoteCommand.ResultType resultType, boolean valid) {
         this.resultType = resultType;
         this.accepted = valid;
      }
   }
}
