FROM nginx

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}
ADD client.zip /usr/share/nginx/html/artifactory/libs-release-local/hsm/client.zip
WORKDIR /home/${container_user}
RUN chown -R ${container_user}:${container_user} /home/${container_user}
USER ${container_user_uid}:${container_user_gid}
