package com.projectgoth.fusion.common;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.emote.EmoteCommandException;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.restapi.enums.MigboAccessMemberTypeEnum;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;

public class ContentUtils {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ContentUtils.class));

   public static Short getValueOrRoundDown(short[] sortedAvailableValue, short requestedValue) {
      if (sortedAvailableValue != null && sortedAvailableValue.length != 0) {
         int index = Arrays.binarySearch(sortedAvailableValue, requestedValue);
         if (index < 0) {
            int wouldBeLocation = -(index + 1);
            if (wouldBeLocation >= sortedAvailableValue.length) {
               return sortedAvailableValue[sortedAvailableValue.length - 1];
            } else {
               return wouldBeLocation > 0 ? sortedAvailableValue[wouldBeLocation - 1] : sortedAvailableValue[0];
            }
         } else {
            return sortedAvailableValue[index];
         }
      } else {
         return null;
      }
   }

   public static <K extends Comparable<K>, V> V getValueOrRoundDown(SortedMap<K, V> sortedMap, K requestedKey) {
      if (sortedMap != null && !sortedMap.isEmpty()) {
         V value = sortedMap.get(requestedKey);
         if (value != null) {
            return value;
         } else {
            SortedMap<K, V> mapOfLesserThanRequested = sortedMap.headMap(requestedKey);
            return mapOfLesserThanRequested != null && mapOfLesserThanRequested.size() != 0 ? mapOfLesserThanRequested.get(mapOfLesserThanRequested.lastKey()) : ((Entry)sortedMap.entrySet().iterator().next()).getValue();
         }
      } else {
         return null;
      }
   }

   public static String normalizeImageFormatType(String imgFormat) {
      return StringUtil.trimmedUpperCase(imgFormat);
   }

   public static Entry<String, short[]>[] getSupportedVirtualGiftResolutions() {
      return ((ContentUtils.ImageTypeResolutionData)ContentUtils.SingletonHolder.SUPPORTED_RESOLUTIONS.getValue()).asEntryArray;
   }

   public static short[] getSupportedVirtualGiftResolutions(String imgFormat) {
      return (short[])((ContentUtils.ImageTypeResolutionData)ContentUtils.SingletonHolder.SUPPORTED_RESOLUTIONS.getValue()).asMap.get(normalizeImageFormatType(imgFormat));
   }

   public static short[] getSupportedStickerPackThumbnailResolutions() {
      return (short[])ContentUtils.SingletonHolder.SUPPORTED_STICKER_PACK_IMAGE_RESOLUTIONS.getValue();
   }

   public static void invalidateSupportVirtualGiftResolutionsCache() {
      ContentUtils.SingletonHolder.SUPPORTED_RESOLUTIONS.invalidateCache();
   }

   public static boolean isStickersEnabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.ENABLE_STICKERS);
   }

   public static boolean deviceCanReceiveStickersNatively(ClientType deviceEnum, short clientVersion) {
      try {
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GuardsetEnabled.RECEIVE_STICKERS_NATIVE_SUPPORT)) {
            return true;
         } else {
            Short minVersion = MemCacheOrEJB.getMinimumClientVersionForAccess(deviceEnum.value(), GuardCapabilityEnum.RECEIVE_STICKERS_NATIVE_SUPPORT.value());
            return minVersion != null && clientVersion >= minVersion;
         }
      } catch (Exception var3) {
         throw new RuntimeException("Error while checking whether device [" + deviceEnum + "] version [" + clientVersion + "] supports receiving of stickers natively", var3);
      }
   }

   public static boolean userCanSendStickers(int userId) throws RemoteException, CreateException {
      try {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         checkUserCanSendStickers(userEJB, userId);
         return true;
      } catch (EmoteCommandException var4) {
         ErrorCause errorCause = var4.getErrorCause();
         String errorCauseCodeStr = errorCause == null ? "" : errorCause.getCode();
         log.info("UserId [" + userId + "] disallowed from sending stickers. Error Cause:[" + errorCauseCodeStr + "]");
         if (errorCause == ErrorCause.EmoteCommandError.INSUFFICIENT_REPUTATION_LEVEL) {
            return false;
         } else if (errorCause == ErrorCause.EmoteCommandError.INSUFFICIENT_RIGHTS) {
            return false;
         } else {
            throw new RuntimeException("Unexpected exception:" + var4.getMessage(), var4);
         }
      } catch (CreateException var5) {
         throw var5;
      } catch (RuntimeException var6) {
         throw var6;
      } catch (Exception var7) {
         throw new RuntimeException("Unexpected exception:" + var7.getMessage(), var7);
      }
   }

   public static void checkUserCanSendStickers(User userEJB, int userId) throws EmoteCommandException, EJBException, RemoteException, CreateException, FusionEJBException {
      int userReputationLevel = MemCacheOrEJB.getUserReputationLevel((String)null, userId);
      int minMigLevel = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.MIN_REPUTATION_LEVEL_FOR_SENDING_STICKERS);
      if (userReputationLevel < minMigLevel) {
         throw new EmoteCommandException(ErrorCause.EmoteCommandError.INSUFFICIENT_REPUTATION_LEVEL, new Object[]{minMigLevel});
      } else {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GuardsetEnabled.SEND_STICKERS_ALLOWED)) {
            boolean hasGuardCapability = userEJB.isUserInMigboAccessList(userId, MigboAccessMemberTypeEnum.WHITELIST.value(), GuardCapabilityEnum.SEND_STICKERS_ALLOWED.value());
            if (!hasGuardCapability) {
               throw new EmoteCommandException(ErrorCause.EmoteCommandError.INSUFFICIENT_RIGHTS, new Object[0]);
            }
         }

      }
   }

   private static class SingletonHolder {
      private static final long CACHE_TIMEOUT_MILLIES = 120000L;
      public static final LazyLoader<ContentUtils.ImageTypeResolutionData> SUPPORTED_RESOLUTIONS = new LazyLoader<ContentUtils.ImageTypeResolutionData>("VIRTUAL_GIFT_DATA_SUPPORTED_RESOLUTIONS", 120000L) {
         protected ContentUtils.ImageTypeResolutionData fetchValue() {
            Map<String, short[]> asMap = new HashMap();
            String[] imageFormats = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_IMAGE_FORMATS);
            String[] arr$ = imageFormats;
            int len$ = imageFormats.length;

            for(int i$xx = 0; i$xx < len$; ++i$xx) {
               String imgFmt = arr$[i$xx];
               imgFmt = ContentUtils.normalizeImageFormatType(imgFmt);
               short[] resolutionArray = SystemProperty.getShortArray(SystemPropertyEntities.ContentService.getVirtualGiftResolutionSettingEnum(imgFmt));
               TreeSet<Short> resolutionSortedSet = new TreeSet();
               short[] resolutionSortedArray = resolutionArray;
               int i = resolutionArray.length;

               for(int i$ = 0; i$ < i; ++i$) {
                  short resolution = resolutionSortedArray[i$];
                  resolutionSortedSet.add(resolution);
               }

               resolutionSortedArray = new short[resolutionSortedSet.size()];
               i = 0;

               for(Iterator i$x = resolutionSortedSet.iterator(); i$x.hasNext(); ++i) {
                  Short resolutionx = (Short)i$x.next();
                  resolutionSortedArray[i] = resolutionx;
               }

               asMap.put(imgFmt, resolutionSortedArray);
            }

            return new ContentUtils.ImageTypeResolutionData(asMap);
         }
      };
      public static final LazyLoader<short[]> SUPPORTED_STICKER_PACK_IMAGE_RESOLUTIONS = new LazyLoader<short[]>("SUPPORTED_STICKER_PACK_IMAGE_RESOLUTIONS", 120000L) {
         protected short[] fetchValue() {
            short[] heights = SystemProperty.getShortArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.STICKER_PACK_IMAGE_RESOLUTION);
            TreeSet<Short> sortedHeightsSet = new TreeSet();
            short[] sortedHeightsArray = heights;
            int idx = heights.length;

            for(int i$x = 0; i$x < idx; ++i$x) {
               short h = sortedHeightsArray[i$x];
               sortedHeightsSet.add(h);
            }

            sortedHeightsArray = new short[sortedHeightsSet.size()];
            idx = 0;

            for(Iterator i$ = sortedHeightsSet.iterator(); i$.hasNext(); ++idx) {
               Short hx = (Short)i$.next();
               sortedHeightsArray[idx] = hx;
            }

            return sortedHeightsArray;
         }
      };
   }

   private static class ImageTypeResolutionData {
      public final Entry<String, short[]>[] asEntryArray;
      public final Map<String, short[]> asMap;

      public ImageTypeResolutionData(Map<String, short[]> asMap) {
         this.asEntryArray = (Entry[])asMap.entrySet().toArray(new Entry[asMap.size()]);
         this.asMap = asMap;
      }
   }
}
