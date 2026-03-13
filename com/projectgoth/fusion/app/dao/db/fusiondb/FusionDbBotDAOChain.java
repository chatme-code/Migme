package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.BotDAOChain;
import com.projectgoth.fusion.data.BotData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

public class FusionDbBotDAOChain extends BotDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbBotDAOChain.class);

   public BotData getBot(int botID) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      BotData var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select * from bot where id = ? and status = 1");
         ps.setInt(1, botID);
         rs = ps.executeQuery();
         BotData var5 = rs.next() ? new BotData(rs) : null;
         return var5;
      } catch (SQLException var11) {
         log.error(String.format("Failed to get BotData for bot:%s", botID), var11);
         var6 = super.getBot(botID);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }
}
