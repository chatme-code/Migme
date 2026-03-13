package com.projectgoth.fusion.search;

import java.util.Iterator;
import org.json.JSONObject;

public abstract class BaseIndex {
   private static final int MAX_UPDATE_ATTEMPTS = 20;
   private static final int SLEEP_BETWEEN_ATTEMPTS = 100;

   private static JSONObject getDocumentForUpdate(ElasticSearch.IndexType indexType, ElasticSearch.DocumentType documentType, int id, JSONObject defaultFields) throws Exception {
      JSONObject document = ElasticSearch.get(indexType, documentType, id);
      if (document == null) {
         document = new JSONObject();
         document.put("_version", -1);
         document.put("_source", new JSONObject());
         JSONObject source = document.getJSONObject("_source");
         Iterator it = defaultFields.keys();

         while(it.hasNext()) {
            String key = (String)it.next();
            source.put(key, defaultFields.get(key));
         }
      }

      return document;
   }

   protected static void updateDocument(ElasticSearch.IndexType indexType, ElasticSearch.DocumentType documentType, int id, JSONObject fieldsToUpdate, JSONObject defaultFields) throws Exception {
      int attempt = 1;

      while(attempt <= 20) {
         try {
            JSONObject document = getDocumentForUpdate(indexType, documentType, id, defaultFields);
            JSONObject source = document.getJSONObject("_source");
            if (fieldsToUpdate != null) {
               Iterator it = fieldsToUpdate.keys();

               while(it.hasNext()) {
                  String key = (String)it.next();
                  source.put(key, fieldsToUpdate.get(key));
               }
            }

            ElasticSearch.index(indexType, documentType, id, document.getInt("_version"), source);
            return;
         } catch (VersionConflictException var10) {
            Thread.sleep(100L);
            ++attempt;
         }
      }

      throw new Exception("Update timed out (too many VersionConflictExceptions)");
   }
}
