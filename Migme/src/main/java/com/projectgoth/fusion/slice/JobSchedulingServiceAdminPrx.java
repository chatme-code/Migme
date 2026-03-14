package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface JobSchedulingServiceAdminPrx extends ObjectPrx {
   JobSchedulingServiceStats getStats() throws FusionException;

   JobSchedulingServiceStats getStats(Map<String, String> var1) throws FusionException;
}
