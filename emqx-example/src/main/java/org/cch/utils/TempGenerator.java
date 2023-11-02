package org.cch.utils;

import io.smallrye.mutiny.Multi;
import org.cch.inbound.MqttDeviceMessageConsumer;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.cch.entity.Device;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

@ApplicationScoped
public class TempGenerator {
    private static final Logger LOG = Logger.getLogger(TempGenerator.class);
    @Outgoing("device-temp")
    public Multi<Device> generate() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
                .onOverflow().drop()
                .map(tick -> {
                    Device device = new Device("itachi-01", (int) (Math.random() * (80 - 3)) + 3, (int) (Math.random() * (100 - 40)) + 40);
                    LOG.info("Sending Device: " + device.toString());
                    return device;
                });
    }
}
