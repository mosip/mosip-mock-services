FROM openjdk:11

ARG container_user=mosip

WORKDIR /home/${container_user}/
# install packages and create user

USER container_user

RUN apt-get -y update \
&& apt-get install -y unzip 
#&& groupadd -g ${container_user_gid} ${container_user_group} \
#&& useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user}

COPY ./target/mockabis.jar mockabis-service.jar

# select container user for all tasks
#USER ${container_user_uid}:${container_user_gid}

