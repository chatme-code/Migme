package com.projectgoth.fusion.userevent.store;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class StoreUserEventDA {
   PrimaryIndex<String, StoreUserEvent> primaryIndex;

   public StoreUserEventDA(EntityStore store) throws DatabaseException {
      this.primaryIndex = store.getPrimaryIndex(String.class, StoreUserEvent.class);
   }

   public PrimaryIndex<String, StoreUserEvent> getPrimaryIndex() {
      return this.primaryIndex;
   }
}
