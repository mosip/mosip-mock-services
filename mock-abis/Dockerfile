FROM openjdk:11

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# environment variable to pass github branch to pickup configuration from, at docker runtime
ENV spring_config_label_env=${spring_config_label}
# environment variable to pass active profile such as DEV, QA etc at docker runtime
ENV active_profile_env=${active_profile}
# environment variable to pass spring configuration url, at docker runtime
ENV spring_config_url_env=${spring_config_url}
# environment variable to pass glowroot, at docker runtime
ENV is_glowroot_env=${is_glowroot}

COPY ./target/mockabis.jar  mockabis-service.jar
RUN echo "Build application"
EXPOSE 8081
CMD java -jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}"  -Dspring.cloud.config.uri="${spring_config_url_env}" mockabis-service.jar;
    