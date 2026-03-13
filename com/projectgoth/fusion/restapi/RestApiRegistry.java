package com.projectgoth.fusion.restapi;

import com.projectgoth.fusion.payment.PaymentResource;
import com.projectgoth.fusion.restapi.data.FusionRestExceptionMapper;
import com.projectgoth.fusion.restapi.resource.AccountResource;
import com.projectgoth.fusion.restapi.resource.ApplicationResource;
import com.projectgoth.fusion.restapi.resource.CampaignResource;
import com.projectgoth.fusion.restapi.resource.ChatroomResource;
import com.projectgoth.fusion.restapi.resource.CountriesResource;
import com.projectgoth.fusion.restapi.resource.CountryResource;
import com.projectgoth.fusion.restapi.resource.EventResource;
import com.projectgoth.fusion.restapi.resource.GatingResource;
import com.projectgoth.fusion.restapi.resource.GroupResource;
import com.projectgoth.fusion.restapi.resource.HashtagResource;
import com.projectgoth.fusion.restapi.resource.MISResource;
import com.projectgoth.fusion.restapi.resource.MerchantResource;
import com.projectgoth.fusion.restapi.resource.MigboDataserviceProxyResource;
import com.projectgoth.fusion.restapi.resource.PartnerResource;
import com.projectgoth.fusion.restapi.resource.PromotedResource;
import com.projectgoth.fusion.restapi.resource.RecommendationResource;
import com.projectgoth.fusion.restapi.resource.RegistrationTokenResource;
import com.projectgoth.fusion.restapi.resource.RewardResource;
import com.projectgoth.fusion.restapi.resource.SSOResource;
import com.projectgoth.fusion.restapi.resource.SettingsResource;
import com.projectgoth.fusion.restapi.resource.StoreResource;
import com.projectgoth.fusion.restapi.resource.SystemResource;
import com.projectgoth.fusion.restapi.resource.TestResource;
import com.projectgoth.fusion.restapi.resource.UserResource;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

public class RestApiRegistry extends Application {
   private Set<Object> singletons = new HashSet();
   private Set<Class<?>> classes = new HashSet();

   public RestApiRegistry() {
      this.singletons.add(new UserResource());
      this.singletons.add(new SSOResource());
      this.singletons.add(new SettingsResource());
      this.singletons.add(new EventResource());
      this.singletons.add(new GatingResource());
      this.singletons.add(new TestResource());
      this.singletons.add(new MISResource());
      this.singletons.add(new MigboDataserviceProxyResource());
      this.singletons.add(new PartnerResource());
      this.singletons.add(new ApplicationResource());
      this.singletons.add(new MerchantResource());
      this.singletons.add(new SystemResource());
      this.singletons.add(new ChatroomResource());
      this.singletons.add(new GroupResource());
      this.singletons.add(new RegistrationTokenResource());
      this.singletons.add(new CountriesResource());
      this.singletons.add(new CountryResource());
      this.singletons.add(new PaymentResource());
      this.singletons.add(new RecommendationResource());
      this.singletons.add(new StoreResource());
      this.singletons.add(new HashtagResource());
      this.singletons.add(new RewardResource());
      this.singletons.add(new AccountResource());
      this.singletons.add(new PromotedResource());
      this.singletons.add(new CampaignResource());
      this.classes.add(FusionRestExceptionMapper.class);
   }

   public Set<Class<?>> getClasses() {
      return this.classes;
   }

   public Set<Object> getSingletons() {
      return this.singletons;
   }
}
