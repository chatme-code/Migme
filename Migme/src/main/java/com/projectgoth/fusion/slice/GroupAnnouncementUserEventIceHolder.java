package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GroupAnnouncementUserEventIceHolder {
   public GroupAnnouncementUserEventIce value;

   public GroupAnnouncementUserEventIceHolder() {
   }

   public GroupAnnouncementUserEventIceHolder(GroupAnnouncementUserEventIce value) {
      this.value = value;
   }

   public GroupAnnouncementUserEventIceHolder.Patcher getPatcher() {
      return new GroupAnnouncementUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GroupAnnouncementUserEventIceHolder.this.value = (GroupAnnouncementUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GroupAnnouncementUserEventIce";
      }
   }
}
