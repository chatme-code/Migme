package com.projectgoth.fusion.rewardsystem.stateprocessors;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.log4j.Log4JUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

public class UserProgramCASState {
   private static final Logger log = Log4JUtils.getLogger(UserProgramCASState.class);
   private static ConcurrentHashMap<String, RewardProgramStateHandler> handlers = new ConcurrentHashMap();

   public static boolean apply(Jedis r, RewardProgramData program, RewardProgramTrigger trigger, String stateParentKey) throws Exception {
      String handlerClassFullPath = program.getRewardProgramStateHandlerClassFullPath();
      if (StringUtil.isBlank(handlerClassFullPath)) {
         return true;
      } else {
         RewardProgramStateHandler handler = getInstance(handlerClassFullPath);
         if (handler == null) {
            return false;
         } else {
            String stateKey = stateParentKey + ":" + handler.getStateKeySuffix();

            RewardProgramStateHandler.PerformReturn ret;
            try {
               while(true) {
                  UserProgramCASState.StateTxn txn = new UserProgramCASState.StateTxn(r, stateKey);

                  try {
                     ret = handler.perform(program, trigger, txn.state);
                     if (!ret.save || txn.tryCommit(ret.newState)) {
                        break;
                     }

                     if (log.isDebugEnabled()) {
                        log.debug(Thread.currentThread().getName() + " transaction conflicted. Restarting transaction");
                     }
                  } finally {
                     txn.close();
                  }
               }
            } catch (JedisException var15) {
               log.error("Jedis exception:" + var15, var15);
               ret = RewardProgramStateHandler.PerformReturn.NOTHING;
            } catch (Exception var16) {
               log.error("Uncaught exception:" + var16, var16);
               ret = RewardProgramStateHandler.PerformReturn.NOTHING;
            }

            return ret.cont;
         }
      }
   }

   private static RewardProgramStateHandler getInstance(String classFullPath) {
      RewardProgramStateHandler currentHandler = (RewardProgramStateHandler)handlers.get(classFullPath);
      if (null == currentHandler) {
         RewardProgramStateHandler newHandler = instantiate(classFullPath);
         if (null == newHandler) {
            return null;
         } else {
            handlers.put(classFullPath, newHandler);
            return newHandler;
         }
      } else {
         return currentHandler;
      }
   }

   private static RewardProgramStateHandler instantiate(String classFullPath) {
      try {
         File f = new File(classFullPath);
         String className = f.getName();
         String parent = f.getParent();
         if (null != parent) {
            URL[] cp = new URL[]{new URL(f.getParent())};
            URLClassLoader urlcl = new URLClassLoader(cp);
            Class<?> clazz = urlcl.loadClass(className);
            if (clazz != null) {
               return (RewardProgramStateHandler)RewardProgramStateHandler.class.cast(clazz.newInstance());
            }
         }

         return (RewardProgramStateHandler)RewardProgramStateHandler.class.cast(Class.forName(className).newInstance());
      } catch (Exception var7) {
         log.error("Unable to load RewardProgramStateHandler [" + classFullPath + "]. Exception:" + var7, var7);
         return null;
      }
   }

   private static class StateTxn {
      private final Jedis j;
      private final String key;
      public final String state;
      private final Transaction txn;
      private UserProgramCASState.StateTxn.FSM_STATE fsm;

      public StateTxn(Jedis j, String key) throws JedisException {
         this.fsm = UserProgramCASState.StateTxn.FSM_STATE.UNINITIALISED;
         this.j = j;
         this.key = key;
         this.watch(this.key);
         this.fsm = UserProgramCASState.StateTxn.FSM_STATE.WATCHING;
         this.state = j.get(key);
         this.txn = this.multi();
         this.fsm = UserProgramCASState.StateTxn.FSM_STATE.OPEN;
      }

      private void watch(String key) throws JedisException {
         String result = this.j.watch(new String[]{key});
         if (!"ok".equalsIgnoreCase(result)) {
            this.fsm = UserProgramCASState.StateTxn.FSM_STATE.ERROR;
            throw new JedisException("watch(" + key + ") does not return 'ok'. Result:[" + result + "]");
         }
      }

      private Transaction multi() throws JedisException {
         Transaction txn = this.j.multi();
         if (txn == null) {
            this.j.unwatch();
            this.fsm = UserProgramCASState.StateTxn.FSM_STATE.ERROR;
            throw new JedisException("multi() call returns null transaction");
         } else {
            return txn;
         }
      }

      public boolean tryCommit(String newState) throws JedisException {
         Response<String> setResponse = this.txn.set(this.key, newState);
         List<Object> execResult = this.txn.exec();
         if (execResult == null) {
            this.fsm = UserProgramCASState.StateTxn.FSM_STATE.CONFLICT;
            return false;
         } else {
            String setResponseVal = (String)setResponse.get();
            if (!"ok".equalsIgnoreCase(setResponseVal)) {
               this.fsm = UserProgramCASState.StateTxn.FSM_STATE.ERROR;
               throw new JedisException("set (" + this.key + ") does not return 'ok'");
            } else {
               this.fsm = UserProgramCASState.StateTxn.FSM_STATE.COMMITTED;
               return true;
            }
         }
      }

      public void close() throws JedisException {
         if (UserProgramCASState.StateTxn.FSM_STATE.OPEN == this.fsm) {
            this.txn.discard();
            this.fsm = UserProgramCASState.StateTxn.FSM_STATE.ABORTED;
         } else if (UserProgramCASState.StateTxn.FSM_STATE.WATCHING == this.fsm) {
            this.j.unwatch();
            this.fsm = UserProgramCASState.StateTxn.FSM_STATE.ABORTED;
         }

      }

      private static enum FSM_STATE {
         UNINITIALISED,
         WATCHING,
         OPEN,
         ABORTED,
         COMMITTED,
         CONFLICT,
         ERROR;
      }
   }
}
