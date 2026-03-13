package com.projectgoth.fusion.chat.external.yahoo;

public enum YahooService {
   LOGON(1),
   LOGOFF(2),
   ISAWAY(3),
   ISBACK(4),
   IDLE(5),
   MESSAGE(6),
   IDACT(7),
   IDDEACT(8),
   MAILSTAT(9),
   USERSTAT(10),
   NEWMAIL(11),
   CHATINVITE(12),
   CALENDAR(13),
   NEWPERSONALMAIL(14),
   NEWCONTACT(15),
   ADDIDENT(16),
   ADDIGNORE(17),
   PING(18),
   GROUPRENAME(19),
   SYSMESSAGE(20),
   PASSTHROUGH2(22),
   CONFINVITE(24),
   CONFLOGON(25),
   CONFDECLINE(26),
   CONFLOGOFF(27),
   CONFADDINVITE(28),
   CONFMSG(29),
   CHATLOGON(30),
   CHATLOGOFF(31),
   CHATMSG(32),
   GAMELOGON(40),
   GAMELOGOFF(41),
   GAMEMSG(42),
   FILETRANSFER(70),
   VOICECHAT(74),
   NOTIFY(75),
   P2PFILEXFER(77),
   PEERTOPEER(79),
   AUTHRESP(84),
   LIST(85),
   AUTH(87),
   ADDBUDDY(131),
   REMBUDDY(132),
   IGNORECONTACT(133),
   REJECTCONTACT(134),
   KEEPALIVE(138),
   PICTURE(190),
   Y6VISIBILITY(197),
   Y6STATUS(198),
   STATUSV15(240),
   LISTV15(241);

   private short value;

   private YahooService(int value) {
      this.value = (short)value;
   }

   public short getValue() {
      return this.value;
   }

   public static YahooService valueOf(short value) {
      YahooService[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         YahooService service = arr$[i$];
         if (service.value == value) {
            return service;
         }
      }

      return null;
   }
}
