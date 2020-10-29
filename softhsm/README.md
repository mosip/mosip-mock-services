## README
The soft hsm is a HSM simulator. This project shows how to build dockers for the network based soft HSM. The soft hsm by default will listed in port 5666 for network connection.

The client folder contains a reference implementation for a client machine that would like to connect to HSM over network with PKCS11. The client is only for demo and respective projects that need connectivity to the HSM should follow the implementation referenced in here.

The artifactory docker is a simple nginx which can serve the client.zip file. This is to test locally when we do not have the actual artifactory.

The location of the client.zip file is hard coded in the artifactory to artifactory/libs-release-local/hsm/client.zip

## Build instructions.
The build instructions are set in dev-build.sh. This project need not be built in an automated fashion. 

./dev-build.sh 1111 

__Note:__ 1111 is the security pin that would be used to connect to the soft hsm.

The artifactory 

How to run these dockers

### Step 1

docker run -p 5666:5666 softhsm:v1

__Note__ only if you need the latest client.zip

docker cp instanceid:/client.zip .

Only after copying the new client.zip you should build the artifactory 

### Step 2

Run this only if required. This is for testing.

docker run -p 80:80 artifactory:v1


### Step 3

docker run -e ARTIFACTORY_URL="url pointing to the artifactory" -e PKCS11_PROXY_SOCKET="tcp://<ipaddress>:5666" hsmclient:v1

ipaddress of the machine where the softhsm is running.

## Publish
Once completed please push the softhsm to mosipdev and publish the client.zip to artifactory

