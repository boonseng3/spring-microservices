package com.obs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.test.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ApplicationIT {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired // pre-configured to resolve relative paths to http://localhost:${local.server.port}
    private TestRestTemplate restTemplate;
    //    @Autowired
//    private EurekaDiscoveryClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${EUREKA_HOST:localhost}")
    private String serviceDiscoveryServerHost;
    @Value("${EUREKA_PORT:8761}")
    private int serviceDiscoveryServerPort;

    private long timeout = 100000;
    private long interval = 1000;

    // verify eureka server is up
    @Rule
    public final ExternalResource eurekaServerPort = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            TestUtils.verifyPort(serviceDiscoveryServerHost, serviceDiscoveryServerPort, interval, timeout);
        }
    };
    @Rule
    public final ExternalResource registeredInteractionServer = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            TestUtils.verifyRegistered("http", serviceDiscoveryServerHost, serviceDiscoveryServerPort, interval, timeout, "ENDPOINT1");
        }
    };

    @Rule
    public final ExternalResource registeredAuthenticationServer = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            TestUtils.verifyRegistered("http", serviceDiscoveryServerHost, serviceDiscoveryServerPort, interval, timeout, "AUTHENTICATION");
        }
    };

    @Rule
    public final ExternalResource registeredApiGateway = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            TestUtils.verifyRegistered("http", serviceDiscoveryServerHost, serviceDiscoveryServerPort, interval, timeout, "API-GATEWAY");
        }
    };

    @Rule
    public final ExternalResource apiGatewayEcho = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            TestUtils.verifyGatewayGetEndpoint("http", serviceDiscoveryServerHost, serviceDiscoveryServerPort, interval, timeout, "API-GATEWAY", "/endpoint1/api/v1/echo", 200);
        }
    };

    @Rule
    public final ExternalResource authenticationLogin = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            System.out.println("verify authenticationLogin");
            TestUtils.verifyServicePostEndpoint("http", serviceDiscoveryServerHost, serviceDiscoveryServerPort, interval, timeout, "AUTHENTICATION", "/api/v1/login", 401);
        }
    };

    @Rule
    public final ExternalResource authenticationLoginSuccessful = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            System.out.println("verify authenticationLoginSuccessful");
            TestUtils.verifyServiceLoginEndpoint("http", serviceDiscoveryServerHost, serviceDiscoveryServerPort, interval, timeout, "AUTHENTICATION", "/api/v1/login", 200, "user", "P@ssw0rd");
        }
    };


    @Test
    public void login() throws Exception {
        MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<String, String>();
        loginParams.add("username", "user");
        loginParams.add("password", "P@ssw0rd");
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<Void> result = restTemplate.postForEntity("/authentication/api/v1/login", new HttpEntity<MultiValueMap<String, String>>(loginParams, requestHeaders), Void.class);
        System.out.println("result.getStatusCode().value() = " + result.getStatusCode().value());
        System.out.println("result.toString() = " + result.toString());
        String sessionId = result.getHeaders().get("x-auth-token").get(0);
        assertThat(sessionId).isNotEmpty();
        logger.debug("session id: {}", sessionId);


    }
}