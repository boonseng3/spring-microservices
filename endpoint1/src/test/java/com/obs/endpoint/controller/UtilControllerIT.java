package com.obs.endpoint.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.obs.endpoint.Application;
import com.obs.test.TestUtils;
import org.junit.Before;
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
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UtilControllerIT {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired // pre-configured to resolve relative paths to http://localhost:${local.server.port}
    private TestRestTemplate restTemplate;
    @Autowired
    private WebApplicationContext context;
    //    private MockMvc mvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Value("${EUREKA_HOST:localhost}")
    private String serviceDiscoveryServerHost;
    @Value("${EUREKA_PORT:8761}")
    private int serviceDiscoveryServerPort;
    private long timeout = 50000;
    private long interval = 100;

    // verify eureka server is up
    @Rule
    public final ExternalResource eurekaServerPort = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            TestUtils.verifyPort(serviceDiscoveryServerHost, serviceDiscoveryServerPort, interval, timeout);
        }
    };
    @Rule
    public final ExternalResource registeredAuthenticationServer = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            TestUtils.verifyRegistered("http", serviceDiscoveryServerHost, serviceDiscoveryServerPort, interval, timeout, "AUTHENTICATION-SERVER");
        }
    };
    @Rule
    public final ExternalResource authenticationLogin = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            TestUtils.verifyServicePostEndpoint("http", serviceDiscoveryServerHost, serviceDiscoveryServerPort, interval, timeout, "AUTHENTICATION-SERVER", "/api/v1/login", 401);
        }
    };

    @Before
    public void setup() {
//        mvc = MockMvcBuilders
//                .webAppContextSetup(context)
//                .apply(SecurityMockMvcConfigurers.springSecurity())
//                .build();
    }

    @Test
    public void echo() throws Exception {
        Map<String, String> postContent = new HashMap<>();
        postContent.put("field1", "value1");
        postContent.put("field2", "value2");

        Map<String, Object> expectedContent = new HashMap<>();
        Map<String, String> expectedHeaderMap = new HashMap<>();
        expectedHeaderMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        expectedContent.put("params", Collections.EMPTY_MAP);
        expectedContent.put("headers", expectedHeaderMap);
        expectedContent.put("body", postContent);

        System.out.println("expectedContent = " + objectMapper.writeValueAsString(expectedContent));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(postContent), requestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/v1/echo", HttpMethod.POST, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

//
//        MvcResult result = mvc.perform(MockMvcRequestBuilders
//                .post("/api/v1/echo")
//                .content(objectMapper.writeValueAsString(postContent)).contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String content = result.getResponse().getContentAsString();
        String content = responseEntity.getBody();
        JsonNode responseContentNode = objectMapper.readTree(content);
        assertThat(responseContentNode.get("headers").get("content-type").asText()).isEqualTo(MediaType.APPLICATION_JSON_UTF8_VALUE);
        assertThat(((ObjectNode) responseContentNode.get("params")).size()).isEqualTo(0);
        assertThat(responseContentNode.get("body").get("field1").asText()).isEqualTo("value1");
        assertThat(responseContentNode.get("body").get("field2").asText()).isEqualTo("value2");
    }

    @Test
    public void accessAdminResources() throws Exception {
        String token = login("admin", "P@ssw0rd");
        System.out.println("token = " + token);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("x-auth-token", token);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/v1/admin_resource", HttpMethod.POST, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

//        MvcResult result = mvc.perform(MockMvcRequestBuilders
//                .post("/api/v1/admin_resource")
//                .header("x-auth-token", token))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
    }

    @Test
    public void accessUserResources() throws Exception {
        String token = login("user", "P@ssw0rd");
        System.out.println("token = " + token);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("x-auth-token", token);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/v1/user_resource", HttpMethod.POST, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

//        MvcResult result = mvc.perform(MockMvcRequestBuilders
//                .post("/api/v1/user_resource")
//                .header("x-auth-token", token))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
    }

    @Test
    public void accessAdminResourcesUnsuccessful() throws Exception {
        String token = login("user", "P@ssw0rd");
        System.out.println("token = " + token);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("x-auth-token", token);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/v1/admin_resource", HttpMethod.POST, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

//        MvcResult result = mvc.perform(MockMvcRequestBuilders
//                .post("/api/v1/admin_resource")
//                .header("x-auth-token", token))
//                .andDo(print())
//                .andExpect(status().isUnauthorized())
//                .andReturn();


    }

    @Test
    public void adminAccessProfile() throws Exception {
        String token = login("admin", "P@ssw0rd");
        System.out.println("token = " + token);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("x-auth-token", token);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/v1/profile/3", HttpMethod.POST, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        requestEntity = new HttpEntity<>(null, requestHeaders);
        responseEntity = restTemplate.exchange("/api/v1/profile/1", HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        requestEntity = new HttpEntity<>(null, requestHeaders);
        responseEntity = restTemplate.exchange("/api/v1/profile/2", HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void userAccessProfileSuccessful() throws Exception {
        String token = login("user", "P@ssw0rd");
        System.out.println("token = " + token);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("x-auth-token", token);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/v1/profile/3", HttpMethod.POST, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void userAccessProfileUnSuccessful() throws Exception {
        String token = login("user", "P@ssw0rd");
        System.out.println("token = " + token);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("x-auth-token", token);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/v1/profile/2", HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        responseEntity = restTemplate.exchange("/api/v1/profile/1", HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    public void user2AccessProfileSuccessful() throws Exception {
        String token = login("user2", "P@ssw0rd");
        System.out.println("token = " + token);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("x-auth-token", token);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/v1/profile/2", HttpMethod.POST, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void user2AccessProfileUnSuccessful() throws Exception {
        String token = login("user2", "P@ssw0rd");
        System.out.println("token = " + token);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("x-auth-token", token);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/v1/profile/3", HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        responseEntity = restTemplate.exchange("/api/v1/profile/1", HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    private String login(String username, String password) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("username", username);
        params.add("password", password);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, requestHeaders);
        ResponseEntity<Void> responseEntity = restTemplate.exchange("http://localhost:8500/api/v1/login", HttpMethod.POST, requestEntity, Void.class);
        System.out.println("responseEntity = " + responseEntity);
        String token = responseEntity.getHeaders().getFirst("x-auth-token");
        System.out.println("token = " + token);
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();

        return token;
    }
}