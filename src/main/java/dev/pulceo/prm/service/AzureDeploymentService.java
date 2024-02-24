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
    public AzureNode createAzureNode(CreateNewAzureNodeDTO createNewAzureNodeDTO) {

        // retrieve AzureProvider
        AzureProvider azureProvider = this.providerService.readAzureProviderByProviderMetaDataName(createNewAzureNodeDTO.getProviderName()).orElseThrow();

        // TODO: retrieve credentials
        AzureCredentials azureCredentials = azureProvider.getCredentials();

        // Deploy to Azure
        final String userName = AZURE_VM_USERNAME;
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDIoQyWzINPVvv37RLxb/QKO94XCOUo2bIC91XwAXCfoAgy165XNjPSgOLe74MCC/A0rIRt1hBfK18ynDhPSnqYSGXTo74ReEoS8WQ7gGR0e/h27ozuELpOWO8TVotBIuIhmS1Bepnk14TXjpCM/yq4DD8eg9kEz/eq5yjdwTUSMnLg+RERQzLxkWp41LKJ2itKjHh6vy+HDJDOzsSojdd6GeWfOwQkQMtL2Y0S1YEvrbT+rRHmsjZf4j+bxZnw/XpGJkPHZGs9AFwiLX00Q2b0ECuDSBtWaVNbJ0bU8rkimUGo6RHEE7EEtgNpqX0PFt0/Zwn2PFi2UHf5nSD2JESh";
        final String resourceName = createRandomName("pulceo-pna-");

        try {
            String customerData = DeploymentUtil.templateBootStrapPnaScript(this.getCredentialsExportStatements());
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
                    .withCustomData(customerData)
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

    private List<String> getCredentialsExportStatements() {
        List<String> exportStatements = new ArrayList<>();
        exportStatements.add("PNA_MQTT_BROKER_URL=" + this.PNA_MQTT_BROKER_URL);
        exportStatements.add("PNA_MQTT_CLIENT_USERNAME=" + this.PNA_MQTT_CLIENT_USERNAME);
        exportStatements.add("PNA_MQTT_CLIENT_PASSWORD=" + this.PNA_MQTT_CLIENT_PASSWORD);
        exportStatements.add("PNA_USERNAME=" + this.PNA_USERNAME);
        exportStatements.add("PNA_PASSWORD=" + this.PNA_PASSWORD);
        exportStatements.add("PNA_INIT_TOKEN=" + this.PNA_INIT_TOKEN);
        exportStatements.add("USER=" + this.AZURE_VM_USERNAME);
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
