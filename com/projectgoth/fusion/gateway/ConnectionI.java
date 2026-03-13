package com.projectgoth.fusion.gateway;

import Ice.Current;
import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.chatsync.MessageStatusEvent;
import com.projectgoth.fusion.clientsession.SSOEncryptedSessionIDInfo;
import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.common.Base62Encoder;
import com.projectgoth.fusion.common.ByteBufferHelper;
import com.projectgoth.fusion.common.Captcha;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ContentUtils;
import com.projectgoth.fusion.common.DataUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.URLUtil;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.DeviceModeType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataAlert;
import com.projectgoth.fusion.fdl.packets.FusionPktDataImSessionStatus;
import com.projectgoth.fusion.fdl.packets.FusionPktDataMidletAction;
import com.projectgoth.fusion.fdl.packets.FusionPktDataServerQuestion;
import com.projectgoth.fusion.gateway.packet.FusionPktAccountBalance;
import com.projectgoth.fusion.gateway.packet.FusionPktAlert;
import com.projectgoth.fusion.gateway.packet.FusionPktAnonymousCallNotification;
import com.projectgoth.fusion.gateway.packet.FusionPktAvatar;
import com.projectgoth.fusion.gateway.packet.FusionPktContact;
import com.projectgoth.fusion.gateway.packet.FusionPktContactListVersion;
import com.projectgoth.fusion.gateway.packet.FusionPktContactListVersionOld;
import com.projectgoth.fusion.gateway.packet.FusionPktContactOld;
import com.projectgoth.fusion.gateway.packet.FusionPktContactRequest;
import com.projectgoth.fusion.gateway.packet.FusionPktDisplayPicture;
import com.projectgoth.fusion.gateway.packet.FusionPktDisplayPictureOld;
import com.projectgoth.fusion.gateway.packet.FusionPktEmoticonHotKeysOld;
import com.projectgoth.fusion.gateway.packet.FusionPktEmoticonHotkeys;
import com.projectgoth.fusion.gateway.packet.FusionPktFileReceived;
import com.projectgoth.fusion.gateway.packet.FusionPktGroup;
import com.projectgoth.fusion.gateway.packet.FusionPktGroupChat;
import com.projectgoth.fusion.gateway.packet.FusionPktIMSessionStatusOld;
import com.projectgoth.fusion.gateway.packet.FusionPktImSessionStatus;
import com.projectgoth.fusion.gateway.packet.FusionPktLogin;
import com.projectgoth.fusion.gateway.packet.FusionPktLoginOld;
import com.projectgoth.fusion.gateway.packet.FusionPktMailInfo;
import com.projectgoth.fusion.gateway.packet.FusionPktMessage;
import com.projectgoth.fusion.gateway.packet.FusionPktMessageStatusEvent;
import com.projectgoth.fusion.gateway.packet.FusionPktMessageStatusEvents;
import com.projectgoth.fusion.gateway.packet.FusionPktMidletAction;
import com.projectgoth.fusion.gateway.packet.FusionPktMidletActionOld;
import com.projectgoth.fusion.gateway.packet.FusionPktMidletTab;
import com.projectgoth.fusion.gateway.packet.FusionPktNotification;
import com.projectgoth.fusion.gateway.packet.FusionPktPresence;
import com.projectgoth.fusion.gateway.packet.FusionPktPresenceOld;
import com.projectgoth.fusion.gateway.packet.FusionPktRemoveContact;
import com.projectgoth.fusion.gateway.packet.FusionPktRemoveContactOld;
import com.projectgoth.fusion.gateway.packet.FusionPktRemoveGroup;
import com.projectgoth.fusion.gateway.packet.FusionPktRemoveGroupOld;
import com.projectgoth.fusion.gateway.packet.FusionPktServerQuestion;
import com.projectgoth.fusion.gateway.packet.FusionPktServerQuestionOld;
import com.projectgoth.fusion.gateway.packet.FusionPktStatusMessage;
import com.projectgoth.fusion.gateway.packet.FusionPktStatusMessageOld;
import com.projectgoth.fusion.gateway.packet.FusionPktUserEvent;
import com.projectgoth.fusion.gateway.packet.FusionPktWebCallNotification;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.ice.IceAmdInvoker;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.slice.AMD_Connection_putMessageAsync;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.ContactDataIce;
import com.projectgoth.fusion.slice.ContactGroupDataIce;
import com.projectgoth.fusion.slice.CurrencyDataIce;
import com.projectgoth.fusion.slice.EmailAlertPrx;
import com.projectgoth.fusion.slice.EventSystemPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.MessageStatusEventIce;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.SessionPrxHelper;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice._ConnectionDisp;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public abstract class ConnectionI extends _ConnectionDisp {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ConnectionI.class));
   protected SocketChannel channel;
   protected Gateway gateway;
   protected GatewayContext gatewayContext;
   protected int remotePort;
   protected Runnable readCallback = new Runnable() {
      public void run() {
         ConnectionI.this.onChannelReadable();
      }
   };
   protected Runnable writeCallback = new Runnable() {
      public void run() {
         ConnectionI.this.onChannelWritable();
      }
   };
   protected String sessionID;
   protected String vasID;
   protected String encryptedSessionID;
   protected boolean registeredWithRegistry;
   protected ClientType deviceType;
   protected short clientVersion;
   protected String mobileDevice;
   protected String userAgent;
   protected String language;
   protected String loginChallenge;
   protected int userID;
   protected String username;
   protected int countryID;
   protected int migLevel;
   protected boolean isChatRoomAdmin;
   protected int chatroomBans;
   protected ConnectionPrx connectionPrx;
   protected UserPrx userPrx;
   protected SessionPrx sessionPrx;
   protected FusionPktLogin loginPacket;
   protected FusionPktLoginOld loginPacketOld;
   protected long lastAccessed;
   protected long lastGroupChatCreated;
   protected long lastChatRoomNotificationSent;
   protected volatile ChatRoomList chatRoomList;
   protected String remoteAddress;
   protected int screenWidth;
   protected int screenHeight;
   protected int optimalEmoticonHeight;
   protected int fontHeight;
   protected int vgSize;
   protected int optimalVirtualGiftSize;
   protected int stickerSize;
   protected boolean defaultGroupPktSent;
   protected Set<String> pendingWebCallRequests = Collections.synchronizedSet(new HashSet());
   protected Map<String, String> pendingAnonymousCallRequests = new ConcurrentHashMap();
   protected AtomicInteger lastContactListVersionSent = new AtomicInteger();
   private FusionPktServerQuestion serverQuestion;
   private FusionPktServerQuestionOld serverQuestionOld;
   protected List<FusionPktMidletTab> midletTabsAfterLogin = Collections.synchronizedList(new LinkedList());
   protected String captchaID;
   protected FusionRequest captchaRequestingPkt;
   private DeviceModeType deviceMode;
   protected long sessionCreatedTime;
   protected long sessionPrxLastTouchedTime;
   protected AtomicInteger failedSessionTouchCount;
   private static final long BLOCK_PACKETS_IN_SLEEP_MODE_CACHED_TTL_MILLIES = 300000L;
   private static final LazyLoader<Set<Short>> BLOCKED_PACKETS_IN_SLEEPMODE_LOADER = new LazyLoader<Set<Short>>("BLOCKED_PACKETS_IN_SLEEPMODE_LOADER", 300000L) {
      protected Set<Short> fetchValue() {
         HashSet<Short> blockedPacket = new HashSet();
         short[] arr$ = SystemProperty.getShortArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.BLOCKED_PACKETS_UPON_SLEEPMODE);
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            short pktNo = arr$[i$];
            blockedPacket.add(pktNo);
         }

         return Collections.unmodifiableSet(blockedPacket);
      }
   };
   private static ClientType[] BROWSER_DEVICES;

   public static boolean packetTypeIsBlockedInSleepMode(short packetTypeNumber) {
      Set<Short> blockedPackets = (Set)BLOCKED_PACKETS_IN_SLEEPMODE_LOADER.getValue();
      return blockedPackets != null && blockedPackets.contains(packetTypeNumber);
   }

   public static void invalidateBlockedInSleepModePackets() {
      BLOCKED_PACKETS_IN_SLEEPMODE_LOADER.invalidateCache();
   }

   public static boolean checkBrowserDevice(ClientType device) {
      return Arrays.asList(BROWSER_DEVICES).contains(device);
   }

   public ConnectionI(Gateway gateway, SocketChannel channel, GatewayContext gatewayContext) {
      this.deviceMode = DeviceModeType.AWAKE;
      this.failedSessionTouchCount = new AtomicInteger(0);
      this.gateway = gateway;
      this.channel = channel;
      this.gatewayContext = gatewayContext;
      this.remoteAddress = channel.socket().getInetAddress().getHostAddress();
      this.remotePort = channel.socket().getPort();
      this.lastAccessed = System.currentTimeMillis();
   }

   public ConnectionI(Gateway gateway, String remoteAddress, int remotePort, GatewayContext gatewayContext) {
      this.deviceMode = DeviceModeType.AWAKE;
      this.failedSessionTouchCount = new AtomicInteger(0);
      this.gateway = gateway;
      this.channel = null;
      this.gatewayContext = gatewayContext;
      this.remoteAddress = remoteAddress;
      this.remotePort = remotePort;
      this.lastAccessed = System.currentTimeMillis();
   }

   public ConnectionI(ConnectionI connection) {
      this.deviceMode = DeviceModeType.AWAKE;
      this.failedSessionTouchCount = new AtomicInteger(0);
      this.gateway = connection.gateway;
      this.gatewayContext = connection.gatewayContext;
      this.remoteAddress = connection.remoteAddress;
      this.lastAccessed = System.currentTimeMillis();
   }

   public Runnable getReadCallBack() {
      return this.readCallback;
   }

   public Runnable getWriteCallBack() {
      return this.writeCallback;
   }

   public String getUsername(Current __current) {
      return this.username;
   }

   public String getRemoteIPAddress(Current __current) {
      return this.remoteAddress;
   }

   public UserPrx getUserObject(Current __current) {
      return this.userPrx;
   }

   public SessionPrx getSessionObject(Current __current) {
      return this.sessionPrx;
   }

   public SelectionKey getSelectionKey(Selector selector) {
      return this.channel == null ? null : this.channel.keyFor(selector);
   }

   public int getUserID() {
      return this.userID;
   }

   public boolean isChatRoomAdmin() {
      return this.isChatRoomAdmin;
   }

   public int getMigLevel() {
      return this.migLevel;
   }

   public boolean isBannedFromChatrooms() {
      boolean memcacheChatRoomBanKeySet = MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BAN, this.username) != null;
      if (memcacheChatRoomBanKeySet) {
         return true;
      } else {
         if (this.chatroomBans > 0) {
            int maxAdminBansAllowed = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_CHATROOM_BANS);
            if (this.chatroomBans >= maxAdminBansAllowed || memcacheChatRoomBanKeySet) {
               return true;
            }

            int bansBeforeSuspension = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CHATROOM_BANS_BEFORE_SUSPENSION);
            if (this.chatroomBans == bansBeforeSuspension && memcacheChatRoomBanKeySet) {
               return true;
            }
         }

         return false;
      }
   }

   public String getDisplayName() {
      return this.username == null ? this.remoteAddress + ":" + this.remotePort : this.remoteAddress + ":" + this.remotePort + " (" + this.username + ")";
   }

   public int getCountryID() {
      return this.countryID;
   }

   public String getRemoteAddress() {
      return this.remoteAddress;
   }

   public int getPort() {
      return this.gateway.getPort();
   }

   public int getRemotePort() {
      return this.remotePort;
   }

   public Gateway getGateway() {
      return this.gateway;
   }

   public GatewayContext getGatewayContext() {
      return this.gatewayContext;
   }

   public String getSessionID() {
      return this.sessionID;
   }

   public String getVasID() {
      return this.vasID;
   }

   public synchronized String getEncryptedSessionID() {
      int userid;
      User userEJB;
      SSOEncryptedSessionIDInfo eidInfo;
      if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
         if (this.encryptedSessionID == null) {
            if (this.loginPacket == null) {
               log.error(String.format("Unable to create encrypted session id due to username not exist, sid=%s", this.sessionID));
               return null;
            }

            userid = this.userID;
            if (userid <= 0) {
               userid = 0;

               try {
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
                     userid = (new UserObject(this.loginPacket.getUsername())).getUserID(false);
                  } else {
                     userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                     userid = userEJB.getUserID(this.loginPacket.getUsername(), (Connection)null, false);
                  }
               } catch (CreateException var3) {
                  log.error(String.format("Unable to get user bean to get user id from username %s, using 0 as userid in eid for session %s", this.loginPacket.getUsername(), this.sessionID));
               } catch (RemoteException var4) {
                  log.error(String.format("Unable to get user id from username %s, using 0 as userid in eid for session %s", this.loginPacket.getUsername(), this.sessionID));
               } catch (DAOException var5) {
                  log.error(String.format("DAO: Unable to get user id from username %s, using 0 as userid in eid for session %s", this.loginPacket.getUsername(), this.sessionID));
               }
            }

            eidInfo = new SSOEncryptedSessionIDInfo(this.sessionID, userid, this.loginPacket.getUsername(), this.remoteAddress, this.userAgent, 0L);
            this.encryptedSessionID = SSOLogin.generateEncryptedSessionID(eidInfo);
         }

         return this.encryptedSessionID;
      } else {
         if (this.encryptedSessionID == null) {
            if (this.loginPacketOld == null) {
               log.error(String.format("Unable to create encrypted session id due to username not exist, sid=%s", this.sessionID));
               return null;
            }

            userid = this.userID;
            if (userid <= 0) {
               userid = 0;

               try {
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
                     userid = (new UserObject(this.loginPacketOld.getUsername())).getUserID(false);
                  } else {
                     userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                     userid = userEJB.getUserID(this.loginPacketOld.getUsername(), (Connection)null, false);
                  }
               } catch (CreateException var6) {
                  log.error(String.format("Unable to get user bean to get user id from username %s, using 0 as userid in eid for session %s", this.loginPacketOld.getUsername(), this.sessionID));
               } catch (RemoteException var7) {
                  log.error(String.format("Unable to get user id from username %s, using 0 as userid in eid for session %s", this.loginPacketOld.getUsername(), this.sessionID));
               } catch (DAOException var8) {
                  log.error(String.format("DAO: Unable to get user id from username %s, using 0 as userid in eid for session %s", this.loginPacketOld.getUsername(), this.sessionID));
               }
            }

            eidInfo = new SSOEncryptedSessionIDInfo(this.sessionID, userid, this.loginPacketOld.getUsername(), this.remoteAddress, this.userAgent, 0L);
            this.encryptedSessionID = SSOLogin.generateEncryptedSessionID(eidInfo);
         }

         return this.encryptedSessionID;
      }
   }

   public String getLoginChallenge() {
      return this.loginChallenge;
   }

   public ClientType getDeviceType() {
      return this.deviceType;
   }

   public int getDeviceTypeAsInt(Current __current) {
      return this.deviceType == null ? 0 : this.deviceType.value();
   }

   public short getClientVersion(Current __current) {
      return this.clientVersion;
   }

   public String getMobileDevice(Current __current) {
      return this.mobileDevice;
   }

   public String getUserAgent(Current __current) {
      return this.userAgent;
   }

   public String getLanguage() {
      return this.language;
   }

   public void setLanguage(String language) {
      if (language != null) {
         this.language = language;
      }

   }

   public int getScreenWidth() {
      return this.screenWidth;
   }

   public int getScreenHeight() {
      return this.screenHeight;
   }

   public int getOptimalEmoticonHeight() {
      return this.optimalEmoticonHeight;
   }

   public void setOptimalEmoticonHeight(int optimalEmoticonHeight) {
      this.optimalEmoticonHeight = optimalEmoticonHeight;
   }

   public void setFontHeight(int fontHeight) {
      this.fontHeight = fontHeight;
   }

   public int getFontHeight() {
      return this.fontHeight;
   }

   public void setVgSize(int vgSize) {
      this.vgSize = vgSize;
   }

   public int getVgSize() {
      return this.vgSize;
   }

   public void setStickerSize(int stickerSize) {
      this.stickerSize = stickerSize;
   }

   public int getStickerSize() {
      return this.stickerSize;
   }

   public int getOptimalVirtualGiftSize() {
      return this.optimalVirtualGiftSize;
   }

   public void setOptimalVirtualGiftSize(int optimalVirtualGiftSize) {
      this.optimalVirtualGiftSize = optimalVirtualGiftSize;
   }

   public AtomicInteger getLastContactListVersionSent() {
      return this.lastContactListVersionSent;
   }

   public ConnectionPrx getConnectionPrx() {
      return this.connectionPrx;
   }

   public UserPrx getUserPrx() {
      return this.userPrx;
   }

   public SessionPrx getSessionPrx() {
      return this.sessionPrx;
   }

   public FusionPktLogin getLoginPacket() {
      return this.loginPacket;
   }

   /** @deprecated */
   @Deprecated
   public FusionPktLoginOld getLoginPacketOld() {
      return this.loginPacketOld;
   }

   public void setLoginPacket(FusionPktLogin loginPacket) {
      if (loginPacket != null) {
         if (null != loginPacket.getClientType()) {
            this.deviceType = loginPacket.getClientType();
         } else {
            log.warn(String.format("No client type provided in ConnectionI user='%s', sessionId='%s'", loginPacket.getUsername(), loginPacket.getSessionId()));
         }

         this.clientVersion = loginPacket.getClientVersion();
         this.mobileDevice = DataUtils.truncateMobileDevice(loginPacket.getDeviceName(), true, String.format("in ConnectionI user='%s', sessionId='%s'", loginPacket.getUsername(), loginPacket.getSessionId()));
         this.userAgent = DataUtils.truncateMobileDevice(loginPacket.getUserAgent(), true, String.format("in ConnectionI user='%s', sessionId='%s'", loginPacket.getUsername(), loginPacket.getSessionId()));
         this.language = loginPacket.getLanguage();
         this.screenWidth = loginPacket.getScreenWidth() == null ? 0 : loginPacket.getScreenWidth();
         this.screenHeight = loginPacket.getScreenHeight() == null ? 0 : loginPacket.getScreenHeight();
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE425_ENABLED)) {
            this.vasID = loginPacket.getVasTrackingId();
         }
      }

      this.loginPacket = loginPacket;
   }

   /** @deprecated */
   @Deprecated
   public void setLoginPacketOld(FusionPktLoginOld loginPacketOld) {
      if (loginPacketOld != null) {
         if (null != loginPacketOld.getClientType()) {
            this.deviceType = ClientType.fromValue(loginPacketOld.getClientType());
         } else {
            log.warn(String.format("No client type provided in ConnectionI user='%s', sessionId='%s'", loginPacketOld.getUsername(), loginPacketOld.getSessionId()));
         }

         this.clientVersion = loginPacketOld.getClientVersion();
         this.mobileDevice = DataUtils.truncateMobileDevice(loginPacketOld.getMobileDevice(), true, String.format("in ConnectionI user='%s', sessionId='%s'", loginPacketOld.getUsername(), loginPacketOld.getSessionId()));
         this.userAgent = DataUtils.truncateMobileDevice(loginPacketOld.getUserAgent(), true, String.format("in ConnectionI user='%s', sessionId='%s'", loginPacketOld.getUsername(), loginPacketOld.getSessionId()));
         this.language = loginPacketOld.getLanguage();
         this.screenWidth = loginPacketOld.getScreenWidth() == null ? 0 : loginPacketOld.getScreenWidth();
         this.screenHeight = loginPacketOld.getScreenHeight() == null ? 0 : loginPacketOld.getScreenHeight();
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE425_ENABLED)) {
            this.vasID = loginPacketOld.getVASTrackingId();
         }
      }

      this.loginPacketOld = loginPacketOld;
   }

   public FusionPktServerQuestion getServerQuestion() {
      return this.serverQuestion;
   }

   public void setServerQuestion(FusionPktServerQuestion serverQuestion) {
      this.serverQuestion = serverQuestion;
   }

   public FusionPktServerQuestionOld getServerQuestionOld() {
      return this.serverQuestionOld;
   }

   public void setServerQuestionOld(FusionPktServerQuestionOld serverQuestion) {
      this.serverQuestionOld = serverQuestion;
   }

   public void setDeviceType(ClientType deviceType) {
      this.deviceType = deviceType;
   }

   public List<FusionPktMidletTab> getMidletTabsAfterLogin() {
      return this.midletTabsAfterLogin;
   }

   public void addMidletTabAfterLogin(FusionPktMidletTab midletTabAfterLogin) {
      if (midletTabAfterLogin != null) {
         this.midletTabsAfterLogin.add(midletTabAfterLogin);
      }

   }

   public void clearMidletTabAfterLogin() {
      this.midletTabsAfterLogin.clear();
   }

   public ChatRoomList getChatRoomList() {
      if (this.chatRoomList == null) {
         synchronized(this) {
            if (this.chatRoomList == null) {
               this.chatRoomList = new ChatRoomList();
            }
         }
      }

      return this.chatRoomList;
   }

   public ChatRoomDataIce[] getPopularChatRooms(Current __current) throws FusionException {
      ArrayList<ChatRoomDataIce> chatRoomDataIceArray = new ArrayList();
      this.chatRoomList = this.getChatRoomList();
      RegistryPrx registryPrx = this.findRegistry();
      ChatRoomList.Page popularChatRooms = null;

      try {
         popularChatRooms = this.chatRoomList.getPopularChatRooms(registryPrx, this.getCountryID(), true);
         Iterator i$ = popularChatRooms.getPageList().iterator();

         while(i$.hasNext()) {
            ChatRoomData room = (ChatRoomData)i$.next();
            chatRoomDataIceArray.add(room.toIceObject());
         }
      } catch (Exception var7) {
         log.error("Exception in getPopularChatRooms", var7);
         throw new FusionException(var7.getMessage());
      }

      return (ChatRoomDataIce[])chatRoomDataIceArray.toArray(new ChatRoomDataIce[chatRoomDataIceArray.size()]);
   }

   public long getLastAccessed() {
      return this.lastAccessed;
   }

   public long getSessionPrxLastTouchedTime() {
      return this.sessionPrxLastTouchedTime;
   }

   public long getSessionCreatedTime() {
      return this.sessionCreatedTime;
   }

   public boolean clearPendingWebCallRequest(String requestingUsername) {
      return this.pendingWebCallRequests.remove(requestingUsername);
   }

   public String clearPendingAnonymousCallRequest(String requestingUsername) {
      return (String)this.pendingAnonymousCallRequests.remove(requestingUsername);
   }

   protected void accessed() {
      if (System.currentTimeMillis() - this.lastAccessed > (long)this.gateway.getKeepAliveInterval()) {
         this.alwaysTouch();
      }

   }

   private void alwaysTouch() {
      if (this.connectionPrx != null) {
         this.gateway.onConnectionAccessed(this);
      }

      if (this.sessionPrx != null) {
         try {
            this.sessionPrx.touch();
            this.sessionPrxLastTouchedTime = System.currentTimeMillis();
         } catch (Exception var3) {
            log.warn("Exception in ConnectionI.alwaysTouch: lastAccessed=" + this.lastAccessed + " sessionPrxLastTouchedTime=" + this.sessionPrxLastTouchedTime + " sessionCreatedTime=" + this.sessionCreatedTime + " sessionID=" + this.sessionID + " username=" + this.username + " e=" + var3, var3);
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.DISCONNECT_ON_SESSION_FAILURE_ENABLED)) {
               int failed = this.failedSessionTouchCount.incrementAndGet();
               if (failed >= SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.MAX_SESSION_TOUCH_FAILURES)) {
                  log.warn("Disconnecting connection as failed to touch owning session " + failed + " times, " + "sessionID=" + this.sessionID + " username=" + this.username);
                  this.removeConnectionFromIceAdaptor();
                  this.disconnect();
               }
            }
         }
      }

      this.lastAccessed = System.currentTimeMillis();
   }

   public long getLastGroupChatCreated() {
      return this.lastGroupChatCreated;
   }

   public void groupChatCreated() {
      this.lastGroupChatCreated = System.currentTimeMillis();
   }

   public long getLastChatRoomNotificationSent() {
      return this.lastChatRoomNotificationSent;
   }

   public void chatRoomNotificationSent() {
      this.lastChatRoomNotificationSent = System.currentTimeMillis();
   }

   public boolean supportsCaptcha() {
      return this.isMidletVersionAndAbove(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIN_MIDLET_VERSION_FOR_NATIVE_CAPTCHA)) || this.isBrowserDevice();
   }

   public Captcha createCaptcha(FusionRequest requestingPkt) {
      return this.createCaptcha(requestingPkt, 3);
   }

   public Captcha createCaptcha(FusionRequest requestingPkt, int captchaLength) {
      Captcha captcha = this.gatewayContext.getCaptchaService().nextCaptcha(captchaLength);
      this.captchaID = captcha.getId();
      this.captchaRequestingPkt = requestingPkt;
      return captcha;
   }

   public Captcha updateCaptcha() {
      return this.updateCaptcha(3);
   }

   public Captcha updateCaptcha(int captchaLength) {
      Captcha captcha = this.gatewayContext.getCaptchaService().nextCaptcha(captchaLength);
      this.captchaID = captcha.getId();
      return captcha;
   }

   public boolean isCheckingCaptcha() {
      return this.captchaID != null;
   }

   public FusionRequest validateCaptchaResponse(String response) {
      if (this.captchaID != null && this.gatewayContext.getCaptchaService().validateResponse(this.captchaID, response)) {
         FusionRequest requestingPkt = this.captchaRequestingPkt;
         this.captchaRequestingPkt = null;
         this.captchaID = null;
         return requestingPkt;
      } else {
         return null;
      }
   }

   public String newChallengeString() {
      String username = "";
      if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
         if (this.loginPacket != null) {
            username = this.loginPacket.getUsername();
            if (username == null) {
               username = "";
            }
         }
      } else if (this.loginPacketOld != null) {
         username = this.loginPacketOld.getUsername();
         if (username == null) {
            username = "";
         }
      }

      return newChallengeString(username);
   }

   public static String newChallengeString(String username) {
      long time = System.currentTimeMillis() / 100L % 1000000L;
      long hash = Math.abs((long)username.hashCode()) * 1000000L + time;
      return Base62Encoder.encode(hash);
   }

   public static String newSessionID() {
      return (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replaceAll("-", "");
   }

   public boolean isLoggedIn() {
      return this.sessionID != null && this.username != null && this.userPrx != null && this.sessionPrx != null && this.connectionPrx != null;
   }

   public RegistryPrx findRegistry() {
      try {
         return this.gatewayContext.getIcePrxFinder().getRegistry(false);
      } catch (Exception var2) {
         log.error("unable to get registry proxy", var2);
         return null;
      }
   }

   public EmailAlertPrx findEmailAlert() {
      try {
         return this.gatewayContext.getIcePrxFinder().getEmailAlert();
      } catch (Exception var2) {
         log.error("unable to get email alert  proxy", var2);
         return null;
      }
   }

   public EventSystemPrx findEventSystem() {
      try {
         return this.gatewayContext.getIcePrxFinder().getEventSystemProxy();
      } catch (Exception var2) {
         log.error("unable to get event system proxy", var2);
         return null;
      }
   }

   public AuthenticationServicePrx findAuthenticationService() {
      try {
         return this.gatewayContext.getIcePrxFinder().getAuthenticationServiceProxy();
      } catch (Exception var2) {
         log.error("unable to get authentication service proxy", var2);
         return null;
      }
   }

   protected ConnectionPrx addConnectionToIceAdaptor(boolean registerWithRegistry) {
      if (this.connectionPrx == null) {
         try {
            if (this.sessionID == null) {
               this.sessionID = newSessionID();
            }

            if (this.gateway.findConnection(this.sessionID) == null) {
               this.connectionPrx = this.gateway.addConnection(this.sessionID, this);
               if (registerWithRegistry) {
                  RegistryPrx registryPrx = this.findRegistry();
                  if (registryPrx == null) {
                     throw new Exception("Unable to locate registry");
                  }

                  registryPrx.registerConnectionObject(this.sessionID, this.connectionPrx);
                  this.registeredWithRegistry = true;
               }
            } else {
               log.warn("Duplicated session ID " + this.sessionID);
            }
         } catch (Exception var3) {
            log.warn("Exception occured while adding connection to Ice adapter", var3);
            this.removeConnectionFromIceAdaptor();
         }
      }

      return this.connectionPrx;
   }

   public synchronized void removeConnectionFromIceAdaptor() {
      if (this.connectionPrx != null) {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.se493WebSocketsEnabled.getValue()) {
            this.gateway.removeConnection(this.getObjectIdWithinGateway(), this.remoteAddress);
         } else {
            this.gateway.removeConnection(this.sessionID, this.remoteAddress);
         }

         String id = this.sessionID;
         this.sessionID = null;
         this.loginChallenge = null;
         this.connectionPrx = null;
         if (this.registeredWithRegistry && id != null) {
            RegistryPrx registryPrx = this.findRegistry();
            if (registryPrx != null) {
               registryPrx.deregisterConnectionObject(id);
            }
         }
      }

   }

   public void disconnect() {
      this.gateway.onConnectionDisconnected(this);
      if (this.channel != null) {
         int socketCloseDelay = this.gateway.getSocketCloseDelay();
         if (socketCloseDelay > 0) {
            try {
               Thread.sleep((long)socketCloseDelay);
            } catch (Exception var7) {
            }
         }

         try {
            this.channel.socket().shutdownOutput();
         } catch (Exception var6) {
         }

         try {
            this.channel.socket().shutdownInput();
         } catch (Exception var5) {
         }

         try {
            this.channel.socket().close();
         } catch (Exception var4) {
         }

         try {
            this.channel.close();
         } catch (Exception var3) {
         }
      }

   }

   public void disconnect(String reason, Current __current) throws FusionException {
      this.putAlertMessage(reason, "Disconnected", (short)0);
      this.logoutSession();
   }

   public void logout(Current __current) {
      this.logoutSession();
   }

   private void logoutSession() {
      synchronized(this) {
         this.sessionPrx = null;
      }

      this.onSessionTerminated();
      this.disconnect();
   }

   public boolean isOnLoginCalled() {
      return this.sessionID != null && this.loginChallenge != null;
   }

   public ConnectionI onLogin(FusionPktLogin loginPacket) throws Exception {
      this.setLoginPacket(loginPacket);
      this.loginChallenge = this.newChallengeString();
      this.sessionID = newSessionID();
      return this;
   }

   public ConnectionI onLogin(FusionPktLoginOld loginPacket) throws Exception {
      this.setLoginPacketOld(loginPacket);
      this.loginChallenge = this.newChallengeString();
      this.sessionID = newSessionID();
      return this;
   }

   public void onCreatingSession() {
   }

   public void onSessionCreated(UserPrx userPrx, SessionPrx sessionPrx, UserData userData) {
      this.userPrx = userPrx;
      this.sessionPrx = sessionPrx;
      this.sessionCreatedTime = System.currentTimeMillis();
      this.userID = userData.userID;
      this.username = userData.username;
      this.countryID = userData.countryID;
      this.isChatRoomAdmin = userData.chatRoomAdmin;
      if (userData.chatRoomBans != null) {
         this.chatroomBans = userData.chatRoomBans;
      }

      try {
         ReputationLevelData level = null;
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
            level = (new UserObject(this.username)).getReputationLevel();
         } else {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            level = userEJB.getReputationLevelByUserid(userData.userID);
         }

         if (level != null) {
            this.migLevel = level.level;
         }
      } catch (Exception var8) {
         log.warn("Unexpected exception while retrieving miglevel for user [" + this.username + "] " + var8.getMessage(), var8);
      }

      if (this.loginPacket.getFontHeight() != null) {
         int fontHeight = this.loginPacket.getFontHeight();
         this.setFontHeight(fontHeight);

         try {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EMOANDSTICKER_DAO)) {
               this.optimalEmoticonHeight = DAOFactory.getInstance().getEmoAndStickerDAO().getOptimalEmoticonHeight(fontHeight);
            } else {
               Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
               this.optimalEmoticonHeight = contentEJB.getOptimalEmoticonHeight(this.username, fontHeight);
            }
         } catch (Exception var7) {
            this.optimalEmoticonHeight = 0;
         }
      } else {
         this.setFontHeight(0);
      }

      VirtualGiftData.ImageFormatType imageFormatType = this.isMidletVersionAndAbove(400) ? VirtualGiftData.ImageFormatType.PNG : VirtualGiftData.ImageFormatType.GIF;
      short[] supportedVGSize = ContentUtils.getSupportedVirtualGiftResolutions(imageFormatType.getCode());
      int stickerSize;
      if (this.loginPacket.getVirtualGiftPixelSize() != null) {
         stickerSize = this.loginPacket.getVirtualGiftPixelSize();
         this.setVgSize(stickerSize);
         this.optimalVirtualGiftSize = ContentUtils.getValueOrRoundDown(supportedVGSize, (short)stickerSize);
      } else {
         this.setVgSize(0);
         this.optimalVirtualGiftSize = ContentUtils.getValueOrRoundDown(supportedVGSize, (short)this.getFontHeight());
      }

      if (this.loginPacket.getStickerPixelSize() != null) {
         stickerSize = this.loginPacket.getStickerPixelSize();
         this.setStickerSize(stickerSize);
      } else {
         this.setStickerSize(0);
      }

      this.loginPacket = null;
   }

   public void onSessionCreatedOld(UserPrx userPrx, SessionPrx sessionPrx, UserData userData) {
      this.userPrx = userPrx;
      this.sessionPrx = sessionPrx;
      this.sessionCreatedTime = System.currentTimeMillis();
      this.userID = userData.userID;
      this.username = userData.username;
      this.countryID = userData.countryID;
      this.isChatRoomAdmin = userData.chatRoomAdmin;
      if (userData.chatRoomBans != null) {
         this.chatroomBans = userData.chatRoomBans;
      }

      try {
         ReputationLevelData level = null;
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
            level = (new UserObject(this.username)).getReputationLevel();
         } else {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            level = userEJB.getReputationLevelByUserid(userData.userID);
         }

         if (level != null) {
            this.migLevel = level.level;
         }
      } catch (Exception var8) {
         log.warn("Unexpected exception while retrieving miglevel for user [" + this.username + "] " + var8.getMessage(), var8);
      }

      if (this.loginPacketOld.getFontHeight() != null) {
         int fontHeight = this.loginPacketOld.getFontHeight();
         this.setFontHeight(fontHeight);

         try {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EMOANDSTICKER_DAO)) {
               this.optimalEmoticonHeight = DAOFactory.getInstance().getEmoAndStickerDAO().getOptimalEmoticonHeight(fontHeight);
            } else {
               Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
               this.optimalEmoticonHeight = contentEJB.getOptimalEmoticonHeight(this.username, fontHeight);
            }
         } catch (Exception var7) {
            this.optimalEmoticonHeight = 0;
         }
      } else {
         this.setFontHeight(0);
      }

      VirtualGiftData.ImageFormatType imageFormatType = this.isMidletVersionAndAbove(400) ? VirtualGiftData.ImageFormatType.PNG : VirtualGiftData.ImageFormatType.GIF;
      short[] supportedVGSize = ContentUtils.getSupportedVirtualGiftResolutions(imageFormatType.getCode());
      int stickerSize;
      if (this.loginPacketOld.getVGSize() != null) {
         stickerSize = this.loginPacketOld.getVGSize();
         this.setVgSize(stickerSize);
         this.optimalVirtualGiftSize = ContentUtils.getValueOrRoundDown(supportedVGSize, (short)stickerSize);
      } else {
         this.setVgSize(0);
         this.optimalVirtualGiftSize = ContentUtils.getValueOrRoundDown(supportedVGSize, (short)this.getFontHeight());
      }

      if (this.loginPacketOld.getStickerSize() != null) {
         stickerSize = this.loginPacketOld.getStickerSize();
         this.setStickerSize(stickerSize);
      } else {
         this.setStickerSize(0);
      }

      this.loginPacketOld = null;
   }

   public synchronized void onSessionTerminated() {
      this.removeConnectionFromIceAdaptor();
      if (this.sessionPrx != null) {
         try {
            SessionPrxHelper.uncheckedCast(this.sessionPrx.ice_oneway()).endSessionOneWay();
         } catch (Exception var2) {
         }
      }

      this.sessionPrx = null;
      this.userPrx = null;
   }

   public void putMessage(MessageDataIce message, Current __current) throws FusionException {
      MessageData msg = new MessageData(message);
      int destinations = msg.messageDestinations == null ? 0 : msg.messageDestinations.size();
      if (destinations > 1) {
         throw new FusionException("Multiple message destinations in message body");
      } else {
         FusionPktMessage pkt = new FusionPktMessage(msg);
         this.sendFusionPacket(pkt);
      }
   }

   public void putMessageAsync_async(final AMD_Connection_putMessageAsync cb, final MessageDataIce message, Current __current) throws FusionException {
      IceAmdInvoker ivk = new GatewayIceAmdInvoker() {
         public void payload() throws Exception {
            ConnectionI.this.putMessage(message);
         }

         public void ice_response() {
            cb.ice_response();
         }

         public void ice_exception(Exception e) {
            cb.ice_exception(e);
         }

         public String getLogContext() {
            return "Connection.putMessage with message.source=" + message.source;
         }
      };
      ivk.invoke();
   }

   public void putMessageOneWay(MessageDataIce message, Current __current) {
      try {
         this.putMessage(message);
      } catch (Exception var4) {
      }

   }

   public void putMessages(MessageDataIce[] messages, Current __current) throws FusionException {
      MessageDataIce[] arr$ = messages;
      int len$ = messages.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MessageDataIce message = arr$[i$];
         this.putMessage(message);
      }

   }

   public void contactChangedPresenceOneWay(int contactID, int imType, int presence, Current __current) {
      try {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
            FusionPktPresence pkt = new FusionPktPresence(contactID, imType, presence, this);
            this.sendFusionPacket(pkt);
         } else {
            FusionPktPresenceOld pkt = new FusionPktPresenceOld(contactID, imType, presence, this);
            this.sendFusionPacket(pkt);
         }
      } catch (Exception var6) {
      }

   }

   public void contactChangedDisplayPictureOneWay(int contactID, String displayPicture, long timeStamp, Current __current) {
      try {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
            FusionPktDisplayPicture pkt = new FusionPktDisplayPicture();
            pkt.setContactId(contactID);
            pkt.setDisplayPictureGuid(displayPicture);
            pkt.setTimestamp(timeStamp);
            this.sendFusionPacket(pkt);
         } else {
            FusionPktDisplayPictureOld pkt = new FusionPktDisplayPictureOld();
            pkt.setContactID(contactID);
            pkt.setDisplayPicture(displayPicture);
            pkt.setStatusTimeStamp(timeStamp);
            this.sendFusionPacket(pkt);
         }
      } catch (Exception var7) {
      }

   }

   public void contactChangedStatusMessageOneWay(int contactID, String statusMessage, long timeStamp, Current __current) {
      try {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
            FusionPktStatusMessage pkt = new FusionPktStatusMessage();
            pkt.setContactId(contactID);
            pkt.setStatusMessage(statusMessage);
            pkt.setTimestamp(timeStamp);
            this.sendFusionPacket(pkt);
         } else {
            FusionPktStatusMessageOld pkt = new FusionPktStatusMessageOld();
            pkt.setContactID(contactID);
            pkt.setStatusMessage(statusMessage);
            pkt.setStatusTimeStamp(timeStamp);
            this.sendFusionPacket(pkt);
         }
      } catch (Exception var7) {
      }

   }

   public void contactRequest(String contactUsername, int outstandingRequests, Current __current) throws FusionException {
      try {
         if (!this.isMidletVersionAndAbove(410)) {
            String url = SystemProperty.get("UserProfileURL") + contactUsername;
            this.sendFusionPacket(new FusionPktContactRequest(contactUsername, url));
         }

      } catch (NoSuchFieldException var5) {
         throw new FusionException(var5.getMessage());
      }
   }

   public void contactRequestAccepted(ContactDataIce contact, int contactListVersion, int outstandingRequests, Current __current) throws FusionException {
      boolean shouldSendPacket = false;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.USE_GUARDSET_FOR_CONTACT_REQUEST_ACCEPTED)) {
         shouldSendPacket = MemCacheOrEJB.canAccess(this.deviceType, this.clientVersion, GuardCapabilityEnum.FUSION_PKT_CONTACT_REQUEST_ACCEPTED_SUPPORT);
      } else {
         shouldSendPacket = this.isMidletVersionAndAbove(410);
      }

      if (shouldSendPacket) {
         this.sendFusionPacket(new FusionPktContactOld((short)-1, new ContactData(contact), this));
         this.sendContactListVersionPacket(contactListVersion);
      }

   }

   public void contactRequestRejected(String contactUsername, int outstandingRequests, Current __current) throws FusionException {
      if (this.isMidletVersionAndAbove(410)) {
      }

   }

   private void sendNotificationPacket(int outstandingContactRequests) throws FusionException {
      try {
         if (outstandingContactRequests == 0) {
            this.sendFusionPacket(new FusionPktNotification());
         } else {
            String text = null;
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_MESSAGE_DAO)) {
               text = DAOFactory.getInstance().getMessageDAO().getInfoText(47);
            } else {
               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               text = misEJB.getInfoText(47).replaceAll("%n", String.valueOf(outstandingContactRequests));
            }

            String url = URLUtil.replaceViewTypeToken(SystemProperty.get("UserNotificationURL"), this.getDeviceType());
            this.sendFusionPacket(new FusionPktNotification(text, url));
         }

      } catch (CreateException var4) {
         throw new FusionException("Failed to create MIS EJB - " + var4.getMessage());
      } catch (RemoteException var5) {
         throw new FusionException(RMIExceptionHelper.getRootMessage(var5));
      } catch (NoSuchFieldException var6) {
         throw new FusionException(var6.getMessage());
      } catch (DAOException var7) {
         log.error("DAO: Failed to get info text for infoID:47");
         throw new FusionException(var7.getMessage());
      }
   }

   public void contactAdded(ContactDataIce contact, int contactListVersion, boolean guaranteedIsNew, Current __current) throws FusionException {
      ContactData contactData = new ContactData(contact);
      if (guaranteedIsNew || this.deviceType != ClientType.MIDP1 && this.deviceType != ClientType.MIDP2 || contactData.isOtherIMOnly()) {
         if (contactData.contactGroupId == null && !this.isMidletVersionAndAbove(400)) {
            contactData.contactGroupId = -1;
            if (!this.isDefaultGroupPktSent()) {
               FusionPktGroup pktGroup = new FusionPktGroup((short)-1, -1, "migme");
               this.sendFusionPacket(pktGroup);
               this.setDefaultGroupPktSent(true);
            }
         }

         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
            FusionPktContact pktContact = new FusionPktContact(contactData, this);
            this.sendFusionPacket(pktContact);
         } else {
            FusionPktContactOld pktContact = new FusionPktContactOld((short)-1, contactData, this);
            this.sendFusionPacket(pktContact);
         }
      }

      this.sendContactListVersionPacket(contactListVersion);
   }

   public boolean isMidletVersionBelow(int version) {
      return isMidletVersionBelow(this.deviceType, this.clientVersion, version);
   }

   public static boolean isMidletVersionBelow(ClientType deviceType, int currentClientVersion, int maxExpectedVersion) {
      if (null == deviceType) {
         return true;
      } else {
         switch(deviceType) {
         case MIDP1:
         case MIDP2:
            return currentClientVersion < maxExpectedVersion;
         default:
            return false;
         }
      }
   }

   public boolean isMidletVersionAndAbove(int version) {
      return isMidletVersionAndAbove(this.deviceType, this.clientVersion, version);
   }

   public static boolean isMidletVersionAndAbove(ClientType deviceType, int currentClientVersion, int expectedMinVersion) {
      if (null == deviceType) {
         return false;
      } else {
         switch(deviceType) {
         case MIDP1:
         case MIDP2:
         case WINDOWS_MOBILE:
            return currentClientVersion >= expectedMinVersion;
         case ANDROID:
         case BLACKBERRY:
         case MRE:
         case IOS:
            return true;
         case AJAX1:
         case AJAX2:
            if ((Boolean)SystemPropertyEntities.Temp.Cache.se456Packet208ChangesEnabled.getValue()) {
               return false;
            }

            return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.AJAX_CONSIDERED_ABOVE_MIDLET_VERSION_CHECKS);
         default:
            return false;
         }
      }
   }

   public boolean isForgotPasswordNative() {
      return this.deviceType == ClientType.MIDP1 || this.deviceType == ClientType.MIDP2 || this.deviceType == ClientType.ANDROID;
   }

   public boolean isAjax() {
      return ClientType.isAjax(this.deviceType);
   }

   public boolean isBrowserDevice() {
      return checkBrowserDevice(this.deviceType);
   }

   public boolean isMobileClientV2() {
      return ClientType.isMobileClientV2(this.deviceType);
   }

   public boolean isMobileClientV2AndNewVersion() {
      return ClientType.isMobileClientV2AndNewVersion(this.deviceType, this.clientVersion);
   }

   public boolean isMobileClientV2AndNewVersionOrAjax() {
      return ClientType.isMobileClientV2AndNewVersionOrAjax(this.deviceType, this.clientVersion);
   }

   public boolean isUnknownDevice() {
      return this.deviceType == null;
   }

   public boolean isDefaultGroupPktSent() {
      return this.defaultGroupPktSent;
   }

   public void setDefaultGroupPktSent(boolean defaultGroupPktSent) {
      this.defaultGroupPktSent = defaultGroupPktSent;
   }

   public void contactRemoved(int contactID, int contactListVersion, Current __current) throws FusionException {
      if (this.deviceType != ClientType.MIDP1 && this.deviceType != ClientType.MIDP2 || this.clientVersion >= 400) {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week3UseAutogeneratedPacketsEnabled.getValue()) {
            FusionPktRemoveContact pkt = new FusionPktRemoveContact();
            pkt.setContactId(contactID);
            this.sendFusionPacket(pkt);
         } else {
            FusionPktRemoveContactOld pkt = new FusionPktRemoveContactOld((short)-1);
            pkt.setContactID(contactID);
            this.sendFusionPacket(pkt);
         }
      }

      this.sendContactListVersionPacket(contactListVersion);
   }

   public void contactGroupAdded(ContactGroupDataIce contactGroup, int contactListVersion, Current __current) throws FusionException {
      if (this.deviceType != ClientType.MIDP1 && this.deviceType != ClientType.MIDP2 || contactGroup.name.equals("MSN") || contactGroup.name.equals("Yahoo!") || contactGroup.name.equals("AIM") || contactGroup.name.equals("GTalk") || contactGroup.name.equals("Facebook")) {
         FusionPktGroup pkt = new FusionPktGroup((short)-1, new ContactGroupData(contactGroup));
         this.sendFusionPacket(pkt);
      }

      this.sendContactListVersionPacket(contactListVersion);
   }

   public void contactGroupRemoved(int contactGroupID, int contactListVersion, Current __current) throws FusionException {
      if (this.deviceType != ClientType.MIDP1 && this.deviceType != ClientType.MIDP2) {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week3UseAutogeneratedPacketsEnabled.getValue()) {
            FusionPktRemoveGroup pkt = new FusionPktRemoveGroup();
            pkt.setGroupId(contactGroupID);
            this.sendFusionPacket(pkt);
         } else {
            FusionPktRemoveGroupOld pkt = new FusionPktRemoveGroupOld((short)-1);
            pkt.setGroupID(contactGroupID);
            this.sendFusionPacket(pkt);
         }
      }

      this.sendContactListVersionPacket(contactListVersion);
   }

   private void sendContactListVersionPacket(int contactListVersion) throws FusionException {
      if (this.lastContactListVersionSent.getAndSet(contactListVersion) != contactListVersion) {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
            FusionPktContactListVersion version = new FusionPktContactListVersion();
            version.setVersion(contactListVersion);
            version.setTimestamp(System.currentTimeMillis());
            this.sendFusionPacket(version);
         } else {
            FusionPktContactListVersionOld version = new FusionPktContactListVersionOld((short)-1);
            version.setContactListVersion(contactListVersion);
            version.setStatusTimeStamp(System.currentTimeMillis());
            this.sendFusionPacket(version);
         }
      }

   }

   public void otherIMLoggedIn(int imType, Current __current) throws FusionException {
      ImType imEnum = ImType.fromValue(imType);
      if (imEnum == null) {
         throw new FusionException("Unknown IM type " + imType);
      } else {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week3UseAutogeneratedPacketsEnabled.getValue()) {
            this.sendFusionPacket(new FusionPktImSessionStatus(imEnum, FusionPktDataImSessionStatus.StatusType.LOGGED_IN, (String)null));
         } else {
            this.sendFusionPacket(new FusionPktIMSessionStatusOld(imEnum, (byte)1, (String)null));
         }

      }
   }

   public void otherIMLoggedOut(int imType, String reason, Current __current) throws FusionException {
      ImType imEnum = ImType.fromValue(imType);
      if (imEnum == null) {
         throw new FusionException("Unknown IM type " + imType);
      } else {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week3UseAutogeneratedPacketsEnabled.getValue()) {
            this.sendFusionPacket(new FusionPktImSessionStatus(imEnum, FusionPktDataImSessionStatus.StatusType.LOGGED_OUT, "You are no longer connected to " + imEnum.toString() + ". " + reason));
         } else {
            this.sendFusionPacket(new FusionPktIMSessionStatusOld(imEnum, (byte)2, "You are no longer connected to " + imEnum.toString() + ". " + reason));
         }

      }
   }

   public void otherIMConferenceCreated(int imType, String conferenceID, String creator, Current current) throws FusionException {
      FusionPktGroupChat pkt = new FusionPktGroupChat();
      pkt.setGroupChatId(conferenceID);
      pkt.setCreator(creator);
      pkt.setIMType((byte)imType);
      this.sendFusionPacket(pkt);
   }

   public void privateChatNowAGroupChat(String groupChatID, String creator, Current __current) throws FusionException {
      FusionPktGroupChat pkt = new FusionPktGroupChat();
      pkt.setGroupChatId(groupChatID);
      pkt.setCreator(creator);
      pkt.setIMType(ImType.FUSION.value());
      this.sendFusionPacket(pkt);
   }

   public void putEvent(UserEventIce event, Current __current) throws FusionException {
      if (this.isMidletVersionAndAbove(400) || this.isBrowserDevice() || this.isMobileClientV2()) {
         FusionPktUserEvent pkt = new FusionPktUserEvent(Gateway.getTranslator(), this.deviceType, this.username, event);
         if (log.isDebugEnabled()) {
            log.debug("sending event packet to user [" + this.username + "] with event text [" + pkt.getStringField((short)4) + "]");
         }

         this.sendFusionPacket(pkt);
      }

   }

   public void putAlertMessage(String message, String title, short timeout, Current __current) throws FusionException {
      FusionPktAlert pkt = new FusionPktAlert();
      pkt.setAlertType(FusionPktDataAlert.AlertType.INFORMATION);
      pkt.setContentType(AlertContentType.TEXT);
      if (title != null && title.length() > 0) {
         pkt.setTitle(title);
      }

      pkt.setContent(message);
      if (timeout > 0) {
         pkt.setTimeout(timeout);
      }

      this.sendFusionPacket(pkt);
   }

   public void putAlertMessageOneWay(String message, String title, short timeout, Current __current) {
      try {
         this.putAlertMessage(message, title, timeout);
      } catch (Exception var6) {
      }

   }

   public void putServerQuestion(String message, String url, Current __current) throws FusionException {
      if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
         FusionPktServerQuestion pkt = new FusionPktServerQuestion();
         pkt.setQuestionType(FusionPktDataServerQuestion.QuestionType.YES_NO);
         pkt.setTitle("Question");
         pkt.setQuestion(message);
         pkt.setUrl(URLUtil.replaceViewTypeToken(url, this.getDeviceType()));
         this.setServerQuestion(pkt);
         this.sendFusionPacket(pkt);
      } else {
         FusionPktServerQuestionOld pkt = new FusionPktServerQuestionOld();
         pkt.setQuestionType((byte)1);
         pkt.setTitle("Question");
         pkt.setQuestion(message);
         pkt.setURL(URLUtil.replaceViewTypeToken(url, this.getDeviceType()));
         this.setServerQuestionOld(pkt);
         this.sendFusionPacket(pkt);
      }

   }

   public void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol, Current __current) throws FusionException {
      this.pendingWebCallRequests.add(source);
      FusionPktWebCallNotification pkt = new FusionPktWebCallNotification();
      pkt.setSource(source);
      pkt.setSourceProtocol((byte)protocol);
      pkt.setDestination(destination);
      pkt.setGateway(gateway);
      pkt.setGatewayName(gatewayName);
      this.sendFusionPacket(pkt);
   }

   public void putAnonymousCallNotification(String requestingUsername, String requestingMobilePhone, Current __current) throws FusionException {
      try {
         if (!this.isMidletVersionAndAbove(405)) {
            throw new FusionException("Anonymous calling not supported");
         } else {
            this.pendingAnonymousCallRequests.put(requestingUsername, requestingMobilePhone);
            String description = null;
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_MESSAGE_DAO)) {
               description = DAOFactory.getInstance().getMessageDAO().getInfoText(44);
            } else {
               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               description = misEJB.getInfoText(44);
            }

            if (description == null) {
               description = "%s is trying to call you now.";
            }

            FusionPktAnonymousCallNotification pkt = new FusionPktAnonymousCallNotification();
            pkt.setRequestingUsername(requestingUsername);
            pkt.setDescription(description.replaceAll("%s", requestingUsername));
            this.sendFusionPacket(pkt);
         }
      } catch (CreateException var6) {
         throw new FusionException("Failed to create MIS EJB - " + var6.getMessage());
      } catch (RemoteException var7) {
         throw new FusionException(RMIExceptionHelper.getRootMessage(var7));
      } catch (DAOException var8) {
         throw new FusionException(var8.getMessage());
      }
   }

   public void putFileReceived(MessageDataIce messageIce, Current __current) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.MIME_TYPE_MESSAGES_ENABLED) && !StringUtil.iceIsBlank(messageIce.mimeType)) {
         FusionPktMessage msg = new FusionPktMessage(new MessageData(messageIce));
         this.sendFusionPacket(msg);
      } else {
         int fileSize = messageIce.binaryData.length;
         String size;
         if (fileSize < 1024) {
            size = fileSize + " bytes";
         } else {
            size = (new DecimalFormat("0.0 KB")).format((double)fileSize / 1024.0D);
         }

         MessageData.ContentTypeEnum contentType = MessageData.ContentTypeEnum.fromValue(messageIce.contentType);
         if (contentType == null) {
            throw new FusionException("Unsupported file type " + messageIce.contentType);
         }

         String message;
         switch(contentType) {
         case IMAGE:
            message = messageIce.source + " has sent you a picture (" + size + "). View now?";
            break;
         case AUDIO:
            message = messageIce.source + " has sent you a sound clip (" + size + "). Open now?";
            break;
         case VIDEO:
            message = messageIce.source + " has sent you a video (" + size + "). Watch now?";
            break;
         default:
            throw new FusionException("Unsupported file type " + messageIce.contentType);
         }

         if ((this.deviceType.value() != ClientType.ANDROID.value() || this.clientVersion < 300) && this.deviceType.value() != ClientType.BLACKBERRY.value() && this.deviceType.value() != ClientType.MRE.value() && this.deviceType.value() != ClientType.IOS.value()) {
            String viewImageURL = MessageFormat.format(SystemProperty.get("ViewImageURL", "http://mig.me/sites/index.php?c=photos&v=%1&a=received&sender={0}&nid={1}"), messageIce.source, messageIce.messageText);
            FusionPktFileReceived pkt = new FusionPktFileReceived();
            pkt.setSourceType((byte)1);
            pkt.setSource(messageIce.source);
            pkt.setURL(URLUtil.replaceViewTypeToken(viewImageURL, this.deviceType));
            pkt.setInfoMessage(message);
            this.sendFusionPacket(pkt);
         } else {
            FusionPktMessage msg = new FusionPktMessage(new MessageData(messageIce));
            msg.setContentAsString(message);
            msg.setFileName(messageIce.messageText);
            this.sendFusionPacket(msg);
         }
      }

   }

   public void emailNotification(int unreadEmailCount, Current __current) throws FusionException {
      FusionPktMailInfo pkt = new FusionPktMailInfo(unreadEmailCount);
      this.sendFusionPacket(pkt);
   }

   public void emoticonsChanged(String[] hotKeys, String[] alternateKeys, Current __current) throws FusionException {
      if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week3UseAutogeneratedPacketsEnabled.getValue()) {
         FusionPktEmoticonHotkeys pkt = new FusionPktEmoticonHotkeys();
         pkt.setHotkeyList(hotKeys);
         pkt.setAlternateHotkeyList(alternateKeys);
         this.sendFusionPacket(pkt);
      } else {
         FusionPktEmoticonHotKeysOld pkt = new FusionPktEmoticonHotKeysOld();
         pkt.setHotKeys(StringUtil.join((Object[])hotKeys, " "));
         pkt.setAlternateKeys(StringUtil.join((Object[])alternateKeys, " "));
         this.sendFusionPacket(pkt);
      }

   }

   public void themeChanged(String themeLocation, Current __current) throws FusionException {
      try {
         if (themeLocation != null && themeLocation.length() != 0) {
            ByteBuffer themePacket = ByteBufferHelper.readFile(new File(themeLocation));
            this.putGenericPacket(themePacket.array());
         } else if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
            FusionPktMidletAction pkt = new FusionPktMidletAction();
            pkt.setAction(FusionPktDataMidletAction.ActionType.USE_DEFAULT_THEME);
            this.sendFusionPacket(pkt);
         } else {
            FusionPktMidletActionOld pkt = new FusionPktMidletActionOld();
            pkt.setAction(2);
            this.sendFusionPacket(pkt);
         }

      } catch (Exception var4) {
         throw new FusionException(var4.getMessage());
      }
   }

   public void accountBalanceChanged(double balance, double fundedBalance, CurrencyDataIce currency, Current __current) throws FusionException {
      FusionPktAccountBalance pkt = new FusionPktAccountBalance();
      pkt.setAccountBalance((new CurrencyData(currency)).format(balance));
      this.sendFusionPacket(pkt);
   }

   public void avatarChanged(String displayPicture, String statusMessage, Current __current) throws FusionException {
      FusionPktAvatar pkt = new FusionPktAvatar();
      if (displayPicture != null) {
         pkt.setDisplayPictureGuid(displayPicture);
      }

      if (statusMessage != null) {
         pkt.setStatusMessage(statusMessage);
      }

      this.sendFusionPacket(pkt);
   }

   public void putGenericPacket(byte[] packet, Current __current) throws FusionException {
      try {
         this.sendFusionPacket(FusionPacket.parse(packet)[0]);
      } catch (IOException var4) {
         throw new FusionException(var4.getMessage());
      }
   }

   public void pushNotification(Message message, Current __current) throws FusionException {
      if (message == null) {
         throw new FusionException("Message cannot be null");
      } else if (message.parameters == null) {
         throw new FusionException("Message parameter map is null");
      } else {
         if (this.isMidletVersionAndAbove(410)) {
            FusionPktNotification pkt = new FusionPktNotification();
            pkt.setNotificationType(message.notificationType);
            Map<String, String> parameters = message.parameters;
            pkt.setPendingNotificationsTotal(Integer.parseInt((String)parameters.get("totalPending")));
            pkt.setText((String)parameters.get("message"));
            pkt.setURL(URLUtil.replaceViewTypeToken((String)parameters.get("url"), this.deviceType));
            this.sendFusionPacket(pkt);
            log.debug("Pushing packet[14] notification[" + message.notificationType + "] to user:" + message.toUsername);
         } else {
            if (Enums.NotificationTypeEnum.GROUP_INVITE.getType() == message.notificationType) {
               log.debug("Pushing AlertPacket for notification[" + message.notificationType + "] to user:" + message.toUsername);
               this.putAlertMessage((String)message.parameters.get("alertMessage"), (String)null, (short)0);
               return;
            }

            if (Enums.NotificationTypeEnum.VIRTUALGIFT_ALERT.getType() == message.notificationType) {
               log.debug("Pushing ServerQuestionPacket for notification[" + message.notificationType + "] to user:" + message.toUsername);
               return;
            }

            if (Enums.NotificationTypeEnum.FRIEND_INVITE.getType() == message.notificationType) {
            }
         }

      }
   }

   public void setDeviceMode(DeviceModeType mode) {
      this.deviceMode = mode;
      this.onDeviceModeChanged(mode);
   }

   public boolean supportsPageletTab() {
      return this.isMidletVersionAndAbove(400);
   }

   protected boolean isDeviceAwake() {
      return this.deviceMode == DeviceModeType.AWAKE;
   }

   public abstract Gateway.ServerType getServerType();

   public abstract void onChannelReadable();

   public abstract void onChannelWritable();

   public abstract void sendFusionPacket(FusionPacket var1) throws FusionException;

   protected abstract void onDeviceModeChanged(DeviceModeType var1);

   public void putSerializedPacket(byte[] packet, Current __current) throws FusionException {
      FusionPacket pkt = null;
      ObjectInputStream o = null;

      try {
         ByteArrayInputStream b = new ByteArrayInputStream(packet);
         o = new ObjectInputStream(b);
         pkt = (FusionPacket)o.readObject();
      } catch (Exception var13) {
         log.error(var13);
         throw new FusionException(var13.getMessage());
      } finally {
         try {
            o.close();
         } catch (IOException var12) {
            log.error("While trying to close " + o + " during cleanup", var12);
         }

      }

      this.sendFusionPacket(pkt);
   }

   public void putSerializedPacketOneWay(byte[] packet, Current __current) {
      try {
         this.putSerializedPacket(packet);
      } catch (Exception var4) {
         log.error(var4);
      }

   }

   public void putMessageStatusEvent(MessageStatusEventIce mseIce, Current __current) throws FusionException {
      MessageStatusEvent mse = new MessageStatusEvent(mseIce);
      FusionPktMessageStatusEvent pkt = new FusionPktMessageStatusEvent(mse);
      this.sendFusionPacket(pkt);
   }

   public void putMessageStatusEvents(MessageStatusEventIce[] eventsIce, short requestTxnId, Current __current) throws FusionException {
      List<MessageStatusEvent> events = new ArrayList();
      MessageStatusEventIce[] arr$ = eventsIce;
      int len$ = eventsIce.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MessageStatusEventIce msei = arr$[i$];
         MessageStatusEvent mse = new MessageStatusEvent(msei);
         events.add(mse);
      }

      MessageStatusEvent[] arr = (MessageStatusEvent[])events.toArray(new MessageStatusEvent[events.size()]);
      FusionPktMessageStatusEvents eventsPkt = new FusionPktMessageStatusEvents(arr, requestTxnId);
      this.sendFusionPacket(eventsPkt);
   }

   protected String getObjectIdWithinGateway() {
      return this.sessionID;
   }

   static {
      BROWSER_DEVICES = new ClientType[]{ClientType.AJAX1, ClientType.AJAX2, ClientType.MIGBO, ClientType.VAS, ClientType.MERCHANT_CENTER, ClientType.BLAAST};
   }
}
