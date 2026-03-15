package com.projectgoth.fusion.userevent.system.loadbalancing;

import com.projectgoth.fusion.common.HashUtils;

public class FNVHashFunction implements HashFunction {
   public long hash(String input) {
      return HashUtils.truncateToUnsigned32Bits(HashUtils.fnv(input));
   }
}
