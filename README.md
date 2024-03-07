# pulceo-resource-manager 

<img src="docs/assets/pulceo-logo-color.png" alt="pulceo-logo" width="25%" height="auto"/>

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

```bash
k3d cluster create pulceo-test --api-port 40476 --port 80:80@loadbalancer
```
## Run with k3d

**[TODO]: Add a step to generate the secrets**
- Apply the following kubernetes manifest to the cluster
```bash
kubectl --kubeconfig=/home/$USER/.kube/config create configmap prm-configmap \
  --from-literal=PRM_HOST=pulceo-resource-manager
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
  --from-literal=PNA_INIT_TOKEN=${PNA_INIT_TOKEN} \
  --from-literal=PNA_HOST_FQDN=${PNA_HOST_FQDN}
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
