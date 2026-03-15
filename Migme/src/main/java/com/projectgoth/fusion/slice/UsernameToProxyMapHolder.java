package com.projectgoth.fusion.slice;

import java.util.Map;

public final class UsernameToProxyMapHolder {
   public Map<String, UserPrx> value;

   public UsernameToProxyMapHolder() {
   }

   public UsernameToProxyMapHolder(Map<String, UserPrx> value) {
      this.value = value;
   }
}
