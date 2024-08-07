#!/usr/bin/env bash

set -e

# Execute certgen.sh to generate certificates
echo -e "\nExecuting certgen.sh..."
bash certgen.sh

# Execute uploadcert.sh to authenticate and upload certificates
echo -e "\nExecuting uploadcert.sh..."
bash upload-certs.sh

# Execute createp12.sh to create PKCS#12 files
echo -e "\nExecuting createp12.sh..."
bash createp12.sh

# Execute updating-app-properties.sh
echo -e "\nExecuting updating-app-properties.sh..."
bash updating-app-properties.sh

# Execute createp12.sh to create PKCS#12 files
echo -e "\nExecuting upload-zip-to-s3.sh ..."
bash upload-zip-to-s3.sh

echo -e "\nAll scripts executed successfully."
