package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.ContentLocal;
import com.projectgoth.fusion.interfaces.ContentLocalHome;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SecurityQuestion implements Serializable {
   public int id;
   public String question;
   private static final LazyLoader<List<SecurityQuestion>> questions = new LazyLoader<List<SecurityQuestion>>("SECURITY_QEUSTIONS", 3600000L) {
      protected List<SecurityQuestion> fetchValue() throws Exception {
         ContentLocal contentEJB = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         List<SecurityQuestion> questions = contentEJB.getSecurityQeustions();
         return questions;
      }
   };

   public SecurityQuestion(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.question = rs.getString("question");
   }

   public static List<SecurityQuestion> getAllQuestions() {
      return (List)questions.getValue();
   }
}
