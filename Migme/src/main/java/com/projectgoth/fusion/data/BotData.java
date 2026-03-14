package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BotData implements Serializable {
   private long id;
   private String game;
   private String displayName;
   private String commandName;
   private String description;
   private String executableFileName;
   private String libraryPaths;
   private int type;
   private boolean status;
   private String emoticonKeyList;

   public BotData(ResultSet rs) throws SQLException {
      this.id = (long)rs.getInt("ID");
      this.game = rs.getString("Game");
      this.displayName = rs.getString("DisplayName");
      this.commandName = rs.getString("CommandName");
      this.description = rs.getString("Description");
      this.executableFileName = rs.getString("ExecutableFileName");
      this.libraryPaths = rs.getString("LibraryPaths");
      this.type = rs.getInt("Type");
      this.status = rs.getBoolean("Status");
      this.emoticonKeyList = rs.getString("EmoticonKeyList");
   }

   public BotData() {
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getGame() {
      return this.game;
   }

   public void setGame(String game) {
      this.game = game;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String name) {
      this.displayName = name;
   }

   public String getCommandName() {
      return this.commandName;
   }

   public void setCommandName(String commandName) {
      this.commandName = commandName;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getExecutableFileName() {
      return this.executableFileName;
   }

   public void setExecutableFileName(String executableFileName) {
      this.executableFileName = executableFileName;
   }

   public String getLibraryPaths() {
      return this.libraryPaths;
   }

   public void setLibraryPaths(String libraryPaths) {
      this.libraryPaths = libraryPaths;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public boolean isEnabled() {
      return this.status;
   }

   public void setEnabled(boolean enabled) {
      this.status = enabled;
   }

   public String getEmoticonKeyList() {
      return this.emoticonKeyList;
   }

   public void setEmoticonKeyList(String emoticonKeyList) {
      this.emoticonKeyList = emoticonKeyList;
   }

   public static enum BotStateEnum {
      NO_GAME(0),
      GAME_STARTING(1),
      GAME_STARTED(2),
      GAME_JOINING(3),
      GAME_JOIN_ENDED(4),
      PLAYING(5),
      GAME_ENDED(99);

      private int value;
      public static final int ID_NO_GAME = 0;
      public static final int ID_GAME_STARTING = 1;
      public static final int ID_GAME_STARTED = 2;
      public static final int ID_GAME_JOINING = 3;
      public static final int ID_GAME_JOIN_ENDED = 4;
      public static final int ID_PLAYING = 5;
      public static final int ID_GAME_ENDED = 99;

      private BotStateEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static BotData.BotStateEnum fromValue(int value) {
         BotData.BotStateEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            BotData.BotStateEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum BotChannelTypeEnum {
      CHAT_ROOM(1),
      GROUP_CHAT(2);

      private int value;
      public static final int ID_CHAT_ROOM = 1;
      public static final int ID_GROUP_CHAT = 2;

      private BotChannelTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static BotData.BotChannelTypeEnum fromValue(int value) {
         BotData.BotChannelTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            BotData.BotChannelTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum BotCommandEnum {
      JOIN(1),
      PART(2),
      QUIT(3);

      private int value;
      public static final int ID_JOIN = 1;
      public static final int ID_PART = 2;
      public static final int ID_QUIT = 3;

      private BotCommandEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static BotData.BotCommandEnum fromValue(int value) {
         BotData.BotCommandEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            BotData.BotCommandEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
