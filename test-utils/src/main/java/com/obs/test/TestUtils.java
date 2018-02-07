package com.obs.test;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public static void verifyRegistered(String protocol, String host, int port, long interval, long timeout, String serviceId) {
        long totalWait = 0;
        while (true) {
            logger.debug("checking {}://{}:{}/eureka/apps/{}", protocol, host, port, serviceId);
            try (CloseableHttpClient client = HttpClientBuilder.create().build();) {
                HttpGet request = new HttpGet(protocol + "://" + host + ":" + port + "/eureka/apps/" + serviceId);
                HttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String res = EntityUtils.toString(response.getEntity());
                    logger.debug("EntityUtils.toString(response.getEntity()) = {}", res);
                    if (res.contains("<status>UP</status>")) {
                        break;
                    }
                }
                try {
                    Thread.sleep(interval);
                    totalWait += interval;
                    if (totalWait > timeout) {
                        throw new IllegalStateException("Timeout while waiting for " + protocol + "://" + host + ":" + port + "/eureka/apps/" + serviceId);
                    }
                } catch (InterruptedException ie) {
                    throw new IllegalStateException(ie);
                }

            } catch (IOException e) {
                e.printStackTrace();


            }
        }
    }

    public static void verifyPort(String host, int port, long interval, long timeout) {
        // wait for eureka sever port to be up
        SocketAddress address = new InetSocketAddress(host, port);
        long totalWait = 0;
        while (true) {
            try {
                logger.debug("checking {} at {}", host, port);
                SocketChannel.open(address);
                break;
            } catch (IOException e) {
                try {
                    Thread.sleep(interval);
                    totalWait += interval;
                    if (totalWait > timeout) {
                        throw new IllegalStateException("Timeout while waiting for port " + port);
                    }
                } catch (InterruptedException ie) {
                    throw new IllegalStateException(ie);
                }
            }
        }
    }

    public static void verifyGatewayGetEndpoint(String serviceDiscoveryProtocol, String serviceDiscoveryHost, int serviceDiscoveryPort, long interval, long timeout, String serviceId, String endpoint, int status) {

        Map<String, String> endpointInfo = getServiceEndpoint(serviceDiscoveryProtocol, serviceDiscoveryHost, serviceDiscoveryPort, serviceId);
        String ip = endpointInfo.get("host");
        String port = endpointInfo.get("port");
        long totalWait = 0;
        while (true) {
            logger.debug("checking {}://{}:{}{} returns {}", serviceDiscoveryProtocol, ip, port, endpoint, status);
            try (CloseableHttpClient client = HttpClientBuilder.create().build();) {
                HttpGet request = new HttpGet(serviceDiscoveryProtocol + "://" + ip + ":" + port + endpoint);
                HttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() == status) {
                    String res = EntityUtils.toString(response.getEntity());
                    logger.debug("EntityUtils.toString(response.getEntity()) = {}", res);
                    break;
                }
                try {
                    Thread.sleep(interval);
                    totalWait += interval;
                    if (totalWait > timeout) {
                        throw new IllegalStateException("Timeout while waiting for " + serviceDiscoveryProtocol + "://" + ip + ":" + port + endpoint);
                    }
                } catch (InterruptedException ie) {
                    throw new IllegalStateException(ie);
                }

            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    public static void verifyServicePostEndpoint(String serviceDiscoveryProtocol, String serviceDiscoveryHost, int serviceDiscoveryPort, long interval, long timeout, String serviceId, String endpoint, int status) {

        Map<String, String> endpointInfo = getServiceEndpoint(serviceDiscoveryProtocol, serviceDiscoveryHost, serviceDiscoveryPort, serviceId);
        String ip = endpointInfo.get("host");
        String port = endpointInfo.get("port");
        long totalWait = 0;
        while (true) {
            logger.debug("checking {}://{}:{}{} returns {}", serviceDiscoveryProtocol, ip, port, endpoint, status);
            try (CloseableHttpClient client = HttpClientBuilder.create().build();) {
                HttpPost request = new HttpPost(serviceDiscoveryProtocol + "://" + ip + ":" + port + endpoint);
                HttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() == status) {
                    String res = EntityUtils.toString(response.getEntity());
                    logger.debug("EntityUtils.toString(response.getEntity()) = {}", res);
                    break;
                }
                try {
                    Thread.sleep(interval);
                    totalWait += interval;
                    if (totalWait > timeout) {
                        throw new IllegalStateException("Timeout while waiting for " + serviceDiscoveryProtocol + "://" + ip + ":" + port + endpoint);
                    }
                } catch (InterruptedException ie) {
                    throw new IllegalStateException(ie);
                }

            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }


    private static Map<String, String> getServiceEndpoint(String serviceDiscoveryProtocol, String serviceDiscoveryHost,
                                                          int serviceDiscoveryPort, String serviceId) {
        Map<String, String> endpoint = new HashMap<>();
        String port = null, ip = null;
        try (CloseableHttpClient client = HttpClientBuilder.create().build();) {
            HttpGet request = new HttpGet(serviceDiscoveryProtocol + "://" + serviceDiscoveryHost + ":" + serviceDiscoveryPort + "/eureka/apps/" + serviceId);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                String res = EntityUtils.toString(response.getEntity());
                logger.debug("EntityUtils.toString(response.getEntity()) = {}", res);

                int portStartPos = StringUtils.indexOf(res, "<port enabled=\"true\">");
                int portEndPos = StringUtils.indexOf(res, "</port>");
                port = StringUtils.substring(res, portStartPos + 21, portEndPos);
                logger.debug("Port: {}", port);

                int ipStartPos = StringUtils.indexOf(res, "<ipAddr>");
                int ipEndPos = StringUtils.indexOf(res, "</ipAddr>");
                ip = StringUtils.substring(res, ipStartPos + 8, ipEndPos);
                logger.debug("IP: {}", ip);

                endpoint.put("host", ip);
                endpoint.put("port", port);

            } else {
                System.out.println("response.getStatusLine().getStatusCode() = " + response.getStatusLine().getStatusCode());
                throw new IllegalStateException("Not registered for " + serviceDiscoveryProtocol + "://" + serviceDiscoveryHost + ":" + serviceDiscoveryPort + "/eureka/apps/" + serviceId);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return endpoint;
    }

    public static void verifyServiceLoginEndpoint(String serviceDiscoveryProtocol, String serviceDiscoveryHost,
                                                  int serviceDiscoveryPort, long interval, long timeout, String serviceId, String endpoint, int expectedStatus, String username, String password) {


        // get the registered app from eureka
        Map<String, String> endpointInfo = getServiceEndpoint(serviceDiscoveryProtocol, serviceDiscoveryHost, serviceDiscoveryPort, serviceId);
        String ip = endpointInfo.get("host");
        String port = endpointInfo.get("port");

        long totalWait = 0;
        while (true) {
            logger.debug("checking {}://{}:{}{} returns {}", serviceDiscoveryProtocol, ip, port, endpoint, expectedStatus);
            try (CloseableHttpClient client = HttpClientBuilder.create().build();) {
                HttpPost request = new HttpPost(serviceDiscoveryProtocol + "://" + ip + ":" + port + endpoint);

                request.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));
                request.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse response = client.execute(request);
                System.out.println("response.getStatusLine().getStatusCode() = " + response.getStatusLine().getStatusCode());
                if (response.getStatusLine().getStatusCode() == expectedStatus) {
                    String res = EntityUtils.toString(response.getEntity());
                    logger.debug("EntityUtils.toString(response.getEntity()) = {}", res);
                    break;
                }
                try {
                    Thread.sleep(interval);
                    totalWait += interval;
                    if (totalWait > timeout) {
                        throw new IllegalStateException("Timeout while waiting for " + serviceDiscoveryProtocol + "://" + ip + ":" + port + endpoint);
                    }
                } catch (InterruptedException ie) {
                    throw new IllegalStateException(ie);
                }

            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }
}
