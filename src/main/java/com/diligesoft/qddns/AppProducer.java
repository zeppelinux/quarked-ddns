package com.diligesoft.gwifiddns;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
@RegisterForReflection
public class AppProducer {

    @Inject
    Logger logger;

    @Inject
    @ConfigProperty(name = "route53.region")
    String region;

    @Produces
    @ApplicationScoped
    public AmazonRoute53 getAmazonClient(){
        logger.info("initializing Amazon Route53 client");
        return AmazonRoute53ClientBuilder.standard().withRegion(region).build();
    }
}
