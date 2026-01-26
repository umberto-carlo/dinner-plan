package it.ucdm.leisure.dinnerplan.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

@Configuration
public class RestTemplateConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Value("${proxy.host:}")
    private String proxyHost;

    @Value("${proxy.port:0}")
    private int proxyPort;

    @Value("${proxy.username:}")
    private String proxyUsername;

    @Value("${proxy.password:}")
    private String proxyPassword;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        if (proxyHost != null && !proxyHost.isBlank() && proxyPort > 0) {
            logger.info("Configuring Proxy: {}:{}", proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            requestFactory.setProxy(proxy);

            if (proxyUsername != null && !proxyUsername.isBlank() && proxyPassword != null && !proxyPassword.isBlank()) {
                logger.info("Configuring Proxy Authentication for user: {}", proxyUsername);
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        if (getRequestorType() == RequestorType.PROXY) {
                            return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
                        }
                        return super.getPasswordAuthentication();
                    }
                });
            }
        } else {
            logger.info("No Proxy configured");
        }

        return new RestTemplate(requestFactory);
    }
}
