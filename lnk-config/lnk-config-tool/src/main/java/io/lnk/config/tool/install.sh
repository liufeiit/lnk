
mvn clean install -Dtest.skip=true
cp lnk-config-tool/target/lnk-config-tool-1.0.0-SNAPSHOT-install.jar /usr/local/
cd /usr/local/
mkdir lnk-config-tool
mv lnk-config-tool-1.0.0-SNAPSHOT-install.jar lnk-config-tool
cd lnk-config-tool/
jar xvf lnk-config-tool-1.0.0-SNAPSHOT-install.jar
rm -rf lnk-config-tool-1.0.0-SNAPSHOT-install.jar
cd /bin
chmod +x lnk-config
ln -s /usr/local/lnk-config-tool/bin/lnk-config /usr/local/bin/lnk-config
##
lnk-config dev