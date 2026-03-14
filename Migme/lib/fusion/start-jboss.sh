#!/bin/bash

#ETC_DIR=etc
#if [ "$1" = "devlab" ] ; then
#    ETC_DIR=etc-devlab
#fi
#echo using ETC_DIR=$ETC_DIR
#cp -p -v /usr/fusion/${ETC_DIR}/mysql-ds.xml /usr/local/jboss/server/default/deploy/
LOGFILE=/etc/jboss/console.log
#LOGFILE=/usr/local/jboss/server/default/log/server.log 
/etc/init.d/jboss start
RET=$?
if [ $RET -ne 0 ] ; then
    echo Failed, ret=$RET
    tail $LOGFILE
else
	echo waiting for jboss to start up ...
	sleep 1
	while [ `grep " JBoss " $LOGFILE | grep -c " Started in "` -ne 1 ] ; do
	    tail -1 $LOGFILE
	    sleep 1
	done
	echo jboss started
	grep " JBoss " $LOGFILE | grep " Started in "
fi
#LOGFILE=/var/log/jboss/jboss.log
#nohup /usr/local/jboss/bin/run.sh 2>&1 >> ${LOGFILE} &
#echo log file is ${LOGFILE}
if [ "$1" = "-f" ] ; then
 tail -f ${LOGFILE}
fi

