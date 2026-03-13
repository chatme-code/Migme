package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CallData;
import java.rmi.RemoteException;
import java.util.List;
import javax.ejb.EJBObject;

public interface Voice extends EJBObject {
   String getDIDNumber(int var1) throws RemoteException;

   String getFullDIDNumber(int var1) throws RemoteException;

   CallData evaluatePhoneCall(CallData var1) throws RemoteException;

   CallData initiatePhoneCall(CallData var1) throws RemoteException;

   CallData getCallEntryWithCost(int var1) throws RemoteException;

   List getCallEntries(String var1) throws RemoteException;

   List getCallEntriesWithCost(String var1) throws RemoteException;

   List getCallEntries(String var1, Integer var2) throws RemoteException;

   void updateCallDetail(CallData var1) throws RemoteException;

   void chargeCall(CallData var1, AccountEntrySourceData var2) throws RemoteException;

   List getVoiceGateways() throws RemoteException;

   List getVoiceGateways(int var1) throws RemoteException;
}
