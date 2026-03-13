package com.projectgoth.fusion.clientsession;

import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.gateway.packet.FusionPktError;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public interface SSOLoginClientContext {
   AuthenticationServicePrx findAuthenticationServiceProxy(SSOLoginSessionInfo var1);

   void captchaRequired(SSOLoginSessionInfo var1);

   void ssoSessionCreated(SSOLoginSessionInfo var1, UserData var2) throws RemoteException, CreateException, FusionException, LoginException;

   void errorOccurred(String var1);

   void errorOccurred(FusionPktError.Code var1, String var2);

   void errorOccurred(String var1, Exception var2);
}
