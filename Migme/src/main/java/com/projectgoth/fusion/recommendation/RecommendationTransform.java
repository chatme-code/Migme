package com.projectgoth.fusion.recommendation;

import java.sql.ResultSet;

public class RecommendationTransform {
   private final int id;
   private final String name;
   private final String hivescript_UNUSED;
   private final String precompileScript;
   private final String retrievalQuery;
   private final String hivescripturl_UNUSED;
   private final long runInterval;
   private final String type;
   private final String subType;
   private final String domain;
   private final long expiry;
   private final int status;

   public RecommendationTransform(ResultSet rs) throws Exception {
      this.id = rs.getInt("id");
      this.name = rs.getString("name");
      this.hivescript_UNUSED = rs.getString("hivescript");
      this.precompileScript = rs.getString("hive_precompile_script");
      this.retrievalQuery = rs.getString("hive_retrieval_query");
      this.hivescripturl_UNUSED = rs.getString("hivescripturl");
      this.runInterval = rs.getLong("runinterval");
      this.type = rs.getString("type");
      this.subType = rs.getString("sub_type");
      this.domain = rs.getString("domain");
      this.expiry = rs.getLong("expiry");
      this.status = rs.getInt("status");
   }

   public int getID() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public int getStatus() {
      return this.status;
   }

   public String getPrecompiledScript() {
      return this.precompileScript;
   }

   public String getRetrievalQuery() {
      return this.retrievalQuery;
   }

   public long getRunInterval() {
      return this.runInterval;
   }

   public String getType() {
      return this.type;
   }

   public String getSubType() {
      return this.subType;
   }

   public String getDomain() {
      return this.domain;
   }

   public long getExpiry() {
      return this.expiry;
   }
}
