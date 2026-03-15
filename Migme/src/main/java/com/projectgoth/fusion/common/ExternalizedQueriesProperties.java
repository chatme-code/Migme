package com.projectgoth.fusion.common;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Required;

public class ExternalizedQueriesProperties implements ExternalizedQueries {
   private Properties queries;

   @Required
   public void setQueries(Properties queries) {
      this.queries = queries;
   }

   public String getQuery(String name) {
      return this.queries.getProperty(name);
   }
}
