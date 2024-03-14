#!/bin/bash
set -e
set -o noglob
generate_password() {
  local chars=$1
  head /dev/urandom | tr -dc A-Za-z0-9 | head -c $chars ; echo ''
}

validate_mqtt_broker_url() {
  local url=$1
  local regex="^ssl://[a-zA-Z0-9.-]+:[0-9]+$"

  if [[ $url =~ $regex ]]; then
    echo "Valid MQTT broker URL"
  else
    echo "Invalid MQTT broker URL"
    exit 1
  fi
}

validate_alphanumeric() {
  local key=$1
  local str=$2
  local regex="^[a-zA-Z0-9]{8,}$"

  if [[ $str =~ $regex ]]; then
    echo "Valid" "$key"
  else
    echo "Invalid" "$key"
    exit 1
  fi
}

echo ""
echo "PULCEO - Bootstrapping tool. USE AT OWN RISK!!!"
echo ""

echo "Check if a .env-pulceo file is present..."
if [ -f .env-pulceo ]; then
  echo "Found .env-pulceo file...loading variables..."
  source .env-pulceo
else
  echo "No .env-pulceo file found...creating one..."
fi

mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown -R $USER:$USER ~/.kube
sudo chmod 0600 ~/.kube/config

VARIABLES_CHANGED=false

# PNA_MQTT_BROKER_URL
if [ -z "$PNA_MQTT_BROKER_URL" ]; then
  read -p "Enter the MQTT broker URL (should be like ssl://be3147d06377478a8eee29fd8f09495d.s1.eu.hivemq.cloud:8883): " PNA_MQTT_BROKER_URL
  if [ -z "$PNA_MQTT_BROKER_URL" ]; then
    echo "PNA_MQTT_BROKER_URL is required"
    exit 1
  fi
  VARIABLES_CHANGED=true
fi
validate_mqtt_broker_url $PNA_MQTT_BROKER_URL

# PNA_MQTT_CLIENT_USERNAME
if [ -z "$PNA_MQTT_CLIENT_USERNAME" ]; then
  EXAMPLE_MQTT_CLIENT_USERNAME=$(generate_password 8)
  read -p "Enter the MQTT client username (should be like $EXAMPLE_MQTT_CLIENT_USERNAME, ENTER TO ACCEPT THE SUGGESTION): " PNA_MQTT_CLIENT_USERNAME
  if [ -z "$PNA_MQTT_CLIENT_USERNAME" ]; then
    PNA_MQTT_CLIENT_USERNAME=$EXAMPLE_MQTT_CLIENT_USERNAME
  fi
    VARIABLES_CHANGED=true
fi
validate_alphanumeric "PNA_MQTT_CLIENT_USERNAME" $PNA_MQTT_CLIENT_USERNAME

# PNA_MQTT_CLIENT_PASSWORD
if [ -z "$PNA_MQTT_CLIENT_PASSWORD" ]; then
  EXAMPLE_MQTT_CLIENT_PNA_MQTT_CLIENT_PASSWORD=$(generate_password 8)
  read -p "Enter the MQTT client password (should be like $EXAMPLE_MQTT_CLIENT_PNA_MQTT_CLIENT_PASSWORD, ENTER TO ACCEPT THE SUGGESTION): " PNA_MQTT_CLIENT_PASSWORD
  if [ -z "$PNA_MQTT_CLIENT_PASSWORD" ]; then
    PNA_MQTT_CLIENT_PASSWORD=$EXAMPLE_MQTT_CLIENT_PNA_MQTT_CLIENT_PASSWORD
  fi
  VARIABLES_CHANGED=true
fi
validate_alphanumeric "PNA_MQTT_CLIENT_PASSWORD" $PNA_MQTT_CLIENT_PASSWORD

# PNA_USERNAME
# 24 chars
if [ -z "$PNA_USERNAME" ]; then
  PNA_USERNAME=$(generate_password 24)
  VARIABLES_CHANGED=true
