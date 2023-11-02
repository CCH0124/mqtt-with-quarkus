package org.cch.outbound;

import org.cch.entity.Device;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;

@ApplicationScoped
public class MqttDeviceMessageProducer {
    private static final Logger LOG = Logger.getLogger(MqttDeviceMessageProducer.class);

    @Incoming("my-data-stream")
    public Device process(Device data) {
        if (Objects.isNull(data)) {
            LOG.error("Producer Receive Error.");
        }
        if (data.getTemp() < 40 && data.getHumidity() < 60) {
            LOG.info("Producer Receiving readings: " + data.toString());
            return data;
        }
        LOG.warn("Producer Receiving readings: " + data.toString());
        return null;
    }
}
