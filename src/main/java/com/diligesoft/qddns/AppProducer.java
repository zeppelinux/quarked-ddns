package com.diligesoft.qddns;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.jboss.logging.Logger;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
@RegisterForReflection
public class AppProducer {

    @Inject
    Logger logger;

    @Produces
    @ApplicationScoped
    public Route53Client getAmazonClient() {
        logger.info("initializing Route53Client...");
        SdkHttpClient httpClient = ApacheHttpClient.builder().build();
        return Route53Client.builder().region(Region.AWS_GLOBAL).httpClient(httpClient).build();
    }
}
