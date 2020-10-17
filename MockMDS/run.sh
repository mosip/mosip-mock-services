#!/bin/sh

echo "Starting MOCK MDS service..."

nohup java -cp provider-0.0.1-rc1.jar:lib/* org.biometric.provider.ProviderApplication > /tmp/mock_mds.log 2>&1 &

echo "Started MOCK MDS service."
