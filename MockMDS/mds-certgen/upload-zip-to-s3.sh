#!/bin/bash

# Extract environment variables
S3_HOST=$(printenv s3-host)
S3_REGION=$(printenv s3-region)
S3_USER_KEY=$(printenv s3-user-key)
S3_USER_SECRET=$(printenv s3-user-secret)
S3_BUCKET_NAME=$(printenv s3-bucket-name)

# Set region option if defined
if [ ! -z "$S3_REGION" ]; then
  S3_REGION="--region $S3_REGION"
else
  S3_REGION=''
fi

echo -e "\n\n=========================== PACKAGING AND UPLOADING ================================================\n"

# Zip the target, p12 certificates, and application.properties
echo -e "\nPackaging files into mockmds.zip..."
zip -r $work_dir/mockmds.zip $work_dir/target $work_dir/MockMDS/* $work_dir/certs/ $work_dir/application.properties $work_dir/'Biometric Devices'

echo -e "\n\n=========================== CONFIGURING MINIO CLIENT ================================================\n"
# Configure mc client

mc alias set s3 "$S3_HOST" "$S3_USER_KEY" "$S3_USER_SECRET" --api=S3v2

# Create bucket if it does not exist
mc mb s3/"$S3_BUCKET_NAME" --ignore-existing $S3_REGION

# Upload the zip to MinIO
echo -e "\nUploading mockmds.zip to MinIO..."
mc cp $work_dir/mockmds.zip "s3/$S3_BUCKET_NAME/"

echo -e "\n\nmockmds.zip uploaded to MinIO bucket $S3_BUCKET_NAME"