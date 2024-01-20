package dev.pulceo.prm.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;

@SpringBootTest
public class NodeServiceIntegrationTest {

    @Autowired
    private NodeService nodeService;



    @Value("${pna.test.init.token}")
    private String pnaInitToken;

    // for some reason `dynamicPort()` is not working properly
    public static WireMockServer wireMockServer = new WireMockServer(WireMockSpring.options().port(7676));

    @BeforeAll
    static void setupClass() {
        wireMockServer.start();
    }

    @AfterEach
    void after() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void clean() {
        wireMockServer.shutdown();
    }

}
