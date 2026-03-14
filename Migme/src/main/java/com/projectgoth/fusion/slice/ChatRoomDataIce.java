package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;
import java.util.Arrays;

public final class ChatRoomDataIce implements Cloneable, Serializable {
   public int id;
   public String name;
   public String description;
   public int type;
   public String creator;
   public int primaryCountryID;
   public int secondaryCountryID;
   public int groupID;
   public int locationID;
   public int botID;
   public int adultOnly;
   public int maximumSize;
   public int userOwned;
   public String newOwner;
   public int allowKicking;
   public int allowUserKeywords;
   public int allowBots;
   public String language;
   public long dateCreated;
   public long dateLastAccessed;
   public int status;
   public int size;
   public String[] keywords;
   public int themeID;
   public String lockUser;
   public String announcer;
   public String announceMessage;
   public int minMigLevel;
   public String rateLimitByIp;
   public int blockPeriodByIpInSeconds;

   public ChatRoomDataIce() {
   }

   public ChatRoomDataIce(int id, String name, String description, int type, String creator, int primaryCountryID, int secondaryCountryID, int groupID, int locationID, int botID, int adultOnly, int maximumSize, int userOwned, String newOwner, int allowKicking, int allowUserKeywords, int allowBots, String language, long dateCreated, long dateLastAccessed, int status, int size, String[] keywords, int themeID, String lockUser, String announcer, String announceMessage, int minMigLevel, String rateLimitByIp, int blockPeriodByIpInSeconds) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.type = type;
      this.creator = creator;
      this.primaryCountryID = primaryCountryID;
      this.secondaryCountryID = secondaryCountryID;
      this.groupID = groupID;
      this.locationID = locationID;
      this.botID = botID;
      this.adultOnly = adultOnly;
      this.maximumSize = maximumSize;
      this.userOwned = userOwned;
      this.newOwner = newOwner;
      this.allowKicking = allowKicking;
      this.allowUserKeywords = allowUserKeywords;
      this.allowBots = allowBots;
      this.language = language;
      this.dateCreated = dateCreated;
      this.dateLastAccessed = dateLastAccessed;
      this.status = status;
      this.size = size;
      this.keywords = keywords;
      this.themeID = themeID;
      this.lockUser = lockUser;
      this.announcer = announcer;
      this.announceMessage = announceMessage;
      this.minMigLevel = minMigLevel;
      this.rateLimitByIp = rateLimitByIp;
      this.blockPeriodByIpInSeconds = blockPeriodByIpInSeconds;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         ChatRoomDataIce _r = null;

