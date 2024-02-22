package dev.pulceo.prm.service;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.*;
import dev.pulceo.prm.dto.node.CreateNewAzureNodeDTO;
import dev.pulceo.prm.model.node.AzureNode;
import dev.pulceo.prm.model.provider.AzureCredentials;
import dev.pulceo.prm.model.provider.AzureProvider;
import org.eclipse.paho.client.mqttv3.internal.websocket.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AzureDeploymentService {

    private final ProviderService providerService;

    @Autowired
    public AzureDeploymentService(ProviderService providerService) {
        this.providerService = providerService;
    }

    public AzureNode createAzureNode(CreateNewAzureNodeDTO createNewAzureNodeDTO) {

        // TODO: check if provider does exist

        // retrieve AzureProvider
        AzureProvider azureProvider = this.providerService.readAzureProviderByProviderMetaDataName(createNewAzureNodeDTO.getProviderName()).orElseThrow();
        System.out.println(azureProvider);

        // TODO: retrieve credentials
        AzureCredentials azureCredentials = azureProvider.getCredentials();
        System.out.println(azureCredentials);

        // Deploy to Azure
        final String userName = "pulceo";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDIoQyWzINPVvv37RLxb/QKO94XCOUo2bIC91XwAXCfoAgy165XNjPSgOLe74MCC/A0rIRt1hBfK18ynDhPSnqYSGXTo74ReEoS8WQ7gGR0e/h27ozuELpOWO8TVotBIuIhmS1Bepnk14TXjpCM/yq4DD8eg9kEz/eq5yjdwTUSMnLg+RERQzLxkWp41LKJ2itKjHh6vy+HDJDOzsSojdd6GeWfOwQkQMtL2Y0S1YEvrbT+rRHmsjZf4j+bxZnw/XpGJkPHZGs9AFwiLX00Q2b0ECuDSBtWaVNbJ0bU8rkimUGo6RHEE7EEtgNpqX0PFt0/Zwn2PFi2UHf5nSD2JESh";

        try {
            TokenCredential credential = new DefaultAzureCredentialBuilder()
                    .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
                    .build();

            ClientSecretCredential clientSecretCredential = getClientSecretCredential(azureCredentials);

            // If you don't set the tenant ID and subscription ID via environment variables,
            // change to create the Azure profile with tenantId, subscriptionId, and Azure environment.
            AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                    .withLogLevel(HttpLogDetailLevel.BASIC)
                    .authenticate(clientSecretCredential, profile)
                    .withSubscription(azureCredentials.getSubscriptionId());

            // Create a new resource group
            String randomString = "pulceo-asd684";

            Disk disk = azureResourceManager.disks().define(randomString)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(randomString)
                    .withData()
                    .withSizeInGB(10)
                    .withSku(DiskSkuTypes.STANDARD_SSD_LRS)
                    .create();

            // Create an Ubuntu virtual machine in a new resource group.
            VirtualMachine linuxVM = azureResourceManager.virtualMachines().define("testLinuxVM")
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(randomString)
                    .withNewPrimaryNetwork("10.0.0.0/24")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(randomString)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS_GEN2)
                    .withRootUsername(userName)
                    .withSsh(sshKey)
                    .withSize(VirtualMachineSizeTypes.STANDARD_B1S)
                    .withUserData(Base64.encode("echo 'Hello, World!' > /home/pulceo/hello.txt"))
                    .create();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
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
//        private String nodeLocationCity;


        return null;
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
