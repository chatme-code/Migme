package com.projectgoth.fusion.chat.external.yahoo;

import java.util.LinkedList;
import java.util.List;

public class YahooConference {
   private List<String> participants = new LinkedList();

   public void addParticipant(String participant) {
      synchronized(this.participants) {
         if (!this.participants.contains(participant)) {
            this.participants.add(participant);
         }

      }
   }

   public void removeParticipant(String participant) {
      synchronized(this.participants) {
         this.participants.remove(participant);
      }
   }

   public List<String> getParticipants() {
      synchronized(this.participants) {
         return this.participants;
      }
   }
}
