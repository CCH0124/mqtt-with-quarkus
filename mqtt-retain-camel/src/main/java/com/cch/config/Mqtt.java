package com.cch.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "mqtt")
public interface Mqtt {
    
    String brokerUrl();
    
    @WithDefault(value = "itachi/status")
    String topic();

    @WithDefault(value = "true")
    Boolean retained();
}
