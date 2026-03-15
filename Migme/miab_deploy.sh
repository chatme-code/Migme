#ant jar.inc
REMOTESVR=_MY_TARGET_HOST_
if [ $# -gt 0 ] ; then
	REMOTESVR=$1
fi
REMOTEFDIR=/usr/fusion

echo "uploading to ${REMOTESVR}"
. deploy-get-jarfile.sh
ssh -t ${REMOTESVR} rm -fv /tmp/fusion.jar
scp -p $JARFILE ${REMOTESVR}:/tmp/fusion.jar
if [ $? -ne 0 ] ; then exit 2; fi;
DTSTR=`date +'%Y%m%d-%H%M%S'`
ssh -t ${REMOTESVR} \
"
cp -v /tmp/fusion.jar ${REMOTEFDIR}/Fusion.jar
echo files deployed on ${REMOTESVR};

nohup ${REMOTEFDIR}/restartMIAB.sh &> /tmp/restartMIAB.log.$DTSTR &
sleep 2;
tail -f /tmp/restartMIAB.log.$DTSTR
"
if [ $? -ne 0 ] ; then echo failed to run deploy on ${REMOTESVR}; exit 2; fi;
echo ===== done, deployed to jboss on ${REMOTESVR} =====
