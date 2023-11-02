package org.cch.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.cch.entity.Device;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.io.*;

@ApplicationScoped
public class MqttDeviceMessageConsumer {
    private static final Logger LOG = Logger.getLogger(MqttDeviceMessageConsumer.class);
    @Incoming("devices")
    @Outgoing("my-data-stream")
    @Broadcast
    public Device process(byte[] data) {
        try {
            var device = new ObjectMapper().readValue(data, Device.class);
            LOG.info("Consumer Receiving readings: " + device.toString());
            return device;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
