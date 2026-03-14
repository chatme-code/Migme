#!/bin/bash
#/usr/local/jboss/bin/shutdown.sh -S && tail -f /var/log/jboss/jboss.log
LOGFILE=/etc/jboss/console.log
#LOGFILE=/usr/local/jboss/server/default/log/server.log 
/etc/init.d/jboss stop
RET=$?
PATSTR="Halting VM"
if [ $RET -ne 0 ] ; then
    echo Failed, ret=$RET
    tail $LOGFILE
else
    echo waiting for jboss to stop ...
    sleep 1
    while [ `grep -c "$PATSTR" $LOGFILE` -ne 1 ] ; do
        tail -1 $LOGFILE
        sleep 1
    done
    echo jboss stopped
    grep "$PATSTR" $LOGFILE 
fi
