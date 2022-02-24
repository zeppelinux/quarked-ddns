package com.diligesoft.qddns;

import com.diligesoft.qddns.vendors.Vendor;
import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Body;
import org.apache.camel.ExchangeProperty;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.diligesoft.qddns.routes.VendorsRoute.CURRENT_VENDOR;

@ApplicationScoped
@Unremovable
@RegisterForReflection
public class Route53Bean {

    @Inject
    Logger logger;

    @Inject
    @ConfigProperty(name = "route53.hostedZoneId")
    String zoneId;

    @Inject
    @ConfigProperty(name = "route53.domain")
    String domain;

    @Inject
    Route53Client route53;

    private static String IP_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";

    ListResourceRecordSetsRequest request;

    private String currentIp;

    @PostConstruct
    public void init() {
        domain = domain.endsWith(".") ? domain : String.format("%s.", domain);
        request = ListResourceRecordSetsRequest.builder().hostedZoneId(zoneId).build();
    }

    public void process(@Body String localIp, @ExchangeProperty(CURRENT_VENDOR) Vendor vendor) {
        if (localIp == null || localIp.isBlank()) {
            logger.warn("localIp param is null");
            return;
        }

        if (!localIp.matches(IP_REGEX)) {
            throw new IllegalArgumentException(String.format("'%s' string doesn't look like a valid IP Address", localIp));
        }

        ListResourceRecordSetsResponse result = route53.listResourceRecordSets(request);
        if (result == null) {
            throw new IllegalArgumentException("can't find recordSet for the hostedZoneId: " + zoneId);
        }

        ResourceRecordSet recordSet = result.resourceRecordSets().stream()
                .filter(rrs -> domain.equals(rrs.name()))
                .findAny()
                .orElse(null);

        if (recordSet == null) {
            throw new IllegalArgumentException("can't find managedDomain: " + domain);
        }

        currentIp = recordSet.resourceRecords().iterator().next().value();
        if (!localIp.equals(currentIp)) {
            logger.info(String.format("record update is required, ISP IP=%s, Route53 IP=%s", localIp, currentIp));
            Change change = Change.builder().action(ChangeAction.UPSERT).resourceRecordSet(ResourceRecordSet.builder().name(recordSet.name())
                    .type(recordSet.type()).ttl(recordSet.ttl()).resourceRecords(ResourceRecord.builder().value(localIp).build()).build()).build();
            List<Change> changes = new ArrayList<>();
            changes.add(change);
            ChangeBatch batch = ChangeBatch.builder().changes(changes).build();
            ChangeResourceRecordSetsRequest resourceRecordSetsRequest = ChangeResourceRecordSetsRequest.builder().hostedZoneId(zoneId).changeBatch(batch).build();

            ChangeResourceRecordSetsResponse rrsResult = route53.changeResourceRecordSets(resourceRecordSetsRequest);
            currentIp = localIp;
            logger.info(String.format("changeResourceRecordSets response: %s", rrsResult));
        } else {
            logger.info(String.format("ip verified using %s", vendor == null ? "Google WiFi" : vendor.getUrl()));
        }
    }

    @Produces
    @CurrentIp
    public String getCurrentIp() {
        return currentIp;
    }
}
