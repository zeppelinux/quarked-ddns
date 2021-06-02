package com.diligesoft.qddns.vendors;

import com.jayway.jsonpath.JsonPath;
import org.apache.camel.CamelContext;

public class JsonPathVendor extends Vendor {

    String extractor;

    public JsonPathVendor(String url, String extractor) {
        super(url);
        this.extractor = extractor;
    }

    @Override
    public String extract(CamelContext context, String str) {
        Object dataObject = JsonPath.parse(str).read(extractor);
        return dataObject.toString();
    }
}
