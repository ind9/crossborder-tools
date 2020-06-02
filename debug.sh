#!/bin/bash

echo "Debugging records"

rm conf/template.sql3
sqlite3 conf/template.sql3 < src/main/resources/schema.sql
sqlite3 conf/template.sql3 < src/main/resources/2017-nomen.sql
sqlite3 conf/template.sql3 < src/main/resources/2012-nomen.sql

mvn -DskipTests=true clean package dependency:copy-dependencies

find tmp/wrk -type f -delete

if [ -e "target/avahack-1.0-SNAPSHOT.jar" ]
then
  java -Dapp.db.template=/var/depot/src/avhack/conf/template.sql3\
 -Dapp.tmp.dir=/var/depot/src/avhack/tmp/wrk/\
 -Dapp.static=web/static -Dapp.templates=web/ftl\
 -Dapp.queries=/var/depot/src/avhack/conf/\
 -cp target/avahack-1.0-SNAPSHOT.jar:target/dependency/*:conf org.avlara.Main

else
  echo "target/avahack-1.0-SNAPSHOT.jar does not exist"
fi
