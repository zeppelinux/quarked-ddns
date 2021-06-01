package com.diligesoft.qddns.routes;

import com.diligesoft.qddns.VendorsBean;
import com.diligesoft.qddns.VerifyBean;
import com.diligesoft.qddns.vendors.Vendor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class VendorsRoute extends RouteBuilder {

    public static final String CURRENT_VENDOR = "currentVendor";
    public static final String VERIFY_VENDOR = "verifyVendor";

    @Inject
    Endpoints endpoints;

    @Inject
    VendorsBean vendorsBean;

    @Override
    public void configure() {

        from(endpoints.vendorsRoute())
                .errorHandler(noErrorHandler())
                .routeId("public-vendors-route")
                .bean(VendorsBean.class, "getTwo")
                .process(e -> {
                    if (e.getIn().getBody(List.class).isEmpty()) {
                        throw new IllegalStateException("no ip vendors configured");
                    } else if (e.getIn().getBody(List.class).size() == 2) {
                        e.setProperty(VERIFY_VENDOR, e.getIn().getBody(List.class).get(1));
                    }

                    e.getIn().setBody(e.getIn().getBody(List.class).get(0));
                })
                .doTry()
                    .to(endpoints.vendorRoute())
                .doCatch(Throwable.class)
                    .process(e -> {
                        Vendor failed = e.getProperty(CURRENT_VENDOR, Vendor.class);
                        vendorsBean.remove(failed);
                        e.setProperty(CURRENT_VENDOR, e.getProperty(VERIFY_VENDOR, Vendor.class));
                    })
                .endDoTry()
                .bean(VerifyBean.class);


        from(endpoints.vendorRoute())
                .errorHandler(defaultErrorHandler().maximumRedeliveries(1))
                .routeId("vendor-route")
                .log(LoggingLevel.DEBUG, "using vendor: ${body.url}")
                .setProperty(CURRENT_VENDOR, simple("${body}"))
                .setBody(constant(""))
                .toD(String.format("${exchangeProperty.%s.url}", CURRENT_VENDOR))
                .process(e -> {
                    String ip = e.getProperty(CURRENT_VENDOR, Vendor.class).extract(e.getIn().getBody(String.class));
                    e.getIn().setBody(ip);
                })
                .log(LoggingLevel.DEBUG, "ip: ${body}");

    }
}
