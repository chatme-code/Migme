package com.projectgoth.fusion.restapi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

class GzipDecompressingEntity extends HttpEntityWrapper {
   public GzipDecompressingEntity(HttpEntity entity) {
      super(entity);
   }

   public InputStream getContent() throws IOException, IllegalStateException {
      InputStream content = this.wrappedEntity.getContent();
      return new GZIPInputStream(content);
   }

   public long getContentLength() {
      return -1L;
   }
}
