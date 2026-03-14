package com.projectgoth.fusion.userevent.store;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class StoreGeneratorEventDA {
   PrimaryIndex<String, StoreGeneratorEvent> primaryIndex;

   public StoreGeneratorEventDA(EntityStore store) throws DatabaseException {
      this.primaryIndex = store.getPrimaryIndex(String.class, StoreGeneratorEvent.class);
   }

   public PrimaryIndex<String, StoreGeneratorEvent> getPrimaryIndex() {
      return this.primaryIndex;
   }
}
