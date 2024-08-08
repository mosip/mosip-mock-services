# Now generating different p12 certificates for device
echo -e "\n========== Creating Device keys and export to keystore =========="

# KEYSTORE_PWD=${KEYSTORE_PWD}
# export KEYSTORE_PWD
# echo "$KEYSTORE_PWD" > key.pwd
keystore_pwd=mosip123

openssl genrsa -out "$CERT_LOCATION/Device.key" 4096
openssl req -new -key "$CERT_LOCATION/Device.key" -out "$CERT_LOCATION/Device.csr" -subj "/C=$COUNTRY/ST=$STATE/L=$LOCATION/O=Device/OU=Device/CN=Device/"
openssl x509 -req -extensions usr_cert -extfile ./openssl.cnf -days 180 -in "$CERT_LOCATION/Device.csr" -CA "$CERT_LOCATION/mosip-signed-client.crt" -CAkey "$CERT_LOCATION/Client.key" -set_serial 05 -out "$CERT_LOCATION/signed-Device.crt"
openssl pkcs12 -export -in "$CERT_LOCATION/signed-Device.crt" -inkey "$CERT_LOCATION/Device.key" -out "$CERT_LOCATION/Device.p12" -name "Device" -password pass:$keystore_pwd
echo "Device certificate created and exported to Device.p12"

echo -e "\n========== Replacing old .p12 files with new Device.p12 =========="

# Define the target file paths
declare -a TARGET_FILES=(
    "$work_dir/target/Biometric Devices/Finger/Single/Keys/mosipfingersingle.p12"
    "$work_dir/target/Biometric Devices/Finger/Slap/Keys/mosipfingerslap.p12"
    "$work_dir/target/Biometric Devices/Iris/Double/Keys/mosipirisdouble.p12"
    "$work_dir/target/Biometric Devices/Iris/Single/Keys/mosipirissingle.p12"
    "$work_dir/target/Biometric Devices/Face/Keys/mosipface.p12"
)

# Loop through each target file and copy the new Device.p12
for TARGET_FILE in "${TARGET_FILES[@]}"; do
    echo "Replacing $TARGET_FILE with Device.p12"
    cp "$CERT_LOCATION/Device.p12" "$TARGET_FILE"
done

echo -e "Replacement complete."