package com.obs.microservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.microservices.dto.*;
import com.obs.microservices.entity.Profile;
import com.obs.microservices.entity.User;
import com.obs.microservices.repo.ProfileRepo;
import com.obs.microservices.repo.UserRepo;
import org.assertj.core.api.Condition;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Configuration
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@FlywayTest
@TestExecutionListeners(mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
        listeners = {FlywayTestExecutionListener.class})
public class ApplicationTestIT {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired // pre-configured to resolve relative paths to http://localhost:${local.server.port}
    private TestRestTemplate restTemplate;
    @Value("${local.server.port}")
    private int serverPort;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Value("${spring.redis.host}")
    private String redisServerHost;
    @Value("${spring.redis.port}")
    private int redisServerPort;


    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProfileRepo profileRepo;


    private MockMvc mvc;
    @Autowired
    private WebApplicationContext context;

    @Rule
    public final ExternalResource redisServer = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            SocketAddress address = new InetSocketAddress(redisServerHost, redisServerPort);
            long totalWait = 0;

            // check service discovery port up
            while (true) {
                try {
                    logger.debug("checking {} at {}", redisServerHost, redisServerPort);
                    SocketChannel.open(address);
                    break;
                } catch (IOException e) {
                    try {
                        Thread.sleep(100);
                        totalWait += 100;
                        if (totalWait > 10000) {
                            throw new IllegalStateException("Timeout while waiting for port " + redisServerPort);
                        }
                    } catch (InterruptedException ie) {
                        throw new IllegalStateException(ie);
                    }
                }
            }
        }

        @Override
        protected void after() {

        }

    };

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    public RestTemplateBuilder restTemplateBuilder() {
        // disable retry due to server authentication failure, in streaming mode
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);
        return new RestTemplateBuilder().rootUri("http://" + redisServerHost + ":" + serverPort).requestFactory(requestFactory);
    }

    @Test
    public void unauthorized() throws Exception {
        MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<String, String>();
        loginParams.add("username", "user");
        loginParams.add("password", UUID.randomUUID().toString());

        Condition<? super Throwable> hasTokenHeader = new Condition<>(ex -> ((HttpClientErrorException) ex).getResponseHeaders().containsKey("x-auth-token")
                , "Contains x-auth-token");
        assertThatThrownBy(() -> {
            restTemplateBuilder().build().exchange("/api/v1/login", HttpMethod.POST, new HttpEntity<MultiValueMap<String, String>>(loginParams, new HttpHeaders()), String.class);

        }).isInstanceOf(HttpClientErrorException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED)
                .hasMessageContaining("401 null")
                .doesNotHave(hasTokenHeader)
        ;
    }

    @Test
    public void login_successful() throws Exception {
        MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<String, String>();
        loginParams.add("username", "user");
        loginParams.add("password", "P@ssw0rd");
        ResponseEntity<String> result = restTemplate.postForEntity("/api/v1/login", new HttpEntity<MultiValueMap<String, String>>(loginParams, new HttpHeaders()), String.class);
        String sessionId = result.getHeaders().get("x-auth-token").get(0);
        assertThat(sessionId).isNotEmpty();
        logger.debug("session id: {}", sessionId);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("x-auth-token", sessionId);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("currentValue", UUID.randomUUID().toString());
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, requestHeaders);

        result = restTemplate.exchange("/api/v1/session", HttpMethod.POST, requestEntity, String.class);
        assertThat(redisTemplate.hasKey("spring:session:sessions:" + sessionId)).isTrue();

        logger.debug("result: {}", result.getBody());
    }

    @WithMockUser(value = "user")
    @Test
    public void accessProtectedResourceWithLogin() throws Exception {

        User user = userRepo.findByUsername("user").get();
        Profile profile = profileRepo.findByUser(user).get();

        mvc.perform(MockMvcRequestBuilders.get("/profile/" + profile.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"name\": \"user 1\"}"));
        mvc.perform(MockMvcRequestBuilders.get("/profile/" + (profile.getId() + 1)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @WithMockUser(value = "user2")
    @Test
    public void accessProtectedResourceWithLogin2() throws Exception {
        User user = userRepo.findByUsername("user2").get();
        Profile profile = profileRepo.findByUser(user).get();
        mvc.perform(MockMvcRequestBuilders.get("/profile/" + profile.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"name\": \"user 2\"}"));
        mvc.perform(MockMvcRequestBuilders.get("/profile/" + (profile.getId() + 1)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @WithMockUser(value = "admin")
    @Test
    public void accessProtectedResourceWithAdminLogin() throws Exception {
        User user = userRepo.findByUsername("admin").get();
        Profile profile = profileRepo.findByUser(user).get();
        mvc.perform(MockMvcRequestBuilders.get("/profile/" + profile.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"name\": \"admin\"}"));

        user = userRepo.findByUsername("user").get();
        profile = profileRepo.findByUser(user).get();
        mvc.perform(MockMvcRequestBuilders.get("/profile/" + profile.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"name\": \"user 1\"}"));

        user = userRepo.findByUsername("user2").get();
        profile = profileRepo.findByUser(user).get();
        mvc.perform(MockMvcRequestBuilders.get("/profile/" + profile.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"name\": \"user 2\"}"));
    }


    @WithMockUser(value = "admin")
    @Test
    public void accessAcl() throws Exception {
        AclRequest<Long> aclRequest = new AclRequest<>()
                .setOid(Arrays.asList
                        (
                                new ObjectIdentityDto<>()
                                        .setIdentifier(new Long(2))
                                        .setType("com.obs.dto.Profile"),
                                new ObjectIdentityDto<>()
                                        .setIdentifier(new Long(3))
                                        .setType("com.obs.dto.Profile")
                        ))
                .setSid(Arrays.asList("user", "user2"));

        List<AclResponse<Long>> expectedResponse = Arrays.asList(new AclResponse<>().setObjectIdentity(new ObjectIdentityDto<>().setIdentifier(new Long(2)).setType("com.obs.dto.Profile"))
                        .setAcl(new AclDto().setParentAcl(null)
                                .setObjectIdentity(new ObjectIdentityDto<>().setIdentifier(new Long(2)).setType("com.obs.dto.Profile"))
                                .setOwner(new SidDto().setPrincipal("user2"))
                                .setEntriesInheriting(false)
                                .setId(new Long(3))
                                .setEntries(Arrays.asList(new AccessControlEntryDto<>().setPermission(new PermissionDto().setMask(1).setPattern("GET_PROFILE"))
                                        .setId(new Long(3))
                                        .setSid(new SidDto().setPrincipal("user2"))
                                        .setAuditFailure(false)
                                        .setAuditSuccess(false)
                                        .setGranting(true)))),
                new AclResponse<>().setObjectIdentity(new ObjectIdentityDto<>().setIdentifier(new Long(3)).setType("com.obs.dto.Profile"))
                        .setAcl(new AclDto().setParentAcl(null)
                                .setObjectIdentity(new ObjectIdentityDto<>().setIdentifier(new Long(3)).setType("com.obs.dto.Profile"))
                                .setOwner(new SidDto().setPrincipal("user"))
                                .setEntriesInheriting(false)
                                .setId(new Long(2))
                                .setEntries(Arrays.asList(new AccessControlEntryDto<>().setPermission(new PermissionDto().setMask(1).setPattern("GET_PROFILE"))
                                        .setId(new Long(2))
                                        .setSid(new SidDto().setPrincipal("user"))
                                        .setAuditFailure(false)
                                        .setAuditSuccess(false)
                                        .setGranting(true))))
        );

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/acl")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(aclRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse), false));

    }

    @WithMockUser(value = "admin")
    @Test
    public void accessPermission() throws Exception {

        List<CustomPermission> expectedResponse = Arrays.asList(
                new CustomPermission(1, "GET_PROFILE")
        );

        mvc.perform(MockMvcRequestBuilders.get("/api/v1/permissions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse), false));

    }
}