package com.projectgoth.fusion.common.log4j;

import java.util.EmptyStackException;
import java.util.Stack;

public class TLSLoggingInvocationCtxStack extends ThreadLocal<Stack<LoggingInvocationCtx>> {
   public static TLSLoggingInvocationCtxStack getInstance() {
      return TLSLoggingInvocationCtxStack.SingletonHolder.getInstance();
   }

   private TLSLoggingInvocationCtxStack() {
   }

   protected Stack<LoggingInvocationCtx> initialValue() {
      return new Stack();
   }

   public LoggingInvocationCtx getCurrentCtx() {
      Stack stk = (Stack)this.get();

      try {
         return (LoggingInvocationCtx)stk.peek();
      } catch (EmptyStackException var3) {
         throw new IllegalStateException("Failed sanity check. No invocation context in the stack.forgot to pushCtx?", var3);
      }
   }

   public boolean ctxExists() {
      Stack<LoggingInvocationCtx> stk = (Stack)this.get();
      return !stk.empty();
   }

   public LoggingInvocationCtx pushCtx() {
      Stack<LoggingInvocationCtx> stk = (Stack)this.get();
      LoggingInvocationCtx ctx = new LoggingInvocationCtx();
      stk.push(ctx);
      return ctx;
   }

   public void popCtx() {
      Stack<LoggingInvocationCtx> stk = (Stack)this.get();
      stk.pop();
   }

   // $FF: synthetic method
   TLSLoggingInvocationCtxStack(Object x0) {
      this();
   }

   private static class SingletonHolder {
      private static TLSLoggingInvocationCtxStack INSTANCE = new TLSLoggingInvocationCtxStack();

      public static TLSLoggingInvocationCtxStack getInstance() {
         return INSTANCE;
      }
   }
}
