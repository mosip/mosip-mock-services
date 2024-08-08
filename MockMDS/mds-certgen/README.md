# Certificate Generation and Deployment Workflow

This directory contains a set of shell scripts designed to automate the process of generating certificates, uploading them to a Partner Manager, creating PKCS#12 files, updating application properties, and packaging files for deployment. The scripts are designed to work together and are orchestrated through the `entrypoint.sh` script.

## Table of Contents
1. [Overview](#overview)
2. [Scripts Description](#scripts-description)
    - [certgen.sh](#certgensh)
    - [upload-certs.sh](#upload-certssh)
    - [createp12.sh](#createp12sh)
    - [updating-app-properties.sh](#updating-app-propertiessh)
    - [upload-zip-to-s3.sh](#upload-zip-to-s3sh)
    - [entrypoint.sh](#entrypointsh)
3. [Usage](#usage)

## Overview

This workflow automates the following tasks:
1. Generating Root CA, Intermediate CA, and Client certificates.
2. Authenticating with the Partner Manager and uploading the generated certificates.
3. Creating PKCS#12 files for various devices.
4. Updating the application properties file with runtime values.
5. Packaging all necessary files into a ZIP archive and uploading it to a MinIO bucket.

## Scripts Description

### `certgen.sh`

This script is responsible for generating certificates:
- **Root CA Certificate**: A self-signed root certificate.
- **Intermediate CA Certificate**: Signed by the Root CA.
- **Client Certificate**: Signed by the Intermediate CA.

**Key environment variables**:
- `CA`, `SUBCA`, `CLIENT`, `COUNTRY`, `STATE`, `LOCATION`, `CERT_LOCATION`.

**Output**:
- Generates certificates and keys in the specified `CERT_LOCATION`.

### `upload-certs.sh`

This script handles the authentication with the Partner Manager and uploads the generated certificates:
- Authenticates using `clientId` and `secretKey`.
- Registers the partner and uploads the Root CA, Intermediate CA, and Client certificates.

**Key environment variables**:
- `mosip-api-internal-host`, `mosip_deployment_client_secret`, `CLIENT`.

**Output**:
- Uploads the certificates to the Partner Manager and saves the signed client certificate.

### `createp12.sh`

This script generates device-specific PKCS#12 (`.p12`) files:
- Creates a private key and certificate for a device.
- Exports the certificate and key into a PKCS#12 file.
- Replaces existing `.p12` files with the newly generated one.

**Key environment variables**:
- `COUNTRY`, `STATE`, `LOCATION`, `CERT_LOCATION`.

**Output**:
- Creates and updates `.p12` files in specified directories.

### `updating-app-properties.sh`

This script updates the `application.properties` file with dynamic values at runtime:
- Fetches internal host from Kubernetes ConfigMap.
- Replaces placeholders in `application.properties` with the runtime values.

**Output**:
- Updates the `application.properties` file with the correct runtime values.

### `upload-zip-to-s3.sh`

This script packages all relevant files into a ZIP archive and uploads it to a MinIO bucket:
- Zips the `target`, `.p12` certificates, `application.properties`, and `Biometric Devices` directories.
- Configures the MinIO client (`mc`) and uploads the ZIP file.

**Key environment variables**:
- `s3-host`, `s3-region`, `s3-user-key`, `s3-user-secret`, `s3-bucket-name`.

**Output**:
- Uploads the ZIP archive to the specified MinIO bucket.

### `entrypoint.sh`

This is the main orchestration script that sequentially executes all the other scripts:
1. Runs `certgen.sh` to generate certificates.
2. Runs `upload-certs.sh` to authenticate and upload certificates.
3. Runs `createp12.sh` to create PKCS#12 files.
4. Runs `updating-app-properties.sh` to update the properties file.
5. Runs `upload-zip-to-s3.sh` to package and upload the files.

**Output**:
- Executes all the scripts in sequence and provides a summary of the execution.

