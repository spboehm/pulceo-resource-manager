package dev.pulceo.prm.service;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import dev.pulceo.prm.exception.AzureDeploymentServiceException;
import dev.pulceo.prm.model.node.AzureDeloymentResult;
import dev.pulceo.prm.model.provider.AzureCredentials;
import dev.pulceo.prm.model.provider.AzureProvider;
import dev.pulceo.prm.util.DeploymentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

@Service
public class AzureDeploymentService {

    private final ProviderService providerService;

    @Value("${AZURE_SSH_PUBLIC_KEY}")
    private String AZURE_SSH_PUBLIC_KEY;

    @Value("${AZURE_VM_USERNAME}")
    private String AZURE_VM_USERNAME;

    @Value("${PNA_USERNAME}")
    private String PNA_USERNAME;

    @Value("${PNA_PASSWORD}")
    private String PNA_PASSWORD;

    @Value("${PNA_INIT_TOKEN}")
    private String PNA_INIT_TOKEN;

    @Value("${PNA_MQTT_BROKER_URL}")
    private String PNA_MQTT_BROKER_URL;

    @Value("${PNA_MQTT_CLIENT_USERNAME}")
    private String PNA_MQTT_CLIENT_USERNAME;

    @Value("${PNA_MQTT_CLIENT_PASSWORD}")
    private String PNA_MQTT_CLIENT_PASSWORD;

    @Autowired
    public AzureDeploymentService(ProviderService providerService) {
        this.providerService = providerService;
    }

