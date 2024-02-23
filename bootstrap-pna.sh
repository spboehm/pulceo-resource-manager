#!/bin/bash
# === DO NOT REMOVE THE FOLLOWING LINE ===
# {{ EXPORT_PNA_CREDENTIALS }}
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable=traefik" sh -
mkdir -p /home/$USER/.kube
sudo cat /etc/rancher/k3s/k3s.yaml > /home/$USER/.kube/config
chmod 0600 /home/$USER/.kube/config
sudo cat /etc/rancher/k3s/k3s.yaml > /home/$USER/.k3s.yaml
chmod 0600 /home/$USER/.k3s.yaml
sed -i 's/https:\/\/127.0.0.1:6443/https:\/\/10.43.0.1:443/' /home/$USER/.k3s.yaml
export KUBECONFIG=~/.kube/config
sleep 10
# generate namespace
kubectl create namespace pulceo
# create kubectl secret
kubectl create secret generic pna-credentials \
  --from-literal=PNA_MQTT_BROKER_URL=${PNA_MQTT_BROKER_URL} \
  --from-literal=PNA_MQTT_CLIENT_USERNAME=${PNA_MQTT_CLIENT_USERNAME} \
  --from-literal=PNA_MQTT_CLIENT_PASSWORD=${PNA_MQTT_CLIENT_PASSWORD} \
  --from-literal=PNA_USERNAME=${PNA_USERNAME} \
  --from-literal=PNA_PASSWORD=${PNA_PASSWORD} \
  --from-literal=PNA_INIT_TOKEN=${PNA_INIT_TOKEN} \
  --namespace=pulceo
# wait until k3s is completed
kubectl apply -f https://raw.githubusercontent.com/traefik/traefik/v2.10/docs/content/reference/dynamic-configuration/kubernetes-crd-definition-v1.yml
kubectl apply -f https://raw.githubusercontent.com/traefik/traefik/v2.10/docs/content/reference/dynamic-configuration/kubernetes-crd-rbac.yml
