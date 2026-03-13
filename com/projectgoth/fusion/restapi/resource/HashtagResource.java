package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.json.JSONObject;

@Provider
@Path("/hashtag")
public class HashtagResource {
   @GET
   @Path("/countries")
   @Produces({"application/json"})
   public DataHolder<List<CountryData>> getCountriesSupporetdHashtag() throws FusionRestException {
      List countryData = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         countryData = misBean.getCountriesSupportedHashtag();
         return new DataHolder(countryData);
      } catch (Exception var3) {
         throw new FusionRestException(101, "Internal Server Error: Could not fetch supported hashtag country");
      }
   }

   @POST
   @Path("/description")
   @Produces({"application/json"})
   public void updateHashTagDescription(@QueryParam("hashtag") String hashtag, @QueryParam("countryId") int countryId, String jsonString) throws FusionRestException {
      try {
         JSONObject jsonObject = new JSONObject(jsonString);
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         misBean.updateHashTagData("#" + hashtag, countryId, jsonObject.getString("description"));
      } catch (Exception var6) {
         throw new FusionRestException(101, "Internal Server Error: Could not update description hashtag");
      }
   }
}
