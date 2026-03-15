package com.projectgoth.fusion.common;

import com.projectgoth.fusion.common.metrics.MetricsEnums;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.CreditTransferData;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.recommendation.collector.CollectedDataTypeEnum;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;

public class SystemPropertyEntities {
   private static final String SEPARATOR = ":";
   private static final String COLLECTEDDATATYPESPECIFICSETTINGS_LOCAL_NAMESPACE = "CDT";

   public static String getNameWithNamespace(String namespace, String name) {
      return String.format("%s%s%s", namespace, ":", name);
   }

   private static Map<String, SystemPropertyEntities.SystemPropertyEntryInterface> buildSystemPropertyEntryInterfaceMap() {
      Map<String, SystemPropertyEntities.SystemPropertyEntryInterface> propertyNamesToEntriesMap = new HashMap();
      Class[] arr$ = SystemPropertyEntities.class.getDeclaredClasses();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Class<?> clazz = arr$[i$];
         Class[] arr$ = clazz.getInterfaces();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Class<?> interfaze = arr$[i$];
            if (SystemPropertyEntities.SystemPropertyEntryInterface.class.equals(interfaze) && clazz.isEnum()) {
               SystemPropertyEntities.SystemPropertyEntryInterface[] arr$ = (SystemPropertyEntities.SystemPropertyEntryInterface[])clazz.getEnumConstants();
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  SystemPropertyEntities.SystemPropertyEntryInterface e = arr$[i$];
                  propertyNamesToEntriesMap.put(e.getName(), e);
               }
            }
         }
      }

      return Collections.unmodifiableMap(propertyNamesToEntriesMap);
   }

   public static SystemPropertyEntities.SystemPropertyEntryInterface getEntry(String propertyNameWithNamespace) {
      return (SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.PropertyNamesSingletonHolder.PROPERTY_NAMES_TO_ENTRIES_MAP.get(propertyNameWithNamespace);
   }

   private static String getCollectedDataTypeNameSpace(String parentNameSpace, CollectedDataTypeEnum dataType) {
      return getNameWithNamespace(parentNameSpace, "CDT[" + dataType.getCode() + "]");
   }

   public static enum Temp implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ER170_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ER170ChangesEnabled", true)),
      SE511_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE511ChangesEnabled", true)),
      ER78_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ER78ChangesEnabled", true)),
      ER79_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ER79ChangesEnabled", true)),
      ER74_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ER74ChangesEnabled", true)),
      SE425_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE425ChangesEnabled", true)),
      SE433_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE433ChangesEnabled", true)),
      ER68_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ER68ChangesEnabled", true)),
      ER62_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ER62ChangesEnabled", true)),
      WW422_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("WW422ChangesEnabled", true)),
      PT73368964_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PT73368964ChangesEnabled", true)),
      PT73368964_UseAuthForChangePwd_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PT73368964UseAuthForChangePwdChangesEnabled", true)),
      PT74760224_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PT74760224ChangesEnabled", true)),
      WW119_IMAGE_SERVER_ENABLED_REQUEST_URL_PATHS_REGEX(new SystemPropertyEntities.SystemPropertyEntryString("WW119ImageServerEnabledRequestUrlPathsRegex", "^/(a|u|avatar|dp)/.*")),
      WW384_FALL_BACK_ON_ISO_COUNTRY_CODE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("WW384FallBackOnISOCountryCodeEnabled", true)),
      SE423_LOAD_INVITEE_USES_MASTER_DB(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE423LoadInviteeUsesMasterDB", true)),
      SE426_SHARD_ASSIGNMENT_MODE(new SystemPropertyEntities.SystemPropertyEntryInteger("SE426ShardAssignmentMode", RedisShardAssignmentMode.SETNX_AND_GET_PIPELINE_WITH_DIST_LOCK.getEnumValue())),
      SE454_CHATROOM_SYSTEM_MESSAGES_SEND_MIMETYPES(new SystemPropertyEntities.SystemPropertyEntryBoolean("AD585ChatroomSystemMessageSendMimeTypesEnabled", true)),
      SE456_PACKET208_CHANGES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE456Packet208ChangesEnabled", true)),
      SE464_LATEST_MESSAGES_DIGEST_CHANGES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE464LatestMessagesDigestChangesEnabled", true)),
      WW519_EMAIL_NOTIFICATION_USER_SETTINGS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("WW519EmailNotificationUserSettingsEnabled", true)),
      SE351_CHAT_ROOM_DATA_CONCURRENT_COLLECTIONS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE351ChatRoomDataConcurrentCollectionsEnabled", false)),
      SE218_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE218Enabled", true)),
      SE472_CHAT_CXN_PUT_MSG_LOGGING_CHANGES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE472ChatConnectionPutMessageLoggingChangesEnabled", true)),
      SE501_CLEAR_MEMCAHCHED_0N_SSO_DISCONNECT_WAP_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE501ClearMemCachedOnSsoDisconnectWAP", true)),
      SE493_WEB_SOCKETS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE493WebSocketsEnabled", true)),
      SE445_FAILED_AUTHS_EXPIRY_FIX_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE445FailedAuthsExpiryFixEnabled", true)),
      SE503_EXTEND_EXCEPTION_ON_INSUFFICIENT_CREDIT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE503ExtendExceptionOnInsufficientCreditEnabled", true)),
      SE524_CREATE_CHATROOMPKT_ADDITIONAL_DATA_SUPPORT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE524CreateChatRoomPktAdditionalDataSupportEnabled", true)),
      SE523_ALERT_USER_MENTIONED_IN_MIGBO_POST_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE523AlertUserMentionedInMigboPostEnabled", true)),
      SE504_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE504_ENABLED", true)),
      SE378_HOTKEY_PACKET_FULLURLDATA_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE378HotkeyPacketFullUrlDataEnabled", true)),
      SE545_CHAT_USER_END_SESSION_SYNC_CHANGES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE545ChatUserEndSessionSyncChangesEnabled", true)),
      SE546_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE546_ENABLED", true)),
      SE552_NPE_FIX_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE552NPEFixEnabled", true)),
      FI_83_WEEK2_USE_AUTOGENERATED_PACKETS(new SystemPropertyEntities.SystemPropertyEntryBoolean("FI83Week2UseAutogeneratedPacketsEnabled", true)),
      FI_83_WEEK3_USE_AUTOGENERATED_PACKETS(new SystemPropertyEntities.SystemPropertyEntryBoolean("FI83Week3UseAutogeneratedPacketsEnabled", true)),
      SE599_FIX_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE599FixEnabled", true)),
      SE606_NPE_FIX_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE606NpeFixEnabled", true)),
      SE_604_USER_AGENT_TRACKING_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE604UserAgentTrackingEnabled", true)),
      SE605_EMAIL_HYPHENS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE605EmailHyphensEnabled", true)),
      SE_639_ONE_WAY_PROXY_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SE639OneWayProxyEnabled", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "Temp";

      private Temp(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("Temp", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public static class Cache {
         public static LazyLoader<Boolean> ER170_ENABLED = new LazyLoader<Boolean>("ER170_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.ER170_ENABLED);
            }
         };
         public static LazyLoader<Boolean> SE511_ENABLED = new LazyLoader<Boolean>("SE511_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE511_ENABLED);
            }
         };
         public static LazyLoader<Boolean> ER74_ENABLED = new LazyLoader<Boolean>("ER74_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.ER74_ENABLED);
            }
         };
         public static LazyLoader<Boolean> SE433_ENABLED = new LazyLoader<Boolean>("SE433_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE433_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se423LoadInviteeUsesMasterDB = new LazyLoader<Boolean>("SE423_LOAD_INVITEE_USES_MASTER_DB", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE423_LOAD_INVITEE_USES_MASTER_DB);
            }
         };
         public static final LazyLoader<RedisShardAssignmentMode> se426ShardAssignmentMode = new LazyLoader<RedisShardAssignmentMode>("SE426_SHARD_ASSIGNMENT_MODE", 60000L) {
            protected RedisShardAssignmentMode fetchValue() throws Exception {
               int modeCode = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE426_SHARD_ASSIGNMENT_MODE);
               RedisShardAssignmentMode mode = RedisShardAssignmentMode.fromCode(modeCode);
               if (mode == null) {
                  RedisShardAssignmentMode fallbackMode = RedisShardAssignmentMode.SETNX_AND_GET_PIPELINE_WITH_DIST_LOCK;
                  this.getLogger().error("Invalid setting. ModeCode [" + modeCode + "] is unsupported. Falling back to [" + fallbackMode + "]");
                  return fallbackMode;
               } else {
                  return mode;
               }
            }
         };
         public static LazyLoader<Boolean> se454ChatroomSystemMessageSendMimeTypesEnabled = new LazyLoader<Boolean>("SE454_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE454_CHATROOM_SYSTEM_MESSAGES_SEND_MIMETYPES);
            }
         };
         public static LazyLoader<Boolean> se456Packet208ChangesEnabled = new LazyLoader<Boolean>("SE456_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE456_PACKET208_CHANGES_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se464LmdChangesEnabled = new LazyLoader<Boolean>("SE464_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE464_LATEST_MESSAGES_DIGEST_CHANGES_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se351ChatRoomDataConcurrentCollectionsEnabled = new LazyLoader<Boolean>("SE351_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE351_CHAT_ROOM_DATA_CONCURRENT_COLLECTIONS_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se218Enabled = new LazyLoader<Boolean>("SE218_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE218_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se472ChatCxnLoggingChangesEnabled = new LazyLoader<Boolean>("SE472_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE472_CHAT_CXN_PUT_MSG_LOGGING_CHANGES_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se501ClearMemcachedOnSsoDisconnectWapEnabled = new LazyLoader<Boolean>("SE501_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE501_CLEAR_MEMCAHCHED_0N_SSO_DISCONNECT_WAP_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se493WebSocketsEnabled = new LazyLoader<Boolean>("SE493_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE493_WEB_SOCKETS_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se445FailedAuthsExpiryFixEnabled = new LazyLoader<Boolean>("SE445_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE445_FAILED_AUTHS_EXPIRY_FIX_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se524CreateChatRoomPktAdditionalDataSupportEnabled = new LazyLoader<Boolean>("SE524_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE524_CREATE_CHATROOMPKT_ADDITIONAL_DATA_SUPPORT_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se378HotkeyPacketFullUrlDataEnabled = new LazyLoader<Boolean>("SE378_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE378_HOTKEY_PACKET_FULLURLDATA_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se504Enabled = new LazyLoader<Boolean>("SE504_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE504_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se545ChatUserEndSessionSyncChangesEnabled = new LazyLoader<Boolean>("SE545_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE545_CHAT_USER_END_SESSION_SYNC_CHANGES_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se552NPEFixEnabled = new LazyLoader<Boolean>("SE552_NPE_FIX_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE552_NPE_FIX_ENABLED);
            }
         };
         public static LazyLoader<Boolean> fi83Week2UseAutogeneratedPacketsEnabled = new LazyLoader<Boolean>("FI83_WEEK2_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.FI_83_WEEK2_USE_AUTOGENERATED_PACKETS);
            }
         };
         public static LazyLoader<Boolean> fi83Week3UseAutogeneratedPacketsEnabled = new LazyLoader<Boolean>("FI83_WEEK3_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.FI_83_WEEK3_USE_AUTOGENERATED_PACKETS);
            }
         };
         public static LazyLoader<Boolean> se599FixEnabled = new LazyLoader<Boolean>("SE599_FIX_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE599_FIX_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se606NpeFixEnabled = new LazyLoader<Boolean>("SE606_NPE_FIX_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE606_NPE_FIX_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se604UserAgentTrackingEnabled = new LazyLoader<Boolean>("SE_604_USER_AGENT_TRACKING_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE_604_USER_AGENT_TRACKING_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se605EmailHyphensEnabled = new LazyLoader<Boolean>("SE_605_EMAIL_HYPHENS_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE605_EMAIL_HYPHENS_ENABLED);
            }
         };
         public static LazyLoader<Boolean> se639OneWayPrxEnabled = new LazyLoader<Boolean>("SE_639_ONE_WAY_PROXY_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE_639_ONE_WAY_PROXY_ENABLED);
            }
         };
      }
   }

   public static enum MimeDataSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      IMAGE_THUMBNAIL_WIDTH(new SystemPropertyEntities.SystemPropertyEntryInteger("ImageThumbnailWidth", 200));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "MimeData";

      private MimeDataSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("MimeData", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum ChatUserSessionsSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      FINAL_FAILURE_LOG_LEVEL(new SystemPropertyEntities.SystemPropertyEntryInteger("FinalFailureLogLevel", Level.ERROR.toInt())),
      IMMEDIATE_PURGING_DISABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ImmediatePurgingDisabled", true)),
      RETRIES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetriesEnabled", true)),
      TRIES_LIMIT(new SystemPropertyEntities.SystemPropertyEntryInteger("TriesLimit", 3)),
      RETRY_INTERVAL_MILLIS(new SystemPropertyEntities.SystemPropertyEntryInteger("RetryIntervalMillis", 1000)),
      RETRY_EXECUTOR_CORE_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("RetryExecutorCoreSize", 5)),
      PT73110676_CHANGES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PT73110676ChangesEnabled", true)),
      RETRY_STATS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryStatsEnabled", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "ChatUserSessions";

      private ChatUserSessionsSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("ChatUserSessions", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum S3UploaderSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ACCESS_KEY(new SystemPropertyEntities.SystemPropertyEntryString("AccessKey", "")),
      SECRET_KEY(new SystemPropertyEntities.SystemPropertyEntryString("SecretKey", "")),
      S3_BASE_DOMAIN(new SystemPropertyEntities.SystemPropertyEntryString("S3BaseDomain", "s3.amazonaws.com")),
      BUCKET_NAME(new SystemPropertyEntities.SystemPropertyEntryString("BucketName", "")),
      MAX_AGE_FOR_CACHE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxAgeForCache", 86400)),
      USE_CDN_DOMAIN(new SystemPropertyEntities.SystemPropertyEntryBoolean("UseCdnDomain", true)),
      CDN_DOMAIN(new SystemPropertyEntities.SystemPropertyEntryString("CdnDomain", "http://b-img.cdn.mig33.com")),
      AWS_REFRESH(new SystemPropertyEntities.SystemPropertyEntryInteger("AwsRefresh", 300000)),
      MAX_BUCKET_FAILURES(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxBucketFailures", 3)),
      MAX_UPLOAD_FILE_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxUploadFileSize", 5242880));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "S3Uploader";

      private S3UploaderSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("S3Uploader", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum IceThreadMonitorSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      OBJC_TPOOL_MONITOR_STARTUP_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ObjectCacheStartupEnabled", true)),
      OBJC_TPOOL_MONITOR_THREAD_COUNT(new SystemPropertyEntities.SystemPropertyEntryInteger("ObjectCacheThreadCount", 50)),
      OBJC_TPOOL_MONITOR_MINIMUM_INTER_DUMP_DURATION(new SystemPropertyEntities.SystemPropertyEntryInteger("ObjectCacheInterDumpDurationInSeconds", 3600)),
      GWAY_TPOOL_MONITOR_STARTUP_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GatewayStartupEnabled", true)),
      GWAY_TPOOL_MONITOR_THREAD_COUNT(new SystemPropertyEntities.SystemPropertyEntryInteger("GatewayThreadCount", 50)),
      GWAY_TPOOL_MONITOR_MINIMUM_INTER_DUMP_DURATION(new SystemPropertyEntities.SystemPropertyEntryInteger("GatewayInterDumpDurationInSeconds", 3600));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "IceThreadMonitor";

      private IceThreadMonitorSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("IceThreadMonitor", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum IceAsyncSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      OBJECT_CACHE_AMD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ObjectCacheAMDEnabled", false)),
      CREATEUSEROBJECT_AMD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CreateUserObjectAMDEnabled", false)),
      OBJC_TPOOL_CORE_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("ObjcThreadPoolCoreSize", 100)),
      OBJC_TPOOL_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("ObjcThreadPoolMaxSize", 200)),
      OBJC_TPOOL_KEEP_ALIVE_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("ObjcThreadPoolKeepAliveSeconds", 0)),
      OBJC_TPOOL_TASK_QUEUE_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("ObjcThreadPoolTaskQueueMaxSize", 5000)),
      OBJC_REFRESH_THREAD_POOL_PROPERTIES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RefreshThreadPoolPropertiesEnabled", true)),
      OBJC_TPOOL_PROPS_REFRESH_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("ObjcThreadPoolPropsRefreshIntervalSeconds", 300)),
      REGISTRY_AMD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RegistryAMDEnabled", true)),
      REGISTRY_TPOOL_CORE_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("RegistryThreadPoolCoreSize", 10)),
      REGISTRY_TPOOL_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("RegistryThreadPoolMaxSize", 25)),
      REGISTRY_TPOOL_KEEP_ALIVE_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("RegistryThreadPoolKeepAliveSeconds", 0)),
      REGISTRY_TPOOL_TASK_QUEUE_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("RegistryThreadPoolTaskQueueMaxSize", 5000)),
      REGISTRY_REFRESH_THREAD_POOL_PROPERTIES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RegistryThreadPoolPropertiesEnabled", true)),
      REGISTRY_TPOOL_PROPS_REFRESH_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("RegistryThreadPoolPropsRefreshIntervalSeconds", 300)),
      GATEWAY_AMD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GatewayAMDEnabled", true)),
      GATEWAY_TPOOL_CORE_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("GatewayThreadPoolCoreSize", 100)),
      GATEWAY_TPOOL_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("GatewayThreadPoolMaxSize", 200)),
      GATEWAY_TPOOL_KEEP_ALIVE_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("GatewayThreadPoolKeepAliveSeconds", 0)),
      GATEWAY_TPOOL_TASK_QUEUE_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("GatewayThreadPoolTaskQueueMaxSize", 5000)),
      GATEWAY_REFRESH_THREAD_POOL_PROPERTIES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GatewayRefreshThreadPoolPropertiesEnabled", true)),
      GATEWAY_TPOOL_PROPS_REFRESH_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("GatewayThreadPoolPropsRefreshIntervalSeconds", 300));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "IceAsyncSettings";

      private IceAsyncSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("IceAsyncSettings", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum Store implements SystemPropertyEntities.SystemPropertyEntryInterface {
      STORE_OLD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("StoreOldEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "Store";

      private Store(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("Store", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum RegistrySettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      USE_AMD_AMI_GET_MESSAGE_SWITCHBOARD(new SystemPropertyEntities.SystemPropertyEntryBoolean("UseAmdAmiGetMessageSwitchboard", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "Registry";

      private RegistrySettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("Registry", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum CoreChatSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      EMAIL_BASED_OFFLINE_MESSAGE_DISABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("EmailBasedOfflineMessagingDisabled", true)),
      STATS_COLLECTION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("StatsCollectionEnabled", true)),
      STATS_COLLECTION_INTERVAL_MINUTES(new SystemPropertyEntities.SystemPropertyEntryInteger("StatsCollectionIntervalMinutes", 15)),
      MIME_TYPE_MESSAGES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MimeTypeMessagesEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "CoreChat";

      private CoreChatSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("CoreChat", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum DataGridSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", false)),
      DEFAULT_EXECUTOR_SERVICE(new SystemPropertyEntities.SystemPropertyEntryString("DefaultExecutorService", "FES"));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "DataGrid";

      private DataGridSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("DataGrid", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum GiftSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      TRUE_GRID_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("TrueGridEnabled", false)),
      DISTRIBUTED_RESULTS_MAP_TTL_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("DistributedResultsMapTtlSeconds", 1800)),
      DISTRIBUTED_RESULTS_MAP_CONCURRENCY_LEVEL(new SystemPropertyEntities.SystemPropertyEntryInteger("DistributedResultsMapConcurrencyLevel", 4)),
      FUTURE_TIMEOUT_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("FutureTimeoutSeconds", 60)),
      GIFT_EVENT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GiftEventEnabled", false)),
      VIRTUAL_GIFT_RECEIVED_URL(new SystemPropertyEntities.SystemPropertyEntryString("VirtualGiftReceivedURL", "")),
      VIRTUAL_GIFT_NOTIFICATION_SMS(new SystemPropertyEntities.SystemPropertyEntryString("VirtualGiftNotificationSMS", "")),
      GIFT_SHOWER_EVENTS_TO_SEND(new SystemPropertyEntities.SystemPropertyEntryInteger("GiftShowerEventsToSend", 5)),
      GIFT_ALERT_OR_SMS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GiftAlertOrSmsEnabled", false)),
      GIFT_RECEIVE_TIMESTAMP_FORMAT(new SystemPropertyEntities.SystemPropertyEntryString("GiftReceiveTimestampFormat", "dd MMMM yyyy 'AT' hh:mma"));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "Gift";

      private GiftSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("Gift", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum PersistentGroupChatSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", true)),
      GROUP_CHAT_PARTICIPANT_REVIVAL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GroupChatParticipantRevivalEnabled", true)),
      GROUP_CHAT_IDLE_NOTIFICATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GroupChatIdleNotificationEnabled", false)),
      GROUP_CHAT_IDLE_TIMEOUT_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("GroupChatIdleTimeoutMessage", "This group chat has been idle for too long, closing down..."));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "PersistentGroupChat";

      private PersistentGroupChatSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("PersistentGroupChat", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum ChatSyncSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      CHAT_SYNC_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", true)),
      STANDALONE_MESSAGE_SWITCHBOARD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("StandaloneMessageSwitchboardEnabled", false)),
      REFRESH_THREAD_POOL_PROPERTIES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RefreshThreadPoolPropertiesEnabled", false)),
      LATEST_MESSAGES_DIGEST_PACKET_SEND_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("LatestMessagesDigestPacketSendEnabled", true)),
      LATEST_MESSAGES_DIGEST_SNIPPET_MAX_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("LatestMessagesDigestSnippetMaxLength", 50)),
      CHATROOM_SYNC_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ChatRoomSyncEnabled", true)),
      CHATROOM_CC_ALL_SESSIONS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ChatRoomCcAllSessionsEnabled", false)),
      END_SESSION_LEAVE_CHATROOMS_NEW_LOGIC_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("EndSessionLeaveChatRoomsNewLogicEnabled", false)),
      LOG_EXCLUSION_FILTER(new SystemPropertyEntities.SystemPropertyEntryString("LogExclusionFilter", "")),
      MAX_TRIES_READ_ASYNC(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxTriesReadAsync", 3)),
      MAX_TRIES_READ_SYNC(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxTriesReadSync", 3)),
      MAX_TRIES_WRITE_ASYNC(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxTriesWriteAsync", 3)),
      MAX_TRIES_WRITE_SYNC(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxTriesWriteSync", 3)),
      RETRY_STORE_MESSAGE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryStoreMessageEnabled", true)),
      RETRY_LIVESYNC_MESSAGE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryLiveSyncMessageEnabled", true)),
      RETRY_CLOSED_CHAT_NOTIFICATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryClosedChatNotificationEnabled", true)),
      RETRY_CURRENT_CHAT_LIST_UPDATE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryCurrentChatListUpdateEnabled", true)),
      RETRY_GET_CHATS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryGetChatsEnabled", true)),
      RETRY_GET_MESSAGES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryGetMessagesEnabled", true)),
      RETRY_LATEST_MESSAGES_DIGEST_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryLatestMessagesDigestEnabled", true)),
      RETRY_RENAME_CHAT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryRenameChatEnabled", true)),
      RETRY_STORE_GROUP_CHAT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryStoreGroupChatEnabled", true)),
      RETRY_GET_MESSAGE_STATUS_EVENTS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryGetMessageStatusEventsEnabled", true)),
      RETRY_STORE_MESSAGE_STATUS_EVENTS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetryStoreMessageStatusEventsEnabled", true)),
      STATS_COLLECTION_INTERVAL_MINUTES(new SystemPropertyEntities.SystemPropertyEntryInteger("StatsCollectionIntervalMinutes", 15)),
      CPU_TIME_STATS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CPUTimeStatsEnabled", false)),
      WALLCLOCK_TIME_STATS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("WallclockTimeStatsEnabled", false)),
      MESSAGE_COMPRESSION_ALGORITHM(new SystemPropertyEntities.SystemPropertyEntryString("MessageCompressionAlgorithm", "")),
      OLD_CHAT_LISTS_COMPRESSION_ALGORITHM(new SystemPropertyEntities.SystemPropertyEntryString("OldChatListsCompressionAlgorithm", "")),
      RENEW_EXPIRY_ON_READ_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RenewExpiryOnReadEnabled", false)),
      RENEW_EXPIRY_ON_WRITE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RenewExpiryOnWriteEnabled", true)),
      CHAT_DEFINITION_EXPIRY_HOURS(new SystemPropertyEntities.SystemPropertyEntryInteger("ChatDefinitionExpiryHours", 96)),
      CHAT_CONTENT_EXPIRY_HOURS(new SystemPropertyEntities.SystemPropertyEntryInteger("ChatExpiryHours", 96)),
      CURRENT_CHAT_LIST_EXPIRY_HOURS(new SystemPropertyEntities.SystemPropertyEntryInteger("CurrentChatListExpiryHours", 96)),
      OLD_CHAT_LISTS_EXPIRY_HOURS(new SystemPropertyEntities.SystemPropertyEntryInteger("OldChatListsExpiryHours", 96)),
      CHAT_LIST_VERSION_EXPIRY_HOURS(new SystemPropertyEntities.SystemPropertyEntryInteger("ChatListVersionExpiryHours", 96)),
      CURRENT_CHAT_LIST_MAX_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("CurrentChatListMaxLength", 100)),
      OLD_CHAT_LISTS_MAX_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("OldChatListsMaxLength", 100)),
      CHAT_MAX_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("ChatMaxLength", 1000)),
      CHAT_MAX_LENGTH_TRUNCATION(new SystemPropertyEntities.SystemPropertyEntryInteger("ChatMaxLengthTruncation", 950)),
      J2ME_CHAT_MANAGER_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("J2MEChatManagerEnabled", true)),
      MAX_GET_CHAT_REQUESTS_PER_MINUTE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxGetChatRequestsPerMinute", 10000)),
      MAX_GET_MESSAGE_REQUESTS_PER_MINUTE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxGetMessageRequestsPerMinute", 10000)),
      MAX_CHATS_STORED_PER_MINUTE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxChatsStoredPerMinute", 10000)),
      MAX_MESSAGES_STORED_PER_MINUTE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxMessagesStoredPerMinute", 10000)),
      MAX_J2ME_PUSHES_PER_MINUTE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxJ2MEPushesPerMinute", 10000)),
      MAX_CHAT_LIST_UPDATES_PER_MINUTE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxChatListUpdatesPerMinute", 10000)),
      MAX_GET_CHATS_REQUESTS_PER_USER_PER_DAY(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxGetChatsRequestsPerUserPerDay", 1000)),
      MAX_GET_MESSAGES_REQUESTS_PER_USER_PER_DAY(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxGetMessagesRequestsPerUserPerDay", 1000)),
      MAX_CHATS_STORED_PER_USER_PER_DAY(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxChatsStoredPerUserPerDay", 200)),
      MAX_MESSAGES_STORED_PER_USER_PER_DAY(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxMessagesStoredPerUserPerDay", 2000)),
      GET_MESSAGES_SYSADMIN_LIMIT(new SystemPropertyEntities.SystemPropertyEntryInteger("GetMessagesSysAdminLimit", 50)),
      TPOOL_CORE_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadPoolCoreSize", 100)),
      TPOOL_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadPoolMaxSize", 200)),
      TPOOL_KEEP_ALIVE_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadPoolKeepAliveSeconds", 0)),
      TPOOL_TASK_QUEUE_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadPoolTaskQueueMaxSize", 5000)),
      TPOOL_PROPS_REFRESH_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadPoolPropsRefreshIntervalSeconds", 300)),
      STORAGE_TIMEOUT_MILLIS(new SystemPropertyEntities.SystemPropertyEntryInteger("StorageTimeoutMillis", 10000)),
      RETRIEVAL_TIMEOUT_MILLIS(new SystemPropertyEntities.SystemPropertyEntryInteger("RetrievalTimeoutMillis", 10000));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "ChatSync";

      private ChatSyncSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("ChatSync", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum ContentService implements SystemPropertyEntities.SystemPropertyEntryInterface, EnumUtils.IEnumValueGetter<String> {
      RETRIEVE_VIRTUAL_GIFT_STORE_ITEM_ID(new SystemPropertyEntities.SystemPropertyEntryBoolean("RetrieveVirtualGiftStoreItem", true)),
      VIRTUAL_GIFT_MESSAGE_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("VirtualGiftMessageLength", 40)),
      VIRTUAL_GIFT_POST_MESSAGES(new SystemPropertyEntities.SystemPropertyEntryString("VirtualGiftPostMessages", "Can you feel the love?  I sent the %s gift to %s %s")),
      VIRTUAL_GIFT_POST_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("VirtualGiftPostMessage", "I put a smile on someone's face today! I sent a %s gift to %s %s")),
      VIRTUAL_GIFT_POST_MESSAGE_CUSTOM(new SystemPropertyEntities.SystemPropertyEntryString("VirtualGiftPostMessageCustom", "I sent a %s gift to %s %s \"%s\" ")),
      ENABLE_FUSION_PKT_GET_EMOTICON_HIGH_RES(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableFusionPktGetEmoticonHighRes", false)),
      VIRTUAL_GIFT_RESOLUTIONS_DEFAULT(new SystemPropertyEntities.SystemPropertyEntryInteger("VirtualGiftResolutionsDefault", 64)),
      VIRTUAL_GIFT_RESOLUTIONS(new SystemPropertyEntities.SystemPropertyEntryShortArray("VirtualGiftResolutions", new short[]{12, 14, 16})),
      VIRTUAL_GIFT_RESOLUTIONS_GIF(new SystemPropertyEntities.SystemPropertyEntryShortArray("VirtualGiftResolutions[GIF]", new short[]{12, 14, 16})),
      VIRTUAL_GIFT_RESOLUTIONS_PNG(new SystemPropertyEntities.SystemPropertyEntryShortArray("VirtualGiftResolutions[PNG]", new short[]{12, 14, 16, 64})),
      VIRTUAL_GIFT_IMAGE_FORMATS(new SystemPropertyEntities.SystemPropertyEntryStringArray("VirtualGiftImageFormats", new String[]{"PNG", "GIF"})),
      ENABLE_STICKERS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableStickers", false)),
      MIN_REPUTATION_LEVEL_FOR_SENDING_STICKERS(new SystemPropertyEntities.SystemPropertyEntryInteger("MinReputationLevelForSendingStickers", 1)),
      DEFAULT_FREE_STICKER_PACKS(new SystemPropertyEntities.SystemPropertyEntryStringArray("DefaultFreeStickerPacks", new String[0])),
      STICKER_PACK_IMAGE_RESOLUTION(new SystemPropertyEntities.SystemPropertyEntryShortArray("StickerPackImageResolution", new short[]{36, 48, 72, 96})),
      STICKER_PACK_THUMBNAIL_BASE_FOLDER_URL(new SystemPropertyEntities.SystemPropertyEntryString("StickerPackThumbnailBaseUrl", "images/emoticons/stickers")),
      STICKER_PACK_THUMBNAIL_DEFAULT_EXTENSION(new SystemPropertyEntities.SystemPropertyEntryString("StickerPackThumbnailDefaultExtension", ".png")),
      STICKER_IMAGE_BASE_FOLDER_URL(new SystemPropertyEntities.SystemPropertyEntryString("StickerImageBaseFolderUrl", "images/emoticons/stickers")),
      STICKER_IMAGE_PHYSICAL_FOLDER(new SystemPropertyEntities.SystemPropertyEntryString("StickerImagePhyiscalFolder", "/usr/fusion/emoticons/stickers")),
      LOAD_EMOTICONS_FROM_REDIS(new SystemPropertyEntities.SystemPropertyEntryBoolean("LoadEmoticonsFromRedis", false)),
      LOAD_THUMBNAIL_STICKER_DIR_AS_PREVIEW(new SystemPropertyEntities.SystemPropertyEntryBoolean("LoadThumbnailStickerDirForPreview", true)),
      MINIBLOG_TEXT_CONTENT_MAX_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("MiniblogTextContentMaxLength", 180)),
      IGNORE_ON_ALREADY_OWNED_STICKER_OR_EMOTICON_PACK(new SystemPropertyEntities.SystemPropertyEntryBoolean("IgnoreOnAlreadyOwnedStickerOrEmoticonPack", true)),
      /** @deprecated */
      @Deprecated
      ENABLE_NEW_IMPLEMENTATION_FOR_CREATE_MINI_BLOG_TEXT(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableNewImplementationForCreateMiniBlogText", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "ContentService";
      private static final short[] EMPTY_SHORT = new short[0];

      private ContentService(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }

      public String getEnumValue() {
         return StringUtil.trimmedUpperCase(this.getEntry().getName());
      }

      public static SystemPropertyEntities.SystemPropertyEntryInterface getVirtualGiftResolutionSettingEnum(String imageFormatName) {
         if (StringUtil.isBlank(imageFormatName)) {
            return VIRTUAL_GIFT_RESOLUTIONS;
         } else {
            final String propertyName = (VIRTUAL_GIFT_RESOLUTIONS.getEntry().getName() + "[" + imageFormatName.trim() + "]").toUpperCase();
            SystemPropertyEntities.ContentService contentServiceEnum = (SystemPropertyEntities.ContentService)SystemPropertyEntities.ContentService.SingletonHolder.lookupByName.get(propertyName);
            return (SystemPropertyEntities.SystemPropertyEntryInterface)(contentServiceEnum != null ? contentServiceEnum : new SystemPropertyEntities.SystemPropertyEntryInterface() {
               public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
                  return new SystemPropertyEntities.SystemPropertyEntryShortArray(propertyName, SystemPropertyEntities.ContentService.EMPTY_SHORT);
               }

               public String getName() {
                  return SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.ContentService.NS, this.getEntry().getName());
               }
            });
         }
      }

      private static class SingletonHolder {
         public static final Map<String, SystemPropertyEntities.ContentService> lookupByName = EnumUtils.buildLookUpMap(new HashMap(), SystemPropertyEntities.ContentService.class);
      }
   }

   public static enum CreditTransferFeeTypeSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", false)),
      FEE_PERCENTAGE(new SystemPropertyEntities.SystemPropertyEntryDouble("FeePercentage", 5.0D)),
      MINIMUM_TRANSFER_AMOUNT_REQUIRED_IN_USD(new SystemPropertyEntities.SystemPropertyEntryDouble("MinimumTransferAmountRequiredInUSD", 0.2D));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "CreditTransferFeeSetting";

      private CreditTransferFeeTypeSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.CreditTransferFeeTypeSettings.CreditTransferFeeSpecificSetting forTransferFee(CreditTransferData.CreditTransferFeeEnum e) {
         return new SystemPropertyEntities.CreditTransferFeeTypeSettings.CreditTransferFeeSpecificSetting(e, this);
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("CreditTransferFeeSetting", this.value.getName());
      }

      private class CreditTransferFeeSpecificSetting implements SystemPropertyEntities.SystemPropertyEntryInterface {
         private CreditTransferData.CreditTransferFeeEnum creditTransferFeeType;
         private SystemPropertyEntities.SystemPropertyEntry<?> value;

         public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
            return this.value;
         }

         public String getName() {
            return SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.getNameWithNamespace("CreditTransferFeeSetting", this.creditTransferFeeType.name()), this.value.getName());
         }

         CreditTransferFeeSpecificSetting(CreditTransferData.CreditTransferFeeEnum creditFeeTransferType, SystemPropertyEntities.SystemPropertyEntryInterface parentProperty) {
            this.value = parentProperty.getEntry();
            this.creditTransferFeeType = creditFeeTransferType;
         }
      }
   }

   public static enum CreditTransferFee implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", false)),
      ENABLED_TO_PUBLIC(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledToPublic", false)),
      REVERSE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ReverseEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "CreditTransferFee";

      private CreditTransferFee(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("CreditTransferFee", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum Reputation implements SystemPropertyEntities.SystemPropertyEntryInterface {
      SCORE_TO_LEVEL_CACHE_ENTRY_TTL_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("ScoreToLevelCacheEntryTtlMillis", 600000L));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "Reputation";

      private Reputation(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("Reputation", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   private static enum RecommendationServiceSettings_CollectedAddressBookContactData implements SystemPropertyEntities.SystemPropertyEntryInterface {
      __UNIT_TEST_ENTRY_LONG1(new SystemPropertyEntities.SystemPropertyEntryLong("__unitTestEntryLong1", 1200L)),
      __UNIT_TEST_ENTRY_STRING0(new SystemPropertyEntities.SystemPropertyEntryString("__unitTestEntryString0", "CollectedAddressBookContactData_String0default")),
      DATA_COLLECTION_SERVICE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("DataCollectionServiceEnabled", false)),
      UPLOAD_TICKET_CIPHER_CIPHER_PASSPHRASE(new SystemPropertyEntities.SystemPropertyEntryString("UploadTicketCipherPassphrase", "*mR.$2rk04#VELG!")),
      UPLOAD_TICKET_EXPIRY_SECS(new SystemPropertyEntities.SystemPropertyEntryLong("UploadTicketExpirySecs", 300L)),
      UPLOAD_DATA_MIN_MIG_LEVEL(new SystemPropertyEntities.SystemPropertyEntryInteger("UploadDataMinMigLevel", -1)),
      RATE_LIMIT_UPLOAD_TICKET_REQUEST(new SystemPropertyEntities.SystemPropertyEntryString("RateLimitUploadTicketRequest", "1000/1M")),
      RATE_LIMIT_UPLOAD_DATA(new SystemPropertyEntities.SystemPropertyEntryString("RateLimitUploadData", "1000/1M")),
      RATE_LIMIT_UPLOAD_TICKET_REQUEST_PER_USER_ID(new SystemPropertyEntities.SystemPropertyEntryString("RateLimitUploadTicketRequestPerUserID", "1/30S")),
      RATE_LIMIT_UPLOAD_DATA_PER_USER_ID(new SystemPropertyEntities.SystemPropertyEntryString("RateLimitUploadDataPerUserID", "1/30S"));

      private SystemPropertyEntities.SystemPropertyEntry<?> entry;
      private static final String NS = SystemPropertyEntities.getCollectedDataTypeNameSpace("RecommendationService", CollectedDataTypeEnum.ADDRESSBOOKCONTACT);

      private RecommendationServiceSettings_CollectedAddressBookContactData(SystemPropertyEntities.SystemPropertyEntry<?> entry) {
         this.entry = entry;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.entry.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.entry;
      }
   }

   public static enum RecommendationServiceSettings implements SystemPropertyEntities.SystemPropertyEntryInterfaceWithNamespace {
      __UNIT_TEST_ENTRY_LONG0(new SystemPropertyEntities.SystemPropertyEntryLong("__unitTestEntryLong0", 100L)),
      __UNIT_TEST_ENTRY_LONG1(new SystemPropertyEntities.SystemPropertyEntryLong("__unitTestEntryLong1", 200L)),
      __UNIT_TEST_ENTRY_STRING0(new SystemPropertyEntities.SystemPropertyEntryString("__unitTestEntryString0", "String0default")),
      __UNIT_TEST_ENTRY_STRING1(new SystemPropertyEntities.SystemPropertyEntryString("__unitTestEntryString1", "String1default")),
      DATA_COLLECTION_SERVICE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("DataCollectionServiceEnabled", false)),
      DATA_COLLECTION_SERVICE_ENDPOINTS(new SystemPropertyEntities.SystemPropertyEntryString("DataCollectionServiceEndPoints", "")),
      ENABLE_RDCS_PROXY_WITH_CACHED_CONNECTION(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableRDCSProxyWithCachedConnection", false)),
      UPLOAD_TICKET_CIPHER_CIPHER_PASSPHRASE(new SystemPropertyEntities.SystemPropertyEntryString("UploadTicketCipherPassphrase", "*mR.$2rk04#VELG!")),
      UPLOAD_TICKET_EXPIRY_SECS(new SystemPropertyEntities.SystemPropertyEntryLong("UploadTicketExpirySecs", 300L)),
      UPLOAD_DATA_MIN_MIG_LEVEL(new SystemPropertyEntities.SystemPropertyEntryInteger("UploadDataMinMigLevel", -1)),
      RATE_LIMIT_UPLOAD_TICKET_REQUEST(new SystemPropertyEntities.SystemPropertyEntryString("RateLimitUploadTicketRequest", "1000/1M")),
      RATE_LIMIT_UPLOAD_DATA(new SystemPropertyEntities.SystemPropertyEntryString("RateLimitUploadData", "1000/1M")),
      RATE_LIMIT_UPLOAD_TICKET_REQUEST_PER_USER_ID(new SystemPropertyEntities.SystemPropertyEntryString("RateLimitUploadTicketRequestPerUserID", "1/30S")),
      RATE_LIMIT_UPLOAD_DATA_PER_USER_ID(new SystemPropertyEntities.SystemPropertyEntryString("RateLimitUploadDataPerUserID", "1/30S")),
      MAX_NUMBER_OF_ENTRIES_PER_UPLOAD(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxNumberOfEntriesPerUpload", 1000)),
      MAX_TOTAL_ENTRY_SIZES_PER_UPLOAD(new SystemPropertyEntities.SystemPropertyEntryLong("MaxTotalEntrySizesPerUpload", 1000L)),
      GENERATION_SERVICE_VIA_INTERNAL_TIMER_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GenerationServiceViaInternalTimerEnabled", true)),
      GENERATION_SERVICE_VIA_QUARTZ_SCHEDULER_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GenerationServiceViaQuartzSchedulerEnabled", false)),
      RGS_INTERNAL_TIMER_REFRESH_INTERVAL_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("RgsInternalTimerRefreshIntervalSeconds", 300)),
      RGS_INTERNAL_TIMER_SCHEDULE_GAP_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("RgsInternalTimerScheduleGapSeconds", 600)),
      RGS_MAX_PIPELINE_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("RgsMaxPipelineLength", 1000)),
      DELIVERY_SERVICE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("DeliveryServiceEnabled", false)),
      HIVE2_JDBC_DRIVER_CLASS(new SystemPropertyEntities.SystemPropertyEntryString("Hive2JdbcDriverClass", "org.apache.hive.jdbc.HiveDriver")),
      HIVE2_JDBC_URL(new SystemPropertyEntities.SystemPropertyEntryString("Hive2JdbcUrl", "jdbc:hive2://log01")),
      HIVE2_JDBC_USERNAME(new SystemPropertyEntities.SystemPropertyEntryString("Hive2JdbcUsername", "hive")),
      HIVE2_JDBC_PASSWORD(new SystemPropertyEntities.SystemPropertyEntryString("Hive2JdbcPassword", "")),
      FEATURED(new SystemPropertyEntities.SystemPropertyEntryStringArray("Featured", new String[0])),
      BLACKLISTED(new SystemPropertyEntities.SystemPropertyEntryStringArray("Blacklisted", new String[0])),
      FEATURED_RECOMMENDATIONS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("FeaturedRecommendationsEnabled", true)),
      RECOMMENDATIONS_CACHE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RecommendationsCacheEnabled", false)),
      RECOMMENDATIONS_CACHE_EXPIRY_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryLong("RecommendationsCacheExpiryInSeconds", 3600L)),
      RECOMMENDATIONS_BLACKLIST_FILTER_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RecommendationsBlacklistFilterEnabled", true)),
      INDIVIDUAL_USER_RECOMMENDATION_REASON_DETAILS_PART1(new SystemPropertyEntities.SystemPropertyEntryString("IndividualUserRecommendationReasonDetailsPart1", "Also followed by $1")),
      INDIVIDUAL_USER_RECOMMENDATION_REASON_DETAILS_PART2(new SystemPropertyEntities.SystemPropertyEntryString("IndividualUserRecommendationReasonDetailsPart2", " and %s other people")),
      GLOBAL_USER_RECOMMENDATION_REASON(new SystemPropertyEntities.SystemPropertyEntryString("GlobalUserRecommendationReason", "People you might want to know")),
      REGIONAL_USER_RECOMMENDATION_REASON(new SystemPropertyEntities.SystemPropertyEntryString("RegionalUserRecommendationReason", "Most active users in your country")),
      USER_RECOMMENDATION_WEIGHT_FOR_FRIENDS(new SystemPropertyEntities.SystemPropertyEntryDouble("UserRecommendationWeightForFriends", 3.0D)),
      USER_RECOMMENDATION_WEIGHT_FOR_FOLLOWING(new SystemPropertyEntities.SystemPropertyEntryDouble("UserRecommendationWeightForFollowing", 2.0D)),
      USER_RECOMMENDATION_WEIGHT_FOR_FOLLOWER(new SystemPropertyEntities.SystemPropertyEntryDouble("UserRecommendationWeightForFollower", 1.0D)),
      MIX_USER_RECOMMENDATION_ENABLE(new SystemPropertyEntities.SystemPropertyEntryBoolean("MixUserRecommendationEnable", false)),
      MIX_USER_RECOMMENDATION_WEIGHT(new SystemPropertyEntities.SystemPropertyEntryDouble("MixUserRecommendationWeight", 1000.0D)),
      GLOBAL_VERIFIED_USER_RECOMMENDATION_ENABLE(new SystemPropertyEntities.SystemPropertyEntryBoolean("GlobalVerifiedUserRecommendationEnable", true)),
      REGIONAL_MOST_ACTIVE_USER_RECOMMENDATION_ENABLE(new SystemPropertyEntities.SystemPropertyEntryBoolean("RegionalMostActiveUserRecommendationEnable", false)),
      USER_RECOMMENDATION_MIN_THRESHOLD(new SystemPropertyEntities.SystemPropertyEntryInteger("UserRecommendationMinThreshold", 10)),
      BOOST_GLOBAL_VERIFIED_USER_RECOMMENDATION_ENABLE(new SystemPropertyEntities.SystemPropertyEntryBoolean("BoostGlobalVerifiedUserRecommendationEnable", true)),
      GLOBAL_VERIFIED_USER_RECOMMENDATION_BOOST_WEIGHT(new SystemPropertyEntities.SystemPropertyEntryDouble("GlobalVerifiedUserRecommendationBoostWeight", 1000.0D)),
      REGIONAL_MOST_ACTIVE_USER_SPAM_THRESHOLD(new SystemPropertyEntities.SystemPropertyEntryDouble("RegionalMostActiveUserSpamThreshold", 10000.0D)),
      DEFAULT_RESULT_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("DefaultResultSize", 10)),
      MAX_ALLOWED_RESULT_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxAllowedResultSize", 1000)),
      REDIS_LOCATION_ERROR_LOGGING_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RedisLocationErrorLoggingEnabled", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> entry;
      private static final String NS = "RecommendationService";

      public String getNamespace() {
         return "RecommendationService";
      }

      public SystemPropertyEntities.RecommendationServiceSettings.RecommendationTypeSpecificSetting forRecommendationType(com.projectgoth.fusion.recommendation.delivery.Enums.RecommendationTypeEnum recType) {
         return new SystemPropertyEntities.RecommendationServiceSettings.RecommendationTypeSpecificSetting(recType, this);
      }

      public SystemPropertyEntities.SystemPropertyEntryInterface forCollectedDataType(CollectedDataTypeEnum dataType) {
         SystemPropertyEntities.SystemPropertyEntryInterface entryInterface = SystemPropertyEntities.getEntry(SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.getCollectedDataTypeNameSpace("RecommendationService", dataType), this.entry.getName()));
         return (SystemPropertyEntities.SystemPropertyEntryInterface)(entryInterface != null ? entryInterface : new SystemPropertyEntities.RecommendationServiceSettings.CollectedDataTypeSpecificSetting(dataType, this));
      }

      private RecommendationServiceSettings(SystemPropertyEntities.SystemPropertyEntry<?> entry) {
         this.entry = entry;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.entry;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("RecommendationService", this.entry.getName());
      }

      private static final class CollectedDataTypeSpecificSetting implements SystemPropertyEntities.SystemPropertyEntryInterface {
         private final SystemPropertyEntities.SystemPropertyEntry<?> entry;
         private final String name;

         public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
            return this.entry;
         }

         public String getName() {
            return this.name;
         }

         CollectedDataTypeSpecificSetting(CollectedDataTypeEnum dataType, SystemPropertyEntities.SystemPropertyEntryInterface parentProperty) {
            this.entry = parentProperty.getEntry();
            if (dataType != null) {
               this.name = SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.getCollectedDataTypeNameSpace("RecommendationService", dataType), this.entry.getName());
            } else {
               this.name = SystemPropertyEntities.getNameWithNamespace("RecommendationService", this.entry.getName());
            }

         }
      }

      public static class RecommendationTypeSpecificSetting extends SystemPropertyEntryWithParent {
         RecommendationTypeSpecificSetting(com.projectgoth.fusion.recommendation.delivery.Enums.RecommendationTypeEnum recType, SystemPropertyEntities.RecommendationServiceSettings parentProperty) {
            super(SystemPropertyEntryFallbackCreatorRegistry.getInstance(parentProperty), recType == null ? null : recType.name(), parentProperty);
         }

         public SystemPropertyEntities.RecommendationServiceSettings.RecommendationTargetSpecificSetting forTargetType(com.projectgoth.fusion.recommendation.delivery.Enums.RecommendationTargetEnum targetType) {
            return new SystemPropertyEntities.RecommendationServiceSettings.RecommendationTargetSpecificSetting(this.getSystemPropertyEntryFallbackCreator(), targetType, this);
         }
      }

      public static class RecommendationTargetSpecificSetting extends SystemPropertyEntryWithParent {
         RecommendationTargetSpecificSetting(SystemPropertyEntryFallbackCreator sysPropEntryFallbackCreator, com.projectgoth.fusion.recommendation.delivery.Enums.RecommendationTargetEnum targetType, SystemPropertyEntities.SystemPropertyEntryInterfaceWithNamespace parentProperty) {
            super(sysPropEntryFallbackCreator, targetType == null ? null : targetType.value(), parentProperty);
         }

         public SystemPropertyEntities.RecommendationServiceSettings.RecommendationViewSpecificSetting forViewType(SSOEnums.View viewType) {
            return new SystemPropertyEntities.RecommendationServiceSettings.RecommendationViewSpecificSetting(this.getSystemPropertyEntryFallbackCreator(), viewType, this);
         }

         public SystemPropertyEntities.RecommendationServiceSettings.RecommendationCountrySpecificSetting forCountryId(Integer countryId) {
            return new SystemPropertyEntities.RecommendationServiceSettings.RecommendationCountrySpecificSetting(this.getSystemPropertyEntryFallbackCreator(), countryId, this);
         }
      }

      public static class RecommendationCountrySpecificSetting extends SystemPropertyEntryWithParent {
         RecommendationCountrySpecificSetting(SystemPropertyEntryFallbackCreator sysPropEntryFallbackCreator, Integer countryId, SystemPropertyEntities.SystemPropertyEntryInterfaceWithNamespace parentProperty) {
            super(sysPropEntryFallbackCreator, countryId == null ? null : String.valueOf(countryId), parentProperty);
         }
      }

      public static class RecommendationViewSpecificSetting extends SystemPropertyEntryWithParent {
         RecommendationViewSpecificSetting(SystemPropertyEntryFallbackCreator sysPropEntryFallbackCreator, SSOEnums.View viewType, SystemPropertyEntities.SystemPropertyEntryInterfaceWithNamespace parentProperty) {
            super(sysPropEntryFallbackCreator, viewType == null ? null : String.valueOf(viewType.value()), parentProperty);
         }
      }
   }

   public static enum SmsSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      LOG_REFUSED_TO_SEND(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogWhenRefusedToSend", false)),
      SMS_ENGINE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SmsEngineEnabled", false)),
      SEND_ACTIVATION_CODE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendActivationCodeEnabled", false)),
      SEND_FORGOT_PASSWORD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendForgotPasswordEnabled", false)),
      SEND_USER_REFERRAL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendUserReferralEnabled", false)),
      SEND_USER_REFERRAL_ACTIVATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendUserReferralActivationEnabled", false)),
      SEND_MIG33_WAP_PUSH_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendMig33WapPushEnabled", false)),
      SEND_MIG33_PREMIUM_SMS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendMig33PremiumSmsEnabled", false)),
      SEND_TT_NOTIFICATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendTtNotificationEnabled", false)),
      SEND_SMS_CALLBACK_HELP_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendSmsCallbackHelpEnabled", false)),
      SEND_SMS_CALLBACK_BALANCE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendSmsCallbackBalanceEnabled", false)),
      SEND_EMAIL_ALERT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendEmailAlertEnabled", false)),
      SEND_BANK_TRANSFER_CONFIRMATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendBankTransferConfirmationEnabled", false)),
      SEND_LOW_BALANCE_ALERT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendLowBalanceAlertEnabled", false)),
      SEND_MERCHANT_USER_ACTIVATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendMerchantUserActivationEnabled", false)),
      SEND_BUZZ_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendBuzzEnabled", false)),
      SEND_LOOKOUT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendLookoutEnabled", false)),
      SEND_WESTERN_UNION_CONFIRMATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendWesternUnionConfirmationEnabled", false)),
      SEND_MOBILE_CONTENT_DOWNLOAD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendMobileContentDownloadEnabled", false)),
      SEND_SMS_VOUCHER_RECHARGE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendSmsVoucherRechargeEnabled", false)),
      SEND_VIRTUAL_GIFT_NOTIFICATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendVirtualGiftNotificationEnabled", false)),
      SEND_GROUP_ANNOUNCEMENT_NOTIFICATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendGroupAnnouncementNotificationEnabled", false)),
      SEND_BANGLALINK_URL_DOWNLOAD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendBanglalinkUrlDownloadEnabled", false)),
      SEND_BANGLALINK_VOUCHER_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendBanglalinkVoucherEnabled", false)),
      SEND_GROUP_EVENT_NOTIFICATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendGroupEventNotificationEnabled", false)),
      SEND_INDOSAT_URL_DOWNLOAD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendIndosatUrlDownloadEnabled", false)),
      SEND_SUBSCRIPTION_EXPIRY_NOTIFICATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendSubscriptionExpiryNotificationEnabled", false)),
      SEND_MARKETING_REWARD_NOTIFICATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendMarketingRewardNotificationEnabled", false)),
      SEND_USER_REFERRAL_VIA_GAMES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendUserReferralViaGamesEnabled", false)),
      SEND_INVITATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendInvitationEnabled", false)),
      SEND_VERIFICATION_CODE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendVerificationCodeEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Sms";

      private SmsSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum TokenizerSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      DEFAULT_CHARSET(new SystemPropertyEntities.SystemPropertyEntryString("DefaultCharset", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789")),
      MINIMUM_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("MinimumLength", 20)),
      DEFAULT_EXPIRATION_IN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("DefaultExpirationInMillis", 600000L));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "Tokenizer";

      private TokenizerSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("Tokenizer", this.value.getName());
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }
   }

   public static enum IMSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      MINMIGLEVEL(new SystemPropertyEntities.SystemPropertyEntryInteger("MinMigLevel", Integer.MIN_VALUE)),
      MAX_CONCURRENT_CONNECTIONS(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxConcurrentConnections", 500)),
      DISCARD_MESSAGE_ABOVE_THRESHOLD_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("DiscardMessageAboveThresholdEnabled", false)),
      MAX_MESSAGES_RECEIVE_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("MaxMessagesReceiveRateLimit", "60/1M")),
      DEFAULT_ONLINE_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("DefaultOnlineString", "I'm using migme (http://mig.me)..."));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "IMSettings";

      private IMSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.IMSettings.IMSpecificSetting forIM(ImType e) {
         return new SystemPropertyEntities.IMSettings.IMSpecificSetting(e, this);
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("IMSettings", this.value.getName());
      }

      private class IMSpecificSetting implements SystemPropertyEntities.SystemPropertyEntryInterface {
         private ImType imType;
         private SystemPropertyEntities.SystemPropertyEntry<?> value;

         public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
            return this.value;
         }

         public String getName() {
            return SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.getNameWithNamespace("IMSettings", this.imType.name()), this.value.getName());
         }

         IMSpecificSetting(ImType imType, SystemPropertyEntities.SystemPropertyEntryInterface parentProperty) {
            this.value = parentProperty.getEntry();
            this.imType = imType;
         }
      }
   }

   public static enum ForgotUsername implements SystemPropertyEntities.SystemPropertyEntryInterface {
      REQUEST_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("RequestRateLimit", "10/1D")),
      EMAIL_SUBJECT(new SystemPropertyEntities.SystemPropertyEntryString("EmailSubject", "Welcome back to migme! %1")),
      EMAIL_CONTENT(new SystemPropertyEntities.SystemPropertyEntryString("EmailContent", "Hi %1,\n\nWelcome back to migme. Your username on migme is:\n\n%2\n\nClick the below link to login migme and have fun.\n\nhttps://login.mig.me\n\nThanks,\nThe migme Team"));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "ForgotUsername";

      private ForgotUsername(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum ForgotPassword implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", false)),
      ENABLED_FORGOT_PASSWORD_VIA_SECURITY_QUESTION(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledForgotPasswordViaSecurityQuestion", true)),
      ENABLED_FORGOT_PASSWORD_VIA_SMS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledForgotPasswordViaSMS", true)),
      ENABLED_FORGOT_PASSWORD_VIA_EMAIL(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledForgotPasswordViaEmail", true)),
      ENABLED_CHECK_COUNTRY_FOR_FORGOT_PASSWORD_VIA_SMS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledCheckCountryForForgotPasswordViaSMS", true)),
      ENABLED_CHECK_RETRIEVE_TIME_FOR_FORGOT_PASSWORD_VIA_SMS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledCheckRetrieveTimeForForgotPasswordViaSMS", true)),
      ENABLED_RATELIMIT_PER_IP(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledRateLimitPerIP", true)),
      WHITELIST_COUNTRIES_FOR_SMS(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("WhitelistCountriesForSMS", new int[]{106, 107, 157})),
      ATTEMPT_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("AttemptRateLimit", "10/1D")),
      ATTEMPT_RATE_LIMIT_VIA_EMAIL(new SystemPropertyEntities.SystemPropertyEntryString("AttemptRateLimitViaEmail", "3/1D")),
      ATTEMPT_RATE_LIMIT_VIA_SQ(new SystemPropertyEntities.SystemPropertyEntryString("AttemptRateLimitViaSQ", "3/2D")),
      ATTEMPT_RATE_LIMIT_VIA_SMS(new SystemPropertyEntities.SystemPropertyEntryString("AttemptRateLimitViaSMS", "3/5D")),
      ATTEMPT_RATE_LIMIT_PER_IP(new SystemPropertyEntities.SystemPropertyEntryString("AttemptRateLimitPerIP", "10/1D")),
      ENABLED_CHECK_FORGOT_PASSWORD_VIA_SMS_MAX_RETRIEVE_TIME(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledCheckForgotPasswordViaSMSMaxRetrieveTime", true)),
      FORGOT_PASSWORD_VIA_SMS_MAX_RETRIEVE_TIME(new SystemPropertyEntities.SystemPropertyEntryInteger("ForgotPasswordViaSMSMaxRetrieveTime", 2)),
      FORGOT_PASSWORD_SMS_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("ForgotPasswordSMSMessage", "Please reset your migme password using this code:%s. Ignore this message if you didn't request a password reset.")),
      EMAIL_CONTENT(new SystemPropertyEntities.SystemPropertyEntryString("EmailContent", "Hi %1,\n\nYou recently asked to reset your migme password. To complete your request, please follow this link: %2\n\nIf you did not request a new password, please ignore this email.\n\nThanks,\n\nThe migme Team")),
      EMAIL_SUBJECT(new SystemPropertyEntities.SystemPropertyEntryString("EmailSubject", "Reset your migme Password")),
      RESPONSE_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("ResponseRateLimit", "10/1M")),
      CHANGE_PASSWORD_URL(new SystemPropertyEntities.SystemPropertyEntryString("ChangePasswordUrl", "https://login.mig33.com/recoverpassword/%1/%2")),
      MIGME_CHANGE_PASSWORD_URL(new SystemPropertyEntities.SystemPropertyEntryString("MigmeChangePasswordUrl", "https://login.mig.me/recoverpassword/%1/%2")),
      THROW_CAPTCHA_ON_CHANGE_PASSWORD_RATE_LIMIT_HIT(new SystemPropertyEntities.SystemPropertyEntryBoolean("ThrowCaptchaOnChangePasswordRateLimitHit", false)),
      TOKEN_EXPIRATION_IN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("TokenExpirationInMillis", 69120000L));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "ForgotPassword";

      private ForgotPassword(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum FacebookConnect implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "FacebookConnect";

      private FacebookConnect(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Payments_MIMOPAY_ASYNCH_UPDATE implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", true)),
      SLEEP_MILLIS_AFTER_ONE_RECORD(new SystemPropertyEntities.SystemPropertyEntryInteger("SleepMillisAfterOneRecord", 5)),
      SLEEP_MILLIS_AFTER_ONE_BATCH(new SystemPropertyEntities.SystemPropertyEntryInteger("SleepMillisAfterOneBatch", 15000)),
      MAX_REC_COUNT_PER_FETCH(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxRecCountPerFetch", 1000)),
      MAX_PENDING_LIFE_SPAN_SEC(new SystemPropertyEntities.SystemPropertyEntryLong("MaxPendingLifeSpanSec", 604800L));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.Payments_MIMOPAY.NS, "AsynchUpdate");

      private Payments_MIMOPAY_ASYNCH_UPDATE(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Payments_PAYPAL_ASYNCH_UPDATE implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", true)),
      SLEEP_MILLIS_AFTER_ONE_RECORD(new SystemPropertyEntities.SystemPropertyEntryInteger("SleepMillisAfterOneRecord", 5)),
      SLEEP_MILLIS_AFTER_ONE_BATCH(new SystemPropertyEntities.SystemPropertyEntryInteger("SleepMillisAfterOneBatch", 15000)),
      MAX_REC_COUNT_PER_FETCH(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxRecCountPerFetch", 1000)),
      MAX_PENDING_LIFE_SPAN_SEC(new SystemPropertyEntities.SystemPropertyEntryLong("MaxPendingLifeSpanSec", 1200L));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.Payments_PAYPAL.NS, "AsynchUpdate");

      private Payments_PAYPAL_ASYNCH_UPDATE(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Payments_PAYPAL_RateLimit implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", false)),
      NAMESPACE(new SystemPropertyEntities.SystemPropertyEntryLong("NameSpace", 1L)),
      TOTAL_AMOUNT_USD_MIG33(new SystemPropertyEntities.SystemPropertyEntryDouble("TotalAmountUsdUser", 100.0D)),
      TOTAL_AMOUNT_USD_MIG33_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryDouble("TotalAmountUsdMerchant", 300.0D)),
      TOTAL_AMOUNT_USD_MIG33_TOP_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryDouble("TotalAmountUsdTopMerchant", 5000.0D)),
      TOTAL_AMOUNT_USD_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("TotalAmountUsdIntervalSeconds", 86400)),
      TOTAL_AMOUNT_USD_CACHE_EXPIRY_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("TotalAmountUsdCacheExpirySecs", 900)),
      TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_MIG33(new SystemPropertyEntities.SystemPropertyEntryDouble("TotalAmountAllTransactionsUsdUser", 1000.0D)),
      TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_MIG33_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryDouble("TotalAmountAllTransactionsUsdMerchant", 3000.0D)),
      TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_MIG33_TOP_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryDouble("TotalAmountAllTransactionsUsdTopMerchant", 20000.0D)),
      TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("TotalAmountAllTransactionsUsdIntervalSeconds", 86400)),
      TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_CACHE_EXPIRY_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("TotalAmountAllTransactionsUsdCacheExpirySecs", 900)),
      TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_MIG33(new SystemPropertyEntities.SystemPropertyEntryDouble("TotalAmountCountryTransactionsUsdUser", 500.0D)),
      TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_MIG33_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryDouble("TotalAmountCountryTransactionsUsdMerchant", 1000.0D)),
      TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_MIG33_TOP_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryDouble("TotalAmountCountryTransactionsUsdTopMerchant", 10000.0D)),
      TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("TotalAmountCountryTransactionsUsdIntervalSeconds", 86400)),
      TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_CACHE_EXPIRY_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("TotalAmountCountryTransactionsUsdCacheExpirySecs", 900)),
      NUMBER_TRANSACTIONS_MIG33(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberTransactionsUser", 5)),
      NUMBER_TRANSACTIONS_MIG33_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberTransactionsMerchant", 5)),
      NUMBER_TRANSACTIONS_MIG33_TOP_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberTransactionsTopMerchant", 10)),
      NUMBER_TRANSACTIONS_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberTransactionsIntervalSeconds", 86400000)),
      NUMBER_TRANSACTIONS_CACHE_EXPIRY_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberTransactionsCacheExpirySeconds", 900)),
      NUMBER_ALL_TRANSACTIONS_MIG33(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberAllTransactionsUser", 500)),
      NUMBER_ALL_TRANSACTIONS_MIG33_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberAllTransactionsMerchant", 500)),
      NUMBER_ALL_TRANSACTIONS_MIG33_TOP_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberAllTransactionsTopMerchant", 1000)),
      NUMBER_ALL_TRANSACTIONS_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberAllTransactionsIntervalSeconds", 86400000)),
      NUMBER_ALL_TRANSACTIONS_CACHE_EXPIRY_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberAllTransactionsExpirySecs", 900)),
      NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_MIG33(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberPaypalAccountTransactionsUser", 10)),
      NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_MIG33_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberPaypalAccountTransactionsMerchant", 10)),
      NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_MIG33_TOP_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberPaypalAccountTransactionsTopMerchant", 10)),
      NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberPaypalAccountTransactionsIntervalSeconds", 86400)),
      NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_CACHE_EXPIRY_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberPaypalAccountTransactionsExpirySecs", 900)),
      NUMBER_COUNTRY_TRANSACTIONS_MIG33(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberCountryTransactionsUser", 50)),
      NUMBER_COUNTRY_TRANSACTIONS_MIG33_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberCountryTransactionsMerchant", 50)),
      NUMBER_COUNTRY_TRANSACTIONS_MIG33_TOP_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberCountryTransactionsTopMerchant", 100)),
      NUMBER_COUNTRY_TRANSACTIONS_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberCountryTransactionsIntervalSeconds", 86400)),
      NUMBER_COUNTRY_TRANSACTIONS_CACHE_EXPIRY_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberCountryTransactionsCacheExpirySecs", 900)),
      STATS_QUERY_THRESHOLD_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("StatsQueryThresholdSecs", 2592000)),
      INITIATE_PAYMENT_ACCESS_RATELIMIT(new SystemPropertyEntities.SystemPropertyEntryString("InitiatePaymentRateLimit", "1/1S;10/1M"));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      public static final String NS = SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.Payments_PAYPAL.NS, "RateLimit");

      private Payments_PAYPAL_RateLimit(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Payments_PAYPAL implements SystemPropertyEntities.SystemPropertyEntryInterface {
      SUPPORTED_CURRENCIES(new SystemPropertyEntities.SystemPropertyEntryStringArray("SupportedCurrencies", new String[]{"AUD", "CAD", "EUR", "GBP", "JPY", "USD", "NZD", "CHF", "HKD", "SGD", "SEK", "DKK", "PLN", "NOK", "HUF", "CZK", "ILS", "MXN", "BRL", "MYR", "PHP", "TWD", "THB", "TRY"})),
      NON_TOP_MERCHANTS_ACCESS_ONLY_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("NonTopMerchantsAccessOnlyEnabled", false)),
      VENDOR_BASE_URL(new SystemPropertyEntities.SystemPropertyEntryString("VendorBaseURL", "")),
      SET_EXPRESS_CHECKOUT_URL(new SystemPropertyEntities.SystemPropertyEntryString("SetExpressCheckoutUrl", "")),
      API_VERSION(new SystemPropertyEntities.SystemPropertyEntryInteger("APIVersion", 95)),
      USERNAME(new SystemPropertyEntities.SystemPropertyEntryString("Username", "")),
      PASSWORD(new SystemPropertyEntities.SystemPropertyEntryString("Password", "")),
      SIGNATURE(new SystemPropertyEntities.SystemPropertyEntryString("Signature", "")),
      CANCEL_URL(new SystemPropertyEntities.SystemPropertyEntryString("CancelUrl", "")),
      RETURN_URL(new SystemPropertyEntities.SystemPropertyEntryString("ReturnUrl", "")),
      KEEP_ALIVE_IN_SEC(new SystemPropertyEntities.SystemPropertyEntryInteger("KeepAliveInSec", 60)),
      MAX_CONNECTIONS(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxConnections", 200)),
      STRICT_HTTPS_CERT_CHECK(new SystemPropertyEntities.SystemPropertyEntryBoolean("StrictHttpsCertCheck", true)),
      ENABLE_GATEWAY_REQUEST_RESPONSE_LOGGING(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableGatewayRequestReponseLogging", true)),
      TIME_OUT_IN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryInteger("TimeOutInMillis", 30000)),
      ENABLED_TO_ALL_USERS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledToAllUsers", false)),
      EXPRESS_CHECKOUT_SESSION_TIMEOUT_MILLIS(new SystemPropertyEntities.SystemPropertyEntryInteger("ExpressCheckoutSessionTimeoutMillis", 300000)),
      BLACKLIST_USERNAME(new SystemPropertyEntities.SystemPropertyEntryStringArray("BlacklistUsername", new String[0])),
      BLACKLIST_IP_ADDRESS(new SystemPropertyEntities.SystemPropertyEntryStringArray("BlacklistIpAddress", new String[0])),
      BLACKLIST_PAYPAL_ACCOUNT(new SystemPropertyEntities.SystemPropertyEntryStringArray("BlasklistPaypalAccount", new String[0])),
      BLACKLIST_COUNTRY(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("BlasklistCountry", new int[0])),
      WHITELIST_USERNAME(new SystemPropertyEntities.SystemPropertyEntryStringArray("WhitelistUsername", new String[0])),
      MIN_AMOUNT_USD_MIG33(new SystemPropertyEntities.SystemPropertyEntryDouble("MinAmountUSDUser", 0.5D)),
      MIN_AMOUNT_USD_MIG33_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryDouble("MinAmountUSDMerchant", 0.5D)),
      MIN_AMOUNT_USD_MIG33_TOP_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryDouble("MinAmountUSDTopMerchant", 0.5D)),
      MAX_AMOUNT_USD_MIG33(new SystemPropertyEntities.SystemPropertyEntryDouble("MaxAmountUSDUser", 20.0D)),
      MAX_AMOUNT_USD_MIG33_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryDouble("MaxAmountUSDMerchant", 100.0D)),
      MAX_AMOUNT_USD_MIG33_TOP_MERCHANT(new SystemPropertyEntities.SystemPropertyEntryDouble("MaxAmountUSDTopMerchant", 1000.0D)),
      DEFAULT_CURRENCY(new SystemPropertyEntities.SystemPropertyEntryString("DefaultCurrency", "USD")),
      CLIENT_DEFINED_RETURN_URLS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ClientDefinedReturnUrlsEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      public static final String NS = SystemPropertyEntities.getNameWithNamespace("Payments", "PAYPAL");

      private Payments_PAYPAL(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Payments_MIMOPAY implements SystemPropertyEntities.SystemPropertyEntryInterface {
      GATEWAY_USERNAME(new SystemPropertyEntities.SystemPropertyEntryString("GatewayUsername", "")),
      GATEWAY_PASSWORD(new SystemPropertyEntities.SystemPropertyEntryString("GatewayPassword", "")),
      GATEWAY_SHARED_SECRET(new SystemPropertyEntities.SystemPropertyEntryString("GatewaySharedSecret", "")),
      MERCHANT_CODE(new SystemPropertyEntities.SystemPropertyEntryString("MerchantCode", "")),
      GAME_CODE(new SystemPropertyEntities.SystemPropertyEntryString("GameCode", "")),
      CREDIT_RELOAD_PTYPE(new SystemPropertyEntities.SystemPropertyEntryString("CreditReloadPType", "")),
      VENDOR_BASE_URL(new SystemPropertyEntities.SystemPropertyEntryString("VendorBaseURL", "")),
      CREDIT_RELOAD_PATH(new SystemPropertyEntities.SystemPropertyEntryString("CreditReloadPath", "")),
      QUERY_TRANSACTION_STATUS_PATH(new SystemPropertyEntities.SystemPropertyEntryString("QueryTransactionStatusPath", "")),
      RELOAD_VALUE_CURRENY(new SystemPropertyEntities.SystemPropertyEntryString("ReloadValueCurrency", "IDR")),
      SUPPORTED_CURRENCIES(new SystemPropertyEntities.SystemPropertyEntryStringArray("SupportedCurrencies", new String[]{"IDR"})),
      DEFAULT_CURRENCY(new SystemPropertyEntities.SystemPropertyEntryString("DefaultCurrency", "USD")),
      NON_TOP_MERCHANTS_ACCESS_ONLY_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("NonTopMerchantsAccessOnlyEnabled", true)),
      KEEP_ALIVE_IN_SEC(new SystemPropertyEntities.SystemPropertyEntryInteger("KeepAliveInSec", 60)),
      MAX_CONNECTIONS(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxConnections", 200)),
      STRICT_HTTPS_CERT_CHECK(new SystemPropertyEntities.SystemPropertyEntryBoolean("StrictHttpsCertCheck", true)),
      TIME_OUT_IN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryInteger("TimeOutInMillis", 30000)),
      MAXIMUM_PENDING_PAYMENTS_COUNT(new SystemPropertyEntities.SystemPropertyEntryInteger("MaximumPendingPaymentsCount", 0)),
      ENABLE_RELOAD_KEY_VALIDATION(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableReloadKeyValidation", true)),
      EXPECTED_RELOAD_KEY_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("ExpectedReloadKeyLength", 16)),
      ENABLED_TO_ALL_USERS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledToAllUsers", false)),
      ENABLE_GATEWAY_REQUEST_RESPONSE_LOGGING(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableGatewayRequestResponseLogging", true)),
      RELOAD_KEY_RECYCLE_PERIOD_DAYS(new SystemPropertyEntities.SystemPropertyEntryInteger("ReloadKeyRecyclePeriodDays", 730)),
      ONLY_FOR_INDONESIAN_USERS(new SystemPropertyEntities.SystemPropertyEntryBoolean("OnlyForIndonesianUsers", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      public static final String NS = SystemPropertyEntities.getNameWithNamespace("Payments", "MIMOPAY");

      private Payments_MIMOPAY(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static class MessageStatusEventSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      public static final SystemPropertyEntities.MessageStatusEventSettings MESSAGE_STATUS_EVENTS_ENABLED = new SystemPropertyEntities.MessageStatusEventSettings(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", false));
      public static final SystemPropertyEntities.MessageStatusEventSettings RATE_LIMITING_ENABLED = new SystemPropertyEntities.MessageStatusEventSettings(new SystemPropertyEntities.SystemPropertyEntryBoolean("RateLimitingEnabled", true));
      public static final SystemPropertyEntities.MessageStatusEventSettings MIN_GUID_CHARS = new SystemPropertyEntities.MessageStatusEventSettings(new SystemPropertyEntities.SystemPropertyEntryInteger("MinGUIDChars", 4));
      public static final SystemPropertyEntities.MessageStatusEventSettings MAX_GUID_CHARS = new SystemPropertyEntities.MessageStatusEventSettings(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxGUIDChars", 128));
      public static final SystemPropertyEntities.MessageStatusEventSettings OTHER_CLIENTS_RX_ENABLED = new SystemPropertyEntities.MessageStatusEventSettings(new SystemPropertyEntities.SystemPropertyEntryBoolean("ReceiveFromOtherClientsEnabled", false));
      public static final SystemPropertyEntities.MessageStatusEventSettings OTHER_CLIENTS_TX_ENABLED = new SystemPropertyEntities.MessageStatusEventSettings(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendToOtherClientsEnabled", false));
      public static final SystemPropertyEntities.MessageStatusEventSettings MAX_GET_MESSAGE_STATUS_EVENTS_REQUESTS_PER_MINUTE = new SystemPropertyEntities.MessageStatusEventSettings(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxGetMessageStatusEventsRequestsPerMinute", 10000));
      private static final SystemPropertyEntities.MessageStatusEventSettings.MinVersionLookup rxFromLookup = new SystemPropertyEntities.MessageStatusEventSettings.MinVersionLookup("ReceiveFrom");
      private static final SystemPropertyEntities.MessageStatusEventSettings.MinVersionLookup sendToLookup = new SystemPropertyEntities.MessageStatusEventSettings.MinVersionLookup("SendTo");
      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "MsgStatusEvent";

      public static SystemPropertyEntities.MessageStatusEventSettings getReceiveFromMinVersionProp(ClientType device) {
         return rxFromLookup.getProp(device);
      }

      public static SystemPropertyEntities.MessageStatusEventSettings getSendToMinVersionProp(ClientType device) {
         return sendToLookup.getProp(device);
      }

      public static Short getReceiveFromMinVersion(ClientType device) {
         return (Short)((Map)rxFromLookup.getValue()).get(device);
      }

      public static Short getSendToMinVersion(ClientType device) {
         return (Short)((Map)sendToLookup.getValue()).get(device);
      }

      MessageStatusEventSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }

      private static class MinVersionLookup extends LazyLoader<Map<ClientType, Short>> {
         private final String propPrefix;
         private final Map<ClientType, Short> map = new HashMap();

         public MinVersionLookup(String propPrefix) {
            super(propPrefix + "_LOOKUP", 60000L);
            this.propPrefix = propPrefix;
         }

         public SystemPropertyEntities.MessageStatusEventSettings getProp(ClientType device) {
            String propName = this.propPrefix + device.toString() + "MinVersion";
            return new SystemPropertyEntities.MessageStatusEventSettings(new SystemPropertyEntities.SystemPropertyEntryInteger(propName, Integer.MAX_VALUE));
         }

         protected Map<ClientType, Short> fetchValue() throws Exception {
            this.map.clear();
            ClientType[] arr$ = ClientType.values();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               ClientType device = arr$[i$];
               int minVersion = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)this.getProp(device));
               if (minVersion != Integer.MAX_VALUE) {
                  this.map.put(device, (short)minVersion);
               }
            }

            return this.map;
         }
      }

      public static class Cache {
         public static LazyLoader<Boolean> enabled = new LazyLoader<Boolean>("MESSAGE_STATUS_EVENTS_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MessageStatusEventSettings.MESSAGE_STATUS_EVENTS_ENABLED);
            }
         };
      }
   }

   public static enum Payments implements SystemPropertyEntities.SystemPropertyEntryInterface, EnumUtils.IEnumValueGetter<String> {
      MIG33_AJAX_ENABLED_VENDORS(new SystemPropertyEntities.SystemPropertyEntryStringArray("MIG33_AJAX_EnabledVendors", new String[0])),
      WAP_ENABLED_VENDORS(new SystemPropertyEntities.SystemPropertyEntryStringArray("WAP_EnabledVendors", new String[0])),
      MIDLET_ENABLED_VENDORS(new SystemPropertyEntities.SystemPropertyEntryStringArray("MIDLET_EnabledVendors", new String[0])),
      MTK_MRE_ENABLED_VENDORS(new SystemPropertyEntities.SystemPropertyEntryStringArray("MTK_MRE_EnabledVendors", new String[0])),
      TOUCH_ENABLED_VENDORS(new SystemPropertyEntities.SystemPropertyEntryStringArray("TOUCH_EnabledVendors", new String[0])),
      BLACKBERRY_ENABLED_VENDORS(new SystemPropertyEntities.SystemPropertyEntryStringArray("BLACKBERRY_EnabledVendors", new String[0])),
      IOS_ENABLED_VENDORS(new SystemPropertyEntities.SystemPropertyEntryStringArray("IOS_EnabledVendors", new String[0])),
      MIG33_CORPORATE_ENABLED_VENDORS(new SystemPropertyEntities.SystemPropertyEntryStringArray("MIG33_CORPORATE_EnabledVendors", new String[0])),
      MIN_AMOUNT(new SystemPropertyEntities.SystemPropertyEntryDouble("MinAmount", 0.0D)),
      BONUS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("StrictAmountCheckEnabled", false)),
      DEFAULT_CURRENCY(new SystemPropertyEntities.SystemPropertyEntryString("DefaultCurrency", "USD")),
      DEFAULT_RATE_LIMIT_CURRENCY(new SystemPropertyEntities.SystemPropertyEntryString("DefaultRateLimitCurrency", "USD")),
      THIRD_PARTY_PAYMENT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ThirdPartyPaymentEnabled", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      public static final String NS = "Payments";
      private static HashMap<String, SystemPropertyEntities.Payments> lookupMap = new HashMap();

      private Payments(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("Payments", this.value.getName());
      }

      public String getEnumValue() {
         return this.getName().trim().toUpperCase();
      }

      public static SystemPropertyEntities.Payments getSystemPropertyKeyForEnabledVendors(String viewTypeName) {
         return (SystemPropertyEntities.Payments)lookupMap.get(SystemPropertyEntities.getNameWithNamespace("Payments", viewTypeName.trim() + "_EnabledVendors").toUpperCase());
      }

      static {
         EnumUtils.populateLookUpMap(lookupMap, SystemPropertyEntities.Payments.class);
      }
   }

   public static enum Payments_MOL implements SystemPropertyEntities.SystemPropertyEntryInterface {
      MERCHANT_ID(new SystemPropertyEntities.SystemPropertyEntryString("MerchantID", "201210090174")),
      MERCHANT_PIN(new SystemPropertyEntities.SystemPropertyEntryString("MerchantPIN", "")),
      VENDOR_BASE_URL(new SystemPropertyEntities.SystemPropertyEntryString("VendorBaseURL", "")),
      HEARTBEAT_PATH(new SystemPropertyEntities.SystemPropertyEntryString("HeartbeatPath", "")),
      PURCHASE_PATH(new SystemPropertyEntities.SystemPropertyEntryString("PurchasePath", "")),
      MIN_AMOUNT(new SystemPropertyEntities.SystemPropertyEntryDouble("MinAmount", 0.0D)),
      QUERY_TRANSACTION_STATUS_PATH(new SystemPropertyEntities.SystemPropertyEntryString("QueryTransactionStatusPath", "")),
      MAX_AMOUNT(new SystemPropertyEntities.SystemPropertyEntryDouble("MaxAmount", 1000.0D)),
      HEARTBEAT_URL(new SystemPropertyEntities.SystemPropertyEntryString("HeartbeatUrl", "/api/login/s_module/heartbeat.asmx/GetHeartBeat")),
      PURCHASE_URL(new SystemPropertyEntities.SystemPropertyEntryString("PurchaseUrl", "/api/login/s_module/heartbeat.asmx/GetHeartBeat")),
      SUPPORTED_CURRENCIES(new SystemPropertyEntities.SystemPropertyEntryStringArray("SupportedCurrencies", new String[]{"USD", "MYR", "SGD", "INR", "PHP", "IDR", "THB"})),
      DEFAULT_CURRENCY(new SystemPropertyEntities.SystemPropertyEntryString("DefaultCurrency", "USD")),
      NON_TOP_MERCHANTS_ACCESS_ONLY_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("NonTopMerchantsAccessOnlyEnabled", true)),
      KEEP_ALIVE_IN_SEC(new SystemPropertyEntities.SystemPropertyEntryInteger("KeepAliveInSec", 60)),
      MAX_CONNECTIONS(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxConnections", 200)),
      STRICT_HTTPS_CERT_CHECK(new SystemPropertyEntities.SystemPropertyEntryBoolean("StrictHttpsCertCheck", true)),
      TIME_OUT_IN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryInteger("TimeOutInMillis", 30000)),
      MAXIMUM_PENDING_PAYMENTS_COUNT(new SystemPropertyEntities.SystemPropertyEntryInteger("MaximumPendingPaymentsCount", 0)),
      ENABLED_TO_ALL_USERS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledToAllUsers", false)),
      ENABLE_STRICT_AMOUNT_CHECK(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableStrictAmountCheck", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      public static final String NS = SystemPropertyEntities.getNameWithNamespace("Payments", "MOL");

      private Payments_MOL(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Payments_CREDITCARD implements SystemPropertyEntities.SystemPropertyEntryInterface {
      HML_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", true)),
      HML_GUARDSET_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("HMLGuardSetEnabled", true)),
      HML_CALLBACK_URL(new SystemPropertyEntities.SystemPropertyEntryString("HMLCallBackUrl", "")),
      DEFAULT_CURRENCY(new SystemPropertyEntities.SystemPropertyEntryString("DefaultCurrency", "USD")),
      NON_HML_CC_PAYMENT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("NonHmlCcPaymentEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      public static final String NS = SystemPropertyEntities.getNameWithNamespace("Payments", "CREDITCARD");

      private Payments_CREDITCARD(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Payments_MOL_ASYNCH_UPDATE implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", true)),
      SLEEP_MILLIS_AFTER_ONE_RECORD(new SystemPropertyEntities.SystemPropertyEntryInteger("SleepMillisAfterOneRecord", 5)),
      SLEEP_MILLIS_AFTER_ONE_BATCH(new SystemPropertyEntities.SystemPropertyEntryInteger("SleepMillisAfterOneBatch", 15000)),
      MAX_REC_COUNT_PER_FETCH(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxRecCountPerFetch", 1000)),
      MAX_PENDING_LIFE_SPAN_SEC(new SystemPropertyEntities.SystemPropertyEntryLong("MaxPendingLifeSpanSec", 1800L));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.Payments_MOL.NS, "AsynchUpdate");

      private Payments_MOL_ASYNCH_UPDATE(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum OfflineMessageSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      OLD_STYLE_OLM_STORAGE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("OldStyleOLMStorageEnabled", false)),
      OFFLINE_V2_EMOTES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("OfflineV2EmotesEnabled", false)),
      SEND_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendEnabled", false)),
      PUSH_TO_MIGBO_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PushToMigboEnabled", false)),
      CHECK_YOUR_MIGBO_MSGS_ALERT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CheckYourMigboMessagesAlertEnabled", false)),
      STATS_COLLECTION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("StatsCollectionEnabled", false)),
      SENDING_TO_NON_FRIENDS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendingToNonFriendsEnabled", false)),
      REFRESH_THREAD_POOL_PROPERTIES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RefreshThreadPoolPropertiesEnabled", false)),
      OFFLINE_MESSAGE_GUARDSET_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("OfflineMessageGuardsetEnabled", true)),
      MSG_STORED_CONF_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MessageStoredConfirmationEnabled", false)),
      RATE_LIMITING_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RateLimitingEnabled", false)),
      CHECK_FOR_DEACTIVATED_RECIPIENT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CheckForDeactivatedRecipientEnabled", false)),
      MIN_MIG_LEVEL_FOR_SENDING(new SystemPropertyEntities.SystemPropertyEntryInteger("MinMigLevelForSending", 0)),
      MIN_MIG_LEVEL_FOR_RECEIVING(new SystemPropertyEntities.SystemPropertyEntryInteger("MinMigLevelForReceiving", 0)),
      RX_LIMIT_ANDROID_3PLUS(new SystemPropertyEntities.SystemPropertyEntryInteger("ReceiveLimitAndroid3Plus", 30)),
      RX_LIMIT_BLACKBERRY(new SystemPropertyEntities.SystemPropertyEntryInteger("ReceiveLimitBlackberry", 50)),
      RX_LIMIT_J2ME(new SystemPropertyEntities.SystemPropertyEntryInteger("ReceiveLimitJ2ME", 10)),
      RX_LIMIT_MRE(new SystemPropertyEntities.SystemPropertyEntryInteger("ReceiveLimitMRE", 5)),
      RX_LIMIT_DEFAULT(new SystemPropertyEntities.SystemPropertyEntryInteger("ReceiveLimitDefault", 10)),
      EXPIRY_DAYS(new SystemPropertyEntities.SystemPropertyEntryInteger("ExpiryDays", 30)),
      TPOOL_CORE_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadPoolCoreSize", 100)),
      TPOOL_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadPoolMaxSize", 200)),
      TPOOL_KEEP_ALIVE_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadPoolKeepAliveSeconds", 0)),
      TPOOL_TASK_QUEUE_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadPoolTaskQueueMaxSize", 5000)),
      TPOOL_PROPS_REFRESH_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadPoolPropsRefreshIntervalSeconds", 300)),
      STORAGE_TIMEOUT_MILLIS(new SystemPropertyEntities.SystemPropertyEntryInteger("StorageTimeoutMillis", 10000)),
      TX_DAILY_LIMIT_PER_USER(new SystemPropertyEntities.SystemPropertyEntryInteger("SendDailyLimit", 2000)),
      TX_DAILY_LIMIT_PER_USER_PAIR(new SystemPropertyEntities.SystemPropertyEntryInteger("SendDailyLimitPerUserPair", 200)),
      RX_DAILY_LIMIT_PER_USER(new SystemPropertyEntities.SystemPropertyEntryInteger("ReceiveDailyLimit", 2000)),
      TX_DAILY_LIMIT_PER_SUSPECTED_ABUSER(new SystemPropertyEntities.SystemPropertyEntryInteger("SendDailyLimitPerSuspectedAbuser", 5)),
      THREAD_LENGTH_LIMIT(new SystemPropertyEntities.SystemPropertyEntryInteger("ThreadLengthLimit", 50)),
      STATS_COLLECTION_INTERVAL_MINUTES(new SystemPropertyEntities.SystemPropertyEntryInteger("StatsCollectionIntervalMinutes", 5));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "OfflineMsg";

      private OfflineMessageSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum MerchantTagSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      GET_MERCHANT_TAG_DETAILS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GetMerchantTagDetailsEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "MerchantTagSettings";

      private MerchantTagSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum UserProfileSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      GET_ADMIN_LABELS_VERSION_1(new SystemPropertyEntities.SystemPropertyEntryBoolean("GetAdminLabelsVersion1", false)),
      GET_USER_MERCHANT_LABELS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GetUserMerchantLabelsEnabled", false)),
      GET_USER_LABELS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GetUserLabelsEnabled", false)),
      DO_SYNCHRONOUS_PROFILE_CACHE_INVALIDATION_ON_MIGBO_UPON_PROFILE_SETTING_UPDATE(new SystemPropertyEntities.SystemPropertyEntryBoolean("DoSyncMigboProfileCacheInvalidationUponProfileSettingUpdate", false)),
      FAIL_PROFILE_SETTING_UPDATE_IF_FAIL_DO_SYNCHRONOUS_PROFILE_CACHE_INVALIDATION_ON_MIGBO(new SystemPropertyEntities.SystemPropertyEntryBoolean("FailProfileSettingUpdateIfFailDoSyncMigboProfileCacheInvalidation", false)),
      DO_SYNCHRONOUS_PRIVACY_CACHE_INVALIDATION_ON_MIGBO_UPON_COMM_SETTING_UPDATE(new SystemPropertyEntities.SystemPropertyEntryBoolean("DoSyncMigboPrivacyCacheInvalidationUponCommSettingUpdate", false)),
      FAIL_COMM_SETTING_UPDATE_IF_FAIL_DO_SYNCHRONOUS_PRIVACY_CACHE_INVALIDATION_ON_MIGBO(new SystemPropertyEntities.SystemPropertyEntryBoolean("FailCommSettingUpdateIfFailDoSyncMigboPrivacyCacheInvalidation", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "UserProfileSettings";

      private UserProfileSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum ObjectCacheLoadBalancing implements SystemPropertyEntities.SystemPropertyEntryInterface {
      FILTER_MASK(new SystemPropertyEntities.SystemPropertyEntryInteger("FilterMask", 1)),
      GC_MULTIPLIER(new SystemPropertyEntities.SystemPropertyEntryDouble("GCMultiplier", 1.0D)),
      CHATROOM_MULTIPLIER(new SystemPropertyEntities.SystemPropertyEntryDouble("ChatRoomMultiplier", 1.0D)),
      USER_OBJECT_MULTIPLIER(new SystemPropertyEntities.SystemPropertyEntryDouble("UserObjectMultiplier", 1.0D)),
      SESSION_OBJECT_MULTIPLIER(new SystemPropertyEntities.SystemPropertyEntryDouble("SessionObjectMultiplier", 1.0D)),
      MEMORY_UTILIZATION_MULTIPLIER(new SystemPropertyEntities.SystemPropertyEntryDouble("MemoryUtilizationMultiplier", 100.0D)),
      OLDEST_ADMISSIBLE_STATS_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("OldestAdmissibleStatsSeconds", 300));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "ObjectCacheLoadBalancing";

      private ObjectCacheLoadBalancing(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum AccountTransaction implements SystemPropertyEntities.SystemPropertyEntryInterface {
      AUTO_APPROVE_CREDIT_CARD_PAYMENT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CreditCardAutoApproveEnabled", false)),
      MAX_NEW_USER_CC_PURCHASE_USD(new SystemPropertyEntities.SystemPropertyEntryDouble("MaxNewUserPurchaseUsd", 20.0D)),
      NEW_USER_THRESHOLD_HOURS(new SystemPropertyEntities.SystemPropertyEntryInteger("NewUserThresholdHours", 48)),
      GLOBAL_COLLECT_URL(new SystemPropertyEntities.SystemPropertyEntryString("GlobalCollectURL", "")),
      AUTO_APPROVE_CREDIT_CARD_RISKY_COUNTRIES(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("AutoApproveCreditCardRiskyCountries", new int[0])),
      AUTO_CREDIT_CARD_RECHARGE_BONUS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("AutoCreditCardRechargeBonusEnabled", false)),
      CREDIT_CARD_RECHARGE_BONUS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CreditCardRechargeBonusEnabled", false)),
      CREDIT_CARD_PAYMENT_STRICT_SSL_CHECK(new SystemPropertyEntities.SystemPropertyEntryBoolean("CreditCardPaymentStrictSslCheck", true)),
      CREDIT_CARD_PAYMENT_STRICT_CC_COUNTRY_CHECK(new SystemPropertyEntities.SystemPropertyEntryBoolean("CreditCardPaymentStrictCcCountryCheck", true)),
      CREDIT_CARD_MAXIMUM_AUTO_APPROVE_AMOUNT_USD(new SystemPropertyEntities.SystemPropertyEntryDouble("CreditCardMaximumAutoApproveAmountUsd", 50.0D)),
      CREDIT_CARD_FIRST_NAME_LAST_NAME_CHECK_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CreditCardFirstNameLastNameCheckEnabled", false)),
      TRANSACTION_TYPES_FOR_MIN_BALANCE_CALC(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("TransactionTypesForMinBalanceCalc", new int[]{AccountEntryData.TypeEnum.MARKETING_REWARD.value(), AccountEntryData.TypeEnum.REFERRAL_CREDIT.value(), AccountEntryData.TypeEnum.ACTIVATION_CREDIT.value(), AccountEntryData.TypeEnum.SMS_CHARGE.value(), AccountEntryData.TypeEnum.CALL_CHARGE.value(), AccountEntryData.TypeEnum.SUBSCRIPTION.value(), AccountEntryData.TypeEnum.CHATROOM_KICK_CHARGE.value(), AccountEntryData.TypeEnum.CREDIT_EXPIRED.value(), AccountEntryData.TypeEnum.EMOTICON_PURCHASE.value(), AccountEntryData.TypeEnum.AVATAR_PURCHASE.value(), AccountEntryData.TypeEnum.GAME_ITEM_PURCHASE.value(), AccountEntryData.TypeEnum.GAME_REWARD.value(), AccountEntryData.TypeEnum.THIRD_PARTY_API_DEBIT.value(), AccountEntryData.TypeEnum.VIRTUAL_GIFT_PURCHASE.value(), AccountEntryData.TypeEnum.EMOTE_PURCHASE.value()})),
      MIN_BALANCE_MAX_LOOK_BACK_DAYS(new SystemPropertyEntities.SystemPropertyEntryInteger("MinBalanceMaxLookBackDays", 90)),
      ENABLE_MIN_BALANCE_CALC_ON_TRANSFER(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableMinBalanceCalcOnTransfer", false)),
      ENABLE_MIN_BALANCE_CALC_ON_POT_GAMES(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableMinBalanceCalcOnPotGames", false)),
      USE_NESTED_QUERY_FOR_MERCHANT_TAG_RETRIEVAL(new SystemPropertyEntities.SystemPropertyEntryBoolean("UseNestedQueryForMerchantTagRetrieval", false)),
      ENABLE_REVERSAL_TRANSFER_BIGINT_AE_ID(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableReversalTransferBigIntAEID", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "AccountTransaction";

      private AccountTransaction(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum AccountTransaction_CreditTransfer implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLE_MIG_LEVEL_CHECK(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableMigLevelCheck", false)),
      SKIP_FETCH_CACHED_REPUTATION_SCORE(new SystemPropertyEntities.SystemPropertyEntryBoolean("SkipFetchCachedReputationScore", true)),
      MIN_MIG_LEVEL(new SystemPropertyEntities.SystemPropertyEntryInteger("MinMigLevel", 10)),
      USER_TYPES_TO_APPLY_MIG_LEVEL_CHECK(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("UserTypesToApplyMigLevelCheck", new int[]{UserData.TypeEnum.MIG33.value()})),
      USERIDS_EXEMPTED_FROM_MIG_LEVEL_CHECK(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("UserIDSExemptedFromMigLevelChecks", new int[0])),
      ENABLE_MIG_LEVEL_CHECK_FOR_ACCOUNT_VERIFIED_USER(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableMigLevelCheckForAccountVerified", false)),
      INSUFFICIENT_MIG_LEVEL_ERROR_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("InsufficientMigLevelErrorMessage", "Hi, using migCredits wisely will help you grow migLevel faster! You can transfer migCredits after reaching level 10.")),
      LOG_SENDERS_PASSING_TRANSFER_ENTITLEMENT_CHECKS(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogSendersPassingTransferEntitlementChecks", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "AccountTransaction:CreditTransfer";

      private AccountTransaction_CreditTransfer(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum AccountEntry implements SystemPropertyEntities.SystemPropertyEntryInterface {
      MAX_DESC_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxDescLength", 128)),
      BANNED_CREDIT_SENDERS(new SystemPropertyEntities.SystemPropertyEntryStringArray("BannedCreditSenders", new String[]{"loadtest"}));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "AccountEntry";

      private AccountEntry(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum EJBCacheDuration implements SystemPropertyEntities.SystemPropertyEntryInterface {
      MIS_CLIENT_TEXT("ClientText"),
      MIS_CURRENCIES("Currencies"),
      MIS_COUNTRIES("Countries"),
      MIS_MIDLET_MENU_ITEMS("MenuItems"),
      CONTENT_EMOTICONS("Emoticons"),
      CONTENT_EMOTICONPACKS("EmoticonPacks"),
      CONTENT_EMOTICONHOTKEYS("EmoticonHotkeys");

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "EJBCacheDuration";

      private EJBCacheDuration(String entryName, Long duration) {
         this.value = new SystemPropertyEntities.SystemPropertyEntryLong(entryName, duration);
      }

      private EJBCacheDuration(String entryName) {
         this(entryName, new Long(300L));
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Invitation implements SystemPropertyEntities.SystemPropertyEntryInterface {
      REFERRAL_INVITATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ReferralInvitationEnabled", false)),
      FACEBOOK_INVITATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("FacebookInvitationEnabled", false)),
      INTERNAL_INVITATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("InternalInvitationEnabled", false)),
      REFERRAL_INVITATION_CIPHER_PASSPHRASE(new SystemPropertyEntities.SystemPropertyEntryString("ReferralInvitationCipherPassPhrase", "mig.33~2012$12#11")),
      REFERRAL_EXPIRE_PERIOD_IN_DAY(new SystemPropertyEntities.SystemPropertyEntryInteger("ReferralExpirePeriodInDay", 30)),
      TRACKING_UNVERIFIED_SIGN_UP_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("TrackingUnverifiedSignUpEnabled", true)),
      MARKETING_MECHANICS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MarketingMechanicsEnabled", true)),
      MARKETING_MECHANICS_CHAIN_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MarketingMechanicsChainEnabled", false)),
      MARKETING_MECHANICS_FOR_REFERRAL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MarketingMechanicsForReferralEnabled", true)),
      MARKETING_MECHANICS_FOR_ACCEPT_INVITATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MarketingMechanicsForAcceptInvitationEnabled", false)),
      MARKETING_MECHANICS_FOR_LOGIN_USING_EXISTING_ACCOUNT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MarketingMechanicsForLoginUsingExistingAccountEnabled", false)),
      REFERRAL_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("ReferralRateLimit", "10/1D")),
      FACEBOOK_REFERRAL_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("FacebookReferralRateLimit", "50/1D")),
      INTERNAL_REFERRAL_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("InternalReferralRateLimit", "50/1D")),
      GUARDSET_FOR_REFERRAL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GuardsetForReferralEnabled", true)),
      ENABLE_MUTUAL_FOLLOWING_FOR_ALL_INVITERS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableMutualFollowingForAllInviters", true)),
      ENABLE_LOG_INVITATION_RESPONSE_FOR_ALL_INVITERS_OF_COLLAPSE_ALERT(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableLogInvitationResponseFprAllInvitersOfCollapseAlert", true)),
      ENABLE_GET_INVITER_FROM_MOBILE_REFERRER(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableGetInviterFromMobileReferrer", true)),
      ENABLE_REFERRAL_ACK_EMAIL(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableReferralAckEmail", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Invitation";

      private Invitation(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Email implements SystemPropertyEntities.SystemPropertyEntryInterface {
      MAX_RETRIEVE_COUNT(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxRetrieveCount", 5000)),
      TIME_SCOPE_FOR_DAILY_DIGEST_TO_USERS_BEING_FOLLOWERED_IN_HOUR(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxRetrieveCount", 24)),
      ENABLE_EXTERNAL_EMAIL_VERIFIED_EVENT_TRIGGER(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableExternalEmailVerifiedEventTrigger", true)),
      ENSURE_TRANSACTED_USER_EMAIL_ADDRESS_UPDATE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnsureTransactedUserEmailAddressUpdate", true)),
      /** @deprecated */
      @Deprecated
      ENABLE_UPDATE_EMAIL_ADDRESS_FIELD_ON_USER_TABLE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableUpdateEmailAddressFieldOnUserTable", false)),
      /** @deprecated */
      @Deprecated
      THROW_ON_FAILED_UPDATE_EMAIL_ADDRESS_FIELD_ON_USER_TABLE(new SystemPropertyEntities.SystemPropertyEntryBoolean("ThrowOnFailedUpdateEmailAddressFieldOnUserTable", false)),
      SEND_EMAIL_VERIFICATION_AFTER_EMAIL_ADDRESS_UPDATE(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendEmailVerificationAfterEmailAddressUpdate", true)),
      ENABLE_USER_ID_CHECK_WHEN_DEREFERENCING_TOKENS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableUserIDCheckWhenDereferencingTokens", false)),
      ENABLED_EMAIL_ADDRESS_VRIFICATION_WITH_TEMPLATE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEmailAddressVerificationWithTemplate", true)),
      EMAIL_ADDRESS_VRIFICATION_TEMPLATE_ID(new SystemPropertyEntities.SystemPropertyEntryInteger("EmailAddressVerificationTemplateID", 103)),
      EXTERNAL_EMAIL_ADDRESS_VERIFICATION_LINK(new SystemPropertyEntities.SystemPropertyEntryString("ExternalEmailAddressVerificationLink", "http://www.mig.me/sites/ajax/settings/account_email_verify?token=")),
      ENABLED_FORGOT_USERNAME_EMAIL_WITH_TEMPLATE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledForgotUsernameEmailWithTemplate", true)),
      FORGOT_USERNAME_EMAIL_TEMPLATE_ID(new SystemPropertyEntities.SystemPropertyEntryInteger("ForgotUsernameEmailTemplateID", 104)),
      ENABLED_FORGOT_PASSWORD_EMAIL_WITH_TEMPLATE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledForgotPasswordEmailWithTemplate", true)),
      FORGOT_PASSWORD_EMAIL_TEMPLATE_ID(new SystemPropertyEntities.SystemPropertyEntryInteger("ForgotPasswordEmailTemplateID", 105)),
      ENABLED_SEND_VERIFICATION_TOKEN_WITH_TEMPLATE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledSendVerificationTokenWithTemplate", true)),
      PASS_THROUGH_EMAIL_TEMPLATE_IDS(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("PassThroughEmailTemplateIds", new int[]{103, 104, 105})),
      EMAIL_BASIC_CHECKS_REGEX(new SystemPropertyEntities.SystemPropertyEntryString("EmailBasicChecksRegex", "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@([A-Za-z0-9-]+\\.)+([A-Za-z]{2,})$"));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "EMAIL";

      private Email(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Alert implements SystemPropertyEntities.SystemPropertyEntryInterface {
      MIGBO_SYS_ALERTS_FOR_NEW_USER(new SystemPropertyEntities.SystemPropertyEntryString("MigboSysAlertsForNewUser", "")),
      PT72238252_MIGBO_PERSISTENT_ALERTS_EXCLUDE_EXPIRE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PT72238252MigboPersistentAlertsExludeExpireEnabled", true)),
      MIGBO_SYS_ALERTS_FOR_NEW_USER_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MigboSysAlertsForNewUserEnabled", false)),
      MIGBO_SYS_ALERTS_FOR_INVITER_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MigboSysAlertsForInviterEnabled", false)),
      MIGBO_SYS_ALERTS_FOR_INVITER_ENABLED_WHEN_INVITEE_ACCEPT_INVITATION(new SystemPropertyEntities.SystemPropertyEntryBoolean("MigboSysAlertsForInviterEnabledWhenInviteeAcceptInvitation", false)),
      ENABLED_MANDATORY_RESET_UNREAD_COUNT(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledMandatoryResetUnreadCount", true)),
      MANDATORY_RESET_UNREAD_COUNT_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("MandatoryResetUnreadCountRateLimit", "1/1M"));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Alert";

      private Alert(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Migbo implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ELASTIC_SEARCH_DISABLED(new SystemPropertyEntities.SystemPropertyEntryInteger("ElasticSearchDisabled", 0)),
      MIGBO_DISABLED(new SystemPropertyEntities.SystemPropertyEntryInteger("MigboDisabled", 0)),
      WATCHLIST_DISABLED(new SystemPropertyEntities.SystemPropertyEntryInteger("WatchListDisabled", 1)),
      ONEWAY_MIGBO_API_CALLS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("OneWayMigboApiCallsEnabled", true)),
      VALHALLA_MIGBO_URL(new SystemPropertyEntities.SystemPropertyEntryString("ValhallaMigboURL", "http://v.mig33.com")),
      VALHALLA_MIGBO_DATASVC_HOST(new SystemPropertyEntities.SystemPropertyEntryString("ValhallaMigboDataSvcHost", "v-api.mig33.com")),
      VALHALLA_MIGBO_DATASVC_PORT(new SystemPropertyEntities.SystemPropertyEntryInteger("ValhallaMigboDataSvcPort", 8080)),
      VALHALLA_MIGBO_DATASVC_USEHTTPS(new SystemPropertyEntities.SystemPropertyEntryBoolean("ValhallaMigboDataSvcUseHttps", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Migbo";

      private Migbo(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Emote implements SystemPropertyEntities.SystemPropertyEntryInterface {
      BAN_INDEX_MAX(new SystemPropertyEntities.SystemPropertyEntryInteger("BanIndexMax", 5)),
      BAN_INDEX_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("BanIndexRateLimit", "15/1M")),
      KICK_INDEX_MAX(new SystemPropertyEntities.SystemPropertyEntryInteger("KickIndexMax", 5)),
      KICK_INDEX_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("KickIndexRateLimit", "15/1M")),
      LIST_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("ListRateLimit", "15/1M")),
      FOLLOW_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("FollowRateLimit", "10/1M")),
      UNFOLLOW_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("UnfollowRateLimit", "10/1M")),
      BUMP_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("BumpRateLimit", "10/1M")),
      WARN_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("WarnRateLimit", "10/1M")),
      ADD_MODERATOR_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("AddModeratorRateLimit", "10/1M")),
      REMOVE_MODERATOR_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("RemoveModeratorRateLimit", "10/1M")),
      LIST_MODERATOR_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("ListModeratorRateLimit", "10/1M")),
      SILENCE_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("SilenceRateLimit", "30/1M")),
      KICK_CLEAR_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("KickClearRateLimit", "10/1M")),
      KICK_CLEAR_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("KickClearEnabled", false)),
      EMOTE_COMMAND_RELOAD_INTERVAL_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("CommandReloadIntervalInSeconds", 600)),
      HELP_COMMAND_INFO_MESSAGE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("HelpCommandInfoMessageEnabled", false)),
      HELP_COMMAND_INFO_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("HelpCommandInfoMessage", "Type /help anytime to learn more about Making Friends, Gifting/Emoticons, Miniblog, migLevels, Games, migCredits, Emotes & Username Colors!")),
      HELP_COMMAND_URL_SUFFIX(new SystemPropertyEntities.SystemPropertyEntryString("HelpCommandUrlSuffix", "/sites/index.php?c=migworld&a=get_page&page_id=13995")),
      NEW_TAB_CLIENT_VERSION_MIN(new SystemPropertyEntities.SystemPropertyEntryInteger("NewTabClientVersionMin", 400)),
      MAX_USERNAME_LENGTH_DISPLAY(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxUserNameLengthDisplay", 30)),
      MAX_CHATROOMNAME_LENGTH_DISPLAY(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxChatRoomNameLengthDisplay", 30)),
      MAX_MODERATOR_LIST_LENGTH_DISPLAY(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxModeratorListLengthDisplay", 1500)),
      BUMP_DURATION_SEC(new SystemPropertyEntities.SystemPropertyEntryInteger("BumpDurationSec", 300)),
      SILENCE_TIMEOUT_CHATROOM_MIN(new SystemPropertyEntities.SystemPropertyEntryInteger("SilenceTimeoutChatroomMin", 10)),
      SILENCE_TIMEOUT_CHATROOM_MAX(new SystemPropertyEntities.SystemPropertyEntryInteger("SilenceTimeoutChatroomMax", 600)),
      SILENCE_TIMEOUT_INDIVIDUAL_MIN(new SystemPropertyEntities.SystemPropertyEntryInteger("SilenceTimeoutIndividualMin", 10)),
      SILENCE_TIMEOUT_INDIVIDUAL_MAX(new SystemPropertyEntities.SystemPropertyEntryInteger("SilenceTimeoutIndividualMax", 900)),
      WARN_MESSAGE_MAX_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("WarnMessageMaxLength", 256)),
      COMMUNITY_EMOTES_MIN_CLIENT_VERSION(new SystemPropertyEntities.SystemPropertyEntryInteger("CommunityEmotesMinClientVersion", 400)),
      STICKER_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("StickerRateLimit", "10/1M")),
      UNBAN_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("UnbanRateLimit", "1/5M")),
      BAN_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("BanRateLimit", "1/5M")),
      KILL_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("KillRateLimit", "2/1M")),
      GIFT_INVENTORY_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GiftInventoryEnabled", true)),
      LOG_ALL_EMOTES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogAllEmotesEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Emote";

      private Emote(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum BOT implements SystemPropertyEntities.SystemPropertyEntryInterface {
      PAYOUT_FAILURE_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("PayoutFailureErrorMessage", "Cancelled the game due to an internal error, you were not charged")),
      SEND_BOT_SPENDING_TRIGGER_AFTER_PAYOUT_TRANSACTION(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendBotSpendingTriggerAfterPayoutTransaction", true)),
      SEND_BOT_GAME_WON_TRIGGER_AFTER_PAYOUT_TRANSACTION(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendBotGameWonTriggerAfterPayoutTransaction", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "BOT";

      private BOT(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum MigLevel implements SystemPropertyEntities.SystemPropertyEntryInterface {
      NEW_USER_MAX(new SystemPropertyEntities.SystemPropertyEntryInteger("NewUserMax", 5)),
      GROUP_CREATE_USER_POST_MIN(new SystemPropertyEntities.SystemPropertyEntryInteger("GroupCreateUserPostMin", 3)),
      REPUTATIONSLEVEL_DATA_REFRESH_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("ReputationLevelDataRefreshInSeconds", 600));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "MigLevel";

      private MigLevel(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Registration implements SystemPropertyEntities.SystemPropertyEntryInterface {
      REGISTRATION_DISABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RegistrationDisabled", false)),
      REGISTRATION_DISABLED_ERROR_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("RegistrationDisabledErrMsg", "Registration has been disabled at the moment, please try again later.")),
      REGISTRATION_CONTEXT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RegistrationContextEnabled", false)),
      EMAIL_REGISTRATION_RATE_LIMIT_BY_IP(new SystemPropertyEntities.SystemPropertyEntryString("EmailRegistrationRateLimitByIP", "2000/1D")),
      EMAIL_REGISTRATION_RATE_LIMIT_BY_IP_WHITELIST(new SystemPropertyEntities.SystemPropertyEntryStringArray("EmailRegistrationRateLimitByIPWhitelist", new String[0])),
      EMAIL_REGISTRATION_RATE_LIMIT_BY_DOMAIN(new SystemPropertyEntities.SystemPropertyEntryString("EmailRegistrationRateLimitByDomain", "2000/1D")),
      EMAIL_REGISTRATION_RATE_LIMIT_BY_SANITIZIED_LOCAL_AND_IP(new SystemPropertyEntities.SystemPropertyEntryString("EmailRegistrationRateLimitBySanitizedLocalAndIp", "10/1D")),
      EMAIL_REGISTRATION_RATE_LIMIT_BY_SANITIZIED_LOCAL_AND_DOMAIN(new SystemPropertyEntities.SystemPropertyEntryString("EmailRegistrationRateLimitBySanitizedLocalAndDomain", "10/1D")),
      EMAIL_REGISTRATION_RATE_LIMIT_BY_NONWHITELISTED_DOMAINS_TOP_LEVEL_PART(new SystemPropertyEntities.SystemPropertyEntryString("EmailRegistrationRateLimitByNonWhitelistedDomainsTopLevelPart", "10/1D")),
      EMAIL_REGISTRATION_RATE_LIMIT_BY_DOMAIN_WHITELIST(new SystemPropertyEntities.SystemPropertyEntryStringArray("EmailRegistrationRateLimitByDomainWhitelist", new String[0])),
      EMAIL_REGISTRATION_TIMELIMIT_IN_MINUTES(new SystemPropertyEntities.SystemPropertyEntryInteger("EmailRegistrationTimeLimitInMinutes", 2880)),
      EMAIL_REGISTRATION_GRACE_PERIOD_IN_MINUTES(new SystemPropertyEntities.SystemPropertyEntryInteger("EmailRegistrationGracePeriodInMinutes", 60)),
      EMAIL_REGISTRATION_PATH1_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("EmailPath1Enabled", false)),
      EMAIL_REGISTRATION_PATH2_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("EmailPath2Enabled", false)),
      REGISTRATION_V2_PATH(new SystemPropertyEntities.SystemPropertyEntryString("RegV2Path", "")),
      POST_VERIFICATION_REDIRECT_URL(new SystemPropertyEntities.SystemPropertyEntryString("PostVerificationRedirectURL", "")),
      RESEND_VERIFICATION_EMAIL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ResendVerificationEmailEnabled", true)),
      MOBILE_REGISTRATION_DISABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("mobileRegistrationDisabled", false)),
      MOBILE_REGISTRATION_DISABLED_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("mobileRegistrationDisabledMessage", "Sorry, mobile registration has been disabled for this version of migme. Please go to m.mig33.com to register.")),
      DO_SYNCHRONOUS_PROFILE_CACHE_INVALIDATION_ON_MIGBO_UPON_MOBILE_AUTH(new SystemPropertyEntities.SystemPropertyEntryBoolean("DoSyncMigboProfileCacheInvalidationUponMobileAuth", false)),
      SANITIZED_EMAIL_LOCAL_PART_AND_IP_RATE_LIMIT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SanitizedEmailLocalPartAndIpRateLimitEnabled", false)),
      SANITIZED_EMAIL_LOCAL_PART_AND_DOMAIN_RATE_LIMIT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SanitizedEmailLocalPartAndDomainRateLimitEnabled", false)),
      NON_WHITELISTED_DOMAINS_TOP_LEVEL_PART_RATE_LIMIT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("NonWhitelistedDomainsTopLevelPartRateLimitEnabled", false)),
      COMMON_PREFIX_CHECK_FOR_MOBILE_REGISTRATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CommonPrefixCheckEnabled", false)),
      MAX_COMMON_PREFIX_RECENT_ACTIVATIONS_ALLOWED(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxCommonPrefixRecentActivationsAllowed", 3)),
      NUMBER_OF_RECENT_ACTIVATIONS_FOR_COMMON_PREFIX_CHECK(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberOfRecentActivationsForCommonPrefixCheck", 40000)),
      IPWHITELIST_PREFIX_FOR_COMMON_PREFIX_CHECK(new SystemPropertyEntities.SystemPropertyEntryStringArray("IPWhiteListPrefixForCommonPrefixCheck", new String[]{"10."})),
      RANDOMIZE_MIDLET_REGISTRATION_CAPTCHA_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RandomizeMidletRegistrationCaptchaEnabled", false)),
      MAX_WORD_LENGTH_FOR_RANDOMIZED_MIDLET_REGISTRATION_CAPTCHA(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxWordLengthForRandomizedMidletRegistrationCaptcha", 5)),
      MIN_WORD_LENGTH_FOR_RANDOMIZED_MIDLET_REGISTRATION_CAPTCHA(new SystemPropertyEntities.SystemPropertyEntryInteger("MinWordLengthForRandomizedMidletRegistrationCaptcha", 3)),
      SPECIAL_VALIDATION_FOR_GMAIL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SpecialValidationForGmailEnabled", false)),
      NUMBER_OF_PERIODS_ALLOWED_IN_GMAIL_ADDRESSES(new SystemPropertyEntities.SystemPropertyEntryInteger("NumberOfPeriodsAllowedInGmailAddresses", 3)),
      POST_VERIFICATION_ALLOW_MULTIPLE_AUTO_LOGIN_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PostVerificationAllowMultipleAutoLoginEnabled", true)),
      STRIP_PERIODS_FROM_GMAIL_ADDRESS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("StripPeriodsFromGmailAddressEnabled", false)),
      EMAIL_EXCLUSION_REGULAR_EXPRESSIONS(new SystemPropertyEntities.SystemPropertyEntryString("EmailExclusionRegularExpressions", "")),
      ALLOW_OVERRIDING_USER_REGISTRATION_DATE_FOR_UNIT_TESTING(new SystemPropertyEntities.SystemPropertyEntryBoolean("AllowOverridingUserRegistrationDateForUnitTesting", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Registration";

      private Registration(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum GroupChat implements SystemPropertyEntities.SystemPropertyEntryInterface {
      GROUP_CHAT_PARTICIPANT_LIMIT(new SystemPropertyEntities.SystemPropertyEntryInteger("ParticipantLimit", 10));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "GroupChat";

      private GroupChat(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Group implements SystemPropertyEntities.SystemPropertyEntryInterface {
      MODERATOR_MIN_USER_LEVEL(new SystemPropertyEntities.SystemPropertyEntryInteger("ModeratorMinUserLevel", 10));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Group";

      private Group(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum FusionRestRateLimit implements SystemPropertyEntities.SystemPropertyEntryInterface {
      EVENT_ALERTBATCH(new SystemPropertyEntities.SystemPropertyEntryString("EventAlertBatch", "1/15M"));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "FusionRestRateLimit";

      private FusionRestRateLimit(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum EventQueueSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      MAXIMUM_WORKER_QUEUE_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaximumWorkerQueueSize", 10000)),
      WORKER_THREAD_POOL_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("WorkerThreadPoolSize", 1)),
      QUEUE_CHECK_INTERVAL_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("QueueCheckIntervalInSeconds", 5)),
      RETRY_INTERVAL_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("RetryIntervalInSeconds", 5)),
      SHUTDOWN_WAIT_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("ShutdownWaitlInSeconds", 5)),
      QUEUE_TYPE(new SystemPropertyEntities.SystemPropertyEntryString("QueueType", "redis")),
      PERSISTENT_REDIS_CONNECTION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PersistentRedisConnectionEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "EventQueueSettings";

      private EventQueueSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Chatroom implements SystemPropertyEntities.SystemPropertyEntryInterface {
      CHATROOM_WELCOME_MESSAGE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ChatroomWelcomeMessageEnabled", true)),
      INSUFFICIENT_MIGLEVEL_TO_CREATE_CHATROOM_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("InsufficientMiglevelToCreateChatroomMessage", "You need to be mig level 10 and above to create a chat room")),
      MULTI_ID_CONTROL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MultiIDControlEnabled", false)),
      BLOCK_PERIOD_BY_IP_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("BlockPeriodByIPInSeconds", 1800)),
      BANNED_USERS_PAGE_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("BannedUsersPageSize", MemCachedKeySpaces.CommonKeySpace.CHATROOM_BANNED_USERS.getPageSize())),
      MULTI_ID_CONTROL_IP_WHITELIST(new SystemPropertyEntities.SystemPropertyEntryStringArray("MultiIDControlIPWhitelist", new String[0])),
      NEGATIVE_CACHE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("NegativeCacheEnabled", false)),
      MESSAGE_QUEUE_CHECK_SIZE_BEFORE_PUTMESSAGE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MsgQueueCheckSizeBeforePutMessageEnabled", true)),
      MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN(new SystemPropertyEntities.SystemPropertyEntryLong("MsgQueueAddTimeoutMillisAdmin", 10000L)),
      MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("MsgQueueAddTimeoutMillis", 1000L)),
      MESSAGE_QUEUE_VIA_LIST_MAX_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("MsgQueueViaListMaxSize", Integer.MAX_VALUE)),
      MESSAGE_QUEUE_VIA_LIST_ADD_WAIT_INTERVAL_IN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("MsgQueueViaListAddWaitIntervalInMillis", 500L)),
      MESSAGE_QUEUE_DISPATCH_WINDOW_MAX_DURATION_IN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("MsgQueueMaxDurInMillisPerDispatchWindow", 1000L)),
      MESSAGE_QUEUE_DISPATCH_WINDOW_MAX_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryInteger("MsgQueueMaxMsgPerDispatchWindow", 5)),
      NUE_NEWBIE_CATEGORIES_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("NueNewbieCategoriesEnabled", false)),
      MAX_NEWBIE_MIG33_LEVEL(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxNewbieMig33Level", 10)),
      CATEGORIES_ALWAYS_INCLUDED_IN_LOGIN(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("CategoriesAlwaysIncludedInLogin", new int[0])),
      CATEGORIES_LIST_CACHE_TIME_IN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("CategoriesListCacheTimeInMillis", 3600000L)),
      CHATROOM_LIST_CACHE_TIME_IN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("ChatroomListCacheTimeInMillis", 3600000L)),
      MAX_NAME_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxNameLength", 15)),
      BANNED_NAMES(new SystemPropertyEntities.SystemPropertyEntryString("BannedNames", "")),
      ENABLE_OVERRIDE_EXPIRED_KICK_USER_VOTE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableOverrideExpiredKickUserVote", false)),
      DROP_MESSAGES_FROM_MUTED_USERS(new SystemPropertyEntities.SystemPropertyEntryBoolean("DropMessagesFromMutedUsers", true)),
      MAX_RECOMMENDED_CHATROOMS(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxRecommendedChatrooms", 10)),
      PARTICIPANT_CACHE_DATA(new SystemPropertyEntities.SystemPropertyEntryBoolean("ParticipantCacheData", true)),
      CHECK_USER_PRESENT_BEFORE_DISPATCH(new SystemPropertyEntities.SystemPropertyEntryBoolean("CheckUserPresentBeforeDispatch", true)),
      DELAY_USER_MESSAGE_TIME(new SystemPropertyEntities.SystemPropertyEntryInteger("UserMessageDelayInMs", 100)),
      ENTER_EXIT_MESSAGE_DELAY_IN_MS(new SystemPropertyEntities.SystemPropertyEntryInteger("EnterExitMessageDelayInMs", 2000)),
      SILENCE_FAST_EXIT_MESSAGES(new SystemPropertyEntities.SystemPropertyEntryBoolean("SilenceFastExitMessages", true)),
      EXIT_SILENCE_TIME_IN_MS(new SystemPropertyEntities.SystemPropertyEntryInteger("ExitSilenceTimeInMs", 2500)),
      USE_ENTER_EXIT_MESSAGE_CHECKS(new SystemPropertyEntities.SystemPropertyEntryBoolean("UseEnterExitMessageChecks", true)),
      PT67727788_USE_LOCKLESS_ADMIN_KICK(new SystemPropertyEntities.SystemPropertyEntryBoolean("UseLocklessAdminKick", true)),
      ENABLE_MUTE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableMute", false)),
      USE_IP_RATELIMIT(new SystemPropertyEntities.SystemPropertyEntryBoolean("UseIpRatelimit", true)),
      OVERRIDE_IP_RATELIMIT(new SystemPropertyEntities.SystemPropertyEntryString("OverrideIpRatelimit", "3/1S")),
      DISABLE_VOTE_KICK_TIME_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("DisableVoteKickTimeInSeconds", 10)),
      ENABLED_REFINED_SQL_FOR_SEARCHING_CHATROOM(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledRefinedSqlForSearchingChatroom", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Chatroom";

      private Chatroom(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum LoggingSections implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("Enabled", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      public static final String NS = "LoggingSections";

      private LoggingSections(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("LoggingSections", this.value.getName());
      }
   }

   public static enum Credentials implements SystemPropertyEntities.SystemPropertyEntryInterface {
      PASSWORD_REG_EX(new SystemPropertyEntities.SystemPropertyEntryString("PasswordRegEx", "^(?=.*[a-zA-Z])(?=.*[0-9]).*$")),
      INVALID_PASSWORD_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("InvalidPasswordMessage", "Password must contain at least 6 letters or numbers, and not the same as username"));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Credentials";

      private Credentials(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum CashReceipt implements SystemPropertyEntities.SystemPropertyEntryInterface {
      MATCH_UPON_CREATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MatchUponCreationEnabled", false)),
      VALIDATE_CASH_RECEIPT_STATUS_BEFORE_MATCH(new SystemPropertyEntities.SystemPropertyEntryBoolean("ValidateCashReceiptStatusBeforeMatch", true)),
      ENABLE_CASH_RECEIPT_ID_WITH_NEW_ID_RETRIEVAL(new SystemPropertyEntities.SystemPropertyEntryBoolean("ValidateCashReceiptStatusBeforeMatch", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "CashReceipt";

      private CashReceipt(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Contacts implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ADD_CONTACT_ON_NEW_FOLLOWING_EVENT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("AddContactOnNewFollowingEvent", false)),
      FOLLOW_ON_ADD_CONTACT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("FollowOnAddContactEnabled", false)),
      HANDLE_FOLLOWING_EVENTS_FROM_MIGBO_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("HandleFollowingEventsFromMigboEnabled", false)),
      HANDLE_FRIENDINVITE_EVENTS_FROM_MIGBO_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("HandleFriendInviteEventsFromMigboEnabled", true)),
      ONEWAY_MIGBO_API_CALLS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("OneWayMigboApiCallsEnabled", true)),
      MAX_PENDING_CONTACTS_TO_RETRIEVE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxPendingContactsToRetrieve", 15)),
      MIGBO_INTEGRATION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MigboIntegrationEnabled", false)),
      PUSH_NEW_FOLLOWERS_AS_PENDING_CONTACTS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PushNewFollowersAsPendingContactsEnabled", false)),
      REMOVE_CONTACT_ON_REMOVE_FOLLOWING_EVENT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RemoveContactOnRemoveFollowingEvent", true)),
      UNFOLLOW_ON_REMOVE_CONTACT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("UnfollowOnRemoveContactEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Contacts";

      private Contacts(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum UserNotificationServiceSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      MAX_NOTIFICATION_THREAD_BACKOFF_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryLong("MaxNotificationThreadBackoffInSeconds", 120L)),
      MAX_NOTIFICATION_SERVICE_QUEUE_SIZE(new SystemPropertyEntities.SystemPropertyEntryLong("MaxNotificationServiceQueueSize", 1000L)),
      USER_NOTIFICATION_TRIM_ON_READ_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("UserNotificationTrimOnReadEnabled", true)),
      USER_NOTIFICATION_TRIM_ON_WRITE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("UserNotificationTrimOnWriteEnabled", false)),
      USER_NOTIFICATION_USER_MUTEX_TIMEOUT_MINS(new SystemPropertyEntities.SystemPropertyEntryInteger("UserNotificationUserMutexTimeoutMins", 30)),
      MAX_NOTIFICATIONS_TO_SEND_TO_USER(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxNotificationsToSendToUser", 100)),
      MAX_NOTIFICATIONS_TO_SEND_TO_USER_TRUNCATION(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxNotificationsToSendToUserTruncation", 80)),
      PERSISTENT_ALERT_RECOUNT_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PersistentAlertRecountEnabled", false)),
      NEW_EMAIL_SENDING_LOGIC_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("NewEmailSendingLogicEnabled", true)),
      MAX_EMAIL_MESSAGE_LENGTH_FOR_ENQUEUE_LOG(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxEmailMessageLengthForEnqueueLog", 100)),
      MAX_EMAIL_SUBJECT_LENGTH_FOR_ENQUEUE_LOG(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxEmailSubjectLengthForEnqueueLog", 50)),
      MAX_EMAIL_SENDING_ERROR_MSG_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxEmailSendingErrorMsgLength", 1000)),
      STRIP_PATTERN_FOR_ENQUEUE_LOG(new SystemPropertyEntities.SystemPropertyEntryString("StripPatternForEnqueueLog", "\r|\n|\\|")),
      LOG_BEFORE_SENDING_EMAIL_TO_SMTP_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogBeforeSendingEmailToSMTPEnabled", true)),
      LOG_AFTER_SENDING_EMAIL_TO_SMTP_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogAfterSendingEmailToSMTPEnabled", true)),
      LOG_ENQUEUE_EMAIL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogEnqueueEmailEnabled", true)),
      INTERNAL_EMAIL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("InternalEmailEnabled", true)),
      INTERNAL_EMAIL_WHITELIST(new SystemPropertyEntities.SystemPropertyEntryStringArray("InternalEmailWhiteList", new String[]{"contact@mig.me", "donotreply@mig.me", "noreply@mig.me", "mig@mig.me"})),
      INQUERY_TO_EMAIL_ADDRESS(new SystemPropertyEntities.SystemPropertyEntryString("InqueryFromEmailAddress", "contact@mig.me")),
      NEW_TEMPLATE_PROCESSOR_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("NewTemplateProcessorEnabled", true)),
      ENABLED_SEND_TO_TRANSIENT_EMAIL(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledSendToTransientEmail", true)),
      CHECK_EMAIL_BOUNCEDB_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CheckEmailBounceDBEnabled", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "UserNotificationServiceSettings";

      private UserNotificationServiceSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum SSO implements SystemPropertyEntities.SystemPropertyEntryInterface {
      SUPPORT_ANDROID_VERSION(new SystemPropertyEntities.SystemPropertyEntryInteger("SupportAndroidVersion", 0)),
      SUPPORT_MIDLET_VERSION(new SystemPropertyEntities.SystemPropertyEntryInteger("SupportMidletVersion", 0)),
      FUSION_EID_ANDROID_VERSION(new SystemPropertyEntities.SystemPropertyEntryInteger("FusionEIDAndroidVersion", 0)),
      FUSION_EID_MIDLET_VERSION(new SystemPropertyEntities.SystemPropertyEntryInteger("FusionEIDMidletVersion", 0)),
      EID_ENCRPYTION_KEY(new SystemPropertyEntities.SystemPropertyEntryString("EncryptedSessionIDEncryptionKey", "EeHDV5r|~OLODVD4;:4-fSQ%[w{C#<tu")),
      SSO_SESSION_ALLOWED_VIEWS(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("SSOSessionAllowedViews", new int[0])),
      FUSION_SESSION_ALLOWED_VIEWS(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("FusionSessionAllowedDeviceTypes", new int[0])),
      CREATE_SSO_SESSION_FROM_FUSION(new SystemPropertyEntities.SystemPropertyEntryBoolean("CreateSSOSessionFromFusionEnabled", false)),
      SESSION_ARCHIVE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SessionArchiveEnabled", false)),
      VAS_LOG_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("VASLogEnabled", false)),
      SESSION_EXPIRY_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryLong("SessionExpiryInSeconds", 604800L)),
      SESSION_INITIAL_EXPIRY_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryLong("SessionInitialExpiryInSeconds", 604800L)),
      MAX_FAILED_LOGIN_ATTEMPTS(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxFailedLoginAttempts", 10)),
      MIN_LOGIN_UPDATE_PERIOD_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("MinLoginUpdatePeriod", 60)),
      TEMP_LOGIN_SUSPENSION_ERROR_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("TempLoginSuspensionLoginErrorMessage", "Unable to login right now. Please try again later.")),
      EMAIL_UNAUTH_USER_LOGIN_DISABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("EmailUnauthUserLoginDisabled", true)),
      AUTHENTICATION_FAILED_ATTEMPT_EXIPRY_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("AuthenticationFailedAttemptExpiryInSeconds", 86400)),
      MAX_FAILED_AUTHENTICATION_ATTEMPTS(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxFailedAuthenticationAttempts", 10)),
      DEVICES_REQUIRING_FULL_LOGIN_RESPONSE_ON_CREATE_SESSION(new SystemPropertyEntities.SystemPropertyEntryStringArray("DevicesRequiringFullLoginResponseOnCreateSession", StringUtil.EMPTY_STRING_ARRAY)),
      AUTO_LOGIN_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("AutoLoginEnabled", false)),
      AUTO_LOGIN_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("AutoLoginRateLimit", "3/1D")),
      ENABLE_PRELOGIN_USERNAME_CHAR_VALIDATION(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnablePreloginUsernameCharValidation", false)),
      LOG_INVALID_USERNAME_ON_PRELOGIN(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogInvalidUsernameOnPrelogin", true)),
      FAILED_AUTHS_PER_IP_HALF_LIFE_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("FailedAuthsPerIpHalfLifeSecs", 300)),
      FAILED_AUTHS_PER_IP_THRESHOLD(new SystemPropertyEntities.SystemPropertyEntryInteger("FailedAuthsPerIpThreshold", 1000)),
      FAILED_AUTHS_PER_IP_PACKET_DELAY_UNIT_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("FailedAuthsPerIpPacketDelayUnitSecs", 5)),
      FAILED_AUTHS_PER_IP_PACKET_DELAY_POWER(new SystemPropertyEntities.SystemPropertyEntryDouble("FailedAuthsPerIpPacketDelayPower", 0.5D)),
      FAILED_AUTHS_PER_IP_PACKET_DROP_THRESHOLD(new SystemPropertyEntities.SystemPropertyEntryInteger("FailedAuthsPerIpPacketDropThreshold", 4000)),
      FAILED_AUTHS_PER_IP_CAS_TIMEOUT_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("FailedAuthsPerIpCasTimeoutMillis", 5000L)),
      MAX_SESSIONID_COUNT_FOR_USER(new SystemPropertyEntities.SystemPropertyEntryLong("MaxSessionIdCountForUser", 50L)),
      SESSION_KEY_CONTAINER_EXPIRY_TIME_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("SessionKeyContainerExpiryTimeSecs", 604800));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "SSO";

      private SSO(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }

      public static class Cache {
         public static LazyLoader<Long> MAX_SESSIONID_COUNT_FOR_USER = new LazyLoader<Long>("MAX_SESSIONID_COUNT_FOR_USER", 60000L) {
            protected Long fetchValue() throws Exception {
               return SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.MAX_SESSIONID_COUNT_FOR_USER);
            }
         };
      }
   }

   public static class GuardsetEnabled implements SystemPropertyEntities.SystemPropertyEntryInterface {
      public static final SystemPropertyEntities.GuardsetEnabled FUSION_PKT_GET_EMOTICON_HI_RES_SUPPORT;
      public static final SystemPropertyEntities.GuardsetEnabled SEND_STICKERS_ALLOWED;
      public static final SystemPropertyEntities.GuardsetEnabled RECEIVE_STICKERS_NATIVE_SUPPORT;
      public static final SystemPropertyEntities.GuardsetEnabled UPLOAD_ADDRESSBOOK_DATA;
      private SystemPropertyEntities.SystemPropertyEntryBoolean value;
      private static String NS;

      public GuardsetEnabled(GuardCapabilityEnum guard) {
         this.value = new SystemPropertyEntities.SystemPropertyEntryBoolean(guard == null ? "" : guard.name(), true);
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }

      static {
         FUSION_PKT_GET_EMOTICON_HI_RES_SUPPORT = new SystemPropertyEntities.GuardsetEnabled(GuardCapabilityEnum.FUSION_PKT_GET_EMOTICON_HI_RES_SUPPORT);
         SEND_STICKERS_ALLOWED = new SystemPropertyEntities.GuardsetEnabled(GuardCapabilityEnum.SEND_STICKERS_ALLOWED);
         RECEIVE_STICKERS_NATIVE_SUPPORT = new SystemPropertyEntities.GuardsetEnabled(GuardCapabilityEnum.RECEIVE_STICKERS_NATIVE_SUPPORT);
         UPLOAD_ADDRESSBOOK_DATA = new SystemPropertyEntities.GuardsetEnabled(GuardCapabilityEnum.UPLOAD_ADDRESSBOOK_DATA);
         NS = "GuardsetEnabled";
      }
   }

   public static class DeprecatedPacketFallbackURL implements SystemPropertyEntities.SystemPropertyEntryInterface {
      private SystemPropertyEntities.SystemPropertyEntryString value;
      private static String NS = "DeprecatedPacketFallbackURL";

      public DeprecatedPacketFallbackURL(FusionRequest packet) {
         this.value = new SystemPropertyEntities.SystemPropertyEntryString(packet.getSimpleName(), "");
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static class GatewayRateLimitByChatRoom implements SystemPropertyEntities.SystemPropertyEntryInterface {
      private SystemPropertyEntities.SystemPropertyEntryString value;
      private boolean isGlobalAdmin;
      private static String NS = "GatewayRateLimitByChatRoom";
      private static String NSA = "GatewayRateLimitByChatRoomForGlobalAdmins";

      public GatewayRateLimitByChatRoom(FusionRequest packet) {
         this.value = new SystemPropertyEntities.SystemPropertyEntryString(packet.getSimpleName(), SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.DEFAULT_REQUEST_RATELIMIT));
         this.isGlobalAdmin = false;
      }

      public GatewayRateLimitByChatRoom(FusionRequest packet, boolean isGlobalAdmin) {
         this.value = new SystemPropertyEntities.SystemPropertyEntryString(packet.getSimpleName(), SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.DEFAULT_REQUEST_RATELIMIT));
         this.isGlobalAdmin = isGlobalAdmin;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(this.isGlobalAdmin ? NSA : NS, this.value.getName());
      }
   }

   public static class GatewayRateLimit implements SystemPropertyEntities.SystemPropertyEntryInterface {
      private SystemPropertyEntities.SystemPropertyEntryString value;
      private boolean isGlobalAdmin;
      private static String NS = "GatewayRateLimit";
      private static String NSA = "GatewayRateLimitForGlobalAdmins";

      public GatewayRateLimit(FusionRequest packet) {
         this.value = new SystemPropertyEntities.SystemPropertyEntryString(packet.getSimpleName(), SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.DEFAULT_REQUEST_RATELIMIT));
         this.isGlobalAdmin = false;
      }

      public GatewayRateLimit(FusionRequest packet, boolean isGlobalAdmin) {
         this.value = new SystemPropertyEntities.SystemPropertyEntryString(packet.getSimpleName(), SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.DEFAULT_REQUEST_RATELIMIT));
         this.isGlobalAdmin = isGlobalAdmin;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(this.isGlobalAdmin ? NSA : NS, this.value.getName());
      }
   }

   public static class MetricsLoggingEnabled implements SystemPropertyEntities.SystemPropertyEntryInterface {
      private SystemPropertyEntities.SystemPropertyEntryBoolean value;
      private static String NS = "MetricsLoggingEnabled";

      public MetricsLoggingEnabled(MetricsEnums.MetricsEntryInterface entry) {
         this.value = new SystemPropertyEntities.SystemPropertyEntryBoolean(entry.getSimpleName(), false);
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static class MechanicsEngineCategoryDailyMaximum implements SystemPropertyEntities.SystemPropertyEntryInterface {
      private SystemPropertyEntities.SystemPropertyEntryInteger value;
      private static String NS = "MechanicsEngine:CategoryDailyMax";

      public MechanicsEngineCategoryDailyMaximum(RewardProgramData.CategoryEnum category) {
         this.value = new SystemPropertyEntities.SystemPropertyEntryInteger(category.toString(), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.DEFAULT_CATEGORY_DAILYMAX));
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum MechanicsEngineSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLE_MM_V1(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableMMv1", true)),
      DEFAULT_CATEGORY_DAILYMAX(new SystemPropertyEntities.SystemPropertyEntryInteger("DefaultCategoryDailyMaximum", 100)),
      REWARD_CENTRE_QUEUE_MAXSIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("RewardCentreMaxQueueSize", 10000)),
      REWARD_CENTRE_SCORECAP_REFRESH_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("RewardCentreMaxQueueSize", 300000L)),
      REWARD_CENTRE_SCORECAP_CHECKED_FIRST_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RewardCentreScoreCapCheckFirstEnabled", true)),
      REWARD_CENTRE_LEVEL_SCORECAP_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RewardCentreLevelScorecapEnabled", true)),
      REWARD_PROCESSOR_MATCH_FROM_INVENTORY_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("RewardProcessorMatchFromInventoryEnabled", true)),
      EXCLUDE_TYPE_INVITEE_REWARDED_TRACKING(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("ExcludeTypeInviteeRewardedTracking", new int[]{43})),
      EXCLUDE_REWARD_ID_INVITEE_REWARDED_TRACKING(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("ExcludeRewardIdInviteeRewardedTracking", new int[0])),
      ENABLE_INVITEE_REWARDED_TRACKING(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableInviteeRewardedTracking", true)),
      UNIQUE_INVITEE_REWARDED_MAX_COUNT(new SystemPropertyEntities.SystemPropertyEntryInteger("UniqueInviteeRewardedMaxCount", 30)),
      UNIQUE_INVITEE_REWARDED_REDIS_MAX_COUNT(new SystemPropertyEntities.SystemPropertyEntryInteger("UniqueInviteeRewardedRedisMaxCount", 60)),
      CHATROOM_MESSAGE_TRIGGER_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ChatroomMessageTriggerEnabled", false)),
      MIGLEVEL_INCREASE_PROCESSING_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MigLevelIncreaseProcessingEnabled", true)),
      /** @deprecated */
      @Deprecated
      DEFAULT_USER_EMAIL_ADDRESS_TYPE_FOR_EMAIL_NOTIFICATION(new SystemPropertyEntities.SystemPropertyEntryInteger("DefaultUserEmailAddressTypeForEmailNotification", UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value)),
      /** @deprecated */
      @Deprecated
      ENABLE_PROCESSING_OUTCOME_AS_USERID_STRING(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableProcessingOutcomeAsUserIDString", true)),
      ENABLE_TRIGGER_ON_REFFERED_USER_REWARDED(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableTriggerOnReferredUserRewarded", true)),
      DISPATCHABLE_REFERRED_USER_REWARDED_TRIGGER_TYPES(new SystemPropertyEntities.SystemPropertyEntryShortArray("DispatchableReferredUserRewardedTriggerTypes", new short[]{(short)RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_MIGLEVEL.value(), (short)RewardProgramData.TypeEnum.MERCHANT_TAGGED_USER_MEETS_REWARD_CRITERIA.value()})),
      ENABLE_TRIGGER_ON_USER_REWARDED(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableTriggerOnUserRewarded", false)),
      DISPATCHABLE_USER_REWARDED_TRIGGER_TYPES(new SystemPropertyEntities.SystemPropertyEntryShortArray("DispatchableUserRewardedTriggerTypes", new short[]{(short)RewardProgramData.TypeEnum.USER_MEETS_REWARD_CRITERIA.value()})),
      DEFAULT_CURRENCY(new SystemPropertyEntities.SystemPropertyEntryString("DefaultCurrency", "USD")),
      SAMPLE_SUMMARIZER_MAX_TASK_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("SampleSummarizerMaxTaskSize", 10000)),
      EJB_SAMPLE_SUMMARY_SINK_MAX_TASK_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("EJBSampleSummarySinkMaxTaskSize", 10)),
      ENABLE_TRIGGER_METRICS_LOGGING(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableTriggerMetrics", false)),
      ENABLE_SAMPLE_SUMMARY_PERIODICAL_FLUSH(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableSampleSummaryPeriodicalFlush", true)),
      ENABLE_MINIMISE_REDIS_THREADS_ENGINE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableMinimiseRedisThreadsEngine", true)),
      POLL_INTERVAL_SETTINGS_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("PollIntervalSettingsSecs", 5)),
      TPOOL_CORE_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("TPoolCoreSize", 5)),
      TPOOL_MAX_THREAD_MULTIPLIER(new SystemPropertyEntities.SystemPropertyEntryDouble("TPoolMaxThreadMultiplier", 1.0D)),
      TPOOL_KEEPALIVE_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("TPoolKeepAliveSecs", 5)),
      MAX_WORKERS_QUEUED(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxWorkersQueued", 50)),
      WAIT_ENGINE_SHUTDOWN_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("WaitEngineShutdownSecs", 120)),
      LOG_SCORE_REWARDINGS_THAT_CAUSE_LEVEL_UPS(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogScoreRewardingsThatCauseLevelUps", true)),
      LOG_SCORE_REWARDINGS_THAT_DO_NOT_CAUSE_LEVEL_UPS(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogScoreRewardingsThatDoNotCauseLevelUps", false)),
      ENABLE_TRIGGER_FOR_MERCHANT_TAGGER_ON_TAGGED_USER_REWARDED(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableTriggerForMerchantTaggerWhenTaggedUserRewarded", false)),
      IM_NOTIFICATION_MAX_MESSAGE_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("IMNotificationMaxMessageLength", 1024)),
      FETCH_USER_REPUTATION_FROM_MASTER(new SystemPropertyEntities.SystemPropertyEntryBoolean("FetchUserReputationFromMaster", false)),
      ENABLE_SEND_TRIGGER_FROM_REWARDS_BEAN(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableSendTriggerFromRewardsBean", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "MechanicsEngine";

      private MechanicsEngineSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum RewardDispatcherSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLE_REST_INTERFACE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableRESTInterface", false)),
      ENABLE_LOGGING_RECEIVED_OUTCOMES_FROM_QUEUE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableLoggingReceivedOutcomesFromQueue", true)),
      ENABLE_LOGGING_RECEIVED_OUTCOMES_FROM_REST_INTERFACE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableLoggingReceivedOutcomesFromRESTInterface", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static final String NS = "RewardDispatcherSettings";

      private RewardDispatcherSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace("RewardDispatcherSettings", this.value.getName());
      }
   }

   public static enum MarketingMechanicsV2_RMQ_TriggerRouting implements SystemPropertyEntities.SystemPropertyEntryInterfaceWithNamespace {
      ROUTING_KEY(new SystemPropertyEntities.SystemPropertyEntryString("RoutingKey", "eventq1")),
      PERSISTENT_MODE(new SystemPropertyEntities.SystemPropertyEntryBoolean("PersistentMode", true)),
      SYNC_DISPATCH(new SystemPropertyEntities.SystemPropertyEntryBoolean("SyncDispatch", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "MarketingMechanicsV2:RMQ:TriggerRouting";

      private MarketingMechanicsV2_RMQ_TriggerRouting(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }

      public String getNamespace() {
         return NS;
      }

      public SystemPropertyEntities.MarketingMechanicsV2_RMQ_TriggerRouting.TriggerRoutingForEventTypeSetting forEventTypeID(int eventTypeID) {
         return new SystemPropertyEntities.MarketingMechanicsV2_RMQ_TriggerRouting.TriggerRoutingForEventTypeSetting(String.valueOf(eventTypeID), this);
      }

      public static class TriggerRoutingForEventTypeSetting extends SystemPropertyEntryWithParent {
         protected TriggerRoutingForEventTypeSetting(String subNameSpace, SystemPropertyEntities.SystemPropertyEntryInterfaceWithNamespace parentProperty) {
            super(SystemPropertyEntryFallbackCreatorRegistry.getInstance(parentProperty), subNameSpace, parentProperty);
         }
      }
   }

   public static enum MarketingMechanicsV2_RMQ implements SystemPropertyEntities.SystemPropertyEntryInterface {
      WAIT_CHANNEL_RETURN_TIMEOUT_MILLIS(new SystemPropertyEntities.SystemPropertyEntryInteger("WaitChannelReturnTimeoutMillis", 5000)),
      HOST_ADDR(new SystemPropertyEntities.SystemPropertyEntryString("HostAddr", "127.0.0.1")),
      HOST_PORT(new SystemPropertyEntities.SystemPropertyEntryInteger("HostPort", 5672)),
      MAX_PENDING_ASYNC_CLOSE_CONN_TASK(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxPendingAsyncCloseConnTask", 10)),
      VIRTUAL_HOST(new SystemPropertyEntities.SystemPropertyEntryString("VirtualHost", "leto")),
      USERNAME(new SystemPropertyEntities.SystemPropertyEntryString("Username", "leto")),
      PASSWORD(new SystemPropertyEntities.SystemPropertyEntryString("Password", "ten20304051")),
      CONNECT_TIMEOUT_MILLIS(new SystemPropertyEntities.SystemPropertyEntryInteger("ConnectTimeoutMillis", 10000)),
      EXCHANGE_NAME(new SystemPropertyEntities.SystemPropertyEntryString("ExchangeName", "events")),
      MAX_DISPATCH_TASK_SIZE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxDispatchTaskSize", 10000)),
      REQUESTED_HEARTBEAT_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("RequestedHeartbeatSecs", 20));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "MarketingMechanicsV2:RMQ";

      private MarketingMechanicsV2_RMQ(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum MarketingMechanicsV2 implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLE_OUTBOUND_TRIGGERS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableOutboundTriggers", false)),
      SEND_WHITELIST_TRIGGER_TYPES_ONLY(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendWhitelistedTriggerTypesOnly", false)),
      TRIGGER_TYPE_WHITELIST(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("TriggerTypeWhiteList", new int[0]));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "MarketingMechanicsV2";

      private MarketingMechanicsV2(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum CampaignSetting implements SystemPropertyEntities.SystemPropertyEntryInterface {
      DEFAULT_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryString("DefaultRateLimit", "3/1M"));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "CampaignSetting";

      private CampaignSetting(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Campaign implements SystemPropertyEntities.SystemPropertyEntryInterface {
      CAMPAIGN_TYPE_WHITELIST(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("CampaignTypeWhiteList", new int[]{43}));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Campaign";

      private Campaign(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum DailyDigest implements SystemPropertyEntities.SystemPropertyEntryInterface {
      SEND_DIGEST_TO_BLACK_LIST_DOMAIN_USER_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SendDailyDigestEmailToBlackListDomainUserEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "DailyDigest";

      private DailyDigest(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum JobSchedulerSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      REPUTATION_DAILY_GATHER_AND_PROCESS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ReputationDailyGatherAndProcessEnabled", true)),
      DAILY_DIGEST_FOR_USERS_BEING_FOLLOWED_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("DailyDigestForUsersBeingFollowedEnabled", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "JobSchedulerSettings";

      private JobSchedulerSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum JedisPoolConfig implements SystemPropertyEntities.SystemPropertyEntryInterface {
      JEDIS_POOL_SOCKET_TIMEOUT(new SystemPropertyEntities.SystemPropertyEntryInteger("JedisPoolSocketTimeout", 0)),
      JEDIS_NON_POOL_SOCKET_TIMEOUT(new SystemPropertyEntities.SystemPropertyEntryInteger("JedisNonPoolSocketTimeout", 30000)),
      JEDIS_POOL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("JedisPoolEnabled", true)),
      PROPS_REFRESH_INTERVAL_SECS(new SystemPropertyEntities.SystemPropertyEntryInteger("PropsRefreshIntervalSeconds", 600)),
      PROPS_LOG_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("PropsLogEnabled", true)),
      BLOCK_WHEN_EXHAUSTED(new SystemPropertyEntities.SystemPropertyEntryBoolean("BlockWhenExhausted", true)),
      MAX_TOTAL(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxTotal", 200)),
      MAX_IDLE(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxIdle", 100)),
      MIN_IDLE(new SystemPropertyEntities.SystemPropertyEntryInteger("MinIdle", 1)),
      TEST_ON_BORROW(new SystemPropertyEntities.SystemPropertyEntryBoolean("TestOnBorrow", true)),
      TEST_ON_RETURN(new SystemPropertyEntities.SystemPropertyEntryBoolean("TestOnReturn", false)),
      TEST_WHILE_IDLE(new SystemPropertyEntities.SystemPropertyEntryBoolean("TestWhileIdle", true)),
      NUM_TESTS_PER_EVICTION_RUN(new SystemPropertyEntities.SystemPropertyEntryInteger("NumTestsPerEvictionRun", 200)),
      MIN_EVICTABLE_IDLE_TIME_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("MinEvictableIdleTimeMillis", 60000L)),
      TIME_BETWEEN_EVICTION_RUN_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("TimeBetweenEvictionRunsMillis", 30000L)),
      MAX_WAIT(new SystemPropertyEntities.SystemPropertyEntryLong("maxWait", 3000L));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "JedisPoolConfig";

      private JedisPoolConfig(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }

      public static class Cache {
         public static LazyLoader<Boolean> enabled = new LazyLoader<Boolean>("JEDIS_POOL_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.JEDIS_POOL_ENABLED);
            }
         };
         public static LazyLoader<Integer> poolSocketTimeout = new LazyLoader<Integer>("JEDIS_POOL_SOCKET_TIMEOUT", 60000L) {
            protected Integer fetchValue() throws Exception {
               return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.JEDIS_POOL_SOCKET_TIMEOUT);
            }
         };
         public static LazyLoader<Integer> nonPoolSocketTimeout = new LazyLoader<Integer>("JEDIS_NON_POOL_SOCKET_TIMEOUT", 60000L) {
            protected Integer fetchValue() throws Exception {
               return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.JEDIS_NON_POOL_SOCKET_TIMEOUT);
            }
         };
      }
   }

   public static enum Mig33Voucher_Issuance implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLE_VOUCHER_ISSUANCE_CHECK(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableVoucherIssuanceCheck", false)),
      ENABLE_USER_TYPE_CHECK(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableUserTypeCheck", true)),
      USER_TYPES_ALLOWED_TO_ISSUE(new SystemPropertyEntities.SystemPropertyEntryIntegerArray("UserTypesAllowedToIssue", new int[]{UserData.TypeEnum.MIG33_TOP_MERCHANT.value()})),
      DISALLOWED_USER_TYPE_ERROR_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("DisallowedUserTypeErrorMessage", "##user.type.label## users are not allowed to create vouchers.")),
      ENABLE_MIG_LEVEL_CHECK(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableMigLevelCheck", true)),
      SKIP_FETCH_CACHED_REPUTATION_SCORE(new SystemPropertyEntities.SystemPropertyEntryBoolean("SkipFetchCachedReputationScore", true)),
      MIN_MIG_LEVEL(new SystemPropertyEntities.SystemPropertyEntryInteger("MinMigLevel", 10)),
      INSUFFICIENT_MIG_LEVEL_ERROR_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("InsufficientMigLevelErrorMessage", "Your mig level has to be at least ##min.miglevel## before you are allowed to create vouchers."));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Mig33Voucher:Issuance";

      private Mig33Voucher_Issuance(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Mig33Voucher_Redemption implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ALLOW_EMAIL_VERIFIED_USER(new SystemPropertyEntities.SystemPropertyEntryBoolean("AllowEmailVerifiedUser", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Mig33Voucher:Redemption";

      private Mig33Voucher_Redemption(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum Mig33Voucher implements SystemPropertyEntities.SystemPropertyEntryInterface {
      CREATE_BATCH_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CreateBatchEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "Mig33Voucher";

      private Mig33Voucher(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum DAOSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      ENABLED_GUARDSET_DAO(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledGuardsetDao", false)),
      ENABLED_USERDATA_DAO(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledUserDataDao", false)),
      ENABLED_EMOANDSTICKER_DAO(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEmoAndStickerDao", false)),
      ENABLED_GROUP_DAO(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledGroupDao", false)),
      ENABLED_CHATROOM_DAO(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledChatRoomDao", false)),
      ENABLED_BOT_DAO(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledBotDao", false)),
      ENABLED_MESSAGE_DAO(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledMessageDao", false)),
      ENABLED_EMAIL_DAO(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEmailDao", false)),
      ENABLED_EJB_NODE_GETUSERDATA(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetUserData", true)),
      ENABLED_EJB_NODE_GETBCL(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetBCL", true)),
      ENABLED_EJB_NODE_GETUSERSETTINGS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetUserSettings", true)),
      ENABLED_EJB_NODE_GETGROUPLIST(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetGroupList", true)),
      ENABLED_EJB_NODE_GECONTACTLIST(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetContactist", true)),
      ENABLED_EJB_NODE_ASSIGNDPSM(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeAssignDPSM", true)),
      ENABLED_EJB_NODE_GETUSERID(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetUserID", true)),
      ENABLED_EJB_NODE_GETUSERNAME(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetUserName", true)),
      ENABLED_EJB_NODE_GETUSERREPUTATIONSCOREANDLEVEL(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetUserReputationAndLevel", true)),
      ENABLED_EJB_NODE_GETREPUTATIONLEVELDATA(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetReputationLevelData", true)),
      ENABLED_EJB_NODE_GETBASICMERCHANTDETAILS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetBasicMerchatDetails", true)),
      ENABLED_EJB_NODE_GETGROUPDATA(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetGroupData", true)),
      ENABLED_EJB_NODE_GETINFOTEXT(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetInfoText", true)),
      ENABLED_EJB_NODE_ISUSERINMIGBOACCESSLIST(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeIsUserInMigboAccessList", true)),
      ENABLED_EJB_NODE_GETGROUPMEMBER(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetGroupMember", true)),
      ENABLED_EJB_NODE_ISUSERBLACKLISTEDINGROUP(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeIsUserBlackListedInGroup", true)),
      ENABLED_EJB_NODE_GETACCOUNTBALANCE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetAccountBalance", true)),
      ENABLED_EJB_NODE_GETMODERATORUSERNAMES(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetModeratorUserNames", true)),
      ENABLED_EJB_NODE_GETGROUPCHATROOMS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetGroupChatrooms", true)),
      ENABLED_EJB_NODE_GETMINCLIENTVERSIONFORACCESS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetMinimumClientVersionForAccess", true)),
      ENABLED_EJB_NODE_GETCHATROOMNAMESPERCATEGORY(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetChatroomNamesPerCategory", true)),
      ENABLED_EJB_NODE_GETCHATROOMS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetChatroomNames", true)),
      ENABLED_EJB_NODE_GETFAVCHATROOMS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetFavouriteChatRooms", true)),
      ENABLED_EJB_NODE_GETRECENTCHATROOMS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetRecentChatRooms", true)),
      ENABLED_EJB_NODE_GETCHATROOM(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetChatRoom", true)),
      ENABLED_EJB_NODE_GETBOT(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetBot", true)),
      ENABLED_EJB_NODE_GETOPTIMALEMOTICONHEIGHT(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetOptimalEmoticonHeight", true)),
      ENABLED_EJB_NODE_GETSIMPLECHATROOM(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnabledEJBNodeGetSimpleChatRoom", true)),
      DEFAULT_GETREPUTATIONDATA_USING_CACHE(new SystemPropertyEntities.SystemPropertyEntryBoolean("DefaultGetGroupChatRooms", false)),
      EMOTICON_PACKS_REFRESH_PERIOD_IN_MS(new SystemPropertyEntities.SystemPropertyEntryLong("EmoticonPacksRefreshPeriodInMS", 3600000L)),
      EMOTICONS_REFRESH_PERIOD_IN_MS(new SystemPropertyEntities.SystemPropertyEntryLong("EmoticonsRefreshPeriodInMS", 3600000L)),
      ENABLED_CHAIN_ON_IF_SHOULD_NOT_REACH_HERE_HAPPENS(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableChainOnIfShouldNotReachHereHappens", true)),
      BANNED_USERNAMES_PATTERN(new SystemPropertyEntities.SystemPropertyEntryString("BannedUsernamesPattern", "admin|owner|operator|mig33|miq33|migg|pcgan|crazygrape|ksandy|mig.me|migme|miq.me|miq.me|miqme|loadtest"));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "DAOSettings";

      private DAOSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum SessionCacheSettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      USE_REPUTATION_SERVICE_FOR_SCORE_RETRIEVAL(new SystemPropertyEntities.SystemPropertyEntryBoolean("UseReputationServiceForScoreRetrieval", true)),
      IPV6_SUPPORTED(new SystemPropertyEntities.SystemPropertyEntryBoolean("IPV6Supported", true));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "SessionCacheSettings";

      private SessionCacheSettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }
   }

   public static enum GatewaySettings implements SystemPropertyEntities.SystemPropertyEntryInterface {
      DEFAULT_REQUEST_RATELIMIT(new SystemPropertyEntities.SystemPropertyEntryString("DefaultFusionRequestPktRateLimitExpr", "10/1M")),
      INSTRUMENT_DISCARDED_PACKETS(new SystemPropertyEntities.SystemPropertyEntryBoolean("InstrumentDiscardedPackets", false)),
      INSTRUMENT_BLOCKED_PACKETS(new SystemPropertyEntities.SystemPropertyEntryBoolean("InstrumentBlockedPackets", false)),
      INSTRUMENT_PACKETS(new SystemPropertyEntities.SystemPropertyEntryBoolean("InstrumentPackets", true)),
      DEFAULT_RESPONSE_DELAY(new SystemPropertyEntities.SystemPropertyEntryLong("DefaultFusionPacketDelayInMillis", 5000L)),
      BLOCKED_FUSION_PACKETS(new SystemPropertyEntities.SystemPropertyEntryStringArray("BlockedFusionPackets", new String[0])),
      DELAYED_FUSION_PACKETS(new SystemPropertyEntities.SystemPropertyEntryStringArray("DelayedFusionPackets", new String[0])),
      DISCARDED_FUSION_PACKETS(new SystemPropertyEntities.SystemPropertyEntryStringArray("DiscardedFusionPackets", new String[0])),
      REQUESTS_TO_RATE_LIMIT(new SystemPropertyEntities.SystemPropertyEntryShortArray("RequestsToRateLimit", new short[]{202, 500, 703, 704, 751, 752, 900, 564})),
      RATELIMITED_FUSION_PACKETS(new SystemPropertyEntities.SystemPropertyEntryStringArray("RateLimitedFusionPackets", new String[0])),
      RATELIMITED_SUSPENDABLE_FUSION_PACKETS(new SystemPropertyEntities.SystemPropertyEntryStringArray("RateLimitedSuspendableFusionPackets", new String[0])),
      RATELIMITED_FUSION_PACKETS_BY_CHATROOM(new SystemPropertyEntities.SystemPropertyEntryStringArray("RateLimitedFusionPacketsByChatRoom", new String[0])),
      RATELIMITED_SUSPENDABLE_FUSION_PACKETS_BY_CHATROOM(new SystemPropertyEntities.SystemPropertyEntryStringArray("RateLimitedSuspendableFusionPacketsByChatRoom", new String[0])),
      RATELIMIT_BY_CHATROOM_AND_REMOTEADDRESS(new SystemPropertyEntities.SystemPropertyEntryBoolean("RateLimitByChatroomNameAndRemoteAddress", false)),
      RATELIMIT_BY_REMOTEADDRESS_WHITELIST(new SystemPropertyEntities.SystemPropertyEntryStringArray("RateLimitByRemoteAddressWhiteList", new String[0])),
      DEPRECATED_PACKETS(new SystemPropertyEntities.SystemPropertyEntryStringArray("DeprecatedPackets", new String[0])),
      MAX_MIGLEVEL_FOR_FUSION_PACKETS_RATELIMIT_BY_CHATROOM(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxMigLevelForFusionPacketRateLimitByChatroom", Integer.MAX_VALUE)),
      MAX_MIGLEVEL_FOR_FUSION_PACKETS_RATELIMIT(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxMigLevelForFusionPacketRateLimit", Integer.MAX_VALUE)),
      AJAX_CONSIDERED_ABOVE_MIDLET_VERSION_CHECKS(new SystemPropertyEntities.SystemPropertyEntryBoolean("AjaxConsideredAboveMidletVersionChecks", false)),
      USE_GUARDSET_FOR_CONTACT_REQUEST_ACCEPTED(new SystemPropertyEntities.SystemPropertyEntryBoolean("UseGuardsetForContactRequestAccepted", false)),
      CAPTCHA_IP_BLACKLIST(new SystemPropertyEntities.SystemPropertyEntryStringArray("CaptchaIPBlacklist", new String[0])),
      GENERIC_NATIVECAPTCHA_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("GenericNativeCaptchaEnabled", false)),
      INVITEFRIEND_CAPTCHA_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("InviteFriendCaptchaEnabled", false)),
      INVITEFRIEND_CAPTCHA_ODDS(new SystemPropertyEntities.SystemPropertyEntryDouble("InviteFriendCaptchaOdds", 0.5D)),
      INVITEFRIEND_CAPTCHAFALLBACK_URL(new SystemPropertyEntities.SystemPropertyEntryString("InviteFriendCaptchaFallbackUrl", "http://mig.me/sites/%1/invite/refer_friend")),
      INVITEFRIEND_CAPTCHAFALLBACK_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("InviteFriendCaptchaFallbackMessage", "Invitations on your version of migme have been discontinued. Please upgrade at http://m.mig.me.")),
      INVITEFRIEND_PAGELETFALLBACK_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("InviteFriendPageletFallbackMessage", "Invitations on your version of migme have been discontinued. Please upgrade at http://m.mig.me.")),
      PACKET_DISABLED_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("PacketDisabledMessage", "The feature you've requested has been disabled for your version of mig33. Please upgrade at http://m.mig.me.")),
      BLOCKED_PACKETS_UPON_SLEEPMODE(new SystemPropertyEntities.SystemPropertyEntryShortArray("BlockedPacketsUponSleepMode", new short[]{422, 404, 421, 720})),
      PKT_GET_STICKER_PACK_MAX_PACKIDS_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("PktGetStickerPackMaxPackIDSLength", 32767)),
      /** @deprecated */
      @Deprecated
      ALWAYS_SEND_PKT_GET_EMOTICONS_COMPLETE(new SystemPropertyEntities.SystemPropertyEntryBoolean("AlwaysSendPktGetEmoticonsComplete", true)),
      LOG_EID_DECRYPTION_FAILED_REMOTE_ADDRESS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogEidDecryptionFailedRemoteAddressEnabled", false)),
      TRACK_CONNECTIONS_PER_REMOTE_IP_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("TrackConnectionsPerRemoteIPEnabled", false)),
      PT66915380_ENABLE_CONNECTION_TRACKING(new SystemPropertyEntities.SystemPropertyEntryBoolean("PT66915380EnableConnectionTracking", true)),
      DISCONNECT_ON_SESSION_FAILURE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("DisconnectOnSessionFailureEnabled", true)),
      MAX_SESSION_TOUCH_FAILURES(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxSessionTouchFailures", 5)),
      OPEN_URL_DETAILED_EXCEPTION_LOGGING(new SystemPropertyEntities.SystemPropertyEntryBoolean("OpenUrlDetailedExceptionLogging", true)),
      OPEN_URL_STATS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("OpenUrlStatsEnabled", false)),
      ASYNC_PUT_MESSAGE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("AsyncPutMessageEnabled", false)),
      ASYNC_PUT_MESSAGE_TIMEOUT_MILLIS(new SystemPropertyEntities.SystemPropertyEntryLong("AsyncPutMessageTimeoutMillis", 60000L)),
      LOADTEST_USERS_RATE_LIMIT_BYPASS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("LoadTestUsersRateLimitBypassEnabled", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;
      private static String NS = "GatewaySettings";

      private GatewaySettings(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return SystemPropertyEntities.getNameWithNamespace(NS, this.value.getName());
      }

      public static class Cache {
         public static LazyLoader<Boolean> asyncPutMessageEnabled = new LazyLoader<Boolean>("ASYNC_PUT_MESSAGE_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.ASYNC_PUT_MESSAGE_ENABLED);
            }
         };
         public static LazyLoader<Long> asyncPutMessageTimeoutMillis = new LazyLoader<Long>("ASYNC_PUT_MESSAGE_TIMEOUT_MILLIS", 60000L) {
            protected Long fetchValue() throws Exception {
               return SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.ASYNC_PUT_MESSAGE_TIMEOUT_MILLIS);
            }
         };
         public static LazyLoader<Boolean> loadTestUsersRateLimitBypassEnabled = new LazyLoader<Boolean>("LOADTEST_USERS_RATELIMIT_BYPASS_ENABLED", 60000L) {
            protected Boolean fetchValue() throws Exception {
               return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.LOADTEST_USERS_RATE_LIMIT_BYPASS_ENABLED);
            }
         };
      }
   }

   public static enum Default implements SystemPropertyEntities.SystemPropertyEntryInterface {
      SSO_LOGIN_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SSOLoginEnabled", false)),
      ALLOWED_VERIFICATION_WINDOW_IN_HOURS(new SystemPropertyEntities.SystemPropertyEntryInteger("AllowedVerificationWindowInHours", 96)),
      UNAUTH_USER_LOGIN_BLOCK_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("UnauthUserLoginBlockEnabled", false)),
      UNAUTH_USER_ALLOW_LOGIN_IN_DAYS(new SystemPropertyEntities.SystemPropertyEntryInteger("UnauthUserAllowLoginInDays", 90)),
      UNAUTH_USER_LOGIN_BLOCK_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("UnauthUserLoginBlockMessage", "Authenticate your account at http://m.mig.me/auth by stating your USER ID, MOBILE NO & COUNTRY")),
      EMAIL_VERIFICATION_TOKEN_EXPIRY_IN_SECONDS(new SystemPropertyEntities.SystemPropertyEntryInteger("ExternalEmailVerificationExpirationInSeconds", 604800)),
      EMAIL_VERIFICATION_WITHOUT_USERNAME_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("EmailVerificationWithoutUsernameEnabled", false)),
      MERCHANT_REGISTRATION_NEW_USER_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("NewUserMerchantRegistrationEnabled", false)),
      MERCHANT_REGISTRATION_WITHOUT_MOBILE_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MerchantRegistrationWithoutMobileEnabled", false)),
      NOTIFICATIONS_COUNTER_ON_LOGIN(new SystemPropertyEntities.SystemPropertyEntryBoolean("NotificationsCounterOnLogin", false)),
      BLACKLISTED_EXTERNAL_MAIL_DOMAINS(new SystemPropertyEntities.SystemPropertyEntryStringArray("BlacklistedExternalEmailDomains", new String[0])),
      MAX_ACCOUNTENTRY_PERIOD_BEFORE_ARCHIVAL_IN_DAYS(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxAccountEntryPeriodBeforeArchivalInDays", 120)),
      FREE_VIRTUAL_GIFTS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("FreeVirtualGiftsEnabled", false)),
      EVENT_SYSTEM_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("EventSystemEnabled", true)),
      VIRTUALGIFT_ONEWAY_EVENT_TRIGGER(new SystemPropertyEntities.SystemPropertyEntryBoolean("VirtualGiftOnewayTrigger", false)),
      EVENT_SYSTEM_WELCOME_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("EventSystemWelcomeMessage", "Please visit the miniblog to view the activities of your friends.")),
      CREATE_ACCOUNT_ENTRY_RESTRICTED_ALL(new SystemPropertyEntities.SystemPropertyEntryBoolean("CreateAccountEntryRestrictedAll", false)),
      CREATE_ACCOUNT_ENTRY_RESTRICTION_NONTOPMERCHANT(new SystemPropertyEntities.SystemPropertyEntryBoolean("CreateAccountEntryRestrictedNonTopMerchant", false)),
      CREATED_ACCOUNT_ENTRY_RESTRICTED_TYPES(new SystemPropertyEntities.SystemPropertyEntryStringArray("CreateAccountEntryRestrictedTypes", new String[0])),
      MAX_CONCURRENT_SESSIONS_PER_USER(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxConcurrentSessionsPerUser", 100)),
      MAX_CONCURRENT_SESSIONS_PER_ADMINUSER(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxConcurrentSessionsPerAdminUser", 100)),
      MAX_CONCURRENT_SESSIONS_BREACHED_LOCKDOWN(new SystemPropertyEntities.SystemPropertyEntryLong("MaxConcurrentSessionsBreachedLockdownPeriod", MemCachedKeySpaces.RateLimitKeySpace.MAX_CONCURRENT_SESSIONS_BREACHED.getCacheTime())),
      MAX_CONCURRENT_SESSIONS_BREACHED_LOCKDOWN_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("MaxConcurrentSessionsBreachedLockdownEnabled", false)),
      BCLPERSISTED_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("BclPersistedEnabled", false)),
      WHITELISTED_RECIPIENT_EMAIL_DOMAINS(new SystemPropertyEntities.SystemPropertyEntryStringArray("WhitelistedRecipientEmailDomains", new String[]{"mig33.com"})),
      SUPPORT_EMAIL_ALIASES(new SystemPropertyEntities.SystemPropertyEntryStringArray("SupportEmailAliases", new String[]{"contact@mig.me"})),
      DEFAULT_EMAIL_DOMAIN(new SystemPropertyEntities.SystemPropertyEntryString("MailDomain", "mig33.com")),
      MAX_USERNAME_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxUsernameLength", 128)),
      REFERRAL_SUCCESS_RATIO_EVALUATION_WINDOW_IN_DAYS(new SystemPropertyEntities.SystemPropertyEntryInteger("ReferralSuccessRatioEvaluationWindowInDays", 30)),
      RESET_REFERRAL_SUCCESS_RATIO_ON_CREDIT_AWARD(new SystemPropertyEntities.SystemPropertyEntryBoolean("ResetReferralSuccessRatioOnCreditAward", true)),
      MIN_SYSTEMSMS_ID_FOR_30_DAY_SCAN(new SystemPropertyEntities.SystemPropertyEntryInteger("MinSystemSMSIDFor30DayScan", 209000000)),
      MIN_ACCOUNTENTRY_ID_FOR_30_DAY_SCAN(new SystemPropertyEntities.SystemPropertyEntryInteger("MinAccountEntryIDFor30DayScan", 1350000000)),
      MIN_MIDLET_VERSION_FOR_NATIVE_CAPTCHA(new SystemPropertyEntities.SystemPropertyEntryInteger("MinMidletVersionForNativeCaptcha", 450)),
      USERPROFILE_URL(new SystemPropertyEntities.SystemPropertyEntryString("UserProfileURL", "http://mig.me/sites/%1/profile/home?username=")),
      MAX_CHATROOM_BANS(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxChatRoomBans", 6)),
      CHATROOM_BANS_BEFORE_SUSPENSION(new SystemPropertyEntities.SystemPropertyEntryInteger("ChatRoomBansBeforeSuspension", 3)),
      CHATROOM_BAN_ERROR_MESSAGE(new SystemPropertyEntities.SystemPropertyEntryString("ChatRoomBanErrorMessage", "You are banned from chatrooms. Please try again later.")),
      SHARE_REDIS_DIRECTORY_SLAVE_CONNECTION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ShareRedisDirectorySlaveConnectionEnabled", false)),
      SHARE_REDIS_DIRECTORY_MASTER_CONNECTION_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ShareRedisDirectoryMasterConnectionEnabled", false)),
      SHARE_REDIS_DIRECTORY_SLAVE_CONNECTION_WAIT_IN_MS(new SystemPropertyEntities.SystemPropertyEntryLong("SharedRedisDirectorySlaveWaitInMs", 500L)),
      SHARE_REDIS_DIRECTORY_MASTER_CONNECTION_WAIT_IN_MS(new SystemPropertyEntities.SystemPropertyEntryLong("SharedRedisDirectoryMasterWaitInMs", 500L)),
      SHARED_REDIS_CONNECTION_MAINTENANCE_TASK_RUN_INTERVAL_IN_MS(new SystemPropertyEntities.SystemPropertyEntryLong("SharedRedisConnMaintenanceTaskRunIntervalInMs", 60000L)),
      VIEW_GROUP_MODERATORS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("ViewGroupModeratorsEnabled", true)),
      VALIDATE_FUSION_MSG_RECIPIENT_USERNAME_CHARACTERS(new SystemPropertyEntities.SystemPropertyEntryBoolean("ValidateFusionMsgRecipientUsernameCharacters", true)),
      MIG33_WEB_BASE_URL(new SystemPropertyEntities.SystemPropertyEntryString("Mig33WebBaseURL", "http://www.mig33.com")),
      IMG_WEB_BASE_URL(new SystemPropertyEntities.SystemPropertyEntryString("ImgWebBaseURL", "http://img.mig.me")),
      MIN_MIG_LEVEL_BEFORE_LOGIN_BAN(new SystemPropertyEntities.SystemPropertyEntryInteger("minMigLevelBeforeLoginBan", 0)),
      MIN_MIG_LEVEL_BEFORE_LOGIN_BAN_LOG_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("minMigLevelBeforeLoginBanLogEnabled", false)),
      GMAIL_DOMAINS(new SystemPropertyEntities.SystemPropertyEntryStringArray("GmailDomains", new String[]{"gmail.com", "googlemail.com"})),
      LOGIN_URL(new SystemPropertyEntities.SystemPropertyEntryString("LoginUrl", "https://login.mig.me")),
      USERNAME_AS_SEARCH_KEYWORD_REGEX(new SystemPropertyEntities.SystemPropertyEntryString("UsernameAsSearchKeywordRegEx", "^[a-zA-Z0-9\\.\\-_]{1,20}$")),
      DEFAULT_LANGUAGE(new SystemPropertyEntities.SystemPropertyEntryString("DefaultLanguage", "ENG")),
      BANNEDPASSWORDS(new SystemPropertyEntities.SystemPropertyEntryStringArray("BannedPasswords", new String[]{"\\.adgjm", "0123", "1234", "2345", "3456", "4567", "5678", "6789", "7890", "abcd", "xyz", "(\\w)\\1{3,}", "passwd", "password", "multi", "multy", "sandi", "laluan", "pasavarda", "katavuccollai", "abc123", "123abc", "(\\D\\d)\\1{1,}", "(\\d\\D)\\1{1,}"})),
      MIN_PASSWORD_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("MinPasswordLength", 6)),
      MAX_PASSWORD_LENGTH(new SystemPropertyEntities.SystemPropertyEntryInteger("MaxPasswordLength", 60)),
      SESSIONI_LOG_ICELOCALEXCEPTIONS_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("SessionILogIceLocalExceptionsEnabled", false)),
      REASON_FOR_DISCONNECTING_SPAMMER(new SystemPropertyEntities.SystemPropertyEntryString("ReasonForDisconnectingSpammer", "")),
      LOG_USER_SUSPENSION(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogUserSuspension", true)),
      LOG_SUCCESSFUL_LOGINS(new SystemPropertyEntities.SystemPropertyEntryBoolean("LogSuccessfulLogins", true)),
      CAPTCHA_REQUIRED_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("CaptchaRequiredEnabled", false)),
      FROM_EMAIL_ADDRESS(new SystemPropertyEntities.SystemPropertyEntryString("FromEmailAddress", "donotreply@mig.me")),
      ICE_CONNECTION_STATS(new SystemPropertyEntities.SystemPropertyEntryBoolean("IceConnectionStats", false)),
      ICE_THREAD_STATS(new SystemPropertyEntities.SystemPropertyEntryBoolean("IceThreadStats", false)),
      ASYNC_GIFT_ALL_ENABLED(new SystemPropertyEntities.SystemPropertyEntryBoolean("AsyncGiftAllEnabled", false)),
      VERIFICATION_EMAIL_TEMPLATE_ID(new SystemPropertyEntities.SystemPropertyEntryInteger("VerificationEmailTemplateID", 92)),
      CHECK_AND_POPULATEBCL(new SystemPropertyEntities.SystemPropertyEntryBoolean("CheckAndPopulateBCL", true)),
      ENABLE_LOOKUP_UNS_VIA_ICE(new SystemPropertyEntities.SystemPropertyEntryBoolean("EnableLookupUnsViaIce", false));

      private SystemPropertyEntities.SystemPropertyEntry<?> value;

      private Default(SystemPropertyEntities.SystemPropertyEntry<?> value) {
         this.value = value;
      }

      public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
         return this.value;
      }

      public String getName() {
         return this.value.getName();
      }
   }

   private static class PropertyNamesSingletonHolder {
      public static final Map<String, SystemPropertyEntities.SystemPropertyEntryInterface> PROPERTY_NAMES_TO_ENTRIES_MAP = SystemPropertyEntities.buildSystemPropertyEntryInterfaceMap();
   }

   public interface SystemPropertyEntryInterfaceWithNamespace extends SystemPropertyEntities.SystemPropertyEntryInterface {
      String getNamespace();
   }

   public interface SystemPropertyEntryInterface {
      SystemPropertyEntities.SystemPropertyEntry<?> getEntry();

      String getName();
   }

   public static class SystemPropertyEntryDoubleArray extends SystemPropertyEntities.SystemPropertyEntry<double[]> {
      public SystemPropertyEntryDoubleArray(String name, double[] defaultValue) {
         super(name, defaultValue);
      }
   }

   public static class SystemPropertyEntryIntegerArray extends SystemPropertyEntities.SystemPropertyEntry<int[]> {
      public SystemPropertyEntryIntegerArray(String name, int[] defaultValue) {
         super(name, defaultValue);
      }
   }

   public static class SystemPropertyEntryShortArray extends SystemPropertyEntities.SystemPropertyEntry<short[]> {
      public SystemPropertyEntryShortArray(String name, short[] defaultValue) {
         super(name, defaultValue);
      }
   }

   public static class SystemPropertyEntryStringArray extends SystemPropertyEntities.SystemPropertyEntry<String[]> {
      private static String[] ZERO_LENGTH_STRING = new String[0];

      public SystemPropertyEntryStringArray(String name, String[] defaultValue) {
         super(name, defaultValue);
      }

      public SystemPropertyEntryStringArray(String name) {
         super(name, ZERO_LENGTH_STRING);
      }
   }

   public static class SystemPropertyEntryDouble extends SystemPropertyEntities.SystemPropertyEntry<Double> {
      public SystemPropertyEntryDouble(String name, Double defaultValue) {
         super(name, defaultValue);
      }
   }

   public static class SystemPropertyEntryLong extends SystemPropertyEntities.SystemPropertyEntry<Long> {
      public SystemPropertyEntryLong(String name, Long defaultValue) {
         super(name, defaultValue);
      }
   }

   public static class SystemPropertyEntryByte extends SystemPropertyEntities.SystemPropertyEntry<Byte> {
      public SystemPropertyEntryByte(String name, Byte defaultValue) {
         super(name, defaultValue);
      }
   }

   public static class SystemPropertyEntryInteger extends SystemPropertyEntities.SystemPropertyEntry<Integer> {
      public SystemPropertyEntryInteger(String name, Integer defaultValue) {
         super(name, defaultValue);
      }
   }

   public static class SystemPropertyEntryBoolean extends SystemPropertyEntities.SystemPropertyEntry<Boolean> {
      public SystemPropertyEntryBoolean(String name, Boolean defaultValue) {
         super(name, defaultValue);
      }
   }

   public static class SystemPropertyEntryString extends SystemPropertyEntities.SystemPropertyEntry<String> {
      public SystemPropertyEntryString(String name, String defaultValue) {
         super(name, defaultValue);
      }
   }

   public abstract static class SystemPropertyEntry<ValueType> {
      protected String name;
      protected ValueType defaultValue;

      public SystemPropertyEntry(String name, ValueType defaultValue) {
         this.name = name;
         this.defaultValue = defaultValue;
      }

      public String getName() {
         return this.name;
      }

      public ValueType getDefaultValue() {
         return this.defaultValue;
      }

      public void setDefaultValue(ValueType val) {
         this.defaultValue = val;
      }
   }
}
