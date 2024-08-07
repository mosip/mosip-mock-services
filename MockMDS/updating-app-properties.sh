#!/bin/bash

# Define the placeholders and their respective runtime values from environment variables
keystore_pwd=mosip123
echo "keystore_pwd: $keystore_pwd"

# Fetch API_INTERNAL_HOST from Kubernetes ConfigMap
API_INTERNAL_HOST=$(kubectl get cm global -o jsonpath='{.data.mosip-api-internal-host}')

# Print the fetched API_INTERNAL_HOST for verification
echo "API_INTERNAL_HOST: $API_INTERNAL_HOST"

pwd

# Path to your application.properties file
PROPERTIES_FILE="target/application.properties"

# Update the placeholders in the application.properties file
sed -i "s|\$API_INTERNAL_HOST|$API_INTERNAL_HOST|g" $PROPERTIES_FILE
sed -i "s|\$mosip_regproc_client_secret|$mosip_regproc_client_secret|g" $PROPERTIES_FILE
sed -i "s|\$keystore_pwd|$keystore_pwd|g" $PROPERTIES_FILE

echo "application.properties updated successfully."

# Optionally, print out the updated application.properties for verification
cat $PROPERTIES_FILE
