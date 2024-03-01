#!/bin/bash
kubectl --kubeconfig=/home/$USER/.kube/config create configmap prm-configmap
  --from-literal=PRM_HOST=localhost

kubectl --kubeconfig=/home/$USER/.kube/config create secret generic prm-credentials \
  --from-literal=AZURE_SUBSCRIPTION_ID=${AZURE_SUBSCRIPTION_ID} \
  --from-literal=AZURE_CLIENT_ID=${AZURE_CLIENT_ID} \
  --from-literal=AZURE_CLIENT_SECRET=${AZURE_CLIENT_SECRET} \
  --from-literal=AZURE_TENANT_ID=${AZURE_TENANT_ID} \
  --from-literal=PNA_MQTT_BROKER_URL=${PNA_MQTT_BROKER_URL} \
  --from-literal=PNA_MQTT_CLIENT_USERNAME=${PNA_MQTT_CLIENT_USERNAME} \
  --from-literal=PNA_MQTT_CLIENT_PASSWORD=${PNA_MQTT_CLIENT_PASSWORD} \
  --from-literal=PNA_USERNAME=${PNA_USERNAME} \
  --from-literal=PNA_PASSWORD=${PNA_PASSWORD} \
  --from-literal=PNA_INIT_TOKEN=${PNA_INIT_TOKEN}