package com.projectgoth.fusion.recommendation.collector;

import java.util.Collection;

public interface ICollectorSink<TLoggable> {
   String getName();

   void write(Collection<TLoggable> var1) throws CollectorSinkException;
}
