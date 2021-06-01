package com.diligesoft.gwifiddns.routes;

import com.diligesoft.gwifiddns.Route53Bean;
import io.quarkus.runtime.configuration.ProfileManager;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;

@ApplicationScoped
public class TriggerRoute extends RouteBuilder {

    public static String ROUTE_ID = "trigger-route";

    @Inject
    Logger logger;

    @Inject
    Endpoints endpoints;

    @ConfigProperty(name = "qddns.url")
    String gwifiUrl;

    @ConfigProperty(name = "qddns.routerIP")
    String routerIp;

    @ConfigProperty(name = "qddns.ipCheckInterval")
    String checkInterval;

    @Inject
    CamelContext camelContext;

    private boolean isWorking;


    @Override
    public void configure() {
        errorHandler(defaultErrorHandler().onExceptionOccurred(e -> setState(false)));

        Duration duration = camelContext.getTypeConverter().convertTo(Duration.class, checkInterval);
        if (!ProfileManager.getActiveProfile().equals("dev") && duration.toSeconds() < 180 && isGWiFiNotAvailable(null)) {
            logger.warn("qddns.ipCheckInterval is less than 3 minutes while there is no Google WiFi available, re-setting it to 3m ");
            checkInterval = "3m";
        }

        from(String.format("%s&period=%s", endpoints.getTimerUrl(), checkInterval))
                .routeId(ROUTE_ID)
                .log(LoggingLevel.TRACE, "check triggered...")
                .choice()
                .when().exchange(this::isGWiFiNotAvailable)
                .to(endpoints.vendorsRoute())
                .otherwise()
                .to(gwifiUrl).convertBodyTo(String.class)
                .log(LoggingLevel.TRACE, "qddns response: ${body}")
                .setBody().jsonpath("$.wan.localIpAddress", true)
                .end()
                .bean(Route53Bean.class, "process")
                .process(e -> setState(true));
    }

    public boolean isGWiFiNotAvailable(Exchange ex) {
        return routerIp.trim().isBlank() || routerIp.equalsIgnoreCase("none");
    }

    public void setState(boolean isWorking){
        this.isWorking = isWorking;
    }

    public boolean isWorking() {
        return isWorking;
    }
}
