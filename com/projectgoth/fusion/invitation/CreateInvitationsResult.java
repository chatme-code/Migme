package com.projectgoth.fusion.invitation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class CreateInvitationsResult extends HashMap<String, CreateInvitationsResult.CreateInvitationDetails> {
   public Map<String, Integer> getSendInvitationStatusSummary() {
      HashMap<String, Integer> summary = new HashMap();
      Iterator i$ = this.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, CreateInvitationsResult.CreateInvitationDetails> entry = (Entry)i$.next();
         summary.put(entry.getKey(), ((CreateInvitationsResult.CreateInvitationDetails)entry.getValue()).sendInvitationResult.value());
      }

      return summary;
   }

   public static class CreateInvitationDetails implements Serializable {
      public InvitationUtils.SendInvitationResultEnum sendInvitationResult;
      public Integer invitationID;
      public Integer existingUserID;

      public CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum resultEnum) {
         this.sendInvitationResult = resultEnum;
      }

      public CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum sendInvitationResult, Integer existingUserID) {
         this.sendInvitationResult = sendInvitationResult;
         this.existingUserID = existingUserID;
      }
   }
}
