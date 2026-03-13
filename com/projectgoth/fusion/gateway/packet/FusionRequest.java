package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.Captcha;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.FloodControl;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.URLUtil;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.CaptchaFallbackPagelet;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.Gateway;
import com.projectgoth.fusion.gateway.exceptions.FusionRequestException;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ConnectionPrx;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public abstract class FusionRequest extends FusionPacket {
   public static final String VOICE_AUDIT = "VoiceAudit";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionRequest.class));
   private ConnectionPrx connectionPrx;
   private ConnectionI connection;
   private long responseDelay;
   private boolean skipCaptchaCheck;
   private static short[] NON_ACTIVE_SESSION_PACKETS = new short[]{202, 17, 211};
   private static final LazyLoader<short[]> REQUEST_TYPES_TO_RATE_LIMIT = new LazyLoader<short[]>("RequestTypesToRateLimit", 60000L) {
      protected short[] fetchValue() {
         return SystemProperty.getShortArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.REQUESTS_TO_RATE_LIMIT);
      }
   };

   private static boolean shortArrayContains(short[] values, short value) {
      short[] arr$ = values;
      int len$ = values.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         short v = arr$[i$];
         if (v == value) {
            return true;
         }
      }

      return false;
   }

   public FusionRequest() {
   }

   public FusionRequest(PacketType type) {
      super(type);
   }

   public FusionRequest(PacketType type, short transactionId) {
      super(type, transactionId);
   }

   public FusionRequest(short type) {
      super(type);
   }

   public FusionRequest(short type, short transactionId) {
      super(type, transactionId);
   }

   public FusionRequest(FusionPacket packet) {
      super(packet);
   }

   protected FusionRequest(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public String getSimpleName() {
      return this.getClass().getSimpleName().substring(9);
   }

   public ConnectionI getConnection() {
      return this.connection;
   }

   public void setConnection(ConnectionI connection) {
      this.connection = connection;
   }

   public ConnectionPrx getConnectionPrx() {
      return this.connectionPrx;
   }

   public void setConnectionPrx(ConnectionPrx connectionPrx) {
      this.connectionPrx = connectionPrx;
   }

   public long getResponseDelay() {
      return this.responseDelay;
   }

   public void setResponseDelay(long responseDelay) {
      this.responseDelay = responseDelay;
   }

   public boolean skipCaptchaCheck() {
      return this.skipCaptchaCheck;
   }

   public void setSkipCaptchaCheck(boolean skipCaptchaCheck) {
      this.skipCaptchaCheck = skipCaptchaCheck;
   }

   public static FusionRequest parseRequest(byte[] byteStream) throws IOException {
      FusionPacket[] packets = FusionPacket.parse(byteStream);
      return packets != null && packets.length == 1 ? FusionPacketFactory.getSpecificRequest(packets[0]) : null;
   }

   public static FusionRequest parseXMLRequest(String xml) throws IOException, SAXException, ParserConfigurationException {
      FusionPacket[] packets = FusionPacket.parseXML(xml);
      return packets != null && packets.length == 1 ? FusionPacketFactory.getSpecificRequest(packets[0]) : null;
   }

   public static FusionRequest parseJSONRequest(String json, int version) throws Exception {
      FusionPacket packet = FusionPacket.parseJSON(json, version);
      return FusionPacketFactory.getSpecificRequest(packet);
   }

   public static FusionRequest[] parseJSONArrayRequest(String json, int version) throws Exception {
      FusionPacket[] packets = parseJSONArrayNonSpecific(json, version);
      if (packets == null) {
         return null;
      } else {
         FusionRequest[] result = new FusionRequest[packets.length];

         for(int i = 0; i < packets.length; ++i) {
            result[i] = FusionPacketFactory.getSpecificRequest(packets[i]);
         }

         return result;
      }
   }

   public FusionPacket[] process(ConnectionI connection) throws FusionRequestException {
      if (this.sessionRequired() && !connection.isLoggedIn() && !shortArrayContains(NON_ACTIVE_SESSION_PACKETS, this.type)) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "User not logged in")).toArray();
      } else if (this.isDeprecated(connection)) {
         if (connection.supportsPageletTab()) {
            String fallbackUrl = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.DeprecatedPacketFallbackURL(this)));
            return new FusionPacket[]{new FusionPktOk(this.transactionId, this.getPacketUnsupportedMessage()), new FusionPktMidletTab(this.transactionId, URLUtil.replaceViewTypeToken(fallbackUrl, connection.getDeviceType()), (byte)1)};
         } else {
            return this.buildUnsupportedResponsePacket(this.transactionId).toArray();
         }
      } else {
         this.preValidate(connection);
         Boolean captchaCheckResponse = this.isCaptchaRequired(connection);
         if (null != captchaCheckResponse && (captchaCheckResponse || this.isIPBlackListedForCaptcha(connection)) && !this.skipCaptchaCheck()) {
            log.warn("captcha required for packet [" + this.getSimpleName() + "] username [" + connection.getUsername() + "] ip [" + connection.getRemoteAddress() + "] client[" + connection.getClientVersion() + "] blackListed[" + this.isIPBlackListedForCaptcha(connection) + "]");
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.GENERIC_NATIVECAPTCHA_ENABLED) && connection.supportsCaptcha()) {
               Captcha captcha = connection.createCaptcha(this, 4);

               try {
                  return (Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue() ? (new FusionPktCaptcha(this.transactionId, "Please enter the letters as shown in the image below.", captcha, connection)).toArray() : (new FusionPktCaptchaOld(this.transactionId, "Please enter the letters as shown in the image below.", captcha, connection)).toArray();
               } catch (IOException var12) {
                  return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Internal error")).toArray();
               }
            } else if (connection.supportsPageletTab() && null != this.getCaptchaFallbackPagelet()) {
               CaptchaFallbackPagelet fallback = this.getCaptchaFallbackPagelet();
               return fallback.isSendOkPacket() && !StringUtil.isBlank(fallback.getMessage()) ? new FusionPacket[]{new FusionPktOk(this.transactionId, fallback.getMessage()), new FusionPktMidletTab(this.transactionId, URLUtil.replaceViewTypeToken(fallback.getUrl(), connection.getDeviceType()), (byte)1)} : new FusionPacket[]{new FusionPktMidletTab(this.transactionId, URLUtil.replaceViewTypeToken(fallback.getUrl(), connection.getDeviceType()), (byte)1)};
            } else {
               return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, this.getPacketUnsupportedMessage())).toArray();
            }
         } else {
            boolean isRateLimitedWithSuspensionByProperty = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.RATELIMITED_SUSPENDABLE_FUSION_PACKETS)).contains(this.getSimpleName());
            boolean isRateLimitedByProperty = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.RATELIMITED_FUSION_PACKETS)).contains(this.getSimpleName());
            int maxMigLevelForFusionPacketRateLimit = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.MAX_MIGLEVEL_FOR_FUSION_PACKETS_RATELIMIT);
            if ((isRateLimitedByProperty || isRateLimitedWithSuspensionByProperty) && !StringUtil.isBlank(connection.getUsername()) && connection.getMigLevel() <= maxMigLevelForFusionPacketRateLimit) {
               try {
                  FloodControl.detectFloodingOfFusionRequestPacketByUser(connection.getUsername(), connection.getUserPrx(), connection.isChatRoomAdmin(), this, isRateLimitedWithSuspensionByProperty);
               } catch (Exception var14) {
                  String packetName = this.getSimpleName();
                  String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.GatewayRateLimit(this)));
                  log.warn("Rate limit exceeded packet[" + packetName + "] username[" + connection.getUsername() + "] suspended[" + isRateLimitedWithSuspensionByProperty + "] rateLimit[" + rateLimit + "]");
                  return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var14.getMessage())).toArray();
               }
            }

            boolean isRateLimitedByChatroomWithSuspensionByProperty = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.RATELIMITED_SUSPENDABLE_FUSION_PACKETS_BY_CHATROOM)).contains(this.getSimpleName());
            boolean isRateLimitedByChatroomByProperty = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.RATELIMITED_FUSION_PACKETS_BY_CHATROOM)).contains(this.getSimpleName());
            int maxMigLevelForFusionPacketRateLimitByChatroom = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.MAX_MIGLEVEL_FOR_FUSION_PACKETS_RATELIMIT_BY_CHATROOM);
            if ((isRateLimitedByChatroomByProperty || isRateLimitedByChatroomWithSuspensionByProperty) && connection.getMigLevel() <= maxMigLevelForFusionPacketRateLimitByChatroom) {
               try {
                  FloodControl.detectFloodingOfFusionRequestPacketByChatroom(this.getChatRoomNameForRateLimit(), connection.getRemoteAddress(), connection.getUserPrx(), connection.isChatRoomAdmin(), this, isRateLimitedByChatroomWithSuspensionByProperty);
               } catch (Exception var13) {
                  String packetName = this.getSimpleName();
                  String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.GatewayRateLimitByChatRoom(this)));
                  log.warn("Rate limit by chatroom exceeded packet[" + packetName + "] chatroomName[" + this.getChatRoomNameForRateLimit() + "] userName[" + connection.getUsername() + "] ip[" + connection.getRemoteAddress() + "]suspended[" + isRateLimitedByChatroomWithSuspensionByProperty + "] rateLimit[" + rateLimit + "]");
                  return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var13.getMessage())).toArray();
               }
            }

            return this.processRequest(connection);
         }
      }
   }

   protected boolean isDeprecated(ConnectionI connection) {
      return SystemProperty.isValueInArray(this.getSimpleName(), SystemPropertyEntities.GatewaySettings.DEPRECATED_PACKETS);
   }

   private boolean isIPBlackListedForCaptcha(ConnectionI connection) {
      return SystemProperty.isValueInArray(connection.getRemoteAddress(), SystemPropertyEntities.GatewaySettings.CAPTCHA_IP_BLACKLIST);
   }

   public Boolean isCaptchaRequired(ConnectionI connection) {
      return null;
   }

   public CaptchaFallbackPagelet getCaptchaFallbackPagelet() {
      return null;
   }

   public String getPacketUnsupportedMessage() {
      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.PACKET_DISABLED_MESSAGE);
   }

   public FusionPacket buildUnsupportedResponsePacket(short transactionId) {
      return new FusionPktError(transactionId, FusionPktError.Code.UNDEFINED, this.getPacketUnsupportedMessage());
   }

   protected void preValidate(ConnectionI connection) throws FusionRequestException {
   }

   public Gateway.ThreadPoolName getThreadPool() {
      return Gateway.ThreadPoolName.PRIMARY;
   }

   public FusionPacket getErrorPacket(String errorMessage) {
      return new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, errorMessage);
   }

   public final boolean throttlingRequired() {
      return shortArrayContains((short[])REQUEST_TYPES_TO_RATE_LIMIT.getValue(), this.type);
   }

   public String getChatRoomNameForRateLimit() {
      return null;
   }

   public abstract boolean sessionRequired();

   protected abstract FusionPacket[] processRequest(ConnectionI var1);
}
