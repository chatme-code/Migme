package com.projectgoth.fusion.app.dao;

import com.projectgoth.fusion.app.dao.base.EmailDAOChain;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;

public class EmailDAO {
   private EmailDAOChain readChain;
   private EmailDAOChain writeChain;

   public EmailDAO(EmailDAOChain readChain, EmailDAOChain writeChain) {
      this.readChain = readChain;
      this.writeChain = writeChain;
   }

   public boolean isBounceEmailAddress(String email) throws DAOException {
      return !SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.CHECK_EMAIL_BOUNCEDB_ENABLED) ? false : this.readChain.isBounceEmailAddress(email);
   }
}
