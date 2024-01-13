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
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;

public class App {
    public static void main(String[] args) {
        final String userName = "testtesttesttest";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDIoQyWzINPVvv37RLxb/QKO94XCOUo2bIC91XwAXCfoAgy165XNjPSgOLe74MCC/A0rIRt1hBfK18ynDhPSnqYSGXTo74ReEoS8WQ7gGR0e/h27ozuELpOWO8TVotBIuIhmS1Bepnk14TXjpCM/yq4DD8eg9kEz/eq5yjdwTUSMnLg+RERQzLxkWp41LKJ2itKjHh6vy+HDJDOzsSojdd6GeWfOwQkQMtL2Y0S1YEvrbT+rRHmsjZf4j+bxZnw/XpGJkPHZGs9AFwiLX00Q2b0ECuDSBtWaVNbJ0bU8rkimUGo6RHEE7EEtgNpqX0PFt0/Zwn2PFi2UHf5nSD2JESh";

        try {
            TokenCredential credential = new DefaultAzureCredentialBuilder()
                    .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
                    .build();

            ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                    .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
                    .clientId("")
                    .clientSecret("")
                    .tenantId("")
                    .build();

            // If you don't set the tenant ID and subscription ID via environment variables,
            // change to create the Azure profile with tenantId, subscriptionId, and Azure environment.
            AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                    .withLogLevel(HttpLogDetailLevel.BASIC)
                    .authenticate(clientSecretCredential, profile)
                    .withSubscription("");

            // Create an Ubuntu virtual machine in a new resource group.
            VirtualMachine linuxVM = azureResourceManager.virtualMachines().define("testLinuxVM")
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup("sampleVmResourceGroup")
                    .withNewPrimaryNetwork("10.0.0.0/24")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshKey)
                    .withSize(VirtualMachineSizeTypes.STANDARD_B1S)
                    .create();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
