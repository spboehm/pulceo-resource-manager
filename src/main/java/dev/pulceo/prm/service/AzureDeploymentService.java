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
import dev.pulceo.prm.dto.node.CreateNewAzureNodeDTO;
import dev.pulceo.prm.model.node.AzureNode;
import dev.pulceo.prm.model.provider.AzureCredentials;
import dev.pulceo.prm.model.provider.AzureProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.UUID;

@Service
public class AzureDeploymentService {

    private final ProviderService providerService;

    @Value("${AZURE_SSH_PUBLIC_KEY}")
    private String AZURE_SSH_PUBLIC_KEY;

    @Value("${AZURE_VM_USERNAME}")
    private String AZURE_VM_USERNAME;

    @Autowired
    public AzureDeploymentService(ProviderService providerService) {
        this.providerService = providerService;
    }

    public AzureNode createAzureNode(CreateNewAzureNodeDTO createNewAzureNodeDTO) {

        // TODO: check if provider does exist

        // retrieve AzureProvider
        AzureProvider azureProvider = this.providerService.readAzureProviderByProviderMetaDataName(createNewAzureNodeDTO.getProviderName()).orElseThrow();

        // TODO: retrieve credentials
        AzureCredentials azureCredentials = azureProvider.getCredentials();

        // Deploy to Azure
        final String userName = AZURE_VM_USERNAME;
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCWQ6A+YM1ZDaqOxBBvCJAYfixvuv0zDFBetu+mnYqlTwftVuGRq/cdgiHcEVkBTMKUSkWrUF97bPckHD0sbgc+3eDTtEN/iQsg8q47k4RLxr9ccb6VQpObrl0NJY3WGa/CuXhnNHwNHyUGpbXx6gp0vSGx35ej0RdUbsLZKWxCWUO6W2rwqglXGoqQ5PgJv1P3NckH47w9HF1aWQJncx91lGteMZ+zX6H9xcC9GErTnwDhwTBeidW6rRJoeGEO5ZmBmUl9+k+UsaVo5l/WePHRnkJ9BvYE9qoPUjuwNXJ1WMHVkpe97+NMICGFr7NZXSLqNVR3c3PgOQbR61WVgZJh";
        String resourceName = createRandomName("pulceo-");

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

            // Create an Ubuntu virtual machine in a new resource group.
            VirtualMachine linuxVM = azureResourceManager.virtualMachines().define(resourceName)
                    .withRegion(Region.fromName(createNewAzureNodeDTO.getNodeLocationCountry()))
                    .withNewResourceGroup(resourceName)
                    .withNewPrimaryNetwork("10.0.0.0/24")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(resourceName)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS_GEN2)
                    .withRootUsername(userName)
                    .withSsh(sshKey)
                    .withCustomData("IyEvYmluL3NoCmN1cmwgLXNmTCBodHRwczovL2dldC5rM3MuaW8gfCBJTlNUQUxMX0szU19FWEVDPSItLWRpc2FibGU9dHJhZWZpayIgc2ggLQpta2RpciAtcCB+Ly5rdWJlCnN1ZG8gY2F0IC9ldGMvcmFuY2hlci9rM3MvazNzLnlhbWwgPiAvaG9tZS9wdWxjZW8vLmt1YmUvY29uZmlnCmNobW9kIDA2MDAgfi8ua3ViZS9jb25maWc=")
                    .withOSDiskStorageAccountType(StorageAccountTypes.STANDARD_SSD_LRS)
                    .withSize(VirtualMachineSizeTypes.fromString(createNewAzureNodeDTO.getSku()))
                    .create();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            this.deleteAzureVirtualMachine(resourceName, createNewAzureNodeDTO.getProviderName(), false);
        }

        // then persis


//        private AzureProvider azureProvider;
//        private NodeMetaData nodeMetaData;
//        private Node node;
//        private CloudRegistration cloudRegistration;

//        private String name;
//        private String type;
//        private String sku;

//        private String nodeLocationCountry;
        String nodeLocationCountry = this.getCountryByRegion(createNewAzureNodeDTO.getNodeLocationCountry());
//        private String nodeLocationCity;
        String nodeLocationCity = this.getCityByRegion(createNewAzureNodeDTO.getNodeLocationCountry());

        return null;
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
    public static String createRandomName(String namePrefix) {
        String root = UUID.randomUUID().toString().replace("-", "");
        long millis = Calendar.getInstance().getTimeInMillis();
        long datePart = millis % 10000000L;
        return namePrefix + root.toLowerCase().substring(0, 3) + datePart;
    }

    private String getCityByRegion(String region) {
        if (region.equals("eastus")) {
            return "Virginia";
        } else {
            return "";
        }
    }

    private String getCountryByRegion(String region) {
        if (region.equals("eastus")) {
            return "USA";
        } else {
            return "";
        }
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
