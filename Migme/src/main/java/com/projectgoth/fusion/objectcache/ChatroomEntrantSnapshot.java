package com.projectgoth.fusion.objectcache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class ChatroomEntrantSnapshot {
   private int recentEntrantBufferSize = 50;
   private int lockPeriod = 45;
   private long chatroomLockExpiry = 0L;
   private ArrayList<ChatroomEntrantSnapshot.Entrant> entrantsSnapshot;
   private LinkedHashMap<String, ChatroomEntrantSnapshot.Entrant> recentEntrants;

   public ChatroomEntrantSnapshot(int entrantBufferSize, int lockPeriod) {
      this.entrantsSnapshot = new ArrayList(this.recentEntrantBufferSize);
      this.recentEntrants = new LinkedHashMap<String, ChatroomEntrantSnapshot.Entrant>() {
         protected boolean removeEldestEntry(Entry<String, ChatroomEntrantSnapshot.Entrant> eldest) {
            return this.size() > ChatroomEntrantSnapshot.this.recentEntrantBufferSize;
         }
      };
      this.recentEntrantBufferSize = entrantBufferSize;
      this.lockPeriod = lockPeriod;
   }

   public void addEntrant(String username, int level, String ip) {
      ChatroomEntrantSnapshot.Entrant entrant = new ChatroomEntrantSnapshot.Entrant(username, level, ip);
      this.recentEntrants.put(username, entrant);
   }

   public String getEntrantListStr(int size, int startIndex) {
      String entrantDetails = "";
      int count = 0;

      for(int i = startIndex; i < this.entrantsSnapshot.size() && count < size; ++i) {
         ChatroomEntrantSnapshot.Entrant entrant = (ChatroomEntrantSnapshot.Entrant)this.entrantsSnapshot.get(i);
         entrantDetails = entrantDetails + (i + 1) + ". " + entrant.getUsername() + " " + entrant.getLevel() + " \n";
         ++count;
      }

      return entrantDetails;
   }

   public void initLockExpiry() {
      this.clearSnapshot();
      int i = 1;

      for(Iterator i$ = this.recentEntrants.entrySet().iterator(); i$.hasNext(); ++i) {
         Entry<String, ChatroomEntrantSnapshot.Entrant> entry = (Entry)i$.next();
         this.entrantsSnapshot.add(entry.getValue());
      }

      this.chatroomLockExpiry = System.currentTimeMillis() + (long)(this.lockPeriod * 1000);
   }

   public boolean hasLockExpired() {
      return System.currentTimeMillis() > this.chatroomLockExpiry;
   }

   public boolean isCurrentSnapshotRunning() {
      return this.entrantsSnapshot != null && this.entrantsSnapshot.size() > 0;
   }

   public void clearSnapshot() {
      this.entrantsSnapshot.clear();
   }

   public String[] getSnapshotUsernamesFromIndexes(int[] indexes) {
      List<String> bannedList = new ArrayList(indexes.length);

      for(int i = 0; i < indexes.length; ++i) {
         try {
            String username = ((ChatroomEntrantSnapshot.Entrant)this.entrantsSnapshot.get(indexes[i] - 1)).username;
            bannedList.add(username);
         } catch (IndexOutOfBoundsException var5) {
         }
      }

      String[] bannedUsernames = new String[bannedList.size()];
      bannedList.toArray(bannedUsernames);
      return bannedUsernames;
   }

   private class Entrant {
      private String username;
      private int level;
      private String ip;

      public Entrant(String username, int level, String ip) {
         this.username = username;
         this.level = level;
         this.ip = ip;
      }

      public String getUsername() {
         return this.username;
      }

      public int getLevel() {
         return this.level;
      }

      public String getIp() {
         return this.ip;
      }
   }
}
