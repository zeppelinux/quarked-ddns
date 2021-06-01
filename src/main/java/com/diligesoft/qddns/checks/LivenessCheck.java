package com.diligesoft.qddns.checks;

import com.diligesoft.qddns.routes.TriggerRoute;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@ApplicationScoped
public class LivenessCheck implements HealthCheck {

    @Inject
    TriggerRoute route;

    HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Liveness Check");

    @Override
    public HealthCheckResponse call() {
        return (route.isWorking()?responseBuilder.up():responseBuilder.down()).build();
    }
}
