package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.leto.common.event.chat.ThirdPartyInstantMessageEvent;
import org.apache.log4j.Logger;

public class ThirdPartyInstantMessageTrigger extends RewardProgramTrigger implements ThirdPartyInstantMessageEvent {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ThirdPartyInstantMessageTrigger.class));

   public ThirdPartyInstantMessageTrigger(UserData userData, MessageType imType, ThirdPartyInstantMessageTrigger.EventTypeEnum eventType) {
      super(RewardProgramData.TypeEnum.MANUAL, userData);
      if (eventType == ThirdPartyInstantMessageTrigger.EventTypeEnum.MESSAGE_SENT) {
         switch(imType) {
         case AIM:
            this.programType = RewardProgramData.TypeEnum.AIM_SENT;
            break;
         case FACEBOOK:
            this.programType = RewardProgramData.TypeEnum.FACEBOOK_SENT;
            break;
         case GTALK:
            this.programType = RewardProgramData.TypeEnum.GTALK_SENT;
            break;
         case YAHOO:
            this.programType = RewardProgramData.TypeEnum.YAHOO_SENT;
            break;
         case MSN:
            this.programType = RewardProgramData.TypeEnum.MSN_SENT;
            break;
         default:
            log.warn("Unknown instant message type : " + imType);
         }
      } else if (eventType == ThirdPartyInstantMessageTrigger.EventTypeEnum.MESSAGE_RECEIVED) {
         switch(imType) {
         case AIM:
            this.programType = RewardProgramData.TypeEnum.AIM_RECEIVED;
            break;
         case FACEBOOK:
            this.programType = RewardProgramData.TypeEnum.FACEBOOK_RECEIVED;
            break;
         case GTALK:
            this.programType = RewardProgramData.TypeEnum.GTALK_RECEIVED;
            break;
         case YAHOO:
            this.programType = RewardProgramData.TypeEnum.YAHOO_RECEIVED;
            break;
         case MSN:
            this.programType = RewardProgramData.TypeEnum.MSN_RECEIVED;
            break;
         default:
            log.warn("Unknown instant message type : " + imType);
         }
      } else {
         log.warn("Unknown eventType : " + eventType);
      }

   }

   public static enum EventTypeEnum {
      MESSAGE_SENT,
      MESSAGE_RECEIVED;
   }
}
