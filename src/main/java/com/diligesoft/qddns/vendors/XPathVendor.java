package com.diligesoft.qddns.vendors;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.CamelContext;
import org.apache.camel.language.xpath.XPathBuilder;

@RegisterForReflection
public class XPathVendor extends Vendor {

    XPathBuilder builder;

    XPathVendor(String url, String extractor) {
        super(url);
        builder = XPathBuilder.xpath(extractor);
    }

    @Override
    public String extract(CamelContext context, String str) {
        return builder.evaluate(context, str);
    }
}
