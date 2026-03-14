package com.projectgoth.fusion.app.dao;

import com.projectgoth.fusion.app.dao.base.MessageDAOChain;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MessageDAO {
   private MessageDAOChain readChain;
   private MessageDAOChain writeChain;
   private static SecureRandom randomGen = new SecureRandom();
   private final LazyLoader<Map<Integer, String>> local_help_texts;
   private final LazyLoader<Map<Integer, String>> local_info_texts;

   public MessageDAO(MessageDAOChain readChain, MessageDAOChain writeChain) {
      this.local_help_texts = new LazyLoader<Map<Integer, String>>("LOCAL_HELP_TEXTS", SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EJBCacheDuration.MIS_CLIENT_TEXT) * 1000L) {
         protected Map<Integer, String> fetchValue() throws DAOException {
            if (MessageDAO.this.readChain != null) {
               return MessageDAO.this.readChain.loadHelpTexts();
            } else {
               throw new DAOException("Unable to loadEmoticons");
            }
         }
      };
      this.local_info_texts = new LazyLoader<Map<Integer, String>>("LOCAL_INFO_TEXTS", SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EJBCacheDuration.MIS_CLIENT_TEXT) * 1000L) {
         protected Map<Integer, String> fetchValue() throws DAOException {
            if (MessageDAO.this.readChain != null) {
               return MessageDAO.this.readChain.loadInfoTexts();
            } else {
               throw new DAOException("Unable to loadEmoticons");
            }
         }
      };
      this.readChain = readChain;
      this.writeChain = writeChain;
   }

   public Map<Integer, String> loadHelpTexts() throws DAOException {
      return (Map)this.local_help_texts.getValue();
   }

   public Map<Integer, String> loadInfoTexts() throws DAOException {
      return (Map)this.local_info_texts.getValue();
   }

   public String getInfoText(int infoID) throws DAOException {
      try {
         return (String)this.loadInfoTexts().get(infoID);
      } catch (DAOException var3) {
         return this.readChain.getInfoText(infoID);
      }
   }

   public List<AlertMessageData> getLatestAlertMessageList(int midletVersion, AlertMessageData.TypeEnum type, int countryId, Date minimumDate, AlertContentType alertContentType, int clientType) throws DAOException {
      return this.readChain.getLatestAlertMessageList(midletVersion, type, countryId, minimumDate, alertContentType, clientType);
   }

   public AlertMessageData getLatestAlertMessage(int midletVersion, AlertMessageData.TypeEnum type, int countryId, Date minimumDate, AlertContentType alertContentType, int clientType) throws DAOException {
      List<AlertMessageData> alertMessages = DAOFactory.getInstance().getMessageDAO().getLatestAlertMessageList(midletVersion, type, countryId, minimumDate, alertContentType, clientType);
      if (alertMessages.isEmpty()) {
         return null;
      } else {
         List<Double> accumWeightList = new ArrayList();
         double totalWeight = 0.0D;

         for(Iterator i$ = alertMessages.iterator(); i$.hasNext(); accumWeightList.add(totalWeight)) {
            AlertMessageData alertMessage = (AlertMessageData)i$.next();
            boolean expired = alertMessage.expiryDate.before(new Date());
            boolean neverSeen = minimumDate == null || alertMessage.dateCreated.after(minimumDate);
            if (!expired && (!alertMessage.onceOnly || neverSeen)) {
               totalWeight += alertMessage.weighting;
            }
         }

         if (totalWeight > 0.0D) {
            double pick = randomGen.nextDouble();

            for(int i = 0; i < accumWeightList.size(); ++i) {
               double accumWeight = (Double)accumWeightList.get(i);
               if (accumWeight > 0.0D && accumWeight / totalWeight >= pick) {
                  return (AlertMessageData)alertMessages.get(i);
               }
            }
         }

         return null;
      }
   }
}
