package com.diligesoft.qddns;

import com.diligesoft.qddns.vendors.Vendor;
import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.Body;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.ProducerTemplate;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import static com.diligesoft.qddns.routes.VendorsRoute.CURRENT_VENDOR;
import static com.diligesoft.qddns.routes.VendorsRoute.VERIFY_VENDOR;

@Named
@Unremovable
@ApplicationScoped
@RegisterForReflection
public class VerifyBean {

    @Inject
    Logger logger;

    @Inject
    @CurrentIp
    Instance<String> currentIp;

    @Inject
    ProducerTemplate template;

    @Inject
    VendorsBean pool;

    public String verify(@Body String ip, @ExchangeProperty(VERIFY_VENDOR) Vendor verifyVendor,
                         @ExchangeProperty(CURRENT_VENDOR) Vendor currentVendor) {
        if (currentIp.get() == null) {
            return ip;
        }

        if (!currentIp.get().equals(ip)) {
            logger.info(String.format("ip's are not the same current: %s vs vendor: %s, verifying...", currentIp.get(), ip));
            String verifyIp = template.requestBody("direct:vendor", verifyVendor, String.class);
            if (!currentIp.get().equals(verifyIp)) {
                logger.warn(String.format("verification failed for %s", currentVendor.getUrl()));
                pool.remove(currentVendor);
            }
            return verifyIp;
        } else {
            return ip;
        }
    }
}
