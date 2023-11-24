package com.cch.router;

import org.apache.camel.builder.RouteBuilder;

import com.cch.config.Mqtt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.pahoMqtt5;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.timer;


import org.apache.camel.LoggingLevel;

@ApplicationScoped
public class MqttRouteBuilder extends RouteBuilder {

    @Inject
    Mqtt mqttConfig;
    
    @Override
    public void configure() throws Exception {
        from(
            direct("emqx-broker")
        ).routeId("will-topic-test")
        .log(LoggingLevel.INFO, "send message: ${body}.")
        .to(pahoMqtt5(mqttConfig.topic())
                .clientId("itachi")
                .brokerUrl(mqttConfig.brokerUrl())
                .keepAliveInterval(1)
                .willQos(2)
                .willRetained(true)
                .willPayload("""
                    offline
                """)
                .willTopic("itachi/offline")
        )
        ;
        from(
            timer("generate-payload")
            .delay(1000)
            .fixedRate(true)
            .period(2000)
        ).routeId("generate-payload")
        .to(direct("emqx-broker")).end();
    }
    
}
