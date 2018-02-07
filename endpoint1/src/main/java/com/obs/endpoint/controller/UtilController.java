package com.obs.endpoint.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class UtilController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;


    @RequestMapping(value = "/api/v1/echo")
    public Map<String, Object> echo(HttpServletRequest request, @RequestBody(required = false) String body) throws IOException {
        logger.debug("body: {}", body);


        Map<String, Object> result = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String> headerMap = new HashMap<>();

        result.put("params", paramMap);
        result.put("headers", headerMap);

        request.getParameterMap()
                .forEach((s, strings) -> {
                    paramMap.put(s, strings);
                });
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String paraName = params.nextElement();
            String paramValue = request.getParameter(paraName);
            paramMap.put(paraName, paramValue);
        }
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String paraName = headers.nextElement();
            String paramValue = request.getHeader(paraName);
            headerMap.put(paraName, paramValue);
        }
        if (StringUtils.hasText(body)) {
            try {
                result.put("body", objectMapper.readValue(body, Map.class));
            } catch (JsonParseException e) {
                result.put("body", body);
            }

        }
        logger.debug("result: {}", objectMapper.writeValueAsString(result));
        return result;
    }

    @RequestMapping(value = "/api/v1/info")
    public Map<String, Object> serverInfo(HttpServletRequest request) throws IOException {
        Map<String, Object> result = new HashMap<>();

        URL url = new URL(request.getRequestURL().toString());
        result.put("url", url.getProtocol());
        result.put("host", url.getHost());
        result.put("port", url.getPort());
        return result;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/api/v1/admin_resource")
    public Map<String, Object> adminResource(HttpServletRequest request) throws IOException {
        Map<String, Object> result = new HashMap<>();


        URL url = new URL(request.getRequestURL().toString());
        result.put("url", url.getProtocol());
        result.put("host", url.getHost());
        result.put("port", url.getPort());
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @RequestMapping(value = "/api/v1/user_resource")
    public Map<String, Object> userResource(HttpServletRequest request) throws IOException {
        Map<String, Object> result = new HashMap<>();

        URL url = new URL(request.getRequestURL().toString());
        result.put("url", url.getProtocol());
        result.put("host", url.getHost());
        result.put("port", url.getPort());
        return result;
    }

    @PreAuthorize("hasPermission(#id, 'com.obs.dto.Profile', 'GET_PROFILE') || hasPermission(0, 'com.obs.dto.Profile', 'GET_PROFILE')")
    @RequestMapping(value = "/api/v1/profile/{id}")
    public void accessProfile(HttpServletRequest request, @PathVariable Long id) throws IOException {
        System.out.println("id = " + id);
//        String token = request.getHeader("x-auth-token");
//        HttpHeaders requestHeaders = new HttpHeaders();
//        requestHeaders.add("x-auth-token", token);
//        MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
//        params.add("identifier", id);
//        params.add("type", "com.obs.dto.Profile");
//
//
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(params, requestHeaders);
//        ResponseEntity<AclDto> result = restTemplate.exchange("http://localhost:8500/api/v1/acl?identifier=3&type=com.obs.dto.Profile", HttpMethod.GET, requestEntity, AclDto.class);
//        AclDto acl = null;
//        if (result.getStatusCode().is2xxSuccessful()) {
//            acl = result.getBody();
//            System.out.println("acl = " + acl);
//            System.out.println("acl = " + objectMapper.writeValueAsString(acl));
//
//        } else {
//            logger.error("Retrieve acl status code = {}. Message = {}", result.getStatusCode(), result.toString());
//            throw new RuntimeException("Unable to retrieve acl");
//        }


    }
}
