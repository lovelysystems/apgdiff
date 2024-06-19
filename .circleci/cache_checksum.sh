#!/bin/bash
#This is a simple utility to hash all the files that are necessary to generate a cache key for build tools like CircleCI
RESULT_FILE=$1

if [ -f $RESULT_FILE ]; then
  rm $RESULT_FILE
fi
touch $RESULT_FILE

find . \( -name '*.gradle.kts' -or -name gradle-wrapper.properties -or -name gradle.properties \) -type f |sort|xargs cat|openssl md5 | awk '{print $2}' > $RESULT_FILE
