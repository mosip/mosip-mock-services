# Authentication 
auth_url_env=https://$( printenv mosip-api-internal-host )
client=mosip-deployment-client
secret=${mosip_deployment_client_secret}
date=$(date --utc +%FT%T.%3NZ)

echo -e "\n========== Authenticating with partnermanager =========="

# Make the request
response=$(curl -s -D - -o /dev/null -X 'POST' \
  "$auth_url_env/v1/authmanager/authenticate/clientidsecretkey" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "string",
  "version": "string",
  "requesttime": "'$date'",
  "metadata": {},
  "request": {
    "clientId": "'$client'",
    "secretKey": "'$secret'",
    "appId": "partner"
  }
}') > "$CERT_LOCATION/temp.txt"

# Extract the TOKEN
TOKEN=$(echo "$response" | grep -i 'Authorization:' | awk '{print $2}' | tr -d '\r')

if [[ -z $TOKEN ]]; then
  echo "Authentication Failed / TOKEN not found"
  exit 1
fi

echo "TOKEN: $TOKEN"

partnermanagerUrl=https://$( printenv mosip-api-internal-host )
RAND_MOBILE_NO=$(tr -cd '[:digit:]' < /dev/urandom | fold -w 10 | head -n 1)
RAND_EMAIL_ID=$( echo "$(openssl rand -hex 10)@gmail.com" )
#RAND_EMAIL_ID=findme@gmail.com
echo "RAND_EMAIL_ID: $RAND_EMAIL_ID"

echo -e "\n========== Adding Device Partner =========="

# Upload partner self registration
partner_status=$(curl -X 'POST' \
  "$partnermanagerUrl/v1/partnermanager/partners" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  --cookie "Authorization=$TOKEN" \
  -d '{
        "id": "string",
        "metadata": {},
        "request": {
          "address": "bangalore",
          "contactNumber": "'"$RAND_MOBILE_NO"'",
          "emailId": "'"$RAND_EMAIL_ID"'",
          "organizationName": "'"$CLIENT"'",
          "partnerId": "'"$CLIENT"'",
          "partnerType": "Device_Provider",
          "policyGroup": "mosip policy group",
          "langCode": "eng"
        },
        "requesttime": "'$date'",
        "version": "string"
      }') > "$CERT_LOCATION/CA.txt"

# Debugging Response
echo "partner_status response: $partner_status"

response=$(echo $partner_status | jq .response)
if [[ $response == null ]]; then
  echo $partner_status | jq .errors[0].message
else
  echo $partner_status | jq .response.status
fi

# Extract and upload Root CA
rootCA=$(awk 'NF {sub(/\r/, ""); printf "%s\\n",$0;}' "$CERT_LOCATION/RootCA.crt" | sed 's/\\n$//')
intermediateCA=$(awk 'NF {sub(/\r/, ""); printf "%s\\n",$0;}' "$CERT_LOCATION/IntermediateCA.crt" | sed 's/\\n$//')
clientCRT=$(awk 'NF {sub(/\r/, ""); printf "%s\\n",$0;}' "$CERT_LOCATION/Client.crt" | sed 's/\\n$//')

echo -e "\n========== Uploading Root CA Certificate to Partner Manager =========="

# Upload Root CA Certificate
upload_CA_Certificate_status=$(curl -X 'POST' \
  "$partnermanagerUrl/v1/partnermanager/partners/certificate/ca/upload" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  --cookie "Authorization=$TOKEN" \
  -d '{ "id": "string",
        "metadata": {},
        "request": {
           "certificateData": "'"$rootCA"'",
           "partnerDomain": "DEVICE"
         },
       "requesttime": "'"$date"'",
       "version": "string"
     }') > "$CERT_LOCATION/CA.txt"

# Debugging Response
echo "upload_CA_Certificate_status response: $upload_CA_Certificate_status"

response=$(echo $upload_CA_Certificate_status | jq .response)
if [[ $response == null ]]; then
  echo $upload_CA_Certificate_status | jq .errors[0].message
else
  echo $upload_CA_Certificate_status | jq .response.status
fi

echo -e "\n========== Uploading Intermediate CA Certificate to Partner Manager =========="

# Upload Intermediate CA Certificate
upload_IntermediateCA_status=$(curl -X 'POST' \
  "$partnermanagerUrl/v1/partnermanager/partners/certificate/ca/upload" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  --cookie "Authorization=$TOKEN" \
  -d '{ "id": "string",
        "metadata": {},
        "request": {
           "certificateData": "'"$intermediateCA"'",
           "partnerDomain": "DEVICE"
         },
       "requesttime": "'"$date"'",
       "version": "string"
     }') > "$CERT_LOCATION/CA.txt"

# Debugging Response
echo "upload_IntermediateCA_status response: $upload_IntermediateCA_status"

response=$(echo $upload_IntermediateCA_status | jq .response)
if [[ $response == null ]]; then
  echo $upload_IntermediateCA_status | jq .errors[0].message
else
  echo $upload_IntermediateCA_status | jq .response.status
fi

echo -e "\n========== Uploading Client Certificate to Partner Manager =========="

# Upload Client Certificate
upload_Client_Certificate_status=$(curl -X 'POST' \
  "$partnermanagerUrl/v1/partnermanager/partners/certificate/upload" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  --cookie "Authorization=$TOKEN" \
  -d '{ "id": "string",
        "metadata": {},
        "request": {
           "certificateData": "'"$clientCRT"'",
           "partnerDomain": "DEVICE",
           "partnerId": "'"$CLIENT"'"
         },
       "requesttime": "'"$date"'",
       "version": "string"
     }') > "$CERT_LOCATION/CA.txt"

# Debugging Response
echo "upload_Client_Certificate_status response: $upload_Client_Certificate_status"

response=$(echo $upload_Client_Certificate_status | jq .response)
if [[ $response == null ]]; then
  echo $upload_Client_Certificate_status | jq .errors[0].message
else
  echo $upload_Client_Certificate_status | jq .response.status
fi

# Extract the signedCertificateData
certificate=$(echo "$upload_Client_Certificate_status" | jq -r '.response.signedCertificateData')

if [[ -z $certificate ]]; then
  echo "Certificate not found"
  exit 1
fi

# Save the certificate to the file
echo -e "$certificate" > "$CERT_LOCATION/mosip-signed-client.crt"

echo "Certificate saved to $CERT_LOCATION/mosip-signed-client.crt"