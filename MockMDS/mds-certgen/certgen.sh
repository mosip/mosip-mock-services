#!/usr/bin/env bash

set -e

## The script starts from here
echo -e "\nUSAGE: bash create-certs.sh"
echo "This script will create new rootCA, IntermediateCA & Partner certificates"

# Check if any environment variable is empty
: "${CA:?Need to set CA}"
: "${SUBCA:?Need to set SUBCA}"
: "${CLIENT:?Need to set CLIENT}"
: "${COUNTRY:?Need to set COUNTRY}"
: "${STATE:?Need to set STATE}"
: "${LOCATION:?Need to set LOCATION}"
: "${CERT_LOCATION:?Need to set CERT_LOCATION}"

# Print key-value
echo -e "\n========== Listing properties from environment variables =========="
echo "CA             = $CA"
echo "SUBCA          = $SUBCA"
echo "CLIENT         = $CLIENT"
echo "COUNTRY        = $COUNTRY"
echo "STATE          = $STATE"
echo "LOCATION       = $LOCATION"
echo "CERT_LOCATION  = $CERT_LOCATION"

# Create certs directory
mkdir -p "$CERT_LOCATION"

# Certificate authority
echo -e "\n========== Creating CA certificate =========="

### Generating CA certs
openssl genrsa -out "$CERT_LOCATION/RootCA.key" 4096
openssl req -new -x509 -days 1826 -extensions v3_ca -key "$CERT_LOCATION/RootCA.key" -out "$CERT_LOCATION/RootCA.crt" -subj "/C=$COUNTRY/ST=$STATE/L=$LOCATION/O=$CA/OU=$CA/CN=$CA/"
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in "$CERT_LOCATION/RootCA.key" -out "$CERT_LOCATION/RootCA.key.pkcs8"

# Intermediate CA
echo -e "\n========== Creating SUBCA certificate =========="

### Generating SUBCA certs
openssl genrsa -out "$CERT_LOCATION/IntermediateCA.key" 4096
openssl req -new -key "$CERT_LOCATION/IntermediateCA.key" -out "$CERT_LOCATION/IntermediateCA.csr" -subj "/C=$COUNTRY/ST=$STATE/L=$LOCATION/O=$SUBCA/OU=$SUBCA/CN=$SUBCA/"
openssl x509 -req -days 1000 -extfile ./openssl.cnf -extensions v3_intermediate_ca -in "$CERT_LOCATION/IntermediateCA.csr" -CA "$CERT_LOCATION/RootCA.crt" -CAkey "$CERT_LOCATION/RootCA.key" -out "$CERT_LOCATION/IntermediateCA.crt" -set_serial 01
openssl verify -CAfile "$CERT_LOCATION/RootCA.crt" "$CERT_LOCATION/IntermediateCA.crt"
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in "$CERT_LOCATION/IntermediateCA.key" -out "$CERT_LOCATION/IntermediateCA.key.pkcs8"

# Client certificate from IntermediateCA
echo -e "\n========== Creating CLIENT certificate =========="

### Generating CLIENT certs
openssl genrsa -out "$CERT_LOCATION/Client.key" 4096
openssl req -new -key "$CERT_LOCATION/Client.key" -out "$CERT_LOCATION/Client.csr" -subj "/C=$COUNTRY/ST=$STATE/L=$LOCATION/O=$CLIENT/OU=$CLIENT/CN=$CLIENT/"
openssl x509 -req -extensions usr_cert -extfile ./openssl.cnf -days 1000 -in "$CERT_LOCATION/Client.csr" -CA "$CERT_LOCATION/IntermediateCA.crt" -CAkey "$CERT_LOCATION/IntermediateCA.key" -set_serial 04 -out "$CERT_LOCATION/Client.crt"
openssl verify -CAfile "$CERT_LOCATION/RootCA.crt" -untrusted "$CERT_LOCATION/IntermediateCA.crt" "$CERT_LOCATION/Client.crt"
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in "$CERT_LOCATION/Client.key" -out "$CERT_LOCATION/Client.key.pkcs8"
