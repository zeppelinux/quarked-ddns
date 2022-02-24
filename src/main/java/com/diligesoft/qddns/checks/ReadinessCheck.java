package com.diligesoft.qddns.checks;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.route53.Route53Client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {

    @Inject
    Route53Client client;


    @Inject
    Logger logger;

    HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Readiness check");

    @Override
    public HealthCheckResponse call() {
        logger.debug("in readinessCheck, Amazon client is " + (client == null?"null":"initialized"));
        return (client != null?responseBuilder.up():responseBuilder.down()).build();
    }
}
