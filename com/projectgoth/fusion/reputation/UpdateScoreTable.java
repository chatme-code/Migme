package com.projectgoth.fusion.reputation;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.reputation.file.ScoreSummary;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.LevelTable;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.triggers.ReputationLevelIncreaseTrigger;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.sql.DataSource;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apache.log4j.Logger;

public class UpdateScoreTable {
   protected static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UpdateScoreTable.class));
   private DataSource masterDataSource;
   private DirectoryHolder directoryHolder;
   private SelfPopulatingCache scoreCache;
   private IcePrxFinder icePrxFinder;

   public UpdateScoreTable(DataSource masterDataSource, DirectoryHolder directoryHolder, SelfPopulatingCache scoreCache, IcePrxFinder icePrxFinder) {
      this.masterDataSource = masterDataSource;
      this.directoryHolder = directoryHolder;
      this.scoreCache = scoreCache;
      this.icePrxFinder = icePrxFinder;
   }

   public int process(String runDateString) throws Exception {
      Connection connection = null;
      long start = System.currentTimeMillis();
      int errorLines = 0;
      int lines = 0;

      try {
         log.info("processing score file for time marker [" + runDateString + "] and updating score table");
         connection = this.masterDataSource.getConnection();
         PreparedStatement selectPreparedStatement = connection.prepareStatement("select uid.id, s.score from userid uid left outer join score s on (uid.id = s.userid) where uid.username = ?");
         PreparedStatement updatePreparedStatement = connection.prepareStatement("update score s set s.score = ? where s.userid = ?");
         PreparedStatement insertPreparedStatement = connection.prepareStatement("insert into score (userid,score) values (?,?)");
         SortedMap<Integer, ReputationLevelData> levelDataTable = LevelTable.readLevelDataTable(connection);
         ResultSet rs = null;
         BufferedReader reader = new BufferedReader(new FileReader(this.directoryHolder.getDataDirectory() + this.directoryHolder.getScoreFilename(runDateString)));

         String line;
         while((line = reader.readLine()) != null) {
            List<String> lineParts = StringUtil.split(line, ',');
            int score = Integer.parseInt((String)lineParts.get(ScoreSummary.TOTAL_SCORE_INDEX));
            String username = (String)lineParts.get(0);

            try {
               selectPreparedStatement.setString(1, username);
               rs = selectPreparedStatement.executeQuery();
               if (!rs.next()) {
                  log.error("failed to update score for user [" + username + "], invalid username");
               } else {
                  boolean hasScore = rs.getObject("score") != null;
                  int userID = rs.getInt("id");
                  int oldScore = rs.getInt("score");
                  int newScore = oldScore + score;
                  int rows;
                  if (hasScore) {
                     updatePreparedStatement.setInt(1, newScore);
                     updatePreparedStatement.setInt(2, userID);
                     rows = updatePreparedStatement.executeUpdate();
                     if (rows != 1) {
                        log.error("failed to update score for user [" + username + "], rows effected [" + rows + "]");
                     } else {
                        MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, userID + "");
                     }
                  } else {
                     insertPreparedStatement.setInt(1, userID);
                     insertPreparedStatement.setInt(2, newScore);
                     rows = insertPreparedStatement.executeUpdate();
                     if (rows != 1) {
                        log.error("failed to update score for user [" + username + "], rows effected [" + rows + "]");
                     } else {
                        MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, userID + "");
                     }
                  }

                  if (this.scoreCache != null) {
                     this.scoreCache.remove(username);
                  }

                  ReputationLevelData oldLevelData = LevelTable.getLevelDataForScore(oldScore, levelDataTable);
                  ReputationLevelData newLevelData = LevelTable.getLevelDataForScore(newScore, levelDataTable);
                  if (oldLevelData.level != newLevelData.level) {
                     this.onLevelChanged(userID, username, oldLevelData, newLevelData, connection);
                  }

                  if (lines++ % 1000 == 0) {
                     log.debug(lines + " lines done");
                  }
               }
            } catch (SQLException var45) {
               ++errorLines;
               log.error("failed to update line [" + line + "] number [" + lines + "]", var45);
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var44) {
                  rs = null;
               }

            }
         }
      } catch (Exception var47) {
         log.error("failed to update score table", var47);
         throw var47;
      } finally {
         log.info("finished processing score file for time marker [" + runDateString + "] and updating score table, it took " + (System.currentTimeMillis() - start) / 1000L + " seconds");
         if (connection != null) {
            connection.close();
         }

      }

      return errorLines;
   }

   public void onLevelChanged(int userID, String username, ReputationLevelData oldLevelData, ReputationLevelData newLevelData, Connection connection) {
      if (newLevelData.chatRoomSize != oldLevelData.chatRoomSize) {
         this.updateUserOwnedRoomSize(username, newLevelData.chatRoomSize, connection);
      }

      try {
         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         UserData userData = userBean.loadUser(username, false, false);
         ReputationLevelIncreaseTrigger trigger = new ReputationLevelIncreaseTrigger(userData, newLevelData.level, new Timestamp(System.currentTimeMillis()), -1, RewardProgramData.TypeEnum.MANUAL.getId());
         trigger.amountDelta = 0.0D;
         RewardCentre.getInstance().sendTrigger(trigger);
      } catch (CreateException var10) {
         log.error("Unabled to create userbean. exception : " + var10.getMessage());
      } catch (EJBException var11) {
         log.error("Unable to get user info for " + username + ". exception: " + var11.getMessage());
      } catch (Exception var12) {
         log.error("Unknown error while notifying reward system. exception:" + var12.getMessage());
      }

      try {
         UserNotificationServicePrx unsProxy = this.icePrxFinder.getUserNotificationServiceProxy();
         Map<String, String> parameters = new HashMap();
         parameters.put("miglevel", newLevelData.level.toString());
         unsProxy.notifyFusionUser(new Message(newLevelData.level.toString(), userID, username, Enums.NotificationTypeEnum.MIGLEVEL_INCREASE_ALERT.getType(), System.currentTimeMillis(), parameters));
      } catch (Exception var9) {
         log.error("Unknown error while contacting UNS to generate miglevel increase alert", var9);
      }

   }

   private void updateUserOwnedRoomSize(String username, int chatRoomSize, Connection connection) {
      PreparedStatement psGetChatRooms = null;
      PreparedStatement psUpdateChatRoomSize = null;
      ResultSet rs = null;

      try {
         psGetChatRooms = connection.prepareStatement("select name from chatroom where creator = ? and type = 1 and userowned = 1");
         psUpdateChatRoomSize = connection.prepareStatement("update chatroom set maximumsize = ? where name = ?");
         psGetChatRooms.setString(1, username);
         rs = psGetChatRooms.executeQuery();

         while(rs.next()) {
            String chatRoomName = null;

            try {
               chatRoomName = rs.getString("name");
               psUpdateChatRoomSize.setInt(1, chatRoomSize);
               psUpdateChatRoomSize.setString(2, chatRoomName);
               psUpdateChatRoomSize.executeUpdate();
               ChatRoomUtils.invalidateChatRoomCache(chatRoomName);
               if (this.icePrxFinder != null) {
                  ChatRoomPrx chatRoomPrx = this.icePrxFinder.findChatRoomPrx(chatRoomName);
                  if (chatRoomPrx != null) {
                     chatRoomPrx.setMaximumSize(chatRoomSize);
                  }
               }
            } catch (ObjectNotFoundException var28) {
            } catch (Exception var29) {
               log.error("failed to update chat room size for user [" + username + "], room [" + chatRoomName + "]", var29);
            }
         }
      } catch (Exception var30) {
         log.error("failed to update chat room size for user [" + username + "]", var30);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (psUpdateChatRoomSize != null) {
               psUpdateChatRoomSize.close();
            }
         } catch (SQLException var26) {
            psUpdateChatRoomSize = null;
         }

         try {
            if (psGetChatRooms != null) {
               psGetChatRooms.close();
            }
         } catch (SQLException var25) {
            psGetChatRooms = null;
         }

      }

   }

   public static void main(String[] args) throws Exception {
      Properties databaseProperties = new Properties();
      databaseProperties.load(new FileInputStream(System.getProperty("config.dir") + "database.properties"));
      ComboPooledDataSource datasource = new ComboPooledDataSource();
      log.info("rep jdbc url: " + databaseProperties.getProperty("database.jdbcUrl"));
      datasource.setJdbcUrl(databaseProperties.getProperty("database.jdbcUrl"));
      datasource.setUser(databaseProperties.getProperty("database.username"));
      datasource.setPassword(databaseProperties.getProperty("database.password"));
      datasource.setDriverClass(databaseProperties.getProperty("database.driver"));
      datasource.setMinPoolSize(1);
      datasource.setAcquireIncrement(1);
      datasource.setAcquireRetryAttempts(2);
      datasource.setMaxPoolSize(1);
      UpdateScoreTable update = new UpdateScoreTable(datasource, new DirectoryHolder("/reputation/", "/reputation/scratch/", "/reputation/dump/"), (SelfPopulatingCache)null, (IcePrxFinder)null);
      SortedMap<Integer, ReputationLevelData> levelDataTable = LevelTable.readLevelDataTable(datasource.getConnection());
      ReputationLevelData oldLevelData = LevelTable.getLevelDataForScore(((ReputationLevelData)levelDataTable.get(Integer.parseInt(args[2]))).score, levelDataTable);
      ReputationLevelData newLevelData = LevelTable.getLevelDataForScore(((ReputationLevelData)levelDataTable.get(Integer.parseInt(args[3]))).score, levelDataTable);
      update.onLevelChanged(Integer.parseInt(args[0]), args[1], oldLevelData, newLevelData, datasource.getConnection());
   }
}