elif [ ${#PNA_USERNAME} -ne 24 ]; then
  echo "PNA_USERNAME should be 24 characters long"
  exit 1
fi
validate_alphanumeric "PNA_USERNAME" $PNA_USERNAME

# PNA_PASSWORD
if [ -z "$PNA_PASSWORD" ]; then
  PNA_PASSWORD=$(generate_password 32)
    VARIABLES_CHANGED=true
elif [ ${#PNA_PASSWORD} -ne 32 ]; then
  echo "PNA_PASSWORD should be 32 characters long"
  exit 1
fi
validate_alphanumeric "PNA_PASSWORD" $PNA_PASSWORD

# PNA_INIT_TOKEN
PNA_INIT_TOKEN=$(echo -n "${PNA_USERNAME}:${PNA_PASSWORD}" | base64)

# DOCKER_INFLUXDB_INIT_USERNAME
if [ -z "$DOCKER_INFLUXDB_INIT_USERNAME" ]; then
  DOCKER_INFLUXDB_INIT_USERNAME=$(generate_password 8)
  VARIABLES_CHANGED=true
elif [ ${#DOCKER_INFLUXDB_INIT_USERNAME} -ne 8 ]; then
  echo "DOCKER_INFLUXDB_INIT_USERNAME should be 8 characters long"
  exit 1
fi
validate_alphanumeric "DOCKER_INFLUXDB_INIT_USERNAME" $DOCKER_INFLUXDB_INIT_USERNAME

# DOCKER_INFLUXDB_INIT_PASSWORD
if [ -z "$DOCKER_INFLUXDB_INIT_PASSWORD" ]; then
  DOCKER_INFLUXDB_INIT_PASSWORD=$(generate_password 8)
  VARIABLES_CHANGED=true
elif [ ${#DOCKER_INFLUXDB_INIT_PASSWORD} -ne 8 ]; then
  echo "DOCKER_INFLUXDB_INIT_PASSWORD should be 8 characters long"
  exit 1
fi
validate_alphanumeric "DOCKER_INFLUXDB_INIT_PASSWORD" $DOCKER_INFLUXDB_INIT_PASSWORD

if [ -z "$DOCKER_INFLUXDB_INIT_ADMIN_TOKEN" ]; then
  DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=$(generate_password 8)
  VARIABLES_CHANGED=true
elif [ ${#DOCKER_INFLUXDB_INIT_ADMIN_TOKEN} -ne 8 ]; then
  echo "DOCKER_INFLUXDB_INIT_ADMIN_TOKEN should be 8 characters long"
  exit 1
fi
validate_alphanumeric "DOCKER_INFLUXDB_INIT_ADMIN_TOKEN" $DOCKER_INFLUXDB_INIT_ADMIN_TOKEN

echo "PNA_MQTT_BROKER_URL=$PNA_MQTT_BROKER_URL" > .env-pulceo
echo "PNA_MQTT_CLIENT_USERNAME=$PNA_MQTT_CLIENT_USERNAME" >> .env-pulceo
echo "PNA_MQTT_CLIENT_PASSWORD=$PNA_MQTT_CLIENT_PASSWORD" >> .env-pulceo
echo "PNA_USERNAME=$PNA_USERNAME" >> .env-pulceo
echo "PNA_PASSWORD=$PNA_PASSWORD" >> .env-pulceo
echo "PNA_INIT_TOKEN=$PNA_INIT_TOKEN" >> .env-pulceo
echo "DOCKER_INFLUXDB_INIT_USERNAME=$DOCKER_INFLUXDB_INIT_USERNAME" >> .env-pulceo
echo "DOCKER_INFLUXDB_INIT_PASSWORD=$DOCKER_INFLUXDB_INIT_PASSWORD" >> .env-pulceo
echo "DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=$DOCKER_INFLUXDB_INIT_ADMIN_TOKEN" >> .env-pulceo

if [ "$VARIABLES_CHANGED" = false ]; then
  echo "File .env-pulceo unchanged...DO NOT SHARE THIS FILE WITH ANYONE!!!"
else
  echo "Successfully created .env-pulceo file with all credentials...DO NOT SHARE THIS FILE WITH ANYONE!!!"
fi

kubectl --kubeconfig=/home/$USER/.kube/config create configmap prm-configmap \
  --from-literal=PRM_HOST=pulceo-resource-manager \
  --from-literal=PMS_HOST=pulceo-monitoring-service \
  --from-literal=PSM_HOST=pulceo-service-manager

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

kubectl --kubeconfig=/home/$USER/.kube/config create configmap pms-configmap \
  --from-literal=PSM_HOST=pulceo-service-manager \
  --from-literal=PRM_HOST=pulceo-resource-manager \
  --from-literal=INFLUXDB_URL=http://pms-influxdb:8086 \
  --from-literal=INFLUXDB_ORG=org \
  --from-literal=INFLUXDB_BUCKET=bucket

kubectl --kubeconfig=/home/$USER/.kube/config create secret generic pms-credentials \
  --from-literal=DOCKER_INFLUXDB_INIT_USERNAME=${DOCKER_INFLUXDB_INIT_USERNAME} \
  --from-literal=DOCKER_INFLUXDB_INIT_PASSWORD=${DOCKER_INFLUXDB_INIT_PASSWORD} \
  --from-literal=DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=${DOCKER_INFLUXDB_INIT_ADMIN_TOKEN} \
  --from-literal=INFLUXDB_TOKEN=${DOCKER_INFLUXDB_INIT_ADMIN_TOKEN} \
  --from-literal=PNA_MQTT_BROKER_URL=${PNA_MQTT_BROKER_URL} \
  --from-literal=PNA_MQTT_CLIENT_USERNAME=${PNA_MQTT_CLIENT_USERNAME} \
  --from-literal=PNA_MQTT_CLIENT_PASSWORD=${PNA_MQTT_CLIENT_PASSWORD}

kubectl --kubeconfig=/home/$USER/.kube/config create configmap psm-configmap \
  --from-literal=PRM_HOST=pulceo-resource-manager

kubectl --kubeconfig=/home/$USER/.kube/config create secret generic psm-credentials \
  --from-literal=PNA_MQTT_BROKER_URL=${PNA_MQTT_BROKER_URL} \
  --from-literal=PNA_MQTT_CLIENT_USERNAME=${PNA_MQTT_CLIENT_USERNAME} \
  --from-literal=PNA_MQTT_CLIENT_PASSWORD=${PNA_MQTT_CLIENT_PASSWORD}

kubectl --kubeconfig=/home/$USER/.kube/config apply -f https://raw.githubusercontent.com/spboehm/pulceo-resource-manager/main/prm-deployment.yaml
kubectl --kubeconfig=/home/$USER/.kube/config apply -f https://raw.githubusercontent.com/spboehm/pulceo-monitoring-service/main/pms-deployment.yaml
kubectl --kubeconfig=/home/$USER/.kube/config apply -f https://raw.githubusercontent.com/spboehm/pulceo-service-manager/main/psm-deployment.yaml
