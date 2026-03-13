package com.projectgoth.fusion.common;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.Date;
import org.apache.log4j.Logger;

public class FloodControl {
   private static final String FLOOD_CONTROL_NAMESPACE = "FC";
   private static MemCachedClient rateLimitMemcached;
   private static Logger log;

   public static void detectFlooding(String username, UserPrx userPrx, FloodControl.Action[] actions) throws Exception {
      FloodControl.Action[] arr$ = actions;
      int len$ = actions.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         FloodControl.Action action = arr$[i$];
         detectFlooding(username, userPrx, action);
      }

   }

   public static void detectFlooding(String username, UserPrx userPrx, FloodControl.Action action) throws Exception {
      String key = MemCachedUtils.getCacheKeyInNamespace("FC", username + "/" + action);
      Date expiry = new Date(System.currentTimeMillis() + action.duration);
      if (!rateLimitMemcached.add(key, 1, expiry)) {
         if (rateLimitMemcached.incr(key) > action.maxHits) {
            if (userPrx != null) {
               userPrx.disconnectFlooder("Flooding. Action: " + action);
            }

            throw new Exception("You have been disconnected");
         }
      }
   }

   public static String getRateLimitKeyByUsername(FusionRequest packet, String username) {
      return MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.GENERIC_FUSION_REQUEST_RATE_LIMIT, String.format("%s:%s:%s", packet.getSimpleName(), "U", username));
   }

   public static void detectFloodingOfFusionRequestPacketByUser(String username, UserPrx userPrx, boolean isChatRoomAdmin, FusionRequest packet, boolean disconnect) throws Exception {
      String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.GatewayRateLimit(packet, isChatRoomAdmin)));

      try {
         MemCachedRateLimiter.hit("FC", getRateLimitKeyByUsername(packet, username), rateLimit);
      } catch (MemCachedRateLimiter.LimitExceeded var7) {
         if (userPrx != null && disconnect) {
            userPrx.disconnectFlooder(String.format("Flooding. Action: %s U:%s", packet.getSimpleName(), username));
            throw new Exception("You have been disconnected.");
         } else {
            throw new Exception("Please try again later.");
         }
      } catch (MemCachedRateLimiter.FormatError var8) {
         throw new Exception("Internal Error");
      }
   }

   public static String getRateLimitKeyByChatRoom(FusionRequest packet, String chatRoomName) {
      return MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.GENERIC_FUSION_REQUEST_RATE_LIMIT, String.format("%s:%s:%s", packet.getSimpleName(), "C", chatRoomName));
   }

   public static String getRateLimitKeyByChatRoomAndRemoteAddress(FusionRequest packet, String chatRoomName, String remoteAddress) {
      return MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.GENERIC_FUSION_REQUEST_RATE_LIMIT, String.format("%s:%s:%s:%s", packet.getSimpleName(), "C", chatRoomName, remoteAddress));
   }

   public static void detectFloodingOfFusionRequestPacketByChatroom(String chatRoomName, String remoteAddress, UserPrx userPrx, boolean isChatRoomAdmin, FusionRequest packet, boolean disconnect) throws Exception {
      if (!StringUtil.isBlank(chatRoomName)) {
         chatRoomName = chatRoomName.toLowerCase();
         String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.GatewayRateLimitByChatRoom(packet, isChatRoomAdmin)));
         Boolean useRemoteAddressInRateLimit = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.RATELIMIT_BY_CHATROOM_AND_REMOTEADDRESS);
         if (useRemoteAddressInRateLimit && SystemProperty.isValueInArray(remoteAddress, SystemPropertyEntities.GatewaySettings.RATELIMIT_BY_REMOTEADDRESS_WHITELIST)) {
            log.debug("Skipping ratelimit check on ip " + remoteAddress + " due to whitelisting");
         } else {
            try {
               String rateLimitKey = useRemoteAddressInRateLimit ? getRateLimitKeyByChatRoomAndRemoteAddress(packet, chatRoomName, remoteAddress) : getRateLimitKeyByChatRoom(packet, chatRoomName);
               MemCachedRateLimiter.hit("FC", rateLimitKey, rateLimit);
            } catch (MemCachedRateLimiter.LimitExceeded var9) {
               if (userPrx != null && disconnect) {
                  userPrx.disconnectFlooder(String.format("Flooding. Action: %s C:%s", packet.getSimpleName(), chatRoomName));
                  throw new Exception("You have been disconnected.");
               } else {
                  throw new Exception("Please try again later.");
               }
            } catch (MemCachedRateLimiter.FormatError var10) {
               throw new Exception("Internal Error");
            }
         }
      }
   }

   static {
      rateLimitMemcached = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.rateLimit);
      log = Logger.getLogger(ConfigUtils.getLoggerName(FloodControl.class));
   }

   public static enum Action {
      FILE_UPLOAD_PER_MINUTE(10L, 60000L),
      FILE_UPLOAD_DAILY(1000L, 86400000L),
      INVITE_FRIEND_DAILY(100L, 86400000L),
      INVITE_FRIEND_PER_MINUTE(10L, 60000L),
      SEND_EMAIL(10L, 60000L),
      SET_STATUS(10L, 300000L),
      PHONE_CALL(3L, 1000L),
      DEFAULT_DAILY(200L, 86400000L),
      DEFAULT_PER_MINUTE(10L, 60000L);

      private long maxHits;
      private long duration;

      private Action(long maxHit, long duration) {
         this.maxHits = maxHit;
         this.duration = duration;
      }

      public FloodControl.Action setMaxHits(long maxHits) {
         this.maxHits = maxHits;
         return this;
      }
   }
}
