package com.projectgoth.fusion.app.dao;

import com.projectgoth.fusion.app.dao.base.DAOChain;
import com.projectgoth.fusion.app.dao.base.DAOChainFactory;
import com.projectgoth.fusion.app.dao.db.ejb.EJBDAOChainFactory;
import com.projectgoth.fusion.app.dao.db.elasticsearch.ElasticSearchDAOChainFactory;
import com.projectgoth.fusion.app.dao.db.fusiondb.FusionDbDAOChainFactory;
import com.projectgoth.fusion.app.dao.db.memcache.MemcacheDAOChainFactory;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class DAOFactory {
   private static final Logger log = Logger.getLogger(DAOFactory.class);
   private static DAOFactory instance = new DAOFactory();

   public static DAOFactory getInstance() {
      return instance;
   }

   private DAOFactory() {
      log.info("Loading initial DAO Configuration");

      try {
         this.updateConfiguration();
      } catch (Exception var2) {
         log.error(String.format("Error occurred during DAO setup: %s. Fix dao.properties.", var2.getMessage()), var2);
      }

   }

   public EmailDAO getEmailDAO() {
      return (EmailDAO)DAOChainFactory.DAOChainType.EMAIL.dao;
   }

   public MessageDAO getMessageDAO() {
      return (MessageDAO)DAOChainFactory.DAOChainType.MESSAGE.dao;
   }

   public BotDAO getBotDAO() {
      return (BotDAO)DAOChainFactory.DAOChainType.BOT.dao;
   }

   public UserDataDAO getUserDataDAO() {
      return (UserDataDAO)DAOChainFactory.DAOChainType.USER_DATA.dao;
   }

   public GuardsetDAO getGuardsetDAO() {
      return (GuardsetDAO)DAOChainFactory.DAOChainType.GUARDSET.dao;
   }

   public CampaignDataDAO getCampaignDAO() {
      return (CampaignDataDAO)DAOChainFactory.DAOChainType.CAMPAIGN_DATA.dao;
   }

   public EmoAndStickerDAO getEmoAndStickerDAO() {
      return (EmoAndStickerDAO)DAOChainFactory.DAOChainType.EMO_AND_STICKER.dao;
   }

   public ChatRoomDAO getChatRoomDAO() {
      return (ChatRoomDAO)DAOChainFactory.DAOChainType.CHATROOM.dao;
   }

   public GroupDAO getGroupDAO() {
      return (GroupDAO)DAOChainFactory.DAOChainType.GROUP.dao;
   }

   public RecommendationDAO getRecommendationDAO() {
      return (RecommendationDAO)DAOChainFactory.DAOChainType.RECOMMENDATION.dao;
   }

   public void updateConfiguration() {
      Map<String, DAOChainFactory> factoryMap = new HashMap();
      factoryMap.put("fusiondb", new FusionDbDAOChainFactory());
      factoryMap.put("null", new DAOChainFactory());
      factoryMap.put("memcache", new MemcacheDAOChainFactory());
      factoryMap.put("ejb", new EJBDAOChainFactory());
      factoryMap.put("elasticsearch", new ElasticSearchDAOChainFactory());
      DAOChainFactory.DAOChainType[] arr$ = DAOChainFactory.DAOChainType.values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         DAOChainFactory.DAOChainType type = arr$[i$];
         type.dao = this.createDAOChains(factoryMap, type);
      }

   }

   private Object createDAOChains(Map<String, DAOChainFactory> factoryMap, DAOChainFactory.DAOChainType type) {
      log.info("Creating DAO Chains for type=" + type);
      List<String> readConfiguration = new ArrayList(type.readConfiguration.get());
      List<String> writeConfiguration = new ArrayList(type.writeConfiguration.get());
      if (readConfiguration == null || ((List)readConfiguration).size() == 0) {
         readConfiguration = Arrays.asList("null");
      }

      if (writeConfiguration == null || ((List)writeConfiguration).size() == 0) {
         writeConfiguration = Arrays.asList("null");
      }

      DAOChain readChain = null;
      DAOChain writeChain = null;
      log.info(String.format("Setting up DAO chain %s, Read: %s, Write: %s", type, readConfiguration, writeConfiguration));
      List<DAOChain> readDaoArray = new ArrayList(((List)readConfiguration).size());

      int i;
      for(i = 0; i < ((List)readConfiguration).size(); ++i) {
         readDaoArray.add((Object)null);
      }

      String db;
      DAOChain newNode;
      for(i = ((List)readConfiguration).size() - 1; i >= 0; --i) {
         db = (String)((List)readConfiguration).get(i);
         log.info(String.format("Creating DAO Chain for type:%s, db:%s", type, db));
         if (db.startsWith("!")) {
            ((List)readConfiguration).set(i, (Object)null);
         } else {
            newNode = ((DAOChainFactory)factoryMap.get(db)).createDAOChain(type);
            if (newNode != null) {
               newNode.setNextRead(readChain);
               readDaoArray.set(i, newNode);
               readChain = newNode;
            } else {
               log.error(String.format("DAOChainFactory failed to create a ReadChain for: %s. Please update DAOChainFactory:createDAOChain()", type));
            }
         }
      }

      for(i = ((List)writeConfiguration).size() - 1; i >= 0; --i) {
         db = (String)((List)writeConfiguration).get(i);
         if (!db.startsWith("!")) {
            newNode = null;

            for(int j = ((List)readConfiguration).size() - 1; j >= 0 && newNode == null; --j) {
               if (db.equals(((List)readConfiguration).get(j))) {
                  newNode = (DAOChain)readDaoArray.get(j);
                  ((List)readConfiguration).set(j, (Object)null);
                  break;
               }
            }

            if (newNode == null) {
               newNode = ((DAOChainFactory)factoryMap.get(db)).createDAOChain(type);
            }

            newNode.setNextWrite(writeChain);
            writeChain = newNode;
         }
      }

      assert readChain != null : String.format("ReadChain for %s is null", type);

      assert writeChain != null : String.format("WriteChain for %s is null", type);

      try {
         Constructor<?>[] constructors = type.daoClass.getDeclaredConstructors();

         assert constructors.length == 1 && constructors[0].getParameterTypes().length == 2;

         return constructors[0].newInstance(readChain, writeChain);
      } catch (Exception var12) {
         log.error(String.format("Unable to construct dao type: %s", type), var12);
         return null;
      }
   }
}
