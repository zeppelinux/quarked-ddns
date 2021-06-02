package com.diligesoft.qddns.vendors;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.CamelContext;

import java.util.Map;

@RegisterForReflection
public abstract class Vendor {

    enum ExtractorType {regex, jsonpath, xpath, xpathAndRegex}

    private String url;

    public abstract String extract(CamelContext context, String str) throws Exception;

    Vendor(String url) {
        this.url = url;
    }

    public static Vendor fromConfig(Map.Entry<String, Object> entry) throws Exception {
        Map<String, Object> extract = (Map<String, Object>) entry.getValue();
        if (ExtractorType.regex.name().equals(extract.entrySet().iterator().next().getKey())) {
            return new RegexVendor(entry.getKey(), (String) extract.entrySet().iterator().next().getValue());
        } else if (ExtractorType.jsonpath.name().equals(extract.entrySet().iterator().next().getKey())) {
            return new JsonPathVendor(entry.getKey(), (String) extract.entrySet().iterator().next().getValue());
        } else if (ExtractorType.xpath.name().equals(extract.entrySet().iterator().next().getKey())) {
            return new XPathVendor(entry.getKey(), (String) extract.entrySet().iterator().next().getValue());
        } else if (ExtractorType.xpathAndRegex.name().equals(extract.entrySet().iterator().next().getKey())) {
            String xpath = (String) ((Map) extract.get(ExtractorType.xpathAndRegex.name())).get(ExtractorType.xpath.name());
            String regex = (String) ((Map) extract.get(ExtractorType.xpathAndRegex.name())).get(ExtractorType.regex.name());

            return new XPathAndRegexVendor(entry.getKey(), xpath, regex);
        } else {
            throw new IllegalArgumentException(String.format("extractor type %s is not supported", extract.entrySet().iterator().next().getKey()));
        }
    }

    public String getUrl() {
        return url;
    }
}
