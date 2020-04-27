#!/bin/bash

#installs the pkcs11 libraries.
set -e

echo "Download the client from $ARTIFACTORY_URL"
wget $ARTIFACTORY_URL/artifactory/libs-release-local/hsm/client.zip
echo "Downloaded $ARTIFACTORY_URL"
unzip client.zip
echo "Attempting to install"
cd ./client && ./install.sh 
echo "Installation complete"

exec "$@"
