package com.cch.router;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.pahoMqtt5;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.timer;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.jboss.logging.Logger;

import com.cch.config.Mqtt;
import com.cch.payloay.Device;

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
        
        from(
            direct("emqx-broker")
        ).routeId("qos-test")
        .log(LoggingLevel.INFO, "send message: ${body}.")
        .to(pahoMqtt5(mqttConfig.topic())
                .clientId("itachi")
                .automaticReconnect(true)
                .cleanStart(true)
                .keepAliveInterval(60)
                .qos(0)
                .retained(mqttConfig.retained())
                .brokerUrl(mqttConfig.brokerUrl()))
        ;

        from(
            timer("generate-payload")
            .delay(1000)
            .fixedRate(true)
            .period(2000)
        )
        .process(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                Device device = new Device("itachi", (int) (Math.random() * (80 - 3)) + 3, (int) (Math.random() * (100 - 40)) + 40);
                LOG.info("Sending Device: " + device.deviceName);

                exchange.getMessage().setBody(device);
            }
            
        })
        .marshal().json(JsonLibrary.Jackson)
        .to(direct("emqx-broker")).end();
    }
    
}
