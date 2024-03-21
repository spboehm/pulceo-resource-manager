# Get started with PULCEO's API

Assumptions:

- PULCEO is exposed on `localhost:8081`
- You have created a valid service principal for Microsoft Azure [Create an Azure service principal with Azure CLI](https://learn.microsoft.com/en-us/cli/azure/azure-cli-sp-tutorial-1?tabs=bash)

## Create an On-prem provider for computational resources

A default on-prem provider is automatically created when PULCEO is started.
The name is _default_.

Request:
```bash
curl --request GET \
  --url http://localhost:8081/api/v1/providers/default \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
```

Response:
```json
{
	"uuid": "853fae40-6d41-4d8f-80c9-22727727a4c2",
	"providerName": "default",
	"providerType": "ON_PREM"
}
```

## Create Provider "azure-provider" (Microsoft Azure)

Request:
```bash
curl --request POST \
  --url http://localhost:8081/api/v1/providers \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --data '{
	"providerName": "azure-provider",
	"providerType": "AZURE",
	"clientId": "00000000-00000000-00000000-00000000",
	"clientSecret": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	"tenantId": "00000000-00000000-00000000-00000000",
	"subscriptionId": "00000000-00000000-00000000-00000000"
  }'
```

Response:
```json
{
  "uuid": "00000000-00000000-00000000-00000000",
  "providerName": "azure-provider",
  "providerType": "AZURE"
}
```

## Further cloud providers 

tbd.

## Create Node "cloud1" (Microsoft Azure)

Request:
```bash
curl --request POST \
  --url http://localhost:8081/api/v1/nodes \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --data '{
	"nodeType":"AZURE",
	"providerName":"azure-provider",
	"name":"cloud1",
	"type":"cloud",
	"cpu":"2",
	"mem":"4",
	"region":"westeurope",
	"tags": [
		{
			"key": "properties",
			"value": "Java, .NET, Ruby, MySQL"
		}
	]
  }'
```
    
Response:
```json
{
  "uuid": "d696dbd2-15a9-4cc0-8b01-376255a41ff3",
  "providerName": "azure-provider",
  "hostname": "pulceo-node-ede1859139.westeurope.cloudapp.azure.com",
  "pnaUUID": "e669e920-220a-409f-9866-751225082ab3",
  "node": {
    "name": "cloud1",
    "type": "CLOUD",
    "layer": 1,
    "role": "WORKLOAD",
    "group": "",
    "country": "Netherlands",
    "state": "Noord-Holland",
    "city": "Schiphol",
    "longitude": 4.9,
    "latitude": 52.3667,
    "tags": [
      {
        "key": "properties",
        "value": "Java, .NET, Ruby, MySQL"
      }
    ]
  }
}
```

## Create Node "cloud2" (Microsoft Azure)

Request
```bash
curl --request POST \
  --url http://localhost:8081/api/v1/nodes \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --data '{
	"nodeType":"AZURE",
	"providerName":"azure-provider",
	"name":"cloud2",
	"type":"cloud",
	"cpu":"2",
	"mem":"4",
	"region":"westus",
	"tags": [
		{
			"key": "properties",
			"value": "C++, Spark, MySQL, Linux, .NET, Python"
		}
	]
  }'
```

Response:
```json
{
  "uuid": "141901c3-4305-45e5-9bc9-0b5de5af22d5",
  "providerName": "azure-provider",
  "hostname": "pulceo-node-e031859180.westus.cloudapp.azure.com",
  "pnaUUID": "a9012402-a7bc-422c-ade0-08a547e0ff25",
  "node": {
    "name": "cloud2",
    "type": "CLOUD",
    "layer": 1,
    "role": "WORKLOAD",
    "group": "",
    "country": "USA",
    "state": "California",
    "city": "San Francisco",
    "longitude": -119.852,
    "latitude": 47.233,
    "tags": [
      {
        "key": "properties",
        "value": "C++, Spark, MySQL, Linux, .NET, Python"
      }
    ]
  }
}
```

