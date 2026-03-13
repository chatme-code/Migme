package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.RecommendationDAOChain;
import com.projectgoth.fusion.recommendation.RecommendationTransform;
import com.projectgoth.fusion.recommendation.RecommendationTransformParam;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class FusionDbRecommendationDAOChain extends RecommendationDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbRecommendationDAOChain.class);

   public Map<String, RecommendationTransformParam> getRecommendationTransformParamsByName(int transformID) throws DAOException {
      List<RecommendationTransformParam> params = this.getRecommendationTransformParams(transformID);
      Map<String, RecommendationTransformParam> map = new HashMap();
      Iterator i$ = params.iterator();

      while(i$.hasNext()) {
         RecommendationTransformParam p = (RecommendationTransformParam)i$.next();
         map.put(p.getName(), p);
      }

      return map;
   }

   public List<RecommendationTransformParam> getRecommendationTransformParams(int transformID) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         String sql = "select * from recommendationtransformparam where transformid = ?";
         ps = conn.prepareStatement("select * from recommendationtransformparam where transformid = ?");
         ps.setInt(1, transformID);
         rs = ps.executeQuery();
         ArrayList results = new ArrayList();

         while(rs.next()) {
            RecommendationTransformParam param = new RecommendationTransformParam(rs.getString("name"), rs.getString("value"));
            results.add(param);
         }

         ArrayList var15 = results;
         return var15;
      } catch (Exception var12) {
         log.error("Unable to getRecommendationTransformParams with transformID=" + transformID + " e=" + var12, var12);
         var6 = super.getRecommendationTransformParams(transformID);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public RecommendationTransform getRecommendationTransform(int transformID) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      RecommendationTransform rt;
      try {
         conn = DBUtils.getFusionReadConnection();
         String sql = "select * from recommendationtransform where id=?";
         ps = conn.prepareStatement("select * from recommendationtransform where id=?");
         ps.setInt(1, transformID);
         rs = ps.executeQuery();
         if (rs.next()) {
            rt = new RecommendationTransform(rs);
            RecommendationTransform var7 = rt;
            return var7;
         }

         rt = null;
      } catch (Exception var12) {
         log.error("Unable to getRecommendationTransform with transformID=" + transformID + " e=" + var12, var12);
         rt = super.getRecommendationTransform(transformID);
         return rt;
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return rt;
   }

   public List<RecommendationTransform> getActiveRecommendationTransforms() throws DAOException {
      if (log.isDebugEnabled()) {
         log.info("getRecommendationTransforms on FusionDbRecommendationDAOChain=" + this.hashCode());
      }

      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var5;
      try {
         conn = DBUtils.getFusionReadConnection();
         String sql = "select * from recommendationtransform where status = 1";
         ps = conn.prepareStatement("select * from recommendationtransform where status = 1");
         rs = ps.executeQuery();
         ArrayList results = new ArrayList();

         while(rs.next()) {
            RecommendationTransform rt = new RecommendationTransform(rs);
            results.add(rt);
         }

         ArrayList var14 = results;
         return var14;
      } catch (Exception var11) {
         log.error("Unable to getRecommendationTransforms", var11);
         var5 = super.getActiveRecommendationTransforms();
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var5;
   }
}
