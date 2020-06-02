#!/bin/bash

version=${GO_PIPELINE_LABEL:-0.1.0}
image="crossborder_tools"
full_name="docker-registry.midgard.avalara.io/${image}:${version}"
latest="docker-registry.midgard.avalara.io/${image}:latest"

rm conf/template.sql3
sqlite3 conf/template.sql3 < src/main/resources/schema.sql
sqlite3 conf/template.sql3 < src/main/resources/2017-nomen.sql
sqlite3 conf/template.sql3 < src/main/resources/2012-nomen.sql

mvn clean package
if [ "$?" -eq 0 ];
then
  docker build -t ${image} .

  docker tag ${image} ${full_name}
  docker push ${full_name}

  docker tag ${image} ${latest}
  docker push ${latest}
else
  echo "Maven build failed"
  exit 1
fi