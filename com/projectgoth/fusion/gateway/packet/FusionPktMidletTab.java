package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.URLUtil;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktMidletTab extends FusionPacket {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktMidletTab.class));

   public FusionPktMidletTab() {
      super((short)918);
   }

   public FusionPktMidletTab(short transactionId) {
      super((short)918, transactionId);
   }

   public FusionPktMidletTab(FusionPacket packet) {
      super(packet);
   }

   public FusionPktMidletTab(short transactionId, String url, byte focus) {
      super((short)918, transactionId);
      this.setURL(url);
      this.setFocus(focus);
   }

   public String getURL() {
      return this.getStringField((short)1);
   }

   public void setURL(String url) {
      this.setField((short)1, url);
   }

   public Byte getFocus() {
      return this.getByteField((short)2);
   }

   public void setFocus(byte focus) {
      this.setField((short)2, focus);
   }

   public static FusionPktMidletTab newUserRegistrationWizardTab(short transactionId, boolean mobileVerified, ClientType deviceType) throws CreateException, RemoteException {
      String url = URLUtil.replaceViewTypeToken(SystemProperty.get("NewUserRegistrationWizardURL", ""), deviceType);
      return StringUtil.isBlank(url) ? null : new FusionPktMidletTab(transactionId, url, (byte)(mobileVerified ? 1 : 0));
   }

   public static FusionPktMidletTab newHomeTab(short transactionId) throws CreateException, RemoteException {
      String url = SystemProperty.get("HomeURL", "");
      return StringUtil.isBlank(url) ? null : new FusionPktMidletTab(transactionId, url, (byte)0);
   }

   public static FusionPktMidletTab newMigWorldTab(short transactionId, String ipAddress, ClientType deviceType) throws CreateException, RemoteException {
      Web webEJB = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
      String url = null;
      if (webEJB.isIndosatIP(ipAddress)) {
         url = SystemProperty.get("mig33WorldURLIndosat", "");
      }

      if (url == null || url.length() == 0) {
         url = URLUtil.replaceViewTypeToken(SystemProperty.get("mig33WorldURL", ""), deviceType);
      }

      return url != null && url.length() != 0 ? new FusionPktMidletTab(transactionId, url, (byte)0) : null;
   }

   public static FusionPktMidletTab newVASPortalTab(short transactionId, ClientType deviceType) throws CreateException, RemoteException {
      String url = SystemProperty.get("VASPortalURL", "");
      if (url.length() == 0) {
         return null;
      } else {
         url = URLUtil.replaceViewTypeToken(url, deviceType);
         return new FusionPktMidletTab(transactionId, url, (byte)0);
      }
   }

   public static FusionPktMidletTab newMerchantHomePage(short transactionId, ClientType deviceType) throws CreateException, RemoteException {
      String url = SystemProperty.get("MerchantHomeURL", "");
      if (url.length() == 0) {
         return null;
      } else {
         url = URLUtil.replaceViewTypeToken(url, deviceType);
         return new FusionPktMidletTab(transactionId, url, (byte)0);
      }
   }

   public static FusionPktMidletTab newMigGamesPage(short transactionId, ClientType deviceType) throws CreateException, RemoteException {
      String url = SystemProperty.get("migGamesURL", "");
      if (url.length() == 0) {
         return null;
      } else {
         url = URLUtil.replaceViewTypeToken(url, deviceType);
         return new FusionPktMidletTab(transactionId, url, (byte)0);
      }
   }
}
