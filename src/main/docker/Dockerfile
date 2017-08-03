FROM frolvlad/alpine-oraclejdk8:slim

EXPOSE 8000

VOLUME /tmp
ADD docker-startup.sh /
ADD tds-content-service-*.jar tds-content-service.jar
RUN sh -c 'touch /docker-startup.sh && touch /tds-content-service.jar'
RUN apk --no-cache add curl
#Embed "items" and "stimuli" resources from build machine file-system
RUN mkdir -p /usr/local/tomcat/resources/tds/bank
ADD items /usr/local/tomcat/resources/tds/bank/items
ADD stimuli /usr/local/tomcat/resources/tds/bank/stimuli

ENTRYPOINT ["/docker-startup.sh"]