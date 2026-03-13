package com.projectgoth.fusion.search;

import Ice.Application;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.data.ChatRoomData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;

public class IndexChatRooms extends Application {
   private static final String APP_NAME = "IndexChatRooms";
   static int numChatRoomsProcessed = 0;
   static int numChatRoomsInserted = 0;
   static int numChatRoomsUpdated = 0;
   static RequestCounter requestCounter = new RequestCounter();
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(IndexChatRooms.class));
   static int startID = 0;
   static String dbSlaveURL;
   static String dbSlaveUsername;
   static String dbSlavePassword;
   static DecimalFormat df2 = new DecimalFormat("0.00");
   static int daysAgo = 90;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      boolean showHelp = false;

      for(int i = 0; i < args.length; ++i) {
         if (args[i].equalsIgnoreCase("-startid")) {
            startID = Integer.parseInt(args[i + 1]);
         } else if (args[i].equalsIgnoreCase("-daysago")) {
            daysAgo = Integer.parseInt(args[i + 1]);
         } else if (args[i].equalsIgnoreCase("-?") || args[i].equalsIgnoreCase("-h")) {
            showHelp = true;
         }
      }

      if (showHelp) {
         log.error("Usage: IndexChatRooms [-startid id] [-daysago days]");
         log.error(" -startid : Index chat rooms in chronological order based on their ID, starting from this ID (default 0)");
         log.error(" -daysago : Only migrate chat rooms that have been accessed in the last 'days' days (default 90)");
         log.error("e.g. IndexChatRooms -startid 23717 -daysago 60");
      } else {
         log.info("IndexChatRooms starting.");
         IndexChatRooms app = new IndexChatRooms();
         int status = app.main("IndexChatRooms", args);
         log.info("Terminating");
         System.exit(status);
      }
   }

   public int run(String[] arg0) {
      Integer lastIDMigrated = this.readLastIDMigratedFromFile();
      if (lastIDMigrated != null) {
         startID = lastIDMigrated;
         log.info("Found \"lastidmigrated\" file. Will start migrating from ID " + lastIDMigrated);
      }

      log.info("Starting from chat room ID " + startID);

      try {
         this.loadDBProperties();
      } catch (Exception var21) {
         log.error("Unable to load database configuration file: " + var21.getMessage());
         return 1;
      }

      log.info("Will connect to slave DB at " + dbSlaveURL + " as " + dbSlaveUsername);

      try {
         Connection conn = this.getSlaveConnection();
         conn.close();
      } catch (Exception var20) {
         log.fatal("Unable to connect to to slave DB", var20);
         return 1;
      }

      boolean finished = false;

      try {
         while(!finished) {
            String sql = "select rooms.*, group_concat(keyword.keyword) as keywords from (select * from chatroom where id > " + startID + " and status=1 and datelastaccessed > date_sub(now(), interval " + daysAgo + " day) order by id limit 5000) rooms " + " left outer join chatroomkeyword on rooms.id=chatroomkeyword.chatroomid " + "left join keyword on chatroomkeyword.keywordid=keyword.id " + "group by id, name, description, type, creator, chatroomcategoryid, primarycountryid, secondarycountryid, locationid, groupid, adultonly, maximumsize, " + "userowned, allowkicking, allowuserkeywords, allowBots, language, datecreated, datelastaccessed, status, chatroomthemeid order by id;";
            Connection connSlave = this.getSlaveConnection();
            ResultSet rsChatRooms = connSlave.createStatement().executeQuery(sql);
            Vector rooms = new Vector();

            while(rsChatRooms.next()) {
               rooms.add(new ChatRoomData(rsChatRooms));
            }

            rsChatRooms.close();
            connSlave.close();
            if (rooms.size() == 0) {
               finished = true;
               break;
            }

            for(int i = 0; i < rooms.size(); ++i) {
               ChatRoomData room = (ChatRoomData)rooms.elementAt(i);
               boolean needsUpdate = false;
               JSONObject result = ElasticSearch.get(ElasticSearch.IndexType.CHAT_ROOMS, ElasticSearch.DocumentType.CHAT_ROOM, room.id);
               JSONObject roomJson;
               int j;
               if (result != null) {
                  roomJson = result.getJSONObject("_source");
                  JSONObject fieldsToUpdate = new JSONObject();
                  if (roomJson.getString(ChatRoomsIndex.Field.NAME.toString()) == null || !roomJson.getString(ChatRoomsIndex.Field.NAME.toString()).equalsIgnoreCase(room.name)) {
                     fieldsToUpdate.put(ChatRoomsIndex.Field.NAME.toString(), room.name);
                     needsUpdate = true;
                  }

                  if (!roomJson.has(ChatRoomsIndex.Field.ADULT_ONLY.toString()) || roomJson.get(ChatRoomsIndex.Field.ADULT_ONLY.toString()) == null || roomJson.getBoolean(ChatRoomsIndex.Field.ADULT_ONLY.toString()) != room.adultOnly) {
                     fieldsToUpdate.put(ChatRoomsIndex.Field.ADULT_ONLY.toString(), room.adultOnly);
                     needsUpdate = true;
                  }

                  if (room.dateLastAccessed != null && (!roomJson.has(ChatRoomsIndex.Field.DATE_LAST_ACCESSED.toString()) || roomJson.get(ChatRoomsIndex.Field.DATE_LAST_ACCESSED.toString()) == null || roomJson.getLong(ChatRoomsIndex.Field.DATE_LAST_ACCESSED.toString()) < room.dateLastAccessed.getTime())) {
                     fieldsToUpdate.put(ChatRoomsIndex.Field.DATE_LAST_ACCESSED.toString(), room.dateLastAccessed.getTime());
                     needsUpdate = true;
                  }

                  if (room.primaryCountryID != null && (!roomJson.has(ChatRoomsIndex.Field.PRIMARY_COUNTRY_ID.toString()) || roomJson.get(ChatRoomsIndex.Field.PRIMARY_COUNTRY_ID.toString()) == null || roomJson.getLong(ChatRoomsIndex.Field.PRIMARY_COUNTRY_ID.toString()) != (long)room.primaryCountryID)) {
                     fieldsToUpdate.put(ChatRoomsIndex.Field.PRIMARY_COUNTRY_ID.toString(), room.primaryCountryID);
                     needsUpdate = true;
                  }

                  if (room.secondaryCountryID != null && (roomJson.has(ChatRoomsIndex.Field.SECONDARY_COUNTRY_ID.toString()) || roomJson.get(ChatRoomsIndex.Field.SECONDARY_COUNTRY_ID.toString()) == null || roomJson.getLong(ChatRoomsIndex.Field.SECONDARY_COUNTRY_ID.toString()) != (long)room.secondaryCountryID)) {
                     fieldsToUpdate.put(ChatRoomsIndex.Field.SECONDARY_COUNTRY_ID.toString(), room.secondaryCountryID);
                     needsUpdate = true;
                  }

                  if (room.keywords != null && room.keywords.length > 0) {
                     Set<String> keywordsInES = new HashSet();
                     if (roomJson.has(ChatRoomsIndex.Field.TAGS.toString())) {
                        JSONArray keywordsJson = roomJson.getJSONArray(ChatRoomsIndex.Field.TAGS.toString());

                        for(j = 0; j < keywordsJson.length(); ++j) {
                           keywordsInES.add(keywordsJson.getString(j));
                        }
                     }

                     Set<String> keywordsInDB = new HashSet();
                     String[] arr$ = room.keywords;
                     int len$ = arr$.length;

                     for(int i$ = 0; i$ < len$; ++i$) {
                        String keyword = arr$[i$];
                        keywordsInDB.add(keyword);
                     }

                     if (!keywordsInES.equals(keywordsInDB)) {
                        fieldsToUpdate.put(ChatRoomsIndex.Field.TAGS.toString(), new JSONArray(keywordsInDB));
                        needsUpdate = true;
                     }
                  }

                  if (needsUpdate) {
                     ChatRoomsIndex.updateChatRoom(room.id, room.name, fieldsToUpdate);
                     ++numChatRoomsUpdated;
                  }
               } else {
                  roomJson = new JSONObject();
                  roomJson.put(ChatRoomsIndex.Field.ADULT_ONLY.toString(), room.adultOnly);
                  if (room.keywords != null && room.keywords.length > 0) {
                     Set<String> set = new HashSet();
                     String[] arr$ = room.keywords;
                     int len$ = arr$.length;

                     for(j = 0; j < len$; ++j) {
                        String keyword = arr$[j];
                        set.add(keyword);
                     }

                     roomJson.put(ChatRoomsIndex.Field.TAGS.toString(), new JSONArray(set));
                  }

                  if (room.primaryCountryID != null) {
                     roomJson.put(ChatRoomsIndex.Field.PRIMARY_COUNTRY_ID.toString(), room.primaryCountryID);
                  }

                  if (room.secondaryCountryID != null) {
                     roomJson.put(ChatRoomsIndex.Field.SECONDARY_COUNTRY_ID.toString(), room.secondaryCountryID);
                  }

                  if (room.dateLastAccessed != null) {
                     roomJson.put(ChatRoomsIndex.Field.DATE_LAST_ACCESSED.toString(), room.dateLastAccessed.getTime());
                  }

                  ChatRoomsIndex.updateChatRoom(room.id, room.name, roomJson);
                  ++numChatRoomsInserted;
               }

               ++numChatRoomsProcessed;
               requestCounter.add();
               this.writeLastIDMigratedToFile(room.id);
               startID = room.id;
            }

            log.info("Processed " + numChatRoomsProcessed + " rooms (" + numChatRoomsInserted + " inserted, " + numChatRoomsUpdated + " updated). Processed " + df2.format((double)requestCounter.getRequestsPerSecond()) + "/s");
         }
      } catch (Exception var22) {
         log.error("An exception occurred", var22);
         var22.printStackTrace();
         return 1;
      }

      log.info("Complete");
      this.writeLastIDMigratedToFile(0);
      return 0;
   }

   private Integer readLastIDMigratedFromFile() {
      try {
         File f = new File("lastidmigrated");
         if (f.exists()) {
            FileReader fr = new FileReader(f);
            BufferedReader in = new BufferedReader(fr);
            int id = Integer.parseInt(in.readLine());
            return id;
         } else {
            return null;
         }
      } catch (Exception var5) {
         log.warn("Unable to read 'lastidmigrated' file: " + var5.getMessage());
         return null;
      }
   }

   private void writeLastIDMigratedToFile(int id) {
      try {
         FileWriter fileWriter = new FileWriter("lastidmigrated", false);
         BufferedWriter writer = new BufferedWriter(fileWriter);
         writer.write(Integer.toString(id));
         writer.flush();
         writer.close();
      } catch (Exception var4) {
         log.warn("Unable to write last ID migrated " + id + " to 'lastidmigrated' file: " + var4.getMessage());
      }

   }

   private Connection getSlaveConnection() throws Exception {
      return DriverManager.getConnection(dbSlaveURL, dbSlaveUsername, dbSlavePassword);
   }

   private void loadDBProperties() throws Exception {
      Properties properties = new Properties();
      String propertiesLocation = ConfigUtils.getConfigDirectory() + "database.properties";
      log.info("Loading database configuration file " + propertiesLocation);
      InputStream inputStream = new FileInputStream(new File(propertiesLocation));
      properties.load(inputStream);
      dbSlaveURL = properties.getProperty("mis.database.jdbcUrl");
      if (dbSlaveURL == null) {
         throw new Exception("mis.database.jdbcUrl not specified");
      } else {
         dbSlaveUsername = properties.getProperty("mis.database.username");
         if (dbSlaveUsername == null) {
            throw new Exception("mis.database.username not specified");
         } else {
            dbSlavePassword = properties.getProperty("mis.database.password");
            if (dbSlavePassword == null) {
               throw new Exception("mis.database.password not specified");
            } else {
               log.info("Database configuration successfully loaded");
            }
         }
      }
   }
}
