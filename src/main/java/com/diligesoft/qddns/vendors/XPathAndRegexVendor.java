package com.diligesoft.qddns.vendors;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.CamelContext;
import org.apache.camel.language.xpath.XPathBuilder;

@RegisterForReflection
public class XPathAndRegexVendor extends Vendor {

    XPathBuilder builder;

    RegexVendor regexVendor;

    XPathAndRegexVendor(String url, String xpathExtractor, String regex) {
        super(url);
        builder = XPathBuilder.xpath(xpathExtractor);
        regexVendor = new RegexVendor(url, regex);
    }

    @Override
    public String extract(CamelContext context, String str) {
        return regexVendor.extract(context, builder.evaluate(context, str));
    }
}
