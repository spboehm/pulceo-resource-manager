package dev.pulceo.prm.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.cloud.contract.wiremock.WireMockSpring;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class SimulatedPulceoNodeAgent {

    private final int id;
    private final String hostName;
    private final WireMockServer wireMockServer;
    private static final List<SimulatedPulceoNodeAgent> list = new ArrayList<>();

    public static void main(String[] args) {
        createAgents(2);
        // press any key to stop agents
        try {
            System.in.read();
            stopAgents();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static List<SimulatedPulceoNodeAgent> createAgents(int number) {
        for (int i = 1; i< number + 1; i++) {
            SimulatedPulceoNodeAgent simulatedPulceoNodeAgent = new SimulatedPulceoNodeAgent(i, "127.0.0." + i);
            list.add(simulatedPulceoNodeAgent);
            System.out.println("Create simulated pulceo-node-agent " + simulatedPulceoNodeAgent.id + " on " + simulatedPulceoNodeAgent.hostName);
            simulatedPulceoNodeAgent.start();
        }
        return list;
    }

    public static void resetAgents() {
        for (int i = 0; i < list.size(); i++) {
            SimulatedPulceoNodeAgent simulatedPulceoNodeAgent = list.get(i);
            simulatedPulceoNodeAgent.reset();
        }
    }

    public static void stopAgents() {
        for (int i = 0; i < list.size(); i++) {
            SimulatedPulceoNodeAgent simulatedPulceoNodeAgent = list.get(i);
            simulatedPulceoNodeAgent.stop();
        }
    }

    private SimulatedPulceoNodeAgent(int id, String hostName) {
        this.id = id;
        this.hostName = hostName;
        this.wireMockServer = new WireMockServer(WireMockSpring.options().bindAddress(hostName).port(7676));;
    }

    public void run() {
        // cloud registration
        this.wireMockServer.stubFor(post(urlEqualTo("/api/v1/cloud-registrations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("registration/pna-"+ this.id + "-cloud-registration-response.json")));

        // create new node
        this.wireMockServer.stubFor(post(urlEqualTo("/api/v1/nodes"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-create-new-logical-node.json")));

        // create new link
        this.wireMockServer.stubFor(post(urlEqualTo("/api/v1/links"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("link/pna-create-new-link-response.json")));

        // read local cpu resources
        this.wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/cpu"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-cpu-resource-response.json")));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/memory"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-memory-resources-response.json")));

        // read local storage resources
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/nodes/localNode/storage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("node/pna-read-storage-resources-response.json")));
    }

    private void start() {
        this.wireMockServer.start();
        this.run();
    }

    private void reset() {
        this.wireMockServer.resetAll();
    }

    private void stop() {
        this.wireMockServer.stop();
    }

}