    // TODO: split
    public AzureDeloymentResult deploy(String providerName, String nodeLocationCountry, String sku) throws AzureDeploymentServiceException {

        // retrieve AzureProvider
        AzureProvider azureProvider = this.providerService.readAzureProviderByProviderMetaDataName(providerName).orElseThrow();

        // TODO: retrieve credentials
        AzureCredentials azureCredentials = azureProvider.getCredentials();

        // Deploy to Azure
        final String userName = AZURE_VM_USERNAME;
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCYOKh6255jQgrNTapamvjqqi4l9vdzKg9JKGT7sAUm16rPFFCLEBn1JdRn1HBGHvPtFfyV2QwmHleue2d8WMSge4uvA7TVRUy+sELydfeXGbBn6FZIKLk8J++hmxkxGtK8cAUBxji9mL1J43uuQd+IlyJlI0HlAVow+/BwqdzDscjQhEL/x8EzmWzW7aeBOyrFN9s5WN086kdOt95qYUwC7Gko+zaiiqWYd2e5DyzeCDzFNHjSz0HSAyjedd0PlY55fx7gwaGi+9pkyPsHU4ozgkIndQK69bYvzTkNafSvZd8RddsWfxPxPnzfvjEtEUxzCkbLlOV1U0z6PEhmTpdN94NZ3nPk6ARWxCeHHiipUuRJF+zWutgZblbVOmdMa56AYqOVYJey/oB0BFTlT03SbujbeEP+idy0k5mjNJD2DSxv05K1SQrne54JhInzDb135GsTCPAgCBfTQPnWL72Qb7ONzHVugxSuw3lFJmXFTia5WJ8H37b1IqIvOv5DN5M=";
        final String resourceName = createRandomName("pulceo-node-");

        try {
//            TokenCredential credential = new DefaultAzureCredentialBuilder()
//                    .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
//                    .build();

            ClientSecretCredential clientSecretCredential = getClientSecretCredential(azureCredentials);

            // If you don't set the tenant ID and subscription ID via environment variables,
            // change to create the Azure profile with tenantId, subscriptionId, and Azure environment.
            AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                    .withLogLevel(HttpLogDetailLevel.BASIC)
                    .authenticate(clientSecretCredential, profile)
                    .withSubscription(azureCredentials.getSubscriptionId());

            // === Public IP
            PublicIpAddress publicIPAddress = azureResourceManager.publicIpAddresses().define(resourceName)
                    .withRegion(nodeLocationCountry)
                    .withNewResourceGroup(resourceName)
                    .withLeafDomainLabel(resourceName)
                    .withStaticIP()
                    .create();

            // === Customer data for bootstrapping PNA
            String customerData = DeploymentUtil.templateBootStrapPnaScript(this.getCredentialsExportStatements(publicIPAddress.fqdn()));

            // === Virtual Machine
            VirtualMachine linuxVM = azureResourceManager.virtualMachines().define(resourceName)
                    .withRegion(Region.fromName(nodeLocationCountry))
                    .withNewResourceGroup(resourceName)
                    .withNewPrimaryNetwork("10.0.0.0/24")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingPrimaryPublicIPAddress(publicIPAddress)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS_GEN2)
                    .withRootUsername(userName)
                    .withSsh(sshKey)
                    .withCustomData(customerData)
                    .withOSDiskStorageAccountType(StorageAccountTypes.STANDARD_SSD_LRS)
                    .withSize(VirtualMachineSizeTypes.fromString(sku))
                    .create();

            return AzureDeloymentResult.builder()
                    .resourceGroupName(resourceName)
                    .sku(sku)
                    .fqdn(publicIPAddress.fqdn())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            this.deleteAzureVirtualMachine(resourceName, providerName, false);
            throw new AzureDeploymentServiceException("Could not deploy azure virtual machine...rolling back...", e);
        }

    }

    private List<String> getCredentialsExportStatements(String domain) {
        List<String> exportStatements = new ArrayList<>();
        exportStatements.add("PNA_MQTT_BROKER_URL=" + this.PNA_MQTT_BROKER_URL);
        exportStatements.add("PNA_MQTT_CLIENT_USERNAME=" + this.PNA_MQTT_CLIENT_USERNAME);
        exportStatements.add("PNA_MQTT_CLIENT_PASSWORD=" + this.PNA_MQTT_CLIENT_PASSWORD);
        exportStatements.add("PNA_USERNAME=" + this.PNA_USERNAME);
        exportStatements.add("PNA_PASSWORD=" + this.PNA_PASSWORD);
        exportStatements.add("PNA_INIT_TOKEN=" + this.PNA_INIT_TOKEN);
        exportStatements.add("PNA_HOST_FQDN=" + domain);
        exportStatements.add("USER=" + this.AZURE_VM_USERNAME);
        exportStatements.add("DOMAIN=" + domain);
        return exportStatements;
    }

    public void deleteAzureVirtualMachine(String resourceGroupName, String providerName, boolean dryRun) {
        // retrieve AzureProvider
        AzureProvider azureProvider = this.providerService.readAzureProviderByProviderMetaDataName(providerName).orElseThrow();

        // TODO: retrieve credentials
        AzureCredentials azureCredentials = azureProvider.getCredentials();

        ClientSecretCredential clientSecretCredential = getClientSecretCredential(azureCredentials);

        // If you don't set the tenant ID and subscription ID via environment variables,
        // change to create the Azure profile with tenantId, subscriptionId, and Azure environment.
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(clientSecretCredential, profile)
                .withSubscription(azureCredentials.getSubscriptionId());

        String foundResourceGroupName = azureResourceManager.resourceGroups().getByName(resourceGroupName).name();

        if (foundResourceGroupName == null) {
            throw new RuntimeException("Resource group not found");
        }

        if (dryRun) {
            System.out.println("DRY RUN: Resource group would be deleted");
        } else {
            azureResourceManager.resourceGroups().deleteByName(resourceGroupName);
        }
    }

    // taken from https://github.com/anuchandy/azure-sdk-for-java/blob/bb730c376420f440777c2f4b609424037115cca2/azure-samples/src/main/java/com/microsoft/azure/management/samples/Utils.java
    private static String createRandomName(String namePrefix) {
        String root = UUID.randomUUID().toString().replace("-", "");
        long millis = Calendar.getInstance().getTimeInMillis();
        long datePart = millis % 10000000L;
        return namePrefix + root.toLowerCase().substring(0, 3) + datePart;
    }



    private static ClientSecretCredential getClientSecretCredential(AzureCredentials azureCredentials) {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
                .clientId(azureCredentials.getClientId())
                .clientSecret(azureCredentials.getClientSecret())
                .tenantId(azureCredentials.getTenantId())
                .build();
        return clientSecretCredential;
    }

}
