FROM openjdk:11
# environment variable to pass github branch to pickup configuration from, at docker runtime
ENV spring_config_label_env=${spring_config_label}
# environment variable to pass active profile such as DEV, QA etc at docker runtime
ENV active_profile_env=${active_profile}
# environment variable to pass spring configuration url, at docker runtime
ENV spring_config_url_env=${spring_config_url}
# environment variable to pass glowroot, at docker runtime
ENV is_glowroot_env=${is_glowroot}

COPY ./target/mock-mv.jar  mock-mv.jar
RUN echo "Build application"
EXPOSE 8081
CMD java -jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}"  -Dspring.cloud.config.uri="${spring_config_url_env}" mock-mv.jar;