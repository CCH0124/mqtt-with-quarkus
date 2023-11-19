package org.cch.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "mqtt")
public interface Mqtt {
    
    String brokerUrl();
    
    @WithDefault(value = "itachi/status")
    String topic();

}
