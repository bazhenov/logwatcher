#!/bin/bash

wget http://team.dev.loc/repository/download/bt14/.lastSuccessful/logging-web-frontend.war?guest=1 -O logging-web-frontend.war
FILE_NAME=logwatcher-`date +%Y%m%d-%H%M`.war
scp -S deploy.py logging-web-frontend.war vhost+logwatcher:$FILE_NAME
rm logging-web-frontend.war
