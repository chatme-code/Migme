package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ByteBufferHelper;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ContentUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.recommendation.collector.DataCollectorUtils;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class FusionPktGetEmoticonPreSE378 extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetEmoticon.class));
   private static final Short SMALLEST_VERSION_NUMBER = -32768;
   private static final String HOTKEY_DELIMITER = " ";
   public static final String ERROR_MSG_HOT_KEYS_NOT_SPECIFIED = "Unable to get emoticon - Hot keys not specified";
   public static final String ERROR_MSG_OPTIMAL_EMOTICON_HEIGHT_NOT_SET = "Unable to get emoticon - Optimal emoticon height not set";
   public static final String ERROR_MSG_ERROR_LOADING_HOTKEY = "Error loading hotkey";

   public FusionPktGetEmoticonPreSE378() {
      super((short)913);
   }

   public FusionPktGetEmoticonPreSE378(short transactionId) {
      super((short)913, transactionId);
   }

   public FusionPktGetEmoticonPreSE378(FusionPacket packet) {
      super(packet);
   }

   public String getHotKeys() {
      return this.getStringField((short)1);
   }

   public void setHotKeys(String hotKeys) {
      this.setField((short)1, hotKeys);
   }

   public boolean sessionRequired() {
      return true;
   }

   private static int coalesce(int... vals) {
      int[] arr$ = vals;
      int len$ = vals.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         int v = arr$[i$];
         if (v > 0) {
            return v;
         }
      }

      return vals[vals.length - 1];
   }

   private static short getClosestSupportedVgSize(short requestedVgSize, boolean usePNG) {
      String imgFormatType = usePNG ? VirtualGiftData.ImageFormatType.PNG.getCode() : VirtualGiftData.ImageFormatType.GIF.getCode();
      short[] availableResolutions = ContentUtils.getSupportedVirtualGiftResolutions(imgFormatType);
      if (availableResolutions == null) {
         return 0;
      } else {
         Short result = ContentUtils.getValueOrRoundDown(availableResolutions, requestedVgSize);
         return result == null ? 0 : result;
      }
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      PacketUtils.ErrorLogData errorLogData;
      try {
         return processRequest(this.transactionId, this.getHotKeys(), connection.getStickerSize(), connection.getOptimalEmoticonHeight(), connection.getVgSize(), connection.getFontHeight(), connection.getDeviceType(), connection.getClientVersion());
      } catch (CreateException var4) {
         errorLogData = new PacketUtils.ErrorLogData((short)913, log, Level.ERROR, DataCollectorUtils.newErrorID(), this.transactionId, "Unable to get emoticon - Server Error");
         errorLogData.setExceptionCause(var4);
         return PacketUtils.logAndPrepareFusionPktError(errorLogData).toArray();
      } catch (RemoteException var5) {
         errorLogData = new PacketUtils.ErrorLogData((short)913, log, Level.WARN, DataCollectorUtils.newErrorID(), this.transactionId, "Unable to get emoticon - Internal Error");
         errorLogData.setExceptionCause(var5);
         return PacketUtils.logAndPrepareFusionPktError(errorLogData).toArray();
      } catch (Exception var6) {
         errorLogData = new PacketUtils.ErrorLogData((short)913, log, Level.WARN, DataCollectorUtils.newErrorID(), this.transactionId, "Unable to get emoticon - Unhandled exception");
         errorLogData.setExceptionCause(var6);
         return PacketUtils.logAndPrepareFusionPktError(errorLogData).toArray();
      }
   }

   public static FusionPacket[] processRequest(short transactionId, String hotKeys, int stickerSize, int emoticonHeight, int vgSize, int fontHeight, ClientType deviceType, short clientVersion) throws Exception {
      String errorMessage;
      String detailedMessage;
      PacketUtils.ErrorLogData errorLogData;
      if (emoticonHeight <= 0) {
         errorMessage = "Unable to get emoticon - Optimal emoticon height not set";
         detailedMessage = "ConnectionI has an incorrect state.Cause:[Unable to get emoticon - Optimal emoticon height not set].DeviceType:[" + deviceType + "].Client Version:[" + clientVersion + "].EmoticonHeight:[" + emoticonHeight + "]";
         errorLogData = new PacketUtils.ErrorLogData((short)913, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionId, "Unable to get emoticon - Optimal emoticon height not set");
         errorLogData.setDetailedErrorMessage(detailedMessage);
         return PacketUtils.logAndPrepareFusionPktError(errorLogData).toArray();
      } else if (StringUtil.isBlank(hotKeys)) {
         errorMessage = "Unable to get emoticon - Hot keys not specified";
         detailedMessage = "Invalid field value sent by client.Cause:[Unable to get emoticon - Hot keys not specified].DeviceType:[" + deviceType + "].Client Version:[" + clientVersion + "]." + "Blank HotKeys Length:[" + (hotKeys == null ? "NA" : hotKeys.length()) + "]";
         errorLogData = new PacketUtils.ErrorLogData((short)913, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionId, "Unable to get emoticon - Hot keys not specified");
         errorLogData.setDetailedErrorMessage(detailedMessage);
         return PacketUtils.logAndPrepareFusionPktError(errorLogData).toArray();
      } else {
         boolean usePNG = ConnectionI.isMidletVersionAndAbove(deviceType, clientVersion, 400);
         boolean createBackwardCompatiblePayload = !ConnectionI.isMidletVersionAndAbove(deviceType, clientVersion, 410);
         boolean deviceCanReceiveStickersNatively = ContentUtils.deviceCanReceiveStickersNatively(deviceType, clientVersion);
         boolean hiResEnabled;
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.ENABLE_FUSION_PKT_GET_EMOTICON_HIGH_RES)) {
            boolean minClientVerGuardSetEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GuardsetEnabled.FUSION_PKT_GET_EMOTICON_HI_RES_SUPPORT);
            Short minimumVersion = minClientVerGuardSetEnabled ? MemCacheOrEJB.getMinimumClientVersionForAccess(deviceType.value(), GuardCapabilityEnum.FUSION_PKT_GET_EMOTICON_HI_RES_SUPPORT.value()) : SMALLEST_VERSION_NUMBER;
            hiResEnabled = minimumVersion != null && clientVersion >= minimumVersion;
         } else {
            hiResEnabled = false;
         }

         int requestedVgSize;
         if (vgSize > 0) {
            requestedVgSize = vgSize;
         } else {
            requestedVgSize = fontHeight;
         }

         int optimumVirtualGiftSize = getClosestSupportedVgSize((short)requestedVgSize, usePNG);
         if (log.isDebugEnabled()) {
            log.debug(String.format("connection.getVgSize():%s,connection.getFontHeight():%s,connection.getStickerSize():%s,requestedVgSize:%s,optimumVirtualGiftSize:%s", vgSize, fontHeight, stickerSize, requestedVgSize, Integer.valueOf(optimumVirtualGiftSize)));
         }

         FusionPktGetEmoticonPreSE378.RequestedDimensionSpec requestedDimensionSpec = new FusionPktGetEmoticonPreSE378.RequestedDimensionSpec();
         requestedDimensionSpec.stickerSize = coalesce(stickerSize, optimumVirtualGiftSize, emoticonHeight);
         requestedDimensionSpec.closestSupportedVgSize = (short)coalesce(optimumVirtualGiftSize, emoticonHeight);
         requestedDimensionSpec.emoticonHeight = emoticonHeight;
         String[] hotKeyArray = hotKeys.split(" ");
         ArrayList<FusionPacket> responseList = new ArrayList(hotKeyArray.length);
         String[] arr$ = hotKeyArray;
         int len$ = hotKeyArray.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String hotKey = arr$[i$];

            try {
               FusionPktGetEmoticonPreSE378.AssetData assetData = getEmoticonAssetData(hotKey, requestedDimensionSpec, usePNG, deviceCanReceiveStickersNatively, createBackwardCompatiblePayload);
               if (assetData == null) {
                  assetData = getVirtualGiftAssetData(hotKey, requestedDimensionSpec, usePNG, hiResEnabled, createBackwardCompatiblePayload);
               }

               if (assetData != null) {
                  responseList.add(createFusionPktEmoticon(transactionId, assetData, deviceCanReceiveStickersNatively));
               }
            } catch (Exception var24) {
               PacketUtils.ErrorLogData errorLogData = new PacketUtils.ErrorLogData((short)913, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionId, getErrorMessagePrefixForErrorLoadingHotKeys(hotKey));
               errorLogData.setExceptionCause(var24);
               FusionPktError pktError = PacketUtils.logAndPrepareFusionPktError(errorLogData);
               responseList.add(pktError);
            }
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.ALWAYS_SEND_PKT_GET_EMOTICONS_COMPLETE)) {
            responseList.add(new FusionPktGetEmoticonsComplete(transactionId));
         } else if (hotKeyArray.length > 1) {
            responseList.add(new FusionPktGetEmoticonsComplete(transactionId));
         }

         return (FusionPacket[])responseList.toArray(new FusionPacket[responseList.size()]);
      }
   }

   public static String getErrorMessagePrefixForErrorLoadingHotKeys(String hotKey) {
      return "Error loading hotkey(" + hotKey + ")";
   }

   private static ByteBuffer getContentData(EmoticonData.TypeEnum type, String filePath, boolean createBackwardCompatiblePayload) throws Exception {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.LOAD_EMOTICONS_FROM_REDIS) ? getContentDataFromRedis(type, filePath, createBackwardCompatiblePayload) : getContentDataFromFile(type, filePath, createBackwardCompatiblePayload);
   }

   private static ByteBuffer getContentDataFromFile(EmoticonData.TypeEnum type, String filePath, boolean createBackwardCompatiblePayload) throws IOException {
      ByteBuffer buffer = ByteBufferHelper.readFile(new File(filePath));
      if (createBackwardCompatiblePayload) {
         if (type == EmoticonData.TypeEnum.ANIMATION) {
            buffer.getInt();
            buffer = ByteBuffer.wrap(ByteBufferHelper.readBytes(buffer, buffer.getInt()));
         } else if (type == EmoticonData.TypeEnum.AUDIO) {
            buffer = ByteBuffer.wrap(ByteBufferHelper.readBytes(buffer, buffer.getInt()));
         }
      }

      return buffer;
   }

   private static ByteBuffer getContentDataFromRedis(EmoticonData.TypeEnum type, String filePath, boolean createBackwardCompatiblePayload) throws Exception {
      Jedis slaveInst = null;
      String key = null;

      ByteBuffer var8;
      try {
         slaveInst = Redis.getWebResourcesSlaveInstance();
         key = Redis.KeySpace.WEB_RESOURCE_ENTITY + filePath;
         byte[] keyBytes = Redis.safeEncoderEncode(key);
         byte[] content = slaveInst.get(keyBytes);
         ByteBuffer buffer = ByteBuffer.wrap(content);
         if (createBackwardCompatiblePayload) {
            if (type == EmoticonData.TypeEnum.ANIMATION) {
               buffer.getInt();
               buffer = ByteBuffer.wrap(ByteBufferHelper.readBytes(buffer, buffer.getInt()));
            } else if (type == EmoticonData.TypeEnum.AUDIO) {
               buffer = ByteBuffer.wrap(ByteBufferHelper.readBytes(buffer, buffer.getInt()));
            }
         }

         var8 = buffer;
      } catch (Exception var13) {
         log.error("Exception loading emoticon content from Redis: key=" + key + " e=" + var13, var13);
         throw var13;
      } finally {
         Redis.disconnect(slaveInst, log);
      }

      return var8;
   }

   private static FusionPktGetEmoticonPreSE378.AssetData getVirtualGiftAssetData(String hotKey, FusionPktGetEmoticonPreSE378.RequestedDimensionSpec requestedDimensionSpec, boolean usePNG, boolean enableHiRes, boolean backwardCompatiblePayload) throws Exception {
      Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
      VirtualGiftData virtualGift = contentEJB.getVirtualGiftByHotKey(hotKey);
      if (virtualGift != null) {
         FusionPktGetEmoticonPreSE378.AssetData assetData = new FusionPktGetEmoticonPreSE378.AssetData();
         assetData.type = EmoticonData.TypeEnum.IMAGE;
         assetData.alias = virtualGift.getName();
         assetData.hotkey = hotKey;
         if (enableHiRes) {
            VirtualGiftData.ImageFormatType imageFormatType = usePNG ? VirtualGiftData.ImageFormatType.PNG : VirtualGiftData.ImageFormatType.GIF;
            assetData.imagelocation = (String)ContentUtils.getValueOrRoundDown(virtualGift.getAvailableImageLocations(imageFormatType), requestedDimensionSpec.closestSupportedVgSize);
         } else {
            if (requestedDimensionSpec.emoticonHeight > 16) {
               requestedDimensionSpec.emoticonHeight = 16;
            }

            String location = "";
            switch(requestedDimensionSpec.emoticonHeight) {
            case 12:
               location = usePNG ? virtualGift.getLocation12x12PNG() : virtualGift.getLocation12x12GIF();
               break;
            case 13:
            case 15:
            default:
               throw new IllegalArgumentException("No emoticon found for hotkey " + hotKey + " with height " + requestedDimensionSpec.emoticonHeight);
            case 14:
               location = usePNG ? virtualGift.getLocation14x14PNG() : virtualGift.getLocation14x14GIF();
               break;
            case 16:
               location = usePNG ? virtualGift.getLocation16x16PNG() : virtualGift.getLocation16x16GIF();
            }

            assetData.imagelocation = location;
         }

         assetData.packID = null;
         assetData.alternateHotKeys = Collections.emptyList();
         assetData.contentData = getContentData(assetData.type, assetData.imagelocation, backwardCompatiblePayload);
         return assetData;
      } else {
         return null;
      }
   }

   private static FusionPktGetEmoticonPreSE378.AssetData getEmoticonAssetData(String hotKey, FusionPktGetEmoticonPreSE378.RequestedDimensionSpec requestedDimensionSpec, boolean usePNG, boolean deviceCanNativelyReceiveStickers, boolean backwardCompatiblePayload) throws Exception {
      Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
      int stickerSize = deviceCanNativelyReceiveStickers ? requestedDimensionSpec.stickerSize : requestedDimensionSpec.emoticonHeight;
      EmoticonData emoticonData = contentEJB.getByHotKey(hotKey, requestedDimensionSpec.emoticonHeight, stickerSize);
      if (log.isDebugEnabled()) {
         log.debug(String.format("requestedDimensionSpec:[%s];deviceCanNativelyReceiveStickers:%s", requestedDimensionSpec, deviceCanNativelyReceiveStickers));
         log.debug(String.format("getByHotKey(%s,%s,%s)", hotKey, requestedDimensionSpec.emoticonHeight, stickerSize));
      }

      if (emoticonData != null) {
         FusionPktGetEmoticonPreSE378.AssetData assetData = new FusionPktGetEmoticonPreSE378.AssetData();
         assetData.alternateHotKeys = emoticonData.alternateHotKeys;
         assetData.type = emoticonData.type;
         assetData.alias = emoticonData.alias;
         assetData.hotkey = hotKey;
         String imagelocation = usePNG ? emoticonData.locationPNG : emoticonData.location;
         if (deviceCanNativelyReceiveStickers) {
            if (assetData.type == EmoticonData.TypeEnum.STICKER) {
               assetData.contentData = null;
               assetData.imagelocation = getStickerImageURLLocation(emoticonData.locationPNG);
               if (log.isDebugEnabled()) {
                  log.debug("Asset type:" + assetData.type + ";StickerSupportedMode:" + deviceCanNativelyReceiveStickers + ";imagelocation:[" + assetData.imagelocation + "]");
               }
            } else {
               assetData.contentData = getContentData(assetData.type, imagelocation, backwardCompatiblePayload);
               assetData.imagelocation = null;
            }
         } else if (assetData.type == EmoticonData.TypeEnum.STICKER) {
            String imageFullPath = getStickerImageFullPath(emoticonData.locationPNG);
            assetData.imagelocation = null;
            assetData.contentData = getContentData(assetData.type, imageFullPath, backwardCompatiblePayload);
            if (log.isDebugEnabled()) {
               log.debug("Asset type:" + assetData.type + ";StickerSupportedMode:" + deviceCanNativelyReceiveStickers + ";imagelocation:[" + imageFullPath + "]");
            }
         } else {
            if (log.isDebugEnabled()) {
               log.debug("Asset type:" + assetData.type + ";StickerSupportedMode:" + deviceCanNativelyReceiveStickers + ";imagelocation:[" + imagelocation + "]");
            }

            assetData.imagelocation = null;
            assetData.contentData = getContentData(assetData.type, imagelocation, backwardCompatiblePayload);
         }

         assetData.packID = emoticonData.emoticonPackID;
         return assetData;
      } else {
         return null;
      }
   }

   private static FusionPktEmoticon createFusionPktEmoticon(short transactionId, FusionPktGetEmoticonPreSE378.AssetData assetData, boolean deviceCanNativelyReceiveStickers) {
      FusionPktEmoticon pktEmoticon = new FusionPktEmoticon(transactionId);
      pktEmoticon.setAlias(assetData.alias);
      pktEmoticon.setAlternateHotKeys(StringUtil.join((Collection)assetData.alternateHotKeys, " "));
      if (deviceCanNativelyReceiveStickers) {
         pktEmoticon.setContentType((byte)assetData.type.value());
         if (assetData.type == EmoticonData.TypeEnum.STICKER) {
            String url = StringUtil.isBlank(assetData.imagelocation) ? "" : assetData.imagelocation;
            pktEmoticon.setURL(url);
            if (assetData.packID != null) {
               pktEmoticon.setPackID(assetData.packID);
            }
         } else {
            pktEmoticon.setContent(assetData.contentData.array());
         }
      } else {
         if (assetData.type == EmoticonData.TypeEnum.STICKER) {
            pktEmoticon.setContentType((byte)EmoticonData.TypeEnum.IMAGE.value());
         } else {
            pktEmoticon.setContentType((byte)assetData.type.value());
         }

         pktEmoticon.setContent(assetData.contentData.array());
      }

      pktEmoticon.setHotKey(assetData.hotkey);
      return pktEmoticon;
   }

   public static String getStickerImageURLLocation(String location) {
      return String.format("%s/%s/%s", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIG33_WEB_BASE_URL), SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.STICKER_IMAGE_BASE_FOLDER_URL), location);
   }

   public static String getStickerImageFullPath(String location) {
      return String.format("%s/%s", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.STICKER_IMAGE_PHYSICAL_FOLDER), location);
   }

   private static class RequestedDimensionSpec {
      public short closestSupportedVgSize;
      public int emoticonHeight;
      public int stickerSize;

      private RequestedDimensionSpec() {
      }

      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("[closestSupportedVgSize=");
         builder.append(this.closestSupportedVgSize);
         builder.append(", emoticonHeight=");
         builder.append(this.emoticonHeight);
         builder.append(", stickerSize=");
         builder.append(this.stickerSize);
         builder.append("]");
         return builder.toString();
      }

      // $FF: synthetic method
      RequestedDimensionSpec(Object x0) {
         this();
      }
   }

   private static class AssetData {
      public String hotkey;
      public String alias;
      public String imagelocation;
      public ByteBuffer contentData;
      public List<String> alternateHotKeys;
      public Integer packID;
      public EmoticonData.TypeEnum type;

      private AssetData() {
      }

      // $FF: synthetic method
      AssetData(Object x0) {
         this();
      }
   }
}
