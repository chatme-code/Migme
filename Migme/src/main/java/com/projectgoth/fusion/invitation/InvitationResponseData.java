package com.projectgoth.fusion.invitation;

import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.leto.common.event.invites.InviteeResponse;
import com.projectgoth.leto.common.utils.enums.ValueToEnumMap;
import java.io.Serializable;
import java.util.Date;

public class InvitationResponseData implements Serializable {
   public long id;
   public int invitationId;
   public Date responseTime;
   public InvitationResponseData.ResponseType responseType;
   public String username;

   public static enum ResponseType implements EnumUtils.IEnumValueGetter<Integer> {
      SIGN_UP_UNVERIFIED(InviteeResponse.SIGN_UP_UNVERIFIED),
      SIGN_UP_VERIFIED(InviteeResponse.SIGN_UP_VERIFIED),
      ACCEPT_INVITATION(InviteeResponse.ACCEPT_INVITATION),
      REJECT_INVITATION(InviteeResponse.REJECT_INVITATION),
      LOGIN_USING_EXISTING_ACCOUNT(InviteeResponse.LOGIN_USING_EXISTING_ACCOUNT),
      INVALIDATE(InviteeResponse.INVALIDATE);

      private InviteeResponse inviteeResponseType;

      private ResponseType(InviteeResponse inviteeResponseType) {
         this.inviteeResponseType = inviteeResponseType;
      }

      public Integer getEnumValue() {
         return this.inviteeResponseType.getEnumValue();
      }

      public int getTypeCode() {
         return this.getEnumValue();
      }

      public InviteeResponse toInviteeResponseType() {
         return this.inviteeResponseType;
      }

      public static InvitationResponseData.ResponseType fromTypeCode(int typeCode) {
         return (InvitationResponseData.ResponseType)InvitationResponseData.ResponseType.ValueToEnumMapInstance.INSTANCE.toEnum(typeCode);
      }

      private static final class ValueToEnumMapInstance {
         private static final ValueToEnumMap<Integer, InvitationResponseData.ResponseType> INSTANCE = new ValueToEnumMap(InvitationResponseData.ResponseType.class);
      }
   }
}
