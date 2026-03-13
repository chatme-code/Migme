package com.projectgoth.fusion.interfaces;

import java.util.Collection;
import javax.ejb.EJBLocalObject;

public interface MetricsLocal extends EJBLocalObject {
   boolean logMetricsSampleSummaries(String var1, String var2, Collection var3);
}
