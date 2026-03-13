package com.projectgoth.fusion.app.dao;

import com.projectgoth.fusion.app.dao.base.RecommendationDAOChain;
import com.projectgoth.fusion.recommendation.RecommendationTransform;
import com.projectgoth.fusion.recommendation.RecommendationTransformParam;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class RecommendationDAO {
   private static final Logger log = Logger.getLogger(RecommendationDAO.class);
   private final RecommendationDAOChain readChain;
   private final RecommendationDAOChain writeChain;

   public RecommendationDAO(RecommendationDAOChain readChain, RecommendationDAOChain writeChain) {
      if (log.isDebugEnabled()) {
         log.debug("Initializing RecommendationDAO with readChain=" + readChain + " writeChain=" + writeChain);
      }

      this.readChain = readChain;
      this.writeChain = writeChain;
   }

   public Map<String, RecommendationTransformParam> getRecommendationTransformParamsByName(int transformID) throws DAOException {
      return this.readChain.getRecommendationTransformParamsByName(transformID);
   }

   public List<RecommendationTransformParam> getRecommendationTransformParams(int transformID) throws DAOException {
      return this.readChain.getRecommendationTransformParams(transformID);
   }

   public RecommendationTransform getRecommendationTransform(int transformID) throws DAOException {
      return this.readChain.getRecommendationTransform(transformID);
   }

   public List<RecommendationTransform> getActiveRecommendationTransforms() throws DAOException {
      return this.readChain.getActiveRecommendationTransforms();
   }
}
