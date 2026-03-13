package com.projectgoth.fusion.ejb;

import com.projectgoth.fusion.common.ConfigUtils;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import org.apache.log4j.Logger;

public class EJBHomeCache {
   private static Map<Class, EJBHome> homes = new ConcurrentHashMap();
   private static Map<Class, EJBLocalHome> localHomes = new ConcurrentHashMap();
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EJBHomeCache.class));

   public static EJBHome getHome(String jndiName, Class homeInterface) throws NamingException {
      EJBHome home = (EJBHome)homes.get(homeInterface);
      if (home == null) {
         Object ref = (new InitialContext()).lookup(jndiName);
         home = (EJBHome)PortableRemoteObject.narrow(ref, homeInterface);
         homes.put(homeInterface, home);
      }

      return home;
   }

   public static EJBObject getObject(String jndiName, Class homeInterface) throws CreateException {
      EJBHome home = null;
      Method method = null;

      try {
         method = homeInterface.getMethod("create", (Class[])null);
         home = getHome(jndiName, homeInterface);
         return (EJBObject)method.invoke(home, (Object[])null);
      } catch (NamingException var7) {
         log.error("failed to create bean", var7);
         throw new CreateException(var7.getMessage());
      } catch (NoSuchMethodException var8) {
         log.error("failed to create bean", var8);
         throw new CreateException(var8.getMessage());
      } catch (Exception var9) {
         log.error("failed to create bean", var9);

         try {
            homes.remove(homeInterface);
            home = getHome(jndiName, homeInterface);
            return (EJBObject)method.invoke(home, (Object[])null);
         } catch (Exception var6) {
            log.error("failed to create EJB for " + homeInterface, var9);
            throw new CreateException(var6.getMessage());
         }
      }
   }

   public static EJBLocalHome getLocalHome(String jndiName, Class homeInterface) throws NamingException {
      EJBLocalHome home = (EJBLocalHome)localHomes.get(homeInterface);
      if (home == null) {
         home = (EJBLocalHome)(new InitialContext()).lookup(jndiName);
         localHomes.put(homeInterface, home);
      }

      return home;
   }

   public static EJBLocalObject getLocalObject(String jndiName, Class homeInterface) throws CreateException {
      try {
         Method method = homeInterface.getMethod("create", (Class[])null);
         EJBLocalHome home = getLocalHome(jndiName, homeInterface);
         return (EJBLocalObject)method.invoke(home, (Object[])null);
      } catch (Exception var4) {
         log.error("failed to create bean", var4);
         throw new CreateException(var4.getMessage());
      }
   }
}
