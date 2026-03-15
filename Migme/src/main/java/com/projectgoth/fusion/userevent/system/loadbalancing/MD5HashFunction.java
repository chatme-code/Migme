package com.projectgoth.fusion.userevent.system.loadbalancing;

import com.projectgoth.fusion.common.HashUtils;

public class MD5HashFunction implements HashFunction {
   public long hash(String input) {
      return HashUtils.truncateToUnsigned32Bits(HashUtils.md5asLong(input));
   }
}
