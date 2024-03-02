# pulceo-resource-manager

## General Prerequisites

- Make sure that the following ports are available on the local system:
  - `80/tcp`
  - `443/tcp`
  - `40476/tcp` (for k3d API server)

## Create a free MQTT broker (recommended)

- Create a basic MQTT broker on [HiveMQ](https://console.hivemq.cloud/?utm_source=HiveMQ+Pricing+Page&utm_medium=serverless+signup+CTA+Button&utm_campaign=HiveMQ+Cloud+PaaS&utm_content=serverless)
- Make sure that you select the free plan: Serverless (Free)

## Create your own MQTT broker (optional)

**TODO: Add a guide on how to create a local MQTT broker**

## Run with k3d

- Install [k3d](https://k3d.io/v5.6.0/#learning) on your machine by following the official installation guide
- Create a test cluster with k3d
```bash
k3d cluster create pulceo-test --api-port 40476 --port 80:80@loadbalancer
```
**[TODO]: Add a step to generate the secrets**
- Apply the following kubernetes manifest to the cluster
```bash
kubectl --kubeconfig=/home/$USER/.kube/config create configmap prm-configmap \
  --from-literal=PRM_HOST=localhost
```
```bash
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
```
```bash
kubectl apply -f prm-deployment.yaml
```

- Check if everything is running with: `kubectl get deployment`
```
NAME                      READY   UP-TO-DATE   AVAILABLE   AGE
pulceo-resource-manager   1/1     1            1           5m56s
```

- Check the exposed services with: `kubectl get svc`
```
NAME                      TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)    AGE
kubernetes                ClusterIP   10.43.0.1      <none>        443/TCP    26m
pulceo-resource-manager   ClusterIP   10.43.164.93   <none>        7878/TCP   6m37s
```

pulceo-node-agent is now running and ready to accept workloads under `http://EXTERNAL-IP`

```bash
curl -I http://localhost:80/prm/health
```
```
HTTP/1.1 200 OK
Content-Length: 2
Content-Type: text/plain;charset=UTF-8
Date: Sat, 02 Mar 2024 08:52:52 GMT
```

helm repo add authelia https://charts.authelia.com
helm repo update
externalTrafficPolicy: Local
type: LoadBalancer
kubectl apply -f https://raw.githubusercontent.com/traefik/traefik/v2.11/docs/content/reference/dynamic-configuration/kubernetes-crd-definition-v1.yml
kubectl apply -f https://raw.githubusercontent.com/traefik/traefik/v2.11/docs/content/reference/dynamic-configuration/kubernetes-crd-rbac.yml