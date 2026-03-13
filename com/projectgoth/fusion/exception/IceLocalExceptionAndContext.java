package com.projectgoth.fusion.exception;

import Ice.LocalException;
import Ice.ObjectPrx;

public class IceLocalExceptionAndContext extends RuntimeException {
   private final LocalException rootEx;
   private final ObjectPrx contextPrx;

   public IceLocalExceptionAndContext(LocalException rootEx, ObjectPrx contextPrx) {
      this.rootEx = rootEx;
      this.contextPrx = contextPrx;
   }

   public LocalException getRootException() {
      return this.rootEx;
   }

   public ObjectPrx getProxy() {
      return this.contextPrx;
   }
}
