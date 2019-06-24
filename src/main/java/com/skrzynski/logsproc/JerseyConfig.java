package com.skrzynski.logsproc;

import com.skrzynski.logsproc.log.LogsEndpoint;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(LogsEndpoint.class);
    }
}
