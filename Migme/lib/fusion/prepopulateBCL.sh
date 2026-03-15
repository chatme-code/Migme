echo "JAVA_ARGS=$JAVA_ARGS"

COMMON_BASE=/usr/fusion/
FUSION_BASE=/usr/fusion/

CLASSPATH=$FUSION_BASE/eclipse_bin:$FUSION_BASE:etc

for jar in `find $COMMON_BASE/ -type f |  grep -i jar$` ; do
        #echo "adding " $jar
        CLASSPATH=$CLASSPATH:$jar
done

echo "CLASSPATH = $CLASSPATH"

java $JAVA_ARGS -server -Xmx256m -Dlog.filename=prepopulate -Dlog.dir=$FUSION_BASE/logs/ -Dconfig.dir=$FUSION_BASE/etc/ -classpath $CLASSPATH com.projectgoth.fusion.bclmigration.DataSourcePrePopulateBCL

