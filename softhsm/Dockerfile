FROM ubuntu:18.04 as buildimage
RUN apt-get update \
    && apt-get -y upgrade \
    && apt-get -y install gcc make automake libtool autoconf pkg-config libssl-dev git-core \
    && git clone https://github.com/opendnssec/SoftHSMv2 \
    && cd SoftHSMv2/ && sh autogen.sh && ./configure && make && make install
RUN apt-get -y install cmake libseccomp-dev libssl-dev \
    && git clone https://github.com/SUNET/pkcs11-proxy \
    && cd pkcs11-proxy/ && mkdir build && cd build/ && cmake .. && make && make install
RUN cd SoftHSMv2/ && echo $(git rev-parse HEAD) > ref.softhsm \
    && cd ../pkcs11-proxy/ && echo $(git rev-parse HEAD) > ref.proxy
RUN mkdir /client/ \
    && echo "name=pkcs11-proxy\r\nlibrary=/usr/local/lib/softhsm/libpkcs11-proxy.so\r\nslotListIndex=0\r\n" > /client/pkcs11.cfg \
    && echo "The libpkcs11-proxy.so.0.1 has to be copied to the dockers that need access to soft hsm. \r\n Please copy this library as libpkcs11-proxy.so to the /usr/local/lib/softhsm" > /client/README.md \
    && cp pkcs11-proxy/ref.proxy /client/ && cp SoftHSMv2/ref.softhsm /client/ \
    && cp pkcs11-proxy/build/libpkcs11-proxy.so.0.1 /client/
RUN apt-get -y install zip \
    && echo "#!/bin/bash" > /client/install.sh \
    && echo "cat ref.softhsm" >> /client/install.sh \
    && echo "cat ref.proxy" >> /client/install.sh \
    && echo "mkdir -p /usr/local/lib/softhsm/" >> /client/install.sh \
    && echo "mkdir -p /config/" >> /client/install.sh \
    && echo "cp libpkcs11-proxy.so.0.1 /usr/local/lib/softhsm/libpkcs11-proxy.so" >> /client/install.sh \
    && echo "cp pkcs11.cfg /config/softhsm-application.conf" >> /client/install.sh \
    && chmod +x /client/install.sh \
    && zip -r client.zip /client



FROM ubuntu:18.04
RUN apt-get update \
    && apt-get -y upgrade \
    && apt-get -y install opensc libtool
COPY --from=buildimage /etc/softhsm2.* /etc/
COPY --from=buildimage /var/lib/softhsm /var/lib/softhsm
COPY --from=buildimage /usr/local/lib /usr/local/lib
COPY --from=buildimage /usr/local/bin /usr/local/bin
COPY --from=buildimage /usr/local/share/man /usr/local/share/man
COPY --from=buildimage /SoftHSMv2/src/lib /SoftHSMv2/src/lib
COPY --from=buildimage /SoftHSMv2/src/bin /SoftHSMv2/src/bin
COPY --from=buildimage /SoftHSMv2/libtool /SoftHSMv2/libtool
COPY --from=buildimage /SoftHSMv2/ref.softhsm /ref.softhsm
COPY --from=buildimage /pkcs11-proxy/ref.proxy /ref.proxy
COPY --from=buildimage /client.zip /client.zip

RUN /bin/bash /SoftHSMv2/libtool   --mode=install /usr/bin/install -c   /SoftHSMv2/src/lib/libsofthsm2.la '/usr/local/lib/softhsm' \
&& /bin/bash /SoftHSMv2/libtool   --mode=install /usr/bin/install -c /SoftHSMv2/src/bin/keyconv/softhsm2-keyconv '/usr/local/bin' \
&& /bin/bash /SoftHSMv2/libtool   --mode=install /usr/bin/install -c /SoftHSMv2/src/bin/util/softhsm2-util '/usr/local/bin' \
&& /bin/bash /SoftHSMv2/libtool   --mode=install /usr/bin/install -c /SoftHSMv2/src/bin/dump/softhsm2-dump-file '/usr/local/bin'
ARG security_officer_pin

RUN pkcs11-tool --module /usr/local/lib/softhsm/libsofthsm2.so --init-token --label "mosip-token" --slot 0 --so-pin ${security_officer_pin} \
    && pkcs11-tool --module /usr/local/lib/softhsm/libsofthsm2.so --init-pin --label "mosip-token" --login --so-pin ${security_officer_pin} --pin ${security_officer_pin}
RUN rm -rf /SoftHSMv2 
RUN echo "#!/bin/bash" > start.sh \
    && echo "cat ref.softhsm" >> start.sh \
    && echo "cat ref.proxy" >> start.sh \
    && echo "/usr/local/bin/pkcs11-daemon /usr/local/lib/softhsm/libsofthsm2.so" >> start.sh \
    && chmod +x start.sh
VOLUME /softhsm
EXPOSE 5666
ENV PKCS11_DAEMON_SOCKET="tcp://0.0.0.0:5666"
CMD [ "/start.sh" ]
