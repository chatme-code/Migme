package com.projectgoth.fusion.authentication;

import Ice.Current;
import com.projectgoth.fusion.common.RequestAndRateLongCounter;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.AuthenticationServiceStats;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice._AuthenticationServiceAdminDisp;

public class AuthenticationServiceAdminI extends _AuthenticationServiceAdminDisp {
   private AuthenticationServiceI authenticationService;

   public AuthenticationServiceAdminI(AuthenticationServiceI authenticationService) {
      this.authenticationService = authenticationService;
   }

   public AuthenticationServiceStats getStats(Current __current) throws FusionException {
      AuthenticationServiceStats stats = ServiceStatsFactory.getAuthenticationServiceStats(AuthenticationService.startTime);
      RequestAndRateLongCounter success = this.authenticationService.getSuccessfulAuthentications();
      RequestAndRateLongCounter fails = this.authenticationService.getFailedAuthentications();
      stats.successfulAuthentications = success.getTotalRequests();
      stats.failedAuthentications = fails.getTotalRequests();
      stats.successfulAuthenticationRate = success.getRequestsPerSecond();
      stats.failedAuthenticationRate = fails.getRequestsPerSecond();
      stats.peakSuccessfulAuthenticationRate = success.getMaxRequestsPerSecond();
      stats.peakFailedAuthenticationRate = fails.getMaxRequestsPerSecond();
      stats.peakSuccessfulAuthenticationRateDate = success.getDateOfMaxRequestsPerSecond().getTime();
      stats.peakFailedAuthenticationRateDate = fails.getDateOfMaxRequestsPerSecond().getTime();
      return stats;
   }
}
