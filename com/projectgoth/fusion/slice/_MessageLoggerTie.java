package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _MessageLoggerTie extends _MessageLoggerDisp implements TieBase {
   private _MessageLoggerOperations _ice_delegate;

   public _MessageLoggerTie() {
   }

   public _MessageLoggerTie(_MessageLoggerOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_MessageLoggerOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _MessageLoggerTie) ? false : this._ice_delegate.equals(((_MessageLoggerTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void logMessage(int type, int sourceCountryID, String source, String destination, int numRecipients, String messageText, Current __current) {
      this._ice_delegate.logMessage(type, sourceCountryID, source, destination, numRecipients, messageText, __current);
   }
}
