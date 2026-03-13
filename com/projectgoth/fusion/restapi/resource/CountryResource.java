package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Path("/country")
public class CountryResource {
   @GET
   @Path("/{id}")
   @Produces({"application/json"})
   public DataHolder<CountryData> getCountry(@PathParam("id") int id) throws FusionRestException {
      CountryData countryData = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         countryData = misBean.getCountry(id);
         return new DataHolder(countryData);
      } catch (Exception var4) {
         throw new FusionRestException(101, "Internal Server Error: Could not fetch countryData for the given id");
      }
   }
}
