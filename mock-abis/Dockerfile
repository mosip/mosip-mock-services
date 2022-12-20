FROM openjdk:11

ARG container_user=mosip

ARG container_user_group=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_uid=1001

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_gid=1001

# install packages and create user


RUN apt-get -y update \
&& apt-get install -y unzip \
&& groupadd -g ${container_user_gid} ${container_user_group} \
&& useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user}

USER container_user

WORKDIR /home/${container_user}/

COPY ./target/mockabis.jar mockabis-service.jar

# select container user for all tasks
#USER ${container_user_uid}:${container_user_gid}

