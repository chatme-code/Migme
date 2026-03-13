package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmailTemplateData implements Serializable {
   public Integer id;
   public String name;
   public int templateType;
   public String subjectTemplate;
   public String bodyTemplate;
   public String mimeType;
   private List<EmailTemplateData.PartTemplateData> partList = new ArrayList();

   public EmailTemplateData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.name = rs.getString("name");
      this.templateType = rs.getInt("templatetype");
      this.subjectTemplate = rs.getString("subjecttemplate");
      this.bodyTemplate = rs.getString("bodytemplate");
      this.mimeType = rs.getString("mimetype");
   }

   public boolean addPartTemplate(ResultSet rs) throws SQLException {
      boolean hasValues = false;
      int partid = rs.getInt("partid");
      if (!rs.wasNull()) {
         hasValues = true;
         EmailTemplateData.PartTemplateData partTemplateData = new EmailTemplateData.PartTemplateData();
         int parenttemplateid = rs.getInt("parenttemplateid");
         int partSequence = rs.getInt("partsequence");
         String partContentTemplate = rs.getString("partcontenttemplate");
         String partMimeType = rs.getString("partmimetype");
         partTemplateData.partid = partid;
         partTemplateData.sequence = partSequence;
         partTemplateData.contentTemplate = partContentTemplate;
         partTemplateData.mimeType = partMimeType;
         partTemplateData.parenttemplateid = parenttemplateid;
         this.partList.add(partTemplateData);
      }

      return hasValues;
   }

   public EmailTemplateData.PartTemplateData getExtraPart(int index) {
      return (EmailTemplateData.PartTemplateData)this.partList.get(index);
   }

   public int getExtraPartCount() {
      return this.partList.size();
   }

   public static class PartTemplateData implements Serializable {
      public int parenttemplateid;
      public int partid;
      public int sequence;
      public String contentTemplate;
      public String mimeType;
   }
}
