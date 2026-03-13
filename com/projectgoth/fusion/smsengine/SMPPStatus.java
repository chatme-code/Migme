package com.projectgoth.fusion.smsengine;

public enum SMPPStatus {
   UNKNOWN(Integer.MIN_VALUE),
   ESME_ROK(0),
   ESME_RINVMSGLEN(1),
   ESME_RINVCMDLEN(2),
   ESME_RINVCMDID(3),
   ESME_RINVBNDSTS(4),
   ESME_RALYBND(5),
   ESME_RINVPRTFLG(6),
   ESME_RINVREGDLVFLG(7),
   ESME_RSYSERR(8),
   ESME_RINVSRCADR(10),
   ESME_RINVDSTADR(11),
   ESME_RINVMSGID(12),
   ESME_RBINDFAIL(13),
   ESME_RINVPASWD(14),
   ESME_RINVSYSID(15),
   ESME_RCANCELFAIL(17),
   ESME_RREPLACEFAIL(19),
   ESME_RMSGQFUL(20),
   ESME_RINVSERTYP(21),
   ESME_RINVNUMDESTS(51),
   ESME_RINVDLNAME(52),
   ESME_RINVDESTFLAG(64),
   ESME_RINVSUBREP(66),
   ESME_RINVESMCLASS(67),
   ESME_RCNTSUBDL(68),
   ESME_RSUBMITFAIL(69),
   ESME_RINVSRCTON(72),
   ESME_RINVSRCNPI(73),
   ESME_RINVDSTTON(80),
   ESME_RINVDSTNPI(81),
   ESME_RINVSYSTYP(83),
   ESME_RINVREPFLAG(84),
   ESME_RINVNUMMSGS(85),
   ESME_RTHROTTLED(88),
   ESME_RINVSCHED(97),
   ESME_RINVEXPIRY(98),
   ESME_RINVDFTMSGID(99),
   ESME_RX_T_APPN(100),
   ESME_RX_P_APPN(101),
   ESME_RX_R_APPN(102),
   ESME_RQUERYFAIL(103),
   ESME_RINVOPTPARSTREAM(192),
   ESME_ROPTPARNOTALLWD(193),
   ESME_RINVPARLEN(194),
   ESME_RMISSINGOPTPARAM(195),
   ESME_RINVOPTPARAMVAL(196),
   ESME_RDELIVERYFAILURE(254),
   ESME_RUNKNOWNERR(255);

   private int value;

   private SMPPStatus(int value) {
      this.value = value;
   }

   public int value() {
      return this.value;
   }

   public String description() {
      switch(this) {
      case UNKNOWN:
         return "Unknown";
      case ESME_ROK:
         return "No Error";
      case ESME_RINVMSGLEN:
         return "Message Length is invalid";
      case ESME_RINVCMDLEN:
         return "Command Length is invalid";
      case ESME_RINVCMDID:
         return "Invalid Command ID";
      case ESME_RINVBNDSTS:
         return "Incorrect BIND Status for given command";
      case ESME_RALYBND:
         return "ESME Already in Bound State";
      case ESME_RINVPRTFLG:
         return "Invalid Priority Flag";
      case ESME_RINVREGDLVFLG:
         return "Invalid Registered Delivery Flag";
      case ESME_RSYSERR:
         return "System Error";
      case ESME_RINVSRCADR:
         return "Invalid Source Address";
      case ESME_RINVDSTADR:
         return "Invalid Dest Addr";
      case ESME_RINVMSGID:
         return "Message ID is invalid";
      case ESME_RBINDFAIL:
         return "Bind Failed";
      case ESME_RINVPASWD:
         return "Invalid Password";
      case ESME_RINVSYSID:
         return "Invalid System ID";
      case ESME_RCANCELFAIL:
         return "Cancel SM Failed";
      case ESME_RREPLACEFAIL:
         return "Replace SM Failed";
      case ESME_RMSGQFUL:
         return "Message Queue Full";
      case ESME_RINVSERTYP:
         return "Invalid Service Type";
      case ESME_RINVNUMDESTS:
         return "Invalid number of destinations";
      case ESME_RINVDLNAME:
         return "Invalid Distribution List name";
      case ESME_RINVDESTFLAG:
         return "Destination flag is invalid (submit_multi)";
      case ESME_RINVSUBREP:
         return "Invalid �submit with replace� request (i.e. submit_sm with replace_if_present_flag set)";
      case ESME_RINVESMCLASS:
         return "Invalid esm_class field data";
      case ESME_RCNTSUBDL:
         return "Cannot Submit to Distribution List";
      case ESME_RSUBMITFAIL:
         return "submit_sm or submit_multi failed";
      case ESME_RINVSRCTON:
         return "Invalid Source address TON";
      case ESME_RINVSRCNPI:
         return "Invalid Source address NPI";
      case ESME_RINVDSTTON:
         return "Invalid Destination address TON";
      case ESME_RINVDSTNPI:
         return "Invalid Destination address NPI";
      case ESME_RINVSYSTYP:
         return "Invalid system_type field";
      case ESME_RINVREPFLAG:
         return "Invalid replace_if_present flag";
      case ESME_RINVNUMMSGS:
         return "Invalid number of messages";
      case ESME_RTHROTTLED:
         return "Throttling error (ESME has exceeded allowed message limits)";
      case ESME_RINVSCHED:
         return "Invalid Scheduled Delivery Time";
      case ESME_RINVEXPIRY:
         return "Invalid message validity period (Expiry time)";
      case ESME_RINVDFTMSGID:
         return "Predefined Message Invalid or Not Found";
      case ESME_RX_T_APPN:
         return "ESME Receiver Temporary App Error Code";
      case ESME_RX_P_APPN:
         return "ESME Receiver Permanent App Error Code";
      case ESME_RX_R_APPN:
         return "ESME Receiver Reject Message Error Code";
      case ESME_RQUERYFAIL:
         return "query_sm request failed";
      case ESME_RINVOPTPARSTREAM:
         return "Error in the optional part of the PDU Body";
      case ESME_ROPTPARNOTALLWD:
         return "Optional Parameter not allowed";
      case ESME_RINVPARLEN:
         return "Invalid Parameter Length";
      case ESME_RMISSINGOPTPARAM:
         return "Expected Optional Parameter missing";
      case ESME_RINVOPTPARAMVAL:
         return "Invalid Optional Parameter Value";
      case ESME_RDELIVERYFAILURE:
         return "Delivery Failure (used for data_sm_resp)";
      case ESME_RUNKNOWNERR:
         return "Unknown Error";
      default:
         return "Undefined Enum";
      }
   }

   public static SMPPStatus fromValue(int value) {
      SMPPStatus[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         SMPPStatus e = arr$[i$];
         if (e.value() == value) {
            return e;
         }
      }

      return UNKNOWN;
   }
}
