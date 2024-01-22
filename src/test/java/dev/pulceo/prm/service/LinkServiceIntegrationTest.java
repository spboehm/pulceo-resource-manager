package dev.pulceo.prm.service;


import dev.pulceo.prm.util.SimulatedPulceoNodeAgent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LinkServiceIntegrationTest {

    @BeforeAll
    static void setupClass() {
        SimulatedPulceoNodeAgent.createAgents(2);
    }

    @AfterEach
    void after() {
        SimulatedPulceoNodeAgent.resetAgents();
    }

    @AfterAll
    static void clean() {
        SimulatedPulceoNodeAgent.stopAgents();
    }

    @Test
    public void testCreateLinkWithExistingNodes() throws InterruptedException {
        // given
        // TODO: one simulated
        Thread.sleep(10000);
    }

}
