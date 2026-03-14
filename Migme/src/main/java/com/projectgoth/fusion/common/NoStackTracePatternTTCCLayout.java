package com.projectgoth.fusion.common;

import org.apache.log4j.TTCCLayout;

public class NoStackTracePatternTTCCLayout extends TTCCLayout {
   public boolean ignoresThrowable() {
      return false;
   }
}
