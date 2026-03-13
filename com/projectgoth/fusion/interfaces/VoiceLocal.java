package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.CallData;
import java.util.List;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface VoiceLocal extends EJBLocalObject {
   String getDIDNumber(int var1) throws EJBException;

   String getFullDIDNumber(int var1) throws EJBException;

   CallData evaluatePhoneCall(CallData var1) throws EJBException;

   CallData initiatePhoneCall(CallData var1) throws EJBException;

   CallData getCallEntryWithCost(int var1) throws EJBException;

   List getCallEntries(String var1) throws EJBException;

   List getCallEntriesWithCost(String var1) throws EJBException;

   List getCallEntries(String var1, Integer var2) throws EJBException;
}