         try {
            _r = (ChatRoomDataIce)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.id != _r.id) {
               return false;
            } else if (this.name != _r.name && this.name != null && !this.name.equals(_r.name)) {
               return false;
            } else if (this.description != _r.description && this.description != null && !this.description.equals(_r.description)) {
               return false;
            } else if (this.type != _r.type) {
               return false;
            } else if (this.creator != _r.creator && this.creator != null && !this.creator.equals(_r.creator)) {
               return false;
            } else if (this.primaryCountryID != _r.primaryCountryID) {
               return false;
            } else if (this.secondaryCountryID != _r.secondaryCountryID) {
               return false;
            } else if (this.groupID != _r.groupID) {
               return false;
            } else if (this.locationID != _r.locationID) {
               return false;
            } else if (this.botID != _r.botID) {
               return false;
            } else if (this.adultOnly != _r.adultOnly) {
               return false;
            } else if (this.maximumSize != _r.maximumSize) {
               return false;
            } else if (this.userOwned != _r.userOwned) {
               return false;
            } else if (this.newOwner != _r.newOwner && this.newOwner != null && !this.newOwner.equals(_r.newOwner)) {
               return false;
            } else if (this.allowKicking != _r.allowKicking) {
               return false;
            } else if (this.allowUserKeywords != _r.allowUserKeywords) {
               return false;
            } else if (this.allowBots != _r.allowBots) {
               return false;
            } else if (this.language != _r.language && this.language != null && !this.language.equals(_r.language)) {
               return false;
            } else if (this.dateCreated != _r.dateCreated) {
               return false;
            } else if (this.dateLastAccessed != _r.dateLastAccessed) {
               return false;
            } else if (this.status != _r.status) {
               return false;
            } else if (this.size != _r.size) {
               return false;
            } else if (!Arrays.equals(this.keywords, _r.keywords)) {
               return false;
            } else if (this.themeID != _r.themeID) {
               return false;
            } else if (this.lockUser != _r.lockUser && this.lockUser != null && !this.lockUser.equals(_r.lockUser)) {
               return false;
            } else if (this.announcer != _r.announcer && this.announcer != null && !this.announcer.equals(_r.announcer)) {
               return false;
            } else if (this.announceMessage != _r.announceMessage && this.announceMessage != null && !this.announceMessage.equals(_r.announceMessage)) {
               return false;
            } else if (this.minMigLevel != _r.minMigLevel) {
               return false;
            } else if (this.rateLimitByIp != _r.rateLimitByIp && this.rateLimitByIp != null && !this.rateLimitByIp.equals(_r.rateLimitByIp)) {
               return false;
            } else {
               return this.blockPeriodByIpInSeconds == _r.blockPeriodByIpInSeconds;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      int __h = 5 * __h + this.id;
      if (this.name != null) {
         __h = 5 * __h + this.name.hashCode();
      }

      if (this.description != null) {
         __h = 5 * __h + this.description.hashCode();
      }

      __h = 5 * __h + this.type;
      if (this.creator != null) {
         __h = 5 * __h + this.creator.hashCode();
      }

      __h = 5 * __h + this.primaryCountryID;
      __h = 5 * __h + this.secondaryCountryID;
      __h = 5 * __h + this.groupID;
      __h = 5 * __h + this.locationID;
      __h = 5 * __h + this.botID;
      __h = 5 * __h + this.adultOnly;
      __h = 5 * __h + this.maximumSize;
      __h = 5 * __h + this.userOwned;
      if (this.newOwner != null) {
         __h = 5 * __h + this.newOwner.hashCode();
      }

      __h = 5 * __h + this.allowKicking;
      __h = 5 * __h + this.allowUserKeywords;
      __h = 5 * __h + this.allowBots;
      if (this.language != null) {
         __h = 5 * __h + this.language.hashCode();
      }

      __h = 5 * __h + (int)this.dateCreated;
      __h = 5 * __h + (int)this.dateLastAccessed;
      __h = 5 * __h + this.status;
      __h = 5 * __h + this.size;
      if (this.keywords != null) {
         for(int __i0 = 0; __i0 < this.keywords.length; ++__i0) {
            if (this.keywords[__i0] != null) {
               __h = 5 * __h + this.keywords[__i0].hashCode();
            }
         }
      }

      __h = 5 * __h + this.themeID;
      if (this.lockUser != null) {
         __h = 5 * __h + this.lockUser.hashCode();
      }

      if (this.announcer != null) {
         __h = 5 * __h + this.announcer.hashCode();
      }

      if (this.announceMessage != null) {
         __h = 5 * __h + this.announceMessage.hashCode();
      }

      __h = 5 * __h + this.minMigLevel;
      if (this.rateLimitByIp != null) {
         __h = 5 * __h + this.rateLimitByIp.hashCode();
      }

      __h = 5 * __h + this.blockPeriodByIpInSeconds;
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
      __os.writeInt(this.id);
      __os.writeString(this.name);
      __os.writeString(this.description);
      __os.writeInt(this.type);
      __os.writeString(this.creator);
      __os.writeInt(this.primaryCountryID);
      __os.writeInt(this.secondaryCountryID);
      __os.writeInt(this.groupID);
      __os.writeInt(this.locationID);
      __os.writeInt(this.botID);
      __os.writeInt(this.adultOnly);
      __os.writeInt(this.maximumSize);
      __os.writeInt(this.userOwned);
      __os.writeString(this.newOwner);
      __os.writeInt(this.allowKicking);
      __os.writeInt(this.allowUserKeywords);
      __os.writeInt(this.allowBots);
      __os.writeString(this.language);
      __os.writeLong(this.dateCreated);
      __os.writeLong(this.dateLastAccessed);
      __os.writeInt(this.status);
      __os.writeInt(this.size);
      StringArrayHelper.write(__os, this.keywords);
      __os.writeInt(this.themeID);
      __os.writeString(this.lockUser);
      __os.writeString(this.announcer);
      __os.writeString(this.announceMessage);
      __os.writeInt(this.minMigLevel);
      __os.writeString(this.rateLimitByIp);
      __os.writeInt(this.blockPeriodByIpInSeconds);
   }

   public void __read(BasicStream __is) {
      this.id = __is.readInt();
      this.name = __is.readString();
      this.description = __is.readString();
      this.type = __is.readInt();
      this.creator = __is.readString();
      this.primaryCountryID = __is.readInt();
      this.secondaryCountryID = __is.readInt();
      this.groupID = __is.readInt();
      this.locationID = __is.readInt();
      this.botID = __is.readInt();
      this.adultOnly = __is.readInt();
      this.maximumSize = __is.readInt();
      this.userOwned = __is.readInt();
      this.newOwner = __is.readString();
      this.allowKicking = __is.readInt();
      this.allowUserKeywords = __is.readInt();
      this.allowBots = __is.readInt();
      this.language = __is.readString();
      this.dateCreated = __is.readLong();
      this.dateLastAccessed = __is.readLong();
      this.status = __is.readInt();
      this.size = __is.readInt();
      this.keywords = StringArrayHelper.read(__is);
      this.themeID = __is.readInt();
      this.lockUser = __is.readString();
      this.announcer = __is.readString();
      this.announceMessage = __is.readString();
      this.minMigLevel = __is.readInt();
      this.rateLimitByIp = __is.readString();
      this.blockPeriodByIpInSeconds = __is.readInt();
   }
}
