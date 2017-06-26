#!/bin/sh
#-----------------------------------------------------------------------------------------------------------------------
# File:  docker-startup.sh
#
# Desc:  Start the tds-content-service.jar with the appropriate properties.
#
#-----------------------------------------------------------------------------------------------------------------------

java $JAVA_OPTS -jar /tds-content-service.jar
