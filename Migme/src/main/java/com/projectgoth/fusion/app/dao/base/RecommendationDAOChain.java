package com.projectgoth.fusion.app.dao.base;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.recommendation.RecommendationTransform;
import com.projectgoth.fusion.recommendation.RecommendationTransformParam;
import java.util.List;
import java.util.Map;

public class RecommendationDAOChain implements DAOChain {
   private RecommendationDAOChain nextRead;
   private RecommendationDAOChain nextWrite;

   public void setNextRead(DAOChain a) {
      this.nextRead = (RecommendationDAOChain)a;
   }

   public void setNextWrite(DAOChain a) {
      this.nextWrite = (RecommendationDAOChain)a;
   }

   public Map<String, RecommendationTransformParam> getRecommendationTransformParamsByName(int transformID) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getRecommendationTransformParamsByName(transformID);
      } else {
         throw new DAOException("Unable to getRecommendationTransformParams");
      }
   }

   public List<RecommendationTransformParam> getRecommendationTransformParams(int transformID) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getRecommendationTransformParams(transformID);
      } else {
         throw new DAOException("Unable to getRecommendationTransformParams");
      }
   }

   public RecommendationTransform getRecommendationTransform(int transformID) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getRecommendationTransform(transformID);
      } else {
         throw new DAOException("Unable to getRecommendationTransform");
      }
   }

   public List<RecommendationTransform> getActiveRecommendationTransforms() throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getActiveRecommendationTransforms();
      } else {
         throw new DAOException("Unable to getRecommendationTransforms");
      }
   }
}
