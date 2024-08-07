FROM ubuntu:22.04

# Build arguments
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# Labels
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# Set working directory
WORKDIR /home/${container_user}

ENV work_dir=/home/${container_user}

# Copy files to the working directory
COPY application.properties $work_dir/
COPY ["Biometric Devices", "$work_dir/Biometric Devices"]
COPY entrypoint.sh $work_dir
COPY ./MockMDS/target $work_dir/target
COPY *.sh openssl.cnf $work_dir

# Install packages, download binaries, create user and group in a single RUN command
RUN apt-get update && \
    apt-get install -y curl openssl jq bash sed gawk grep zip && \
    groupadd -g ${container_user_gid} ${container_user_group} && \
    useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user} && \
    curl -O https://dl.min.io/client/mc/release/linux-amd64/archive/mc.RELEASE.2022-07-29T19-17-16Z && \
    chmod +x mc.RELEASE.2022-07-29T19-17-16Z && \
    mv mc.RELEASE.2022-07-29T19-17-16Z /usr/local/bin/mc && \
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl && \
    rm kubectl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* \
    chmod +x $work_dir/*.sh \
    chown -R ${container_user}:${container_user} /home/${container_user}

# Switch to the container user
USER ${container_user_uid}:${container_user_gid}

WORKDIR /home/${container_user}

# Define environment variables
ENV CA= \
    SUBCA= \
    CLIENT= \
    COUNTRY= \
    STATE= \
    LOCATION= \
    CERT_LOCATION=/home/mosip/certs \
    mosip-api-internal-host= \
    mosip_regproc_client_secret= \
    mosip_deployment_client_secret= \
    s3-host= \
    s3-region= \
    s3-user-key= \
    s3-user-secret= \
    s3-bucket-name=

# Run the entrypoint.sh script
ENTRYPOINT ["./entrypoint.sh"]
