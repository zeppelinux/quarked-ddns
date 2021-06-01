package com.diligesoft.qddns.checks;

import com.amazonaws.services.route53.AmazonRoute53;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {

    @Inject
    AmazonRoute53 client;

    HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Readiness check");

    @Override
    public HealthCheckResponse call() {
        return (client != null?responseBuilder.up():responseBuilder.down()).build();
    }
}
