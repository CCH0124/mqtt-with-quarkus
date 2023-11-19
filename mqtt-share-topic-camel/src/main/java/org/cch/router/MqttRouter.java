package org.cch.router;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.pahoMqtt5;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.timer;

import java.net.InetAddress;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.cch.config.Mqtt;
import org.jboss.logging.Logger;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MqttRouter extends RouteBuilder {

    @Inject
    Logger LOG;

    @Inject
    Mqtt mqttConfig;

    @Override
    public void configure() throws Exception {
        from(pahoMqtt5(mqttConfig.topic())
                .clientId("%s-%s".formatted("itachi-share-topic", InetAddress.getLocalHost().getHostName()))
                .brokerUrl(mqttConfig.brokerUrl()))
            .log(LoggingLevel.INFO, "Payload: ${body}")
            .routeId("%s-%s".formatted("itachi-share-topic", InetAddress.getLocalHost().getHostName()))
            .end();
    }
    
}
