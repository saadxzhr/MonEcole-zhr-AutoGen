package com.szschoolmanager.auth.config;

import org.apache.catalina.valves.RemoteIpValve;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ✅ Configuration production-ready
 * Active la traduction des IPs réelles derrière proxy (Nginx, Cloudflare, etc.)
 * Aucun impact négatif en dev local — safe à laisser activé.
 */
@Configuration
public class TomcatProxyConfig {

    @Bean
    public TomcatServletWebServerFactory tomcatCustomizer() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        RemoteIpValve valve = new RemoteIpValve();
        valve.setRemoteIpHeader("X-Forwarded-For");
        valve.setProtocolHeader("X-Forwarded-Proto");
        factory.addContextValves(valve);
        return factory;
    }
}
