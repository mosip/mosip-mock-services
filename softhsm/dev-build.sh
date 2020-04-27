#!/bin/bash
set -e

if [[ $# -eq 0 ]]
  then
    echo "$0 please provide the security officier pin as a argument eg: $0 1111"
    exit 1
fi

docker build . --tag softhsm:v1 --build-arg security_officer_pin=$1

echo "Copy the clilent.zip from the above docker to the current folder"
read -p "Once done Press Enter to continue..."

#Build the java client
cd test-client/ && javac -classpath bcprov-jdk15on-165.jar PKCS11Test.java
cd ..

#setup the client docker
yes | cp client.zip test-client/client.zip
cd test-client && docker build --tag hsmclient:v1 .
rm -rf client.zip
rm -rf PKCS11Test.class
cd ..

#setup the artifactory simulator
yes | cp client.zip artifactory/client.zip
cd artifactory && docker build --tag artifactory:v1 .
rm -rf client.zip
cd ..

echo "Once completed please push the softhsm to mosipdev and publish the client.zip to actual artifactory in the deployment zone"