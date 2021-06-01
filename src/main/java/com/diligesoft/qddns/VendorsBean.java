package com.diligesoft.qddns;

import com.diligesoft.config.utils.cdi.ConfigPropertyMap;
import com.diligesoft.qddns.vendors.Vendor;
import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Unremovable
@ApplicationScoped
@RegisterForReflection
public class VendorsBean {

    @Inject
    Logger logger;

    @ConfigPropertyMap(name = "qddns.vendors")
    Map<String, Object> vendorsConfig;

    List<Vendor> allVendors;

    Random random;

    @PostConstruct
    public void init() {
        allVendors = vendorsConfig.entrySet().stream().map(entry -> {
            try {
                return Vendor.fromConfig(entry);
            } catch (Exception e) {
                logger.error("can't load vendor from config", e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        random = new Random();
    }

    public List<Vendor> getTwo() {
        return random.ints(0, allVendors.size()).distinct().limit(2)
                .mapToObj(i -> allVendors.get(i)).collect(Collectors.toList());
    }

    public void remove(Vendor vendor) {
        allVendors.remove(vendor);
        logger.warn(String.format("there was some problem with the %s, removed", vendor.getUrl()));
    }
}
