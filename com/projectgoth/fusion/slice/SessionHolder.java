package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class SessionHolder {
   public Session value;

   public SessionHolder() {
   }

   public SessionHolder(Session value) {
      this.value = value;
   }

   public SessionHolder.Patcher getPatcher() {
      return new SessionHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            SessionHolder.this.value = (Session)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::Session";
      }
   }
}
