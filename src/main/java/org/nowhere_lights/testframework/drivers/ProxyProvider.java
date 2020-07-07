package org.nowhere_lights.testframework.drivers;

import io.netty.handler.codec.http.HttpHeaders;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.filters.ResponseFilterAdapter;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nowhere_lights.testframework.drivers.utils.PropertiesContext;
import org.openqa.selenium.Proxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProxyProvider {

    private static final Logger _logger = LogManager.getLogger(ProxyProvider.class.getSimpleName());
    private static String PROXY_PORT = "8889";
    private static BrowserMobProxy proxy;
    private static String PROXY_URL;
    private static Proxy seleniumProxy;


    private static Set<String> requests = new HashSet<>();
    private static Set<String> responses = new HashSet<>();
    private static Map<String, String> responseMap = new HashMap<>();

    public void setProxy() {
        try {
            PROXY_URL = InetAddress.getLocalHost().getHostAddress() + ":" + PROXY_PORT;
        } catch (UnknownHostException e) {
            _logger.warn("Something went wrong with connection (host is unknown): " + e.getCause());
            e.printStackTrace();
        }
        proxy = new BrowserMobProxyServer();
        proxy.setMitmDisabled(true);
        proxy.setTrustAllServers(true);
        proxy.start(Integer.parseInt(PROXY_PORT));
        proxy.setHarCaptureTypes(
                CaptureType.REQUEST_HEADERS,
                CaptureType.RESPONSE_HEADERS,
                CaptureType.REQUEST_CONTENT,
                CaptureType.RESPONSE_CONTENT);
        proxy.enableHarCaptureTypes(
                CaptureType.REQUEST_HEADERS,
                CaptureType.RESPONSE_HEADERS,
                CaptureType.REQUEST_CONTENT,
                CaptureType.RESPONSE_CONTENT);

        seleniumProxy = ClientUtil
                .createSeleniumProxy(proxy)
                .setHttpProxy(PROXY_URL)
                .setSslProxy(PROXY_URL)
                .setFtpProxy(PROXY_URL);
        proxy.newHar(PropertiesContext.getInstance().getProperty("urltest"));
    }

    public static Proxy getSeleniumProxy() {
        return seleniumProxy;
    }

    public static BrowserMobProxy getProxy() {
        return proxy;
    }

    public static void setRequestHeader(String header, String value) {
        proxy.addRequestFilter(((request, contents, messageInfo) -> {
            request.headers().set(header, value);
            return null;
        }));
    }

    public void getRequestHeaders() {
        proxy.addRequestFilter(((request, contents, messageInfo) -> {
            HttpHeaders reqHeaders = request.headers();
            String url = messageInfo.getUrl();
            for (Map.Entry<String, String> entry : reqHeaders.entries()) {
                _logger.warn("header: " + entry.getKey());
                _logger.warn("value: " + entry.getValue());
            }
            requests.add(url + "\n\n" + contents.getTextContents());
            return null;
        }));
    }

    public void getResponseHeader(String apiSign) {
        ResponseFilter filter = (((response, contents, messageInfo) -> {
            HttpHeaders respHeaders = response.headers();
            String url = messageInfo.getUrl();
            _logger.warn("filter url " + messageInfo.getOriginalUrl());
            _logger.warn("filter url " + messageInfo.getOriginalUrl().contains(apiSign));
            for (Map.Entry<String, String> entry : respHeaders) {
                _logger.warn("header response: " + entry.getKey());
                _logger.warn("value response: " + entry.getKey());
            }
            if (messageInfo.getOriginalUrl().contains(apiSign)) {
                _logger.warn("reg catcher: " + contents.getTextContents());
                responseMap.put(apiSign, contents.getTextContents());
            }
            responses.add(url + "\n\n" + contents.getTextContents());
        }));
        proxy.addFirstHttpFilterFactory(new ResponseFilterAdapter.FilterSource(filter, 16777216));
    }

}
