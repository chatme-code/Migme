package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/countries")
public class CountriesResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CountriesResource.class));

   @GET
   @Path("/all")
   @Produces({"application/json"})
   public DataHolder<List<CountryData>> getCountries() throws FusionRestException {
      List countrylist = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         countrylist = misBean.getCountries();
         return new DataHolder(countrylist);
      } catch (Exception var3) {
         log.error("Exception caught while removing user from blacklist: " + var3.getMessage());
         throw new FusionRestException(101, "Internal Server Error: Could not fetch countrylist");
      }
   }

   @GET
   @Path("/{ipNumber}")
   @Produces({"application/json"})
   public DataHolder<CountryData> getCountryFromIPNumber(@PathParam("ipNumber") String ipNumber) throws FusionRestException {
      CountryData countryData = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         countryData = misBean.getCountryFromIPNumber(Double.parseDouble(ipNumber));
         return new DataHolder(countryData);
      } catch (Exception var4) {
         throw new FusionRestException(101, "Internal Server Error: Could not fetch countryData for the given ipNumber");
      }
   }

   @GET
   @Path("/location/{id}")
   @Produces({"application/json"})
   public DataHolder<CountryData> getCountryByLocation(@PathParam("id") int id) throws FusionRestException {
      CountryData countryData = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         countryData = misBean.getCountryByLocation(id);
         return new DataHolder(countryData);
      } catch (Exception var4) {
         throw new FusionRestException(101, "Internal Server Error: Could not fetch countryData for the given id");
      }
   }
}
