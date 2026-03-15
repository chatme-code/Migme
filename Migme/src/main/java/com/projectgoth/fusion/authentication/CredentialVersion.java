package com.projectgoth.fusion.authentication;

import com.projectgoth.fusion.common.ValueEnum;
import java.io.Serializable;

public enum CredentialVersion implements ValueEnum<Byte>, Serializable {
   UNENCRYPTED_UNMIGRATED((byte)0),
   UNENCRYPTED_MIGRATED((byte)1),
   KEYCZAR((byte)10);

   private final byte value;

   private CredentialVersion(byte value) {
      this.value = value;
   }

   public Byte value() {
      return this.value;
   }

   public static CredentialVersion fromValue(byte value) {
      CredentialVersion[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         CredentialVersion e = arr$[i$];
         if (e.value() == value) {
            return e;
         }
      }

      return null;
   }
}
