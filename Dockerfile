FROM openjdk:8-jre-alpine

RUN mkdir -p /apps/
RUN mkdir -p /apps/tmp/wrk

ADD conf /apps/conf/
ADD web /apps/web/

ADD target/cbtools-1.0-SNAPSHOT-jar-with-dependencies.jar /apps/lib/

CMD java -Xms1024m -Xmx1024m \
     -Dapp.db.template=/apps/conf/template.sql3 \
     -Dapp.tmp.dir=/apps/tmp/wrk/ \
     -Dapp.static=/apps/web/static \
     -Dapp.templates=/apps/web/ftl \
     -Dapp.queries=/apps/conf/ \
     -cp /apps/lib/*:/apps/conf org.avlara.Main
