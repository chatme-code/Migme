package com.projectgoth.fusion.common.metrics;

public class MetricsEnums {
   public static enum MetricType {
      BOOLEAN,
      STRING,
      INTEGER,
      DOUBLE;
   }

   public static enum FusionRestMetrics implements MetricsEnums.MetricsEntryInterface {
      MIGBO_NEW_POST_APPLICATION(new MetricsEnums.StringMetricsEntry(MetricsEnums.Scope.FUSIONREST, "NewPost", "Application")),
      MIGBO_NEW_POST_TYPE(new MetricsEnums.StringMetricsEntry(MetricsEnums.Scope.FUSIONREST, "NewPost", "Type")),
      MIGBO_NEW_POST_ORIGINALITY(new MetricsEnums.StringMetricsEntry(MetricsEnums.Scope.FUSIONREST, "NewPost", "Originality")),
      TEST_PING(new MetricsEnums.StringMetricsEntry(MetricsEnums.Scope.FUSIONREST, "TestPing", "TestPing"));

      private MetricsEnums.MetricsEntry value;

      private FusionRestMetrics(MetricsEnums.MetricsEntry value) {
         this.value = value;
      }

      public String getEventName() {
         return this.value.eventName;
      }

      public String getMetricName() {
         return this.value.metricName;
      }

      public MetricsEnums.MetricType getMetricType() {
         return this.value.metricType;
      }

      public MetricsEnums.Scope getScope() {
         return this.value.getScope();
      }

      public MetricsEnums.MetricsEntry getEntry() {
         return this.value;
      }

      public String getSimpleName() {
         return this.value.scope + ":" + this.value.eventName + ":" + this.value.metricName;
      }
   }

   public interface MetricsEntryInterface {
      String getEventName();

      String getMetricName();

      MetricsEnums.MetricType getMetricType();

      MetricsEnums.Scope getScope();

      MetricsEnums.MetricsEntry getEntry();

      String getSimpleName();
   }

   public static class DoubleMetricsEntry extends MetricsEnums.MetricsEntry {
      DoubleMetricsEntry(MetricsEnums.Scope scope, String eventName, String metricName) {
         super(scope, eventName, metricName, MetricsEnums.MetricType.DOUBLE);
      }
   }

   public static class IntegerMetricsEntry extends MetricsEnums.MetricsEntry {
      IntegerMetricsEntry(MetricsEnums.Scope scope, String eventName, String metricName) {
         super(scope, eventName, metricName, MetricsEnums.MetricType.INTEGER);
      }
   }

   public static class StringMetricsEntry extends MetricsEnums.MetricsEntry {
      StringMetricsEntry(MetricsEnums.Scope scope, String eventName, String metricName) {
         super(scope, eventName, metricName, MetricsEnums.MetricType.STRING);
      }
   }

   public static class BooleanMetricsEntry extends MetricsEnums.MetricsEntry {
      BooleanMetricsEntry(MetricsEnums.Scope scope, String eventName, String metricName) {
         super(scope, eventName, metricName, MetricsEnums.MetricType.BOOLEAN);
      }
   }

   public abstract static class MetricsEntry {
      protected MetricsEnums.Scope scope;
      protected String eventName;
      protected String metricName;
      protected MetricsEnums.MetricType metricType;

      MetricsEntry(MetricsEnums.Scope scope, String eventName, String metricName, MetricsEnums.MetricType metricType) {
         this.scope = scope;
         this.eventName = eventName;
         this.metricName = metricName;
         this.metricType = metricType;
      }

      public MetricsEnums.Scope getScope() {
         return this.scope;
      }

      public String getEventName() {
         return this.eventName;
      }

      public String getMetricName() {
         return this.metricName;
      }

      public MetricsEnums.MetricType getMetricType() {
         return this.metricType;
      }
   }

   public static enum Scope {
      AUTHENTICATIONSERVICE,
      BOTSERVICE,
      EVENTQUEUEWORKER,
      FUSIONREST,
      GATEWAY,
      JOBSCHEDULINGSERVICE,
      OBJECTCACHE,
      REGISTRY,
      REWARDSYSTEM,
      SESSIONCACHE,
      USERNOTIFICATIONSERVICE;
   }
}
