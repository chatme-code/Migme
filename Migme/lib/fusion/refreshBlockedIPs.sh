echo "JAVA_ARGS=$JAVA_ARGS"

COMMON_BASE=/usr/fusion/
FUSION_BASE=/usr/fusion/

CLASSPATH=$FUSION_BASE/eclipse_bin:$FUSION_BASE:etc

for jar in `find $COMMON_BASE/ -type f | grep -iv swt |  grep -i jar$` ; do
        #echo "adding " $jar
        CLASSPATH=$CLASSPATH:$jar
done

CLASSPATH=$CLASSPATH:$COMMON_BASE/swt/swt-macosx-3.4.4.6.jar

echo "CLASSPATH = $CLASSPATH"

java $JAVA_ARGS -server -Xmx128m -Dlog.filename=refreshBlockedIPs -Dlog.dir=$FUSION_BASE/logs/ -Dconfig.dir=$FUSION_BASE/etc/ -classpath $CLASSPATH com.projectgoth.fusion.ejb.RefreshBlockedRegistrationIP

