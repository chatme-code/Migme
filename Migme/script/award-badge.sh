#!/bin/bash
if [ $# -lt 1 ] ; then
	cat << EOF
Usage: $0 \"badge full name\" [input-tsv-with-names-only | -- ]
Examples:
       $0 "Sturdy Thirty" /tmp/u
       echo -e "infn8loop\nozzy_zig" | $0 "Sturdy Thirty"
EOF
	exit 0
fi
DATESTR=`date +"%Y%m%d"`
BADGEFULLNAME="$1"
DIRNAME=`dirname $0`

BADGENAME=`echo ${BADGEFULLNAME// /_} | perl -p -e "s/(.)/\l\1/g" | perl -p -e "s/[\/'.\-]/_/g"`
if [ `echo "select rp.id,badgeid,rp.name from rewardprogram rp, badgereward br where rp.id=br.rewardprogramid and rp.name like '$BADGEFULLNAME%';" | mysql-slave.sh | wc -l` -ne 2 ] ; then
	echo "incorrect badge full name '$BADGEFULENAME'"
	echo "select rp.id,badgeid,rp.name from rewardprogram rp, badgereward br where rp.id=br.rewardprogramid and rp.name like '$BADGEFULLNAME%';" | mysql-slave.sh
	exit 1
fi

PROGRAMID=`echo "select rp.id from rewardprogram rp, badgereward br where rp.id=br.rewardprogramid and rp.name like '$BADGEFULLNAME%';" | mysql-slave.sh | tail -1`
BADGEID=`echo "select br.badgeid from rewardprogram rp, badgereward br where rp.id=br.rewardprogramid and rp.name like '$BADGEFULLNAME%';" | mysql-slave.sh | tail -1`
echo PROGRAMID=$PROGRAMID, BADGEID=$BADGEID, BADGENAME=$BADGENAME

if [ ${#PROGRAMID} -eq 0 ] || [ ${#BADGEID} -eq 0 ] || [ ${#BADGENAME} -eq 0 ] ; then
	echo Unable to find badge or reward program info
	exit 2;
fi

FILEPRE=${DIRNAME}/badge-${BADGENAME}-${DATESTR}
# get input usernames file from file or stdin
if [ $# -gt 1 ] ; then
	echo "Using inputfile $2"
	cp -v $2 ${FILEPRE}-names.tsv
else
	echo "Using stdin --"
	cat -- > ${FILEPRE}-names.tsv
fi
wc -l ${FILEPRE}-names.tsv

# simple script to use sql to get id and name from name, /home/zehua/bin/
db-get-idname-from-name.sh ${FILEPRE}-names.tsv > ${FILEPRE}-all.tsv
wc -l ${FILEPRE}-all.tsv
# simple script to use sql to get only id and name that have not been awarded from id/name, /home/zehua/bin/
db-get-idname-not-rewarded-badge.sh $BADGEID ${FILEPRE}-all.tsv > ${FILEPRE}.tsv
wc -l ${FILEPRE}.tsv

if [ `cat ${FILEPRE}-all.tsv | wc -l` -ne `cat ${FILEPRE}.tsv | wc -l` ] ; then
	echo number of users not the same!
	wc -l ${FILEPRE}-all.tsv
	wc -l ${FILEPRE}.tsv
	exit 1
fi

if [ `cat ${FILEPRE}.tsv | wc -l` -eq 0 ] ; then
	echo no users to be awarded
	exit 1
fi

echo "nohup bash ${DIRNAME}/scripts/run-clj.sh ${DIRNAME}/scripts/manual_badge_award.clj $PROGRAMID ${FILEPRE}.tsv > ${FILEPRE}.log &"
echo -n "proceed? [ctrl+c to cancel]"
read
export LOGDIR=${DIRNAME}/logs
nohup bash ${DIRNAME}/scripts/run-clj.sh ${DIRNAME}/scripts/manual_badge_award.clj $PROGRAMID ${FILEPRE}.tsv > ${FILEPRE}.log &

tail -f ${FILEPRE}.log
