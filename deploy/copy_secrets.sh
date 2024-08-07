##!/bin/bash
# Copy secrets from other namespaces
# DST_NS: Destination namespace

function copying_secrets() {
  UTIL_URL=https://raw.githubusercontent.com/mosip/mosip-infra/master/deployment/v3/utils/copy_cm_func.sh
  COPY_UTIL=./copy_cm_func.sh
  DST_NS=mds

 # Check if copy_cm_func.sh exists, download if not
  if [ ! -f "$COPY_UTIL" ]; then
    echo "Downloading copy_cm_func.sh from $UTIL_URL"
    wget -q "$UTIL_URL" -O "$COPY_UTIL"
    chmod +x "$COPY_UTIL"
  fi
  echo "Copying configmaps to namespace $DST_NS"
  $COPY_UTIL secret s3 s3 $DST_NS
  $COPY_UTIL secret keycloak keycloak $DST_NS
  $COPY_UTIL secret keycloak-client-secrets keycloak $DST_NS

  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
copying_secrets   # calling function