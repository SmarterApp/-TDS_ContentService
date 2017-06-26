FROM frolvlad/alpine-oraclejdk8:slim

VOLUME /tmp
ADD docker-startup.sh /
ADD tds-content-service-*.jar tds-content-service.jar
RUN sh -c 'touch /docker-startup.sh && touch /tds-content-service.jar'
RUN apk --no-cache add curl
ENTRYPOINT ["/docker-startup.sh"]