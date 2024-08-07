#!/bin/bash
# Installs mocks
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=abis
MDSNS=mds
CHART_VERSION=0.0.1-develop

echo "Create $NS namespace"
kubectl create ns $NS

echo "Create $MDSNS namespace"
kubectl create ns $MDSNS

function mock() {
  echo "Istio label for $NS"
  kubectl label ns $NS istio-injection=enabled --overwrite

  helm repo update

  echo "Copy configmaps"
  sed -i 's/\r$//' copy_cm.sh
  ./copy_cm.sh

  echo "Configuring mock-mds"

  # Additional configuration for mock-mds
  echo "Copy secrets"
  sed -i 's/\r$//' copy_secrets.sh
  ./copy_secrets.sh

  read -p "Provide mockmds bucket name: " s3_bucket
  if [[ -z $s3_bucket ]]; then
    echo "s3_bucket not provided; EXITING;"
    exit 1
  fi
  if [[ $s3_bucket == *[' !@#$%^&*()+']* ]]; then
    echo "s3_bucket should not contain spaces / any special character; EXITING"
    exit 1
  fi

  read -p "Provide mockmds s3 bucket region: " s3_region
  if [[ $s3_region == *[' !@#$%^&*()+']* ]]; then
    echo "s3_region should not contain spaces / any special character; EXITING"
    exit 1
  fi

  read -p "Provide S3 URL: " s3_url
  if [[ -z $s3_url ]]; then
    echo "s3_url not provided; EXITING;"
    exit 1
  fi

  s3_user_key=$(kubectl -n s3 get cm s3 -o json | jq -r '.data."s3-user-key"')


  echo "Installing mock-mv in $NS"
  helm -n $NS template mock-mv mosip/mock-mv --version $CHART_VERSION

  echo "Installing mock-abis in $NS"
  helm -n $NS template mock-abis mosip/mock-abis --version $CHART_VERSION

  echo "Installing mock-mds with provided configuration"
  helm -n $MDSNS template mock-mds mosip/mock-mds \
    --set mockmds.configmaps.s3.s3-host="$s3_url" \
    --set mockmds.configmaps.s3.s3-user-key="$s3_user_key" \
    --set mockmds.configmaps.s3.s3-region="$s3_region" \
    --set mockmds.configmaps.s3.s3-bucket-name="$s3_bucket" \
    -f values.yaml \
    --wait-for-jobs \
    --version $CHART_VERSION

  echo "Reports are moved to S3 under mockmds bucket"
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes

mock   # calling function
