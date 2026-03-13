package com.projectgoth.fusion.ejb;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.EJBExceptionWithErrorCause;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class RewardBean implements SessionBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RewardBean.class));
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private DataSource userRegistrationMaster;
   private DataSource userRegistrationSlave;
   private SecureRandom randomGen;
   private SessionContext context;

   public void setSessionContext(SessionContext newContext) throws EJBException {
      this.context = newContext;
   }

   public void ejbRemove() throws EJBException, RemoteException {
   }

   public void ejbActivate() throws EJBException, RemoteException {
   }

   public void ejbPassivate() throws EJBException, RemoteException {
   }

   public void ejbCreate() throws CreateException {
      try {
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         this.userRegistrationMaster = LookupUtil.getRegistrationMasterDataSource();
         this.userRegistrationSlave = LookupUtil.getRegistrationSlaveDataSource();
         this.randomGen = new SecureRandom();
         SystemProperty.ejbInit(this.dataSourceSlave);
      } catch (Exception var2) {
         log.error("Unable to create RewardSystem EJB", var2);
         throw new CreateException("Unable to create RewardSystem EJB: " + var2.getMessage());
      }
   }

   public int getRewardScoreCap(int level, RewardProgramData.CategoryEnum ce) throws EJBException {
      try {
         Connection conn = this.dataSourceMaster.getConnection();

         int var6;
         try {
            PreparedStatement ps = conn.prepareStatement("select scorecap from rewardscorecap rsc where rsc.level=? and rsc.category=?");

            try {
               ps.setInt(1, level);
               ps.setInt(2, ce.value());
               ResultSet rs = ps.executeQuery();

               try {
                  if (!rs.next()) {
                     return 0;
                  }

                  var6 = rs.getInt(1);
               } finally {
                  rs.close();
               }
            } finally {
               ps.close();
            }
         } finally {
            conn.close();
         }

         return var6;
      } catch (Exception var28) {
         throw new EJBException("failed to getRewardScoreCap :" + var28, var28);
      }
   }

   public HashMap<String, Integer> getRewardScoreCap() throws EJBException {
      try {
         HashMap<String, Integer> result = new HashMap();
         Connection conn = this.dataSourceMaster.getConnection();

         try {
            PreparedStatement ps = conn.prepareStatement("select * from rewardscorecap rsc");

            try {
               ResultSet rs = ps.executeQuery();

               try {
                  while(rs.next()) {
                     String key = RewardCentre.generateCacheKeyForRewardLevelScoreCap(rs.getInt("level"), rs.getInt("category"));
                     result.put(key, rs.getInt("scorecap"));
                  }
               } finally {
                  rs.close();
               }
            } finally {
               ps.close();
            }
         } finally {
            conn.close();
         }

         return result;
      } catch (Exception var27) {
         throw new EJBException("failed to getRewardScoreCap :" + var27, var27);
      }
   }

   public Boolean sendTrigger(RewardProgramTrigger trigger, long waitTimeoutMillis) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_SEND_TRIGGER_FROM_REWARDS_BEAN)) {
         throw new EJBExceptionWithErrorCause(ErrorCause.RewardErrorReasonType.FEATURE_DISABLED, new Object[0]);
      } else {
         try {
            Future<Boolean> futureResult = RewardCentre.getInstance().sendTrigger(trigger);
            if (futureResult == null) {
               throw new EJBException("Null futures after sending trigger");
            } else if (waitTimeoutMillis == -1L) {
               return (Boolean)futureResult.get();
            } else if (waitTimeoutMillis == -2L) {
               return null;
            } else if (waitTimeoutMillis >= 0L) {
               return (Boolean)futureResult.get(waitTimeoutMillis, TimeUnit.MILLISECONDS);
            } else {
               throw new IllegalArgumentException("illegal waitTimeoutMillis : " + waitTimeoutMillis);
            }
         } catch (RuntimeException var5) {
            throw var5;
         } catch (TimeoutException var6) {
            return null;
         } catch (Exception var7) {
            throw new EJBException("Error sending out trigger [" + trigger + "].Exception:" + var7, var7);
         }
      }
   }
}
