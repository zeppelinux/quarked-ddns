package com.diligesoft.gwifiddns;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;
import com.diligesoft.gwifiddns.vendors.Vendor;
import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Body;
import org.apache.camel.ExchangeProperty;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.diligesoft.gwifiddns.routes.VendorsRoute.CURRENT_VENDOR;

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
    AmazonRoute53 route53;

    private static String IP_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";

    ListResourceRecordSetsRequest request;

    private String currentIp;

    @PostConstruct
    public void init() {
        domain = domain.endsWith(".") ? domain : String.format("%s.", domain);
        request = new ListResourceRecordSetsRequest();
        request.setHostedZoneId(zoneId);
    }

    public void process(@Body String localIp, @ExchangeProperty(CURRENT_VENDOR) Vendor vendor) {
        if (localIp == null || localIp.isBlank()) {
            logger.warn("localIp param is null");
            return;
        }

        if (!localIp.matches(IP_REGEX)) {
            throw new IllegalArgumentException(String.format("'%s' string doesn't look like a valid IP Address", localIp));
        }

        ListResourceRecordSetsResult result = route53.listResourceRecordSets(request);
        if (result == null) {
            throw new IllegalArgumentException("can't find recordSet for the hostedZoneId: " + zoneId);
        }

        ResourceRecordSet recordSet = result.getResourceRecordSets().stream()
                .filter(rrs -> domain.equals(rrs.getName()))
                .findAny()
                .orElse(null);

        if (recordSet == null) {
            throw new IllegalArgumentException("can't find managedDomain: " + domain);
        }

        currentIp = recordSet.getResourceRecords().iterator().next().getValue();
        if (!localIp.equals(currentIp)) {
            logger.info(String.format("record update is required, ISP IP=%s, Route53 IP=%s", localIp, currentIp));
            ChangeResourceRecordSetsRequest resourceRecordSetsRequest = new ChangeResourceRecordSetsRequest();
            resourceRecordSetsRequest.setHostedZoneId(zoneId);
            ChangeBatch batch = new ChangeBatch();
            recordSet.getResourceRecords().iterator().next().setValue(localIp);

            Change change = new Change(ChangeAction.UPSERT, recordSet);
            List<Change> changes = new ArrayList<>();
            changes.add(change);
            batch.setChanges(changes);

            resourceRecordSetsRequest.setChangeBatch(batch);
            ChangeResourceRecordSetsResult rrsResult = route53.changeResourceRecordSets(resourceRecordSetsRequest);
            currentIp = localIp;
            logger.info(String.format("changeResourceRecordSets response: %s", rrsResult));
        } else {
            logger.info(String.format("%s says your IP is the same as in Route53", vendor == null ? "Google WiFi" : vendor.getUrl()));
        }
    }

    @Produces
    @CurrentIp
    public String getCurrentIp() {
        return currentIp;
    }
}
