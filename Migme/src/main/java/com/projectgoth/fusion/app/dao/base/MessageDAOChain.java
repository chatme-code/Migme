package com.projectgoth.fusion.app.dao.base;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MessageDAOChain implements DAOChain {
   private MessageDAOChain nextRead;
   private MessageDAOChain nextWrite;

   public void setNextRead(DAOChain a) {
      this.nextRead = (MessageDAOChain)a;
   }

   public void setNextWrite(DAOChain a) {
      this.nextWrite = (MessageDAOChain)a;
   }

   public Map<Integer, String> loadHelpTexts() throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.loadHelpTexts();
      } else {
         throw new DAOException("Unabled to loadHelpTexts");
      }
   }

   public Map<Integer, String> loadInfoTexts() throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.loadHelpTexts();
      } else {
         throw new DAOException("Unabled to loadInfoTexts");
      }
   }

   public String getInfoText(int infoID) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getInfoText(infoID);
      } else {
         throw new DAOException(String.format("Unabled to getInfoText for inforID:%s", infoID));
      }
   }

   public List<AlertMessageData> getLatestAlertMessageList(int midletVersion, AlertMessageData.TypeEnum type, int countryId, Date minimumDate, AlertContentType alertContentType, int clientType) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getLatestAlertMessageList(midletVersion, type, countryId, minimumDate, alertContentType, clientType);
      } else {
         throw new DAOException(String.format("Unable to get LatestAlertMessage List for midletVersion:%s, type:%s, country:%s, date:%s, contentype:%s, clientType:%s ", midletVersion, type, countryId, minimumDate, alertContentType, clientType));
      }
   }
}
