package com.projectgoth.fusion.app.dao.base;

import com.projectgoth.configuration.StringListConfigurationValue;
import com.projectgoth.fusion.app.dao.BotDAO;
import com.projectgoth.fusion.app.dao.CampaignDataDAO;
import com.projectgoth.fusion.app.dao.ChatRoomDAO;
import com.projectgoth.fusion.app.dao.EmailDAO;
import com.projectgoth.fusion.app.dao.EmoAndStickerDAO;
import com.projectgoth.fusion.app.dao.GroupDAO;
import com.projectgoth.fusion.app.dao.GuardsetDAO;
import com.projectgoth.fusion.app.dao.MessageDAO;
import com.projectgoth.fusion.app.dao.RecommendationDAO;
import com.projectgoth.fusion.app.dao.UserDataDAO;
import com.projectgoth.fusion.app.dao.config.FusionConfigEnum;
import com.projectgoth.fusion.app.dao.config.FusionStringListConfigurationValue;

public class DAOChainFactory {
   public DAOChain createDAOChain(DAOChainFactory.DAOChainType type) {
      switch(type) {
      case USER_DATA:
         return this.createUserDataDAOChain();
      case GUARDSET:
         return this.createGuardsetDAOChain();
      case EMO_AND_STICKER:
         return this.createEmoAndStickerDAOChain();
      case CHATROOM:
         return this.createChatRoomDAOChain();
      case GROUP:
         return this.createGroupDAOChain();
      case BOT:
         return this.createBotDAOChain();
      case MESSAGE:
         return this.createMessageDAOChain();
      case EMAIL:
         return this.createEmailDAOChain();
      case CAMPAIGN_DATA:
         return this.createCampaignDataDAOChain();
      case RECOMMENDATION:
         return this.createRecommendationDAOChain();
      default:
         return null;
      }
   }

   public EmailDAOChain createEmailDAOChain() {
      return new EmailDAOChain();
   }

   public MessageDAOChain createMessageDAOChain() {
      return new MessageDAOChain();
   }

   public CampaignDataDAOChain createCampaignDataDAOChain() {
      return new CampaignDataDAOChain();
   }

   public BotDAOChain createBotDAOChain() {
      return new BotDAOChain();
   }

   public GroupDAOChain createGroupDAOChain() {
      return new GroupDAOChain();
   }

   public EmoAndStickerDAOChain createEmoAndStickerDAOChain() {
      return new EmoAndStickerDAOChain();
   }

   public UserDataDAOChain createUserDataDAOChain() {
      return new UserDataDAOChain();
   }

   public GuardsetDAOChain createGuardsetDAOChain() {
      return new GuardsetDAOChain();
   }

   public ChatRoomDAOChain createChatRoomDAOChain() {
      return new ChatRoomDAOChain();
   }

   public RecommendationDAOChain createRecommendationDAOChain() {
      return new RecommendationDAOChain();
   }

   public static enum DAOChainType {
      USER_DATA(UserDataDAO.class),
      GUARDSET(GuardsetDAO.class),
      CAMPAIGN_DATA(CampaignDataDAO.class),
      EMO_AND_STICKER(EmoAndStickerDAO.class),
      CHATROOM(ChatRoomDAO.class),
      GROUP(GroupDAO.class),
      BOT(BotDAO.class),
      MESSAGE(MessageDAO.class),
      EMAIL(EmailDAO.class),
      RECOMMENDATION(RecommendationDAO.class);

      public final Class<?> daoClass;
      public final StringListConfigurationValue readConfiguration;
      public final StringListConfigurationValue writeConfiguration;
      public Object dao;

      private DAOChainType(Class<?> daoClass) {
         this.daoClass = daoClass;
         String configurationBaseName = String.format("db.storage.%s", this.toString().replace("_", "").toLowerCase());
         this.readConfiguration = new FusionStringListConfigurationValue(FusionConfigEnum.DAO, configurationBaseName + ".read");
         this.writeConfiguration = new FusionStringListConfigurationValue(FusionConfigEnum.DAO, configurationBaseName + ".write");
      }
   }
}
