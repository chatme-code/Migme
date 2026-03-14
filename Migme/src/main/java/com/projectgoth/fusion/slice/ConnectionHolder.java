package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ConnectionHolder {
   public Connection value;

   public ConnectionHolder() {
   }

   public ConnectionHolder(Connection value) {
      this.value = value;
   }

   public ConnectionHolder.Patcher getPatcher() {
      return new ConnectionHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ConnectionHolder.this.value = (Connection)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::Connection";
      }
   }
}
