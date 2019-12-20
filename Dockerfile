FROM azul/zulu-openjdk-alpine:11
COPY build/libs/rp-campaign-manager-*-all.jar rp-campaign-manager.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar rp-campaign-manager.jar
