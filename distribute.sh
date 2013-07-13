#!/bin/sh

export COPYFILE_DISABLE=true
mvn -q package
cp -r jetty-distribution logwatcher-$1
cp logwatcher-web/target/logwatcher-web-2.0.war logwatcher-$1/webapps/ROOT.war
tar -czf logwatcher-$1.tar.gz logwatcher-$1
rm -rf logwatcher-$1
