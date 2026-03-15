package com.projectgoth.fusion.reputation.domain;

public interface Metrics {
   char DELIMETER = ',';

   String toLine();

   boolean hasMetrics();
}
