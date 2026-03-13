package com.projectgoth.fusion.restapi.data;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "settings"
)
public class SettingsAccountCommunicationData {
   public SettingsEnums.EveryoneFollowerFriend chat;
   public SettingsEnums.OnOff buzz;
   public SettingsEnums.OnOff lookout;
   public SettingsEnums.EveryoneFollowerFriendHide footprints;
   public SettingsEnums.EveryoneOrFollowerAndFriend feed;
   public static final SettingsEnums.EveryoneFollowerFriend PRIVACY_DEFAULT_CHAT;
   public static final SettingsEnums.OnOff PRIVACY_DEFAULT_BUZZ;
   public static final SettingsEnums.OnOff PRIVACY_DEFAULT_LOOKOUT;
   public static final SettingsEnums.EveryoneFollowerFriendHide PRIVACY_DEFAULT_FOOTPRINTS;
   public static final SettingsEnums.EveryoneOrFollowerAndFriend PRIVACY_DEFAULT_FEED;

   public SettingsAccountCommunicationData() {
   }

   public SettingsAccountCommunicationData(Map<String, Integer> privacySettings) {
      this.chat = privacySettings.containsKey("ChatPv") ? SettingsEnums.EveryoneFollowerFriend.fromValue((Integer)privacySettings.get("ChatPv")) : PRIVACY_DEFAULT_CHAT;
      this.buzz = privacySettings.containsKey("BuzzPv") ? SettingsEnums.OnOff.fromValue((Integer)privacySettings.get("BuzzPv")) : PRIVACY_DEFAULT_BUZZ;
      this.lookout = privacySettings.containsKey("LOPv") ? SettingsEnums.OnOff.fromValue((Integer)privacySettings.get("LOPv")) : PRIVACY_DEFAULT_LOOKOUT;
      this.footprints = privacySettings.containsKey("FPPv") ? SettingsEnums.EveryoneFollowerFriendHide.fromValue((Integer)privacySettings.get("FPPv")) : PRIVACY_DEFAULT_FOOTPRINTS;
      this.feed = privacySettings.containsKey("FeedPv") ? SettingsEnums.EveryoneOrFollowerAndFriend.fromValue((Integer)privacySettings.get("FeedPv")) : PRIVACY_DEFAULT_FEED;
   }

   public Map<String, Integer> retrievePrivacy() {
      Map<String, Integer> values = new HashMap();
      values.put("ChatPv", (this.chat == null ? PRIVACY_DEFAULT_CHAT : this.chat).value());
      values.put("BuzzPv", (this.buzz == null ? PRIVACY_DEFAULT_BUZZ : this.buzz).value());
      values.put("LOPv", (this.lookout == null ? PRIVACY_DEFAULT_LOOKOUT : this.lookout).value());
      values.put("FPPv", (this.footprints == null ? PRIVACY_DEFAULT_FOOTPRINTS : this.footprints).value());
      values.put("FeedPv", (this.feed == null ? PRIVACY_DEFAULT_FEED : this.feed).value());
      return values;
   }

   static {
      PRIVACY_DEFAULT_CHAT = SettingsEnums.EveryoneFollowerFriend.EVERYONE;
      PRIVACY_DEFAULT_BUZZ = SettingsEnums.OnOff.ON;
      PRIVACY_DEFAULT_LOOKOUT = SettingsEnums.OnOff.ON;
      PRIVACY_DEFAULT_FOOTPRINTS = SettingsEnums.EveryoneFollowerFriendHide.HIDE;
      PRIVACY_DEFAULT_FEED = SettingsEnums.EveryoneOrFollowerAndFriend.EVERYONE;
   }
}
