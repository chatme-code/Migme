package com.projectgoth.fusion.interfaces;

import java.util.Collection;
import jakarta.ejb.EJBLocalObject;

public interface MetricsLocal extends EJBLocalObject {
   boolean logMetricsSampleSummaries(String var1, String var2, Collection var3);
}
