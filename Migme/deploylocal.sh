. deploy-get-jarfile.sh
#cp -v -p target/artifacts/lib/fusion.jar /usr/local/jboss/server/default/deploy/fusion-datasyncsvc.jar
cp -v -p $JARFILE /usr/local/jboss/server/default/deploy/Fusion.jar
#cp -v -p target/artifacts/datasyncsvc.war /usr/local/jboss/server/default/deploy/
cp -v $JARFILE /usr/fusion/Fusion.jar

