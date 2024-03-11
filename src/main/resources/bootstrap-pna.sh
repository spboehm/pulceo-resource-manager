#!/bin/bash
# === DO NOT REMOVE THE FOLLOWING LINE ===
# {{ EXPORT_PNA_CREDENTIALS }}
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable=traefik --node-name=pna-k8s-node" sh -
mkdir -p /home/$USER/.kube
sudo cat /etc/rancher/k3s/k3s.yaml > /home/$USER/.kube/config
chown -R $USER:$USER /home/$USER/.kube/config
chmod -R 755 /home/$USER/.kube/config
sudo cat /etc/rancher/k3s/k3s.yaml > /home/$USER/.k3s.yaml
chown -R $USER:$USER /home/$USER/.k3s.yaml
chmod -R 755 /home/$USER/.k3s.yaml
sed -i 's/https:\/\/127.0.0.1:6443/https:\/\/10.43.0.1:443/' /home/$USER/.k3s.yaml
kubectl --kubeconfig=/home/$USER/.kube/config create namespace pulceo
kubectl --kubeconfig=/home/$USER/.kube/config create secret generic pna-credentials \
  --from-literal=PNA_MQTT_BROKER_URL=${PNA_MQTT_BROKER_URL} \
  --from-literal=PNA_MQTT_CLIENT_USERNAME=${PNA_MQTT_CLIENT_USERNAME} \
  --from-literal=PNA_MQTT_CLIENT_PASSWORD=${PNA_MQTT_CLIENT_PASSWORD} \
  --from-literal=PNA_USERNAME=${PNA_USERNAME} \
  --from-literal=PNA_PASSWORD=${PNA_PASSWORD} \
  --from-literal=PNA_INIT_TOKEN=${PNA_INIT_TOKEN} \
  --from-literal=PNA_HOST_FQDN=${PNA_HOST_FQDN} \
  --from-literal=PNA_UUID=$(uuidgen)
kubectl --kubeconfig=/home/$USER/.kube/config apply -f https://raw.githubusercontent.com/spboehm/pulceo-node-agent/main/traefik/0-crd.yaml
kubectl --kubeconfig=/home/$USER/.kube/config apply -f https://raw.githubusercontent.com/spboehm/pulceo-node-agent/main/traefik/1-crd.yaml
kubectl --kubeconfig=/home/$USER/.kube/config apply -f https://raw.githubusercontent.com/spboehm/pulceo-node-agent/main/traefik/2-traefik-services.yaml
kubectl --kubeconfig=/home/$USER/.kube/config apply -f https://raw.githubusercontent.com/spboehm/pulceo-node-agent/main/traefik/3-deployments.yaml
curl -s https://raw.githubusercontent.com/spboehm/pulceo-node-agent/main/traefik/4-routers.yaml | sed 's/localhost.localdomain/'"$DOMAIN"'/g' | kubectl --kubeconfig=/home/$USER/.kube/config apply -f -
