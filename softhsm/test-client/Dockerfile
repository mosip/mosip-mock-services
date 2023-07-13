FROM openjdk:8

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

RUN apt-get update \
    && apt-get -y upgrade \
    && apt-get -y install unzip

#This is only for testing 
ADD bcprov-jdk15on-165.jar /app/bcprov-jdk15on-165.jar
ADD PKCS11Test.class /app/PKCS11Test.class
#End of testing kit not needed when you create your own docker.

ADD configure_start.sh /app/configure_start.sh
RUN chmod +x /app/configure_start.sh

#ADD springbootapp.jar /app/springbootapp.jar
ENTRYPOINT [ "/app/configure_start.sh" ]
CMD ["java", "-cp", "/app/:/app/bcprov-jdk15on-165.jar:.:", "PKCS11Test"]
#CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/springbootapp.jar"]
